;
; Trello Analytics
; Extract and report various interesting statistics from Trello card movements
;


; a helpful website to see json http://www.jsoneditoronline.org
; a helpful plotting page http://data-sorcery.org/category/plotting/


(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set]
            (:gen-class))

  )

(use '(incanter core charts stats))


; Make the main db, just a hierarchical hash map. The main db contains a collection
; of sorted distinct card movements, and a list of cards referenced by those movements
(defn make-db []
  (let [files (get-json-files)
        movements (sort-by milli-time (distinct (all-card-movements files)))
        cards (distinct (get-all-cards files))
        ]
    {:movements movements,
     :cards     cards}
    )
  )





; Inject some analytics to the basic data base
(defn inject-special-movements [db]

  (assoc db :reworks (filter is-rework? (:movements db))
            :interruptions (filter is-interruption? (:movements db))
            :checked-in (filter is-checked-in? (:movements db))
            :cards-that-moved (get-distinct-cards (:movements db))

            )
  )

; Print out some interesting stats
(defn print-db [db]
  (do
    (println "start of analysis date:" (trello-date (first (:movements db))))
    (println "end of analysis date:" (trello-date (last (:movements db))))
    (println "reworks: " (count (:reworks db)))
    (println "interruptions " (count (:interruptions db)))
    (println "check-in" (count (:checked-in db)))
    (print-movement-stats (:movements db))
    ))


; Create a plot of frequencies
(defn plot-movement-frequencies [db]
  (let [movement-week-map (actions-to-by-week-frequency (:movements db))
        interuption-week-map (actions-to-by-week-frequency (:interruptions db))
        rework-week-map (actions-to-by-week-frequency (:reworks db))
        checked-in-week-map (actions-to-by-week-frequency (:checked-in db))
        first-week (week-of-year-from-trello (first (:movements db)))
        last-week (week-of-year-from-trello (last (:movements db)))
        date-range (vec (range first-week last-week))
        plot1 (line-chart date-range (get-vals movement-week-map date-range 0) :legend true :series-label "movements")]
    (do
      (add-categories plot1 date-range (get-vals interuption-week-map date-range 0) :legend true :series-label "interruptions")
      (add-categories plot1 date-range (get-vals rework-week-map date-range 0) :legend true :series-label "rework")
      (add-categories plot1 date-range (get-vals checked-in-week-map date-range 0) :legend true :series-label "checked in")

      )
    plot1
    )
  )

; go through all cards that moved and just print out the name
(defn print-all-card-movements [db]
  (loop
      [cards (:cards-that-moved db)]

    (if (empty? cards)
      nil
      (do
        (println (get-card-name (first cards) (:cards db)))
        (recur (rest cards)))
      )
    )
  )

; main entry point
(defn main []
  (let [db (inject-special-movements (make-db))]
    (do
      (print-db db)
      (view (plot-movement-frequencies db))
      (def cards-that-moved (get-distinct-cards (:movements db)))
      (print-all-card-movements db)
      )
    )
  )


; go (for debugging now)
(main)

; TODO in progress
(comment

  (def card-name (get-card-name (last cards-that-moved) all-cards))
  (println card-name)
  (def m (get-movements-for-card (last cards-that-moved) sorted-movements))
  (println (from-milli-to-days ((get-column-times m) in-progress-column-id 0)))
  (println (from-milli-to-days ((get-column-times m) next-column-id 0)))





  ;(def has-schedule (filter (fn [c] (not-empty (:due c))) (:cards json-map)))
  )