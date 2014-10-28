(ns miq.ctest
  (:require [clojure.core.async :as async :refer [alts! chan >! <! >!! <!! go]])
  )


(def reverse-channel (chan))

(go (>! reverse-channel  "hello"))

(go (while true
      (println (apply (str (reverse (<! reverse-channel)))))))

