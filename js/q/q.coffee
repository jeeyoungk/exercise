# Author : Jeeyoung Kim
# Implementing angularjs promises, for fun and profit.

Q = {
  defer: () -> new Defer()
  fail: (value) ->
    d = Q.defer()
    d.fail(value)
    d.promise

  all: (promises) ->
    # from an array of promises, returns a 
    count = promises.length
    defer = Q.defer()
    if count == 0
      # short circuit.
      defer.success([])
      return defer.promise
    successes = (null for promise of promises)
    failures =  (null for promise of promises)
    hasFailure = false
    decrement = () ->
      count--
      if count == 0
        if hasFailure
          defer.failure(failures)
        else
          defer.success(successes)
    for index, promise of promises
      do (index) ->
        promise.then (s, f) ->
          successes[index] = s
          failures[index] = f
          if f isnt null
            hasFailure = true
          decrement()
    return defer.promise
}

STATE_NEW = 0
STATE_SUCCESS = 1
STATE_FAIL = 2

class Defer
  constructor: () ->
    @queued = []
    @value = null
    @state = STATE_NEW
    @promise = {
      then: (async) =>
        defer = new Defer()
        wrapped = (success, failure) =>
          try
            successValue = async(success, failure)
          catch e
            failValue = e

          if successValue? and successValue.is_promise
            # chaining the promise.
            successValue.then (s, f) ->
              if f isnt null
                defer.fail f
              else
                defer.success s
          else
            # non-promise value.
            if failValue?
              defer.fail(failValue)
            else
              defer.success(successValue)

        if @state is STATE_NEW
          @queued.push wrapped
        else
          @triggerOne wrapped
        return defer.promise
      is_promise: true
    }


  success: (value) ->
    # mark the given promise as success.
    if (operate = (@state is STATE_NEW))
      @value = value
      @state = STATE_SUCCESS
      @triggerAll()
    return operate

  fail: (value) ->
    # fail the given promise.
    if (operate = (@state is STATE_NEW))
      @value = value
      @state = STATE_FAIL
      @triggerAll()
    return operate

  triggerOne: (queue) ->
    if @state is STATE_SUCCESS
      queue(@value, null) # success mode.
    else
      queue(null, @value) # fail mode.

  triggerAll: =>
    for idx, queue of @queued
      @triggerOne(queue)


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

assert = (value) =>
  unless value
    console.error 'failure'

testhello = ->
  # Hello to the world of promises.
  run = false
  d = Q.defer()
  d.promise.then (s, f) => (assert s is 'world'; run = true)
  d.success('world')
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
  d.promise.then((s, f) ->
    assert(s == 1)
    increment()
  )
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
  Q.all([]).then ->
    run = true
  assert(run)

test()

