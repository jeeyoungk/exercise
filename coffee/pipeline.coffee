# Pipeline framework
#
# Allows developres to create a pipeline of computation.
# Similar to Apache Storm, but within a single javascript runtime.
#
# Author: Jeeyoung Kim

# Processing Pipeline
# ===================
# `Pipe` is the basic unit of computation.
# Few things about pipes:
#
# * Pipes are stateless.
# * Pipe has the following lifecycle:
# ```
# onStart -> (onInterval|onMessage)* -> onStop
# ```
# * Per pipe state can be tracked in `context` object. Multiple
# contexts can be initialized via increasing the `concurrency` parameter
# in `PipeConfig`.
# * All pipe operations are executed asynchronously.
# Every `on...()` functions MUST call `context.$complete()` eventually
# to notify the framework. Moreover, pipe is safe from race conditions.
# Per context, There is at most one event handler function running in a given time.
#
class Pipe
  # `onStart` is called on cluster start. Used to initialize the `context` object.
  onStart:    (context) ->
  # `onInterval` is triggered every provided time interval.
  #
  # **Example Use Cases:**
  #
  # * Flusing the aggregated data in pipe.
  # * Periodic status report.
  # * Generating data.
  onInterval: (context) ->
  # `onMessage` is triggered on a new emitted message.
  onMessage:  (context, name, message) ->
  # `onStop` is triggered when the given pipe have fully stopped.
  #
  # * Interval job have finished running.
  # * All parent pipes have stopped.
  #
  # Once stopped, no new jobs will be triggered. Implementation can
  # perform the last round of `$emit()` during `onStop()`.
  onStop:     (context) ->

# Pipe Context
# ------------
# `PipeContext` is a state associated with each concurrent execution of
# pipe. It is also an interface to allow a pipe to communicate with the
# entire system.
#
# **Member Fields:**
#
# Fields starting with `$` are reserved for internal usage. Users should not overwrite them.
#
# * `@$config` - Associated `PipeConfig` object.
# * `@$id` - Id of the context. Number between `[0..concurrency - 1]`.
class PipeContext
  constructor: (@$id, @$config) ->

  # Emtis a message to the underlying `Topology`.
  # Emitted message is delivered to the interested pipes.
  $emit: (message) ->

  # Mark the current event context as completed.
  #
  # **NOTE:** Every `Pipe` event handlers MUST invoke `$complete()`.
  # This can be done asynchronously.
  $complete: ->

# Topology
# ========
class Topology
  constructor: ->
    @names = [] # All registered pipe names.
    @name_to_pipe = {};    @name_to_config = {}
    @name_to_context = {}; @name_to_dependents = {}
    @$state = STATE_NEW # Current state of topology.
    @messageCount = 0   # Number of unprocessed messages within toplogy.

  # Registers a given `(pipe, config)` pair to the toplogy.
  register: (pipe, config) ->
    name = config.name
    assert not @name_to_pipe[name], "Pipe already registered"
    @names.push name
    @name_to_pipe[name] =         pipe
    @name_to_config[name] =       config
    @name_to_context[name] =      []
    @name_to_dependents[name] ||= [] # May be initialized by someone else.
    for id in [0 .. config.concurrency-1] by 1
      @name_to_context[name].push new PipeContextImpl(id, config, this)
    for dependent_name of config.dependencies
      @name_to_dependents[dependent_name] ||= []
      @name_to_dependents[dependent_name].push name
    return true

  # Start the given topology.
  # It iterates through `(pipe, context)` pairs and starts them.
  start: ->
    return false unless @isValid()
    @$moveState STATE_STARTING
    allStarted = counterCallback(1, => @$moveState STATE_RUNNING)
    for name in @names
      pipe = @name_to_pipe[name]
      config = @name_to_config[name]
      for context in @name_to_context[name]
        allStarted.inc()
        context.$onEmit.push @onEmit
        @startPipeContext pipe, context, () -> allStarted.dec()
    allStarted.dec()

  # Stop the given toplogy.
  stop: ->
    for name in @names
      pipe = @name_to_pipe[name]
      config = @name_to_config[name]
      for context in @name_to_context[name]
        context.$moveState STATE_STOPPING

  isValid: ->
    # TODO - validate the cluster.
    #
    # * Circular dependency.
    # * Mapped fields.
    return true

  # General callback function invoked when a message is emitted
  # within the system. Delivers the message to all the dependent pipes.
  onEmit: (source_name, message) =>
    dependents = @name_to_dependents[source_name]
    for dest_name in dependents
      @messageCount++
      dest_config = @name_to_config[dest_name]
      dest_pipe =  @name_to_pipe[dest_name]
      grouper = dest_config.dependencies[source_name]
      rawGroupId = grouper.group(message)
      dest_context_id = rawGroupId % dest_config.concurrency
      dest_context = @name_to_context[dest_name][dest_context_id]
      dest_context.$onComplete.push () => @onMessageComplete()
      queueContextAction dest_context, (context) =>
        dest_pipe.onMessage(context, source_name, message)

  onMessageComplete: -> @messageCount--

  # Set a given pope context into a started state.
  # Starts the periodic interval jobs.
  startPipeContext: (pipe, context, customPostStart) =>
    {interval_ms} = context.$config
    postStart = (context) ->
      context.$moveState STATE_RUNNING
      if interval_ms > 0 then setTimeout(triggerOnInterval, interval_ms)
    triggerOnInterval = ->
      return if context.$state isnt STATE_RUNNING
      context.$onComplete.push () ->
        setTimeout(triggerOnInterval, interval_ms)
      queueContextAction context, (ctx) -> pipe.onInterval(ctx)
    queueContextAction context, (context) ->
      context.$onComplete.push customPostStart
      context.$onComplete.push postStart
      context.$moveState STATE_STARTING
      if pipe.onStart? then pipe.onStart(context)
      else context.$complete() # No onStart provided - just mark as success.

  $moveState: (newState) ->
    # do a state machine transition with an assertion.
    curState = @$state
    assert STATE_TRANSITIONS[curState].indexOf(newState) > -1,
      "Invalid transition: #{curState} -> #{newState}"
    @$state = newState

# Topology Configurations
# =======================
# `PipeConfig` provides two things:
# * Information on how to run a given pipe.
# * Information on how to wire up the pipes.
class PipeConfig
  constructor: (options) ->
    {
      # Name for the given pipe in the topology.
      # It must be unique within the cluster.
      @name
      # Names of the dependent pipes, and the clustering strategy.
      # It is a map {pipe name : grouper}. See `Grouper` for more info.
      @dependencies
      # List of required fields emitted by the given pipe.
      @fields
      # Number of concurrent instances of the given pipe.
      @concurrency
      # how often `onInterval()` event is triggered for a given pipe.
      # `0` disables `onInterval()` altogether.
      @interval_ms
    } = options
    unless @dependencies? then @dependencies = {}
    unless @interval_ms?  then @interval_ms = 0
    unless @concurrency?  then @concurrency = 1
    unless @fields?       then @fields = []

# Grouper
# -------
# `Grouper` is used to control how messages are distributed between
# pipes. Different grouping algorithms can be used to sort messages
# between the dependent pipe instances.
class Grouper
  # `fields()` returns the list of fields required by this Grouper.
  fields: () -> []
  # `group()` returns the corresponding group for the given message.
  group:  (message) ->

# Utility Functions and Objects
# -----------------------------

# Implementation of `PipeContext`.
#
# Internally it installs callbacks on `emit` and `complete` methods
# to execute the follow up code.
class PipeContextImpl extends PipeContext
  constructor: (id, config, @$topology) ->
    super(id, config)
    @$executing =  false # if true, pipeline is currently processing value.
    @$state =      STATE_NEW
    @$onComplete = [] # queued callbacks on complete call. Cleared after completion.
    @$onEmit =     [] # queued callbacks when a message is emitted. Not cleared afterwards.

  $emit: (message) =>
    for field in @$config.fields
      assert message[field]?, "Field #{field} is required."
    @$onEmit.map (onEmit) =>
      onEmit(@$config.name, message)

  $complete: =>
    assert @$executing
    @$executing = false
    onComplete(this) for onComplete in @$onComplete
    @$onComplete = []

  # Change the internal state of `PipeContextImpl` with an assertion.
  $moveState: (newState) ->
    curState = @$state
    assert STATE_TRANSITIONS[curState].indexOf(newState) > -1
    @$state = newState

# Both `Topology` and `PipeContextImpl` maintain a state machine.
# To make state transitions more formal, we specify the valid
# transitions.
STATE_NEW      = 'STATE_NEW'
STATE_STARTING = 'STATE_STARTING'
STATE_RUNNING  = 'STATE_RUNNING'
STATE_STOPPING = 'STATE_STOPPING'
STATE_STOPPED  = 'STATE_STOPPED'

# valid state transitions.
STATE_TRANSITIONS = {
  STATE_NEW:      [STATE_STARTING]
  STATE_STARTING: [STATE_RUNNING]
  STATE_RUNNING:  [STATE_STOPPING]
  STATE_STOPPING: [STATE_STOPPED]
  STATE_STOPPED:  []
}

# Sentinel object for re-queuing completion events.
REQUEUE = {}

# a semaphore implementation. underlying callback is
# invoked when the underlying counter becomes 0.
counterCallback = (value = 0, callback) ->
  invoked = false
  return {
    inc: (delta = 1) ->
      value += delta
    dec: (delta = 1) ->
      value -= delta
      if value <= 0 and not invoked
        invokeed = true
        callback()
  }

assert = (value, message='Assertion Error') ->
  unless value then console.log message

# Actions requiring explicit completion on context
# must not run concurrently. In order to maintain mutual exclusion,
# we queue up the logic to the completion callback queue. Requirements:
# * `context` is a `PipeContext` object.
# * `logic` is a callback that eventually calls `context.$complete()` exactly once.
queueContextAction = (context, logic) ->
  wrapped = (context) ->
    if context.$executing then context.$onComplete.push wrapped
    else
      context.$executing = true
      logic context
  wrapped(context)

# Testing Code
# ------------
test = ->
  class RandomGrouper extends Grouper
    fields: () -> []
    group:  (message) ->  Math.floor(Math.random() * 1024)
  class BigSmallGrouper extends Grouper
    fields: () -> []
    group:  (message) -> if message.source is 'big' then 0 else 1

  t = new Topology()
  producer = {
    onInterval: (context) ->
      source = if Math.random() > 0.75 then "big" else "small"
      context.$emit value: Math.random(), source: source
      context.$complete()
  }
  producerConfig = new PipeConfig {
    name: "PRODUCER"
    fields: ['source']
    interval_ms: 100
    concurrency: 4
  }
  pipeSummary = {
    onStart: (context) ->
      context.sum = 0
      context.count = 0
      context.$complete()
    onInterval: (context) ->
      name = context.$config.name
      console.log "reporting status - name:#{name} id:#{context.$id}, count:#{context.count}, average:#{context.sum / context.count}"
      context.$complete()
    onMessage: (context, name, message) ->
      context.sum += message.value
      context.count++
      context.$complete()
  }
  configSharded = new PipeConfig {
    name: "summary-shard"
    fields: []
    interval_ms: 1000
    concurrency: 2
    dependencies: {
      PRODUCER: new BigSmallGrouper
    }
  }
  configAll = new PipeConfig {
    name: "summary-all"
    fields: []
    interval_ms: 1000
    concurrency: 1
    dependencies: {
      PRODUCER: new RandomGrouper
    }
  }
  t.register producer,    producerConfig
  t.register pipeSummary, configSharded
  t.register pipeSummary, configAll
  t.start()
  setTimeout (-> t.stop()), 10000

test()
