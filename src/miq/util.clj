(ns miq.util
  (:import (java.text SimpleDateFormat))
  (:require [clojure.data.json :as json])
  )


(import '(java.util Calendar))


(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

; Given a map containing a key :xs and whose value is xs, return a map describing some basic stats
; borrowed from Prismatic/plumbing, then modidied
(defn stats [xs]
  (let [s (reduce + xs)
        n (count xs)
        m (/ s n)
        m2 (/ (reduce + (map (fn [x] (* x x)) xs)) n)
        v (- m2 (* m m))
        sorted (sort xs)
        med (nth sorted (/ n 2))
         ]
    {:s s
     :n  n                                                  ; count
     :m  m                                                  ; mean
     :m2 m2                                                 ; mean square
     :v  v
     :med med                                                    ; variance
     }))

; get resource path, some environments set up the working directory differently
(defn resource-path []
  (let [current-directory (System/getProperty "user.dir")]
    (if (.contains current-directory "/src") (str current-directory "/../../resources") (str current-directory "/resources"))
    ))


; some date stuff
(defn today []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (java.util.Date.))
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
(defn get-vals [amap keys default]
  (map (fn [k] (amap k default)) keys)
  )

; convert human readable y/m/d to date format
(defn to-date-from-human [y m d]
  (.parse (java.text.SimpleDateFormat. "dd-MM-yyyy") (apply str (concat d "-" m "-" y)))
  )

; convert human readable y/m/d to date format
(defn to-millis-from-human [y m d]
  (.getTime (to-date-from-human y m d))
  )
