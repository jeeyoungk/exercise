# Author : Jeeyoung Kim
# Implementing angularjs promises, for fun and profit.

Q = {
  # creates a new deferred object.
  defer: () ->
    d = new Defer()
    {
      success: d.success.bind(d)
      fail:    d.fail.bind(d)
      promise: d.promise
    }
  # shortcut for creating failed promises.
  fail: (value) ->
    d = Q.defer()
    d.fail(value)
    d.promise
  # from an array of promises, returns a promise
  # which is resolved when ALL of them are resolved.
  all: (promises) ->
    count = promises.length
    defer = Q.defer()
    if count == 0 # short circuit on empty array.
      defer.success([]); return defer.promise
    resp_succ = (null for promise of promises)
    resp_fail = (null for promise of promises)
    failed = false
    decrement = () ->
      count--
      return unless count is 0
      if failed then defer.failure(resp_fail) else defer.success(resp_succ)

    for index, promise of promises
      do (index) ->
        promise.then (s, f) ->
          resp_succ[index] = s; resp_fail[index] = f
          failed |= f isnt null
          decrement()
    return defer.promise

  # create a wrapped promise that fails automatically on timeout.
  timeout: (promise, timeout, fallback = null) ->
    d = Q.defer()
    setTimeout (-> d.fail "timeout"), timeout
    promise.then (s, f) ->
      if f isnt null
        if fallback? then d.success fallback
        else d.fail f
      else d.success s
    return d.promise
}

# defers can be in 3 state.
STATE_NEW = 0     # newly created unresolved defer.
STATE_SUCCESS = 1 # resolved to success
STATE_FAIL = 2    # resolved to failure

class Defer
  constructor: () ->
    @queued = []
    @value = null
    @state = STATE_NEW

    @promise = {
      # chain the promise with a given function.
      then: (async) =>
        d = new Defer()
        wrapped = (s, f) =>
          try successValue = async(s, f) catch failValue
          if successValue? and successValue.is_promise # chain the promise
            successValue.then (s, f) ->
              if f isnt null then d.fail f
              else d.success s
          else
            if failValue? then d.fail failValue
            else d.success successValue
        if @state is STATE_NEW then @queued.push wrapped
        else @triggerOne wrapped
        d.promise
      done: () => @state isnt STATE_NEW
      is_promise: true
    }
  # resolve as success
  success: (value) -> @resolve value, STATE_SUCCESS
  # resolve as failure
  fail: (value) -> @resolve value, STATE_FAIL
  # resolve as success or failure.
  resolve: (value, state) ->
    if (operate = (@state is STATE_NEW))
      @value = value; @state = state
      @triggerOne(queue) for idx, queue of @queued
    return operate
  triggerOne: (queue) ->
    if @state is STATE_SUCCESS then queue(@value, null)
    else queue(null, @value)

test = ->
  testall_empty()
  testall_normal()
  testchain()
  testchain_failures()
  testduplicate()
  testfan()
  testhello()
  testhello_fail()
  testoutoforder()
  testreturnpromise()
  testtimeout()

assert = (value) =>
  unless value
    console.error 'failure'

testhello = ->
  # Hello to the world of promises.
  run = false
  d = Q.defer()
  assert not d.promise.done()
  d.promise.then (s, f) => (assert s is 'world'; run = true)
  d.success('world')
  assert d.promise.done()
  assert run

testhello_fail = ->
  # test failure methods.
  # defer.fail - resolving as failure.
  # throwing - returing exception as failure.
  # failed promises
  run = false
  d = Q.defer()
  d.promise
    .then (s, f) => (assert f is 'hello'; throw 'world')
    .then (s, f) => (assert f is 'world'; Q.fail('everybody'))
    .then (s, f) => (assert f is 'everybody'; run = true)
  d.fail 'hello'
  assert run

testchain = ->
  # test interaction of chained promises.
  run = false
  d = Q.defer()
  d.promise.then((s, f) => s + 1)
   .then((s, f) => s * 2)
   .then((s, f) =>
     assert(s == 12)
     run = true
   )
  d.success(5)
  assert(run)

testchain_failures = ->
  # testing chain of mixed failure & success.
  run = false
  d = Q.defer()
  d.promise
    .then((s, f) => Q.fail s + 1)
    .then((s, f) => f + 1)
    .then((s, f) => throw s + 1)
    .then((s, f) =>
      assert f == 4
      run = true
    )
  d.success(1)
  assert run

testoutoforder = ->
  # resolve the defer before chaining promise. chained promise should resolve.
  run = false
  d = Q.defer()
  p = d.promise
  d.success(1)
  p.then((s, f) => run = true)
  assert(run)

testreturnpromise = ->
  # promise retuns promise.
  run = false
  start = Q.defer()
  response = Q.defer()
  chained = start.promise.then (s, f) ->
    return unless s == 10
    return response.promise
  chained.then (s, f) =>
    if s == 12
      run = true
  start.success 10
  assert(not run)
  response.success 12
  assert(run)

testfan = ->
  # test the fanout.
  d = Q.defer()
  p = d.promise
  count = 5
  decrement = -> count--
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  p.then((s, f) => decrement())
  d.success('start')
  assert(count == 0)

testduplicate = ->
  # test the duplicate resolution.
  d = Q.defer()
  count = 0
  increment = -> count++
  d.promise.then (s, f) ->
    assert(s == 1)
    increment()
  assert(count == 0)
  assert(d.success(1))
  assert(count == 1)
  assert(not d.success(1))
  assert(not d.fail(1))
  assert(count == 1)

testall_normal = ->
  a = Q.defer()
  b = Q.defer()
  c = Q.defer()
  run = false
  Q.all([a.promise, b.promise, c.promise]).then (s, f) ->
    assert(s[0] == 'a')
    assert(s[1] == 'b')
    assert(s[2] == 'c')
    run = true
  assert(not run)
  a.success('a')
  assert(not run)
  b.success('b')
  assert(not run)
  c.success('c')
  assert(run)

testall_empty = ->
  run = false
  Q.all([]).then -> run = true
  assert(run)

testtimeout = ->
  run = false
  d = Q.defer()
  timeoutPromise = Q.timeout(d.promise, 50)
  timeoutPromise.then (s, f) ->
    assert f is 'timeout'
    run = true
  setTimeout (-> assert not run), 49
  setTimeout (-> assert run), 51

test()

