; Hello world
(println "Hello, world")

; Looping via recursion.
(defn sum
  ([cur] (sum cur 0))
  ([cur acc]
   (if (= cur 0)
     acc
     (sum (- cur 1) (+ acc cur)))
   )
  )

(println "Sum 0 to 100 is" (sum 100))

; let is used to bind variables to a given scope.
(let
  [ x 1 y 2 z 3]
  (let
    [s (+ x y z)]
    (println s)
    )
  )

; Sequences
(println (map (fn [x] (+ x 1)) [1 2 3 4 5 6]))
(println (filter (fn [x] (= (mod x 2) 0)) [1 2 3 4 5 6]))
