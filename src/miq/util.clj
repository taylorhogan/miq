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
  (.format (java.text.SimpleDateFormat. "MM/dd/yyyy")  (java.util.Date.))
)
(defn remove-from-end [s end]
  (if (.endsWith s end)
      (.substring s 0 (- (count s)
                         (count end)))
    s))


(defn to-date [trello-date]
  (.parse  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z"))
  )

(defn to-millis [trello-date]
  (.getTime (.parse  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z")))
  )

(defn week-of-year-from-date
  [date]
	(let [cal (Calendar/getInstance)]
		(.setTime cal date)
		(.get cal Calendar/WEEK_OF_YEAR)))


(defn week-of-year-from-trello [card]
  (week-of-year-from-date(to-date (:date card ))))
