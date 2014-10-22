(ns miq.trello
  (:import (java.util Calendar)
           (clojure inspector$atom_QMARK_))
  (:require [clojure.data.json :as json]
            [miq.util :refer :all])

  )


;map a .json file into a hash map
(defn get-json-files []

  (let
      [directory (clojure.java.io/file (str (resource-path)))
       files (file-seq directory)]
    (filter (fn [f] (.endsWith (.getName f) ".json")) files)
    )
  )




(defn file-to-map [file-path]
  "Return a map of the json data from the file path"
  (json/read-str (slurp file-path) :key-fn keyword)
  )




; define some constants for column ids
(def in-progress-column-id "53067ded5264b32b0bf1dbfa")
(def next-column-id "53067ded5264b32b0bf1dbf9")
(def checked-into-dev "53c7c8c718cd4d9bae3b7c91")
(def checked-into-stable "53067ded5264b32b0bf1dbfb")

; some special virtual columns rules
(def all-checked-in "all")
(def not-any-checked-in "none")


; bunch of filters to find appropriate cards

(defn is-update-card? [c]
  (= "updateCard" (:type c))
  )


(defn has-data-5? [c]
  (= (count (:data c)) 5)
  )

(defn list-before [c]
  (if (nil? (:listBefore (:data c))) nil (:listBefore (:data c)))
  )

(defn list-after [c]
  (if (nil? (:listAfter (:data c))) nil (:listAfter (:data c)))
  )

(defn list-after-id [c]
  (:id (list-after c))
  )

(defn list-before-id [c]
  (:id (list-before c))
  )

(defn has-list-before-after [c]
  (if (or (nil? (list-after c)) (nil? (list-before c))) false true)
  )

(defn card-id-of-action [action]
  (:id (:card (:data action)))
  )

(defn is-card-movement? [c]
  (and (is-update-card? c) (has-data-5? c) (has-list-before-after c) (not= (list-before-id c) (list-after-id c)))
  )

(defn card-movement [json-map]
  (filter is-card-movement? (:actions json-map))
  )


(defn is-card-movement? [c]
  (and (is-update-card? c) (has-data-5? c) (has-list-before-after c) (not= (list-before-id c) (list-after-id c)))
  )

(defn is-checked-in? [movement-c]
  (= (list-after-id movement-c) checked-into-dev)
  )


(defn multiple-equal? [action-col id]
  (cond
    (= all-checked-in id)
    (or
      (= checked-into-dev action-col)
      (= checked-into-stable action-col)
      )
    :else
    (= id action-col)
    )
  )

(defn move-from-to [action from-id to-id]
  (and
    (= (list-before-id action) from-id)
    (= (list-after-id action) to-id)
    )
  )

(defn move-from-to-multiple [action from-id to-id]
  (and
    (multiple-equal? (list-before-id action) from-id)
    (multiple-equal? (list-after-id action) to-id)
    )
  )



(defn is-rework? [movement-c]
  (or
    (and
      (multiple-equal? (list-before-id movement-c) all-checked-in)
      (multiple-equal? (list-after-id movement-c) next-column-id)
      )
    (and
      (multiple-equal? (list-before-id movement-c) all-checked-in)
      (multiple-equal? (list-after-id movement-c) in-progress-column-id)
      )
    )
  )

(defn is-rework1? [movement-c]
  (and
    (not= (list-before-id movement-c) next-column-id)
    (or
      (= (list-after-id movement-c) in-progress-column-id)
      (= (list-after-id movement-c) next-column-id)
      )
    )
  )

(defn is-interruption? [movement-c]
  (and
    (= (list-after-id movement-c) next-column-id)
    (= (list-before-id movement-c) in-progress-column-id)
    )
  )


(defn to-date [trello-date]
  (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z"))
  )

(defn to-millis [trello-date]
  (.getTime (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z")))
  )

(defn week-of-year-from-date
  [date]
  (let [cal (Calendar/getInstance)]
    (.setTime cal date)
    (.get cal Calendar/WEEK_OF_YEAR)))


(defn week-of-year-from-trello [card]
  (week-of-year-from-date (to-date (:date card))))


(defn trello-date [card]
  (:date card)
  )

(defn milli-time [card]
  (to-millis (trello-date card))
  )

(defn build-map [files]
  (loop
      [f files
       ret ()
       ]
    (if (empty? f)
      ret
      (recur (rest f) (concat ret (card-movement (file-to-map (str (.getPath (first f)))))))
      )
    )
  )
