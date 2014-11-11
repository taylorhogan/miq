(ns miq.trello
  (:import (java.util Calendar)
           (clojure inspector$atom_QMARK_))
  (:require [clojure.data.json :as json]
            [miq.util :refer :all])

  )

;TODO Define this and other rules in an file parsed at run time
; define some constants for column ids
(def ^:const in-progress-column-id "53067ded5264b32b0bf1dbfa")
(def ^:const next-column-id "53067ded5264b32b0bf1dbf9")
(def ^:const checked-into-dev "53c7c8c718cd4d9bae3b7c91")
(def ^:const checked-into-stable "53067ded5264b32b0bf1dbfb")
(def ^:const ENHANCEMENT "545e7fab74d650d567cda37f")
(def ^:const BUG "545e7fab74d650d567cda37e")



; some special virtual columns rules
(def ^:const all-checked-in "all")
(def ^:const not-any-checked-in "none")



(defn get-json-files []
  "return all .json files in a directory"
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
  (or
    (= (list-after-id movement-c) checked-into-dev)
    (= (list-after-id movement-c) checked-into-stable)
    )
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

(defn is-enhancement? [card]
  (some (fn [l] (= (:id l) ENHANCEMENT)) (:labels card))
  )

(defn is-bug? [card]
  (some (fn [l] (= (:id l) BUG)) (:labels card))
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

(defn older-than? [card y m d]
  (let [card-date (milli-time card)
        proposal (to-millis-from-human y m d)]
    (> card-date proposal)
    )
  )

(defn all-card-movements [files filter-function]
  (loop
      [f files
       ret ()
       ]
    (if (empty? f)
      (filter filter-function ret)
      (recur (rest f) (concat ret (card-movement (file-to-map (str (.getPath (first f)))))))
      )
    )
  )

(defn build-id-to-card-map [cards]
  (loop [cds cards
         result {}]
    (if (empty? cds)
      result
      (recur (rest cds) (assoc result (:id (first cds)) (first cds))))))



(defn get-all-cards [files]
  (loop
      [f files
       ret ()
       ]
    (if (empty? f)
      ret
      (recur (rest f) (concat ret (:cards (file-to-map (str (.getPath (first f)))))))
      )
    )
  )

(defn actions-to-by-week-frequency [actions]
  "comvert a collection of actions to frequency by week"
  (frequencies (map week-of-year-from-trello actions))
  )



(defn card-actions-id-to-card-id [all]
  (loop
      [ms all
       cards #{}]
    (if (empty? ms)
      cards
      (recur (rest ms) (conj cards (card-id-of-action (first ms))))
      )

    )
  )

(defn get-movements-for-card [card-id movements]
  (loop
      [ms movements
       ret ()]
    (if (empty? ms)
      (reverse ret)
      (recur (rest ms)
             (if (= (card-id-of-action (first ms)) card-id) (cons (first ms) ret) ret)
             )

      )
    )
  )

(defn print-movements [movements]
  (loop
      [ms movements]
    (if (empty? ms) nil
                    (do
                      (println (list-before-id (first ms)) (list-after-id (first ms)))
                      (recur (rest ms)))

                    )
    )
  )

; given a collection of movements on a given card, return a collection of times in each column
(defn get-column-times [movements-of-card]
  (loop
      [ms movements-of-card
       hm {}]

    (if (<= (count ms) 1)
      hm

      (let [col-id (list-after-id (first ms))
            start-time (milli-time (first ms))
            end-time (milli-time (first (rest ms)))
            old-sum (hm col-id 0)]
        (recur
          (rest ms)
          (assoc hm col-id (+ old-sum (- end-time start-time)))

          )
        )

      )
    )
  )








(defn card-from-id [card-id db]
  (get (:card-map db) card-id)
  )

; Given a card-id and a db
(defn get-card-name [card]
  (:name card)
  )

(defn get-due-date [card]
  (:due card)
  )



; Derive the checked in date, or today if not checked in
(defn get-checked-in-date [card db]
  (loop
      [ms (reverse (:movements db))
       date (today)]
    (if (empty? ms)
      date
      (recur (rest ms)
             (if
                 (and
                   (= (:id card) (card-id-of-action (first ms)))
                   (is-checked-in? (first ms))
                   )
               (:date (first ms))
               date
               )
             )

      )
    )

  )


(defn days-late [c db]
  (let [due-date (to-millis (get-due-date c))
        checked-in (to-millis (get-checked-in-date c db))
        late (- checked-in due-date)
        ]
    (from-milli-to-days late)

    )
  )