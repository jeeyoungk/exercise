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

  $('button#simple').click ->
    init()
    ctx.fillStyle = 'rgb(200,0,0)'
    ctx.fillRect 10, 10, 50, 50
    ctx.fillStyle = 'rgba(0,0,200,0.5)'
    ctx.fillRect 30, 30, 55, 50
  $('button#ring').click ->
    init()
    cx = width / 2
    cy = height / 2
    outerRadius = 200
    innerRadius = 50
    N = 16
    for i in [1..N]
      x = cx + outerRadius * Math.sin i / N * TAU
      y = cy + outerRadius * Math.cos i / N * TAU
      ctx.beginPath()
      ctx.arc(x, y, innerRadius, 0, TAU, false)
      ctx.stroke()
  $('button#shape-study').click ->
    init()
    N = 8
    w = width / N
    h = height / N
    r = 0.4 * width / N

    for row in [0..N-1]
      for col in [0..N-1]
        cy = w * (row + 0.5)
        cx = h * (col + 0.5)
        ctx.beginPath()
        clockwise = row % 2 == 0
        start = col / N * TAU
        if 0 <= row % 4 < 2
          end = start + Math.PI
        else
          end = TAU
        ctx.arc(cx, cy, r, start, end, clockwise)
        if row >= N / 2
          ctx.stroke()
        else
          ctx.fill()
  $('button#animation-snake').click ->
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

  # Number rendering related code.
  renderEdge = (r, l) ->
    ctx.beginPath()
    ctx.moveTo(0, -r)
    ctx.arcTo(l + r, -r, l + r, r, r)
    ctx.arcTo(l + r, r, l , r, r)
    ctx.arcTo(-r, r, -r, -r, r)
    ctx.arcTo(-r, -r, 0, -r, r)
    ctx.fill()
  # The topology:
  #  -0-
  # 1   2
  #  -3-
  # 4   5
  #  -6-
  NUM_EDGE = [
    [1, 1, 1, 0, 1, 1, 1]
    [0, 0, 1, 0, 0, 1, 0]
    [1, 0, 1, 1, 1, 0, 1]
    [1, 0, 1, 1, 0, 1, 1]
    [0, 1, 1, 1, 0, 1, 0]
    [1, 1, 0, 1, 0, 1, 1]
    [0, 1, 0, 1, 1, 1, 1]
    [1, 0, 1, 0, 0, 1, 0]
    [1, 1, 1, 1, 1, 1, 1]
    [1, 1, 1, 1, 0, 1, 0]
  ]

  RIGHT = 0
  DOWN = Math.PI / 2

  EDGE_POS = [
    # locations of each edge - [x, y, angle]
    [0, 0, RIGHT]
    [0, 0, DOWN]
    [1, 0, DOWN]
    [0, 1, RIGHT]
    [0, 1, DOWN]
    [1, 1, DOWN]
    [0, 2, RIGHT]
  ]

  renderNumber = (number, L=80, R=5, MARGIN=10) ->
    edges = NUM_EDGE[number]
    for value, idx in edges
      if not value then continue
      [logicalX, logicalY, angle] = EDGE_POS[idx]
      ctx.save()
      ctx.translate(logicalX * L, logicalY * L)
      ctx.rotate(angle)
      ctx.save()
      ctx.translate(MARGIN, 0)
      renderEdge(R, L - MARGIN * 2)
      ctx.restore()
      ctx.restore()

  $('button#animation-number').click ->
    init()
    withInterval (counter) ->
      ctx.fillStyle = 'rgba(32, 32, 32, 0.5)'
      ctx.translate 100, 100
      value = Math.floor(counter / 10) % 10
      renderNumber value

  $('button#number-grid').click ->
    init()
    ctx.fillStyle = 'rgba(32, 32, 32, 0.5)'
    for i in [0..9]
      ctx.save()
      ctx.translate 50 + i * 50, 50
      renderNumber i, 32, 2, 4
      ctx.restore()
