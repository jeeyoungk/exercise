bound = (value, min, max) ->
  if value > max then return max
  if value < min then return min
  return value

rand = (start, end) -> start + Math.random() * (end - start)

move = (point, radius, angle) ->
  [x, y] = point
  x += Math.sin(angle) * radius
  y += Math.cos(angle) * radius
  return [x, y]

$ ->
  canvas = document.getElementById 'canvas'
  ctx = canvas.getContext '2d'
  width = height = 640
  TAU = Math.PI * 2
  interval = null

  ctx.save() # initial save.
  init = ->
    clear()
    if interval != null
      clearInterval(interval)
      interval = null
  clear = ->
    ctx.restore()
    ctx.clearRect 0, 0, width, height
    ctx.save()

  withInterval = (callback) ->
    counter = 0
    wrapped = ->
      counter += 1
      clear()
      callback(counter)
    interval = setInterval wrapped, 50

  init()
  class Snake
    constructor: (@length) ->
      @snake = []
      @angle = rand(0, TAU)
      @cx = width / 2; @cy = height / 2
    addSnake: =>
      r = 2
      @angle = @angle + rand(-Math.PI * 0.2, Math.PI * 0.2)
      [@cx, @cy] = move([@cx, @cy], r, @angle)
      @cx = bound(@cx, 0, width)
      @cy = bound(@cy, 0, height)
      @snake.push([@cx, @cy])
      if @snake.length > @length then @snake.splice(0, 1)

    render: (ctx) =>
      ctx.beginPath()
      for coord, idx in @snake
        [x, y] = coord
        # fuzz up the snake a bit.
        x += rand(-1, 1)
        y += rand(-1, 1)
        if idx is 0 then ctx.moveTo x, y
        else ctx.lineTo x, y
      ctx.stroke()

  snakes = [1000, 100, 100, 50, 50, 50, 50, 50, 5]
    .map((length) -> new Snake(length))

  withInterval (counter) ->
    for snake in snakes
      snake.addSnake()
      snake.render(ctx)
