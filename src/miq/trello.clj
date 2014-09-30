
(ns miq.trello
  (:require [clojure.data.json :as json])
  )


;map a .json file into a hash map
(defn file-to-map [file-path]
  "Return a map of the json data from the file path"
  (json/read-str (slurp file-path) :key-fn keyword)
  )


; define some constants for column ids
(def in-progress-column-id "53067ded5264b32b0bf1dbfa")
(def next-column-id "53067ded5264b32b0bf1dbf9")


; bunch of filters to find appropriate cards

(defn  is-update-card? [c]
  (= "updateCard" (:type c))
  )

(defn has-data-5? [c]
  (= (count (:data c)) 5)
  )

(defn list-before [c]
  (if (nil? (:listBefore (:data c))) nil  (:listBefore (:data c)))
  )

(defn list-after [c]
  (if (nil? (:listAfter (:data c))) nil  (:listAfter (:data c)))
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

(defn is-card-movement? [c]
  (and (is-update-card? c) (has-data-5? c) (has-list-before-after c) (not= (list-before-id c) (list-after-id  c)))
  )

(defn card-movement [json-map]
  (filter is-card-movement? (:actions json-map))
  )


(defn is-card-movement? [c]
  (and (is-update-card? c) (has-data-5? c) (has-list-before-after c) (not= (list-before-id c) (list-after-id  c)))
  )



(defn is-rework? [movement-c]
  (and
    (not= (list-before-id movement-c) next-column-id)
    (= (list-after-id movement-c) in-progress-column-id)
    )
  )

(defn is-interruption? [movement-c]
  (and
    (= (list-after-id movement-c) next-column-id)
    (= (list-before-id movement-c) in-progress-column-id)
    )
  )
