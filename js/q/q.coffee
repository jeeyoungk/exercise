# Author : Jeeyoung Kim
#
# Implementing angularjs promises, for fun and profit.

# Public Interface
# ----------------
Q = {
  # **Public**. Creates a new `Defer`.
  defer: () ->
    d = new Defer(); wrapped = {}
    ['success','fail','promise'].map (method) -> wrapped[method] = d[method].bind(d)
    wrapped
  # **Public**. Convenience function to create a failed promise.
  fail: (value) ->
    d = Q.defer()
    d.fail(value)
    d.promise()
  # **Public**. Derive a promise which is resolved iff all the provided
  # promises are resolved. The derived promise is successful
  # iff all subpromises are successful.
  all: (promises) ->
    count = promises.length; defer = Q.defer()
    # short circuit on empty array.
    if count is 0
      defer.success([]); return defer.promise()
    # Aggregated results
    resp_succ = (null for promise of promises)
    resp_fail = (null for promise of promises)
    failed = false
    # decrements the number of unresolved promises, and resolve the derived
    # promise upon completion.
    decrement = () ->
      count--
      return unless count is 0
      if failed then defer.fail(resp_fail)
      else defer.success(resp_succ)
    for index, promise of promises
      do (index) ->
        promise.then (s, f) ->
          resp_succ[index] = s; resp_fail[index] = f
          failed |= f isnt null
          decrement()
    return defer.promise()
  # **Public**. Derive a promise which automatically fails on timeout.
  timeout: (promise, timeout) ->
    d = Q.defer()
    setTimeout (-> d.fail "timeout"), timeout
    promise.then (s, f) ->
      if f isnt null then d.fail f
      else d.success s
    return d.promise()
}

# Implementation
# --------------
#
# `Defer` is in 3 one of states.
# * `NEW` - unresolved promise.
# * `SUCCESS,FAIL` - resolved promise.
STATE_NEW = 0
STATE_SUCCESS = 1
STATE_FAIL = 2

class Defer
  constructor: () -> @queued = []; @value = null; @state = STATE_NEW
  # **Public**. Resolve as success.
  success: (value) -> @resolve value, STATE_SUCCESS
  # **Public**. Resolve as failure.
  fail: (value) -> @resolve value, STATE_FAIL
  # **Public**. Creates an externally exposed promise object.
  # libraries should only expose promises to the client, and encapsulate
  # defers as implementation detail.
  promise: -> {
    # **Public**. chain the promise with a given function.
    # Returns a derived promise object.
    then: (async) =>
      d = new Defer()
      wrapped = (s, f) =>
        try successValue = async(s, f) catch failValue
        if successValue? and successValue.__is_promise
          successValue.then (s, f) ->
            if f isnt null then d.fail f
            else d.success s
        else
          if failValue? then d.fail failValue
          else d.success successValue
      if @state is STATE_NEW then @queued.push wrapped
      else @triggerOne wrapped
      d.promise()
    # **Public**. true if the given promise is resolved.
    done: () => @state isnt STATE_NEW
    # sentinal value to distinguish promise objects.
    __is_promise: true
  }
  # resolve as success or failure.
  resolve: (value, state) ->
    if (operate = (@state is STATE_NEW))
      @value = value; @state = state
      @triggerOne(queue) for idx, queue of @queued
    return operate
  # trigger a given queued asynchronous function.
  triggerOne: (queue) ->
    if @state is STATE_SUCCESS then queue @value, null
    else queue null, @value

# Unit Tests
# ----------
test = ->
  testall_empty()
  testall_failure()
  testall_normal()
  testchain_normal()
  testchain_failures()
  testduplicate()
  testfan()
  testhello()
  testhello_fail()
  testoutoforder()
  testreturnpromise()
  testtimeout()

assert = (value) =>
  unless value then console.error 'failure'

testhello = ->
  # Hello to the world of promises.
  run = false
  d = Q.defer()
  assert not d.promise().done()
  d.promise().then (s, f) => (assert s is 'world'; run = true)
  d.success('world')
  assert d.promise().done()
  assert run

testhello_fail = ->
  # test failure methods:
  # * `defer.fail()` - resolving as failure.
  # * `throw`ing - returing exception as failure.
  # * `Q.fail()`
  run = false
  d = Q.defer()
  d.promise()
    .then (s, f) => (assert f is 'hello'; throw 'world')
    .then (s, f) => (assert f is 'world'; Q.fail('everybody'))
    .then (s, f) => (assert f is 'everybody'; run = true)
  d.fail 'hello'
  assert run

testchain_normal = ->
  # Test promise resolution is fully propagated through a chain.
  # Success-only workflow.
  run = false
  d = Q.defer()
  d.promise().then((s, f) => s + 1)
   .then((s, f) => s * 2)
   .then((s, f) => assert s is 12; run = true
   )
  d.success(5)
  assert(run)

# Test promise resolution is fully propagated through a chain.
# Mixed workflow of successes and failures.
testchain_failures = ->
  run = false
  d = Q.defer()
  d.promise()
    .then((s, f) => Q.fail s + 1)
    .then((s, f) => f + 1)
    .then((s, f) => throw s + 1)
    .then((s, f) => assert f is 4; run = true
    )
  d.success(1)
  assert run

# Promise is resolved before chained via `then`.
# Chained promises should resolve regardless.
testoutoforder = ->
  run = false
  d = Q.defer()
  p = d.promise()
  d.success(1)
  p.then((s, f) => run = true)
  assert(run)

# Promise returning promise. Q framework should
# unwrap them accordingly.
testreturnpromise = ->
  run = false
  start = Q.defer()
  response = Q.defer()
  chained = start.promise().then (s, f) ->
    return unless s is 10
    return response.promise()
  chained.then (s, f) => assert s is 12; run = true
  start.success 10
  assert not run
  response.success 12
  assert run

# Test a fanout of promises - multiple derived promises
# on top of a single promise.
testfan = ->
  d = Q.defer()
  p = d.promise()
  count = 5
  decrement = -> count--
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  d.success('start')
  assert(count is 0)

# Duplicate promise resolutions are ignored.
testduplicate = ->
  d = Q.defer()
  count = 0
  increment = -> count++
  d.promise().then (s, f) ->
    assert s is 1
    increment()
  assert count is 0
  assert d.success 1
  assert count is 1
  assert not d.success 1
  assert not d.fail 1
  assert count is 1

# test `Q.all()`, where all promises are successful.
testall_normal = ->
  a = Q.defer()
  b = Q.defer()
  c = Q.defer()
  run = false
  Q.all([a.promise(), b.promise(), c.promise()]).then (s, f) ->
    assert s[0] is 'a'
    assert s[1] is 'b'
    assert s[2] is 'c'
    run = true
  a.success('a'); assert not run
  b.success('b'); assert not run
  c.success('c'); assert run

# test `Q.all()`, where all promises are successful.
testall_failure = ->
  a = Q.defer()
  b = Q.defer()
  c = Q.defer()
  run = false
  Q.all([a.promise(), b.promise(), c.promise()]).then (s, f) ->
    assert f[0] is null
    assert f[1] is null
    assert f[2] is 'c'
    run = true
  a.success('a'); assert not run
  b.success('b'); assert not run
  c.fail('c'); assert run

# Test `Q.all()` with an empty array of promises.
testall_empty = ->
  run = false
  Q.all([]).then -> run = true
  assert(run)

# Test `Q.timeout()`.
testtimeout = ->
  run = false
  d = Q.defer()
  timeoutPromise = Q.timeout(d.promise(), 50)
  timeoutPromise.then (s, f) ->
    assert f is 'timeout'
    run = true
  setTimeout (-> assert not run), 49
  setTimeout (-> assert run), 51

test()

