(ns miq.util
  (:require [clojure.data.json :as json])
  )


(import '(java.util Calendar))

; get resource path, some environments set up the working directory differently
(defn resource-path []
  (let [current-directory (System/getProperty "user.dir")]
    (if (.contains current-directory "/src") (str current-directory "/../../resources") (str current-directory "/resources"))
    ))


; some date stuff
(defn today []
  (.format (java.text.SimpleDateFormat. "MM/dd/yyyy") (java.util.Date.))
  )

; Remove suffix from string
(defn remove-from-end [string suffix]
  (if (.endsWith string suffix)
    (.substring string 0 (- (count string)
                       (count suffix)))
    string))

; convert milliseconds to days
(defn from-milli-to-days [milli]
  (/ (double milli) (* 1000.0 60.0 60.0 24.0)))

; given a hashmap and a collection of keys return the associated collection of values
(defn get-vals [amap keys]
  (map (fn [k] (amap k)) keys)
  )