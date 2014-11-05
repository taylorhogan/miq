; just a lab worksheet to test things out

(ns miq.lab
  (:require [clojure.core.async :as async :refer [alts! alts!! chan >! <! >!! <!! go]])
  )




(let [c1 (chan)
      c2 (chan)]
  (go (while true
        (let [[v ch] (alts! [c1 c2])]
          (println "Read" v))))
  (go (>! c1 "hi on channel 1"))
  (go (>! c2 "there on channel 2"))
   (go (>! c2 "there on channel 2"))
  )

(defn memoize [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)))))

(defn fib [n]
  (if (<= n 1)
    n
    (+ (fib (dec n)) (fib (- n 2)))))

(time (fib 35))


(def fib (memoize fib))

(time (fib 35))



(let [n 1000
      cs (repeatedly n chan)
      begin (System/currentTimeMillis)]
  (doseq [c cs] (go (>! c "hi\n")))
  (dotimes [i n]
    (let [[v c] (alts!! cs)]
      (spit "channel.txt" v :append true)
      ))
  (println "Read" n "msgs in" (- (System/currentTimeMillis) begin) "ms"))