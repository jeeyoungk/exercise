# Pipeline framework
#
# Allows developres to create a pipeline of computation.
#
# Author: Jeeyoung Kim

# Processing Pipeline
# -------------------
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
  # triggered when the cluster is starting.
  onStart:    (context) ->
  # triggered every provided time interval. This is useful to enforce "flushing" semantic
  # within the pipe.
  onInterval: (context) ->
  # triggered when a dependency have emitted a message.
  onMessage:  (context, name, message) ->
  # triggered when the cluster is terminating.
  # Once stopped, neither new messages nor interval jobs will be received
  # by the agent.
  onStop:     (context) ->

# `PipeContext` is a state associated with each concurrent execution of
# pipe. It is also an interface to allow a pipe to communicate with the
# entire system.
#
# **Fields:**
#
# Fields starting with `$` are reserved for internal usage. Users should not overwrite them.
#
# * `@$config` - Associated `PipeConfig` object.
# * `@$id` - Id of the context. Number between `[0..concurrency - 1]`.
class PipeContext
  constructor: (@$id, @$config) ->

  # Emit a message.
  $emit: (message) ->

  # mark the given context as completed.
  # Every `Pipe` event handlers should invoke `$complete()` at the end.
  $complete: ->


# Topology Configurations
# -----------------------
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

class Grouper
  # required fields.
  fields: () -> []
  # grouping function. maps message -> int.
  group:  (message) ->

# Implementation detail of the pipe context.
# Internally it has callbacks on emit and completion to make things
# efficient.
class PipeContextImpl extends PipeContext
  constructor: (id, config, @$topology) ->
    super(id, config)
    @$running = false
    @$onComplete = [] # queued callbacks on complete call - they're cleared.
    @$onEmit = []     # persistent handlers.

  $emit: (message) =>
    # _TODO_: Implement validation on emitted message.
    @$onEmit.map (onEmit) =>
      onEmit(@$config.name, message)

  $complete: =>
    assert @$running
    @$running = false
    # trigger all the completions.
    for onComplete in @$onComplete
      onComplete(this)
    @$onComplete = []

class Topology
  constructor: ->
    @names = []
    @name_to_pipe = {}
    @name_to_config = {}
    @name_to_context = {}
    @name_to_dependents = {}
    @handles = [] # various handles.

  # Registers a given pipe & config into the topology.
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

  start: ->
    return false unless @_isValid()
    # start the cluster.
    # TODO - validate the cluster.
    for index, name of @names
      pipe = @name_to_pipe[name]
      config = @name_to_config[name]
      {interval_ms} = config
      for i in [0 .. config.concurrency - 1] by 1
        context = @name_to_context[name][i]
        context.$onEmit.push @_onEmit
        do (context, pipe, interval_ms) ->
          triggerOnInterval = ->
            context.$onComplete.push (context) ->
              setTimeout(triggerOnInterval, interval_ms)
            queueContextAction context, (context) ->
              pipe.onInterval(context)
          queueContextAction context, (context) ->
            if interval_ms > 0
              context.$onComplete.push (context) ->
                setTimeout(triggerOnInterval, interval_ms)
            pipe.onStart(context)
  stop: ->
    # stop the cluster.

  _isValid: ->
    # TODO - validate the cluster.
    #
    # * Circular dependency.
    # * Mapped fields.
    return true
  # General callback function invoked when a message is emitted
  # within the system. Delivers the message to all the dependent pipes.
  _onEmit: (source_name, message) =>
    dependents = @name_to_dependents[source_name]
    for dest_name in dependents
      dest_config = @name_to_config[dest_name]
      dest_pipe =  @name_to_pipe[dest_name]
      grouper = dest_config.dependencies[source_name]
      rawGroupId = grouper.group(message)
      dest_context_id = rawGroupId % dest_config.concurrency
      dest_context = @name_to_context[dest_name][dest_context_id]
      queueContextAction dest_context, (context) ->
        dest_pipe.onMessage(context, source_name, message)

# Utility Functions and objects
# -----------------------------

# Sentinel object for re-queuing completion events.
REQUEUE = {}

hashCode = (string) ->
  hash = 0
  return hash if string.length is 0
  i = 0
  while i < string.length
    char = string.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash # Convert to 32bit integer
    i++
  hash

assert = (value, message='Assertion Error') ->
  unless value then console.log message

# Actions requiring explicit completion on context
# must not run concurrently. In order to maintain mutual exclusion,
# we queue up the logic to the completion callback queue. Requirements:
# * `context` is a `PipeContext` object.
# * `logic` is a callback that eventually calls `context.$complete()` exactly once.
queueContextAction = (context, logic) ->
  wrapped = (context) ->
    if context.$running
      context.$onComplete.push wrapped
    else
      context.$running = true
      logic(context)
  wrapped(context)

# Testing Code
# ------------
test = ->
  class RandomGrouper extends Grouper
    fields: () -> []
    group: () ->  Math.floor(Math.random() * 1024)

  t = new Topology()
  producerA = {
    onStart:    (context) ->
      context.counter = 0
      context.$complete()
    onInterval: (context) ->
      context.counter++
      context.$emit value: Math.random()
      sleep = Math.floor(Math.random() * 1000)
      setTimeout (-> context.$complete()), sleep
  }
  configA = new PipeConfig {
    name: "A"
    fields: ['text']
    interval_ms: 100
    concurrency: 4
  }
  pipeB = {
    onStart: (context) ->
      context.sum = 0
      context.count = 0
      context.$complete()
    onInterval: (context) ->
      console.log "reporting status: id:#{context.$id}, count:#{context.count}"
      context.$complete()
    onMessage: (context, name, message) ->
      context.sum += message.value
      context.count++
      context.$complete()
  }
  configB = new PipeConfig {
    name: "B"
    fields: []
    interval_ms: 1000
    concurrency: 2
    dependencies: {
      A: new RandomGrouper()
    }
  }
  t.register(producerA, configA)
  t.register(pipeB, configB)
  t.start()

test()
