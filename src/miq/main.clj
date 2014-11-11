;
; Trello Analytics
; Extract and report various interesting statistics from Trello card movements
;

; a helpful website to see json http://www.jsoneditoronline.org
; a helpful plotting page http://data-sorcery.org/category/plotting/
;
; A json file is converted into a hierarchical hash map (db)
; special maps are injected to this db
; movements

; TODO Need to generalize the idea of "interesting" transitions, for plotting, printing...
(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [miq.print :refer :all]
            [miq.plot :refer :all]
            [clojure.set :as set]
            (:gen-class))

  )


; Make the main db, just a hierarchical hash map. The main db contains a collection
; of sorted distinct card movements, and a list of cards referenced by those movements
(defn make-db [filter-function]
  (let [files (get-json-files)
        movements (sort-by milli-time (distinct (all-card-movements files filter-function)))
        cards (distinct (get-all-cards files))
        card-map (build-id-to-card-map cards)
        ]
    {:movements movements,
     :cards     cards
     :card-map  card-map
     :files     files}
    )
  )





; Inject some analytics to the basic data base
(defn inject-special-movements [db]

  (assoc db :reworks (filter is-rework? (:movements db))
            :interruptions (filter is-interruption? (:movements db))
            :checked-in (filter is-checked-in? (:movements db))
            :card-idxs-that-moved (movements-to-card-id (:movements db))

            )
  )





; go through all cards that moved and just print out the name
(defn get-lateness [db]
  (let
      [card-idxs (:card-idxs-that-moved db)
       cards (map (fn [c] (card-from-id c db)) card-idxs)
       has-due-date (filter (fn [c] (if (nil? (get-due-date c)) false true)) cards)
       ]
    (loop
        [xs has-due-date
         result ()]
      (if (empty? xs)
        result
        (recur (rest xs) (conj result (days-late (first xs) db)))
        )
      )

    )
  )


(defn date-filter [c]
  (if (empty? c)
    false
    (older-than? c "2014" "1" "20")
    )
  )


; main entry point
(defn main []
  (let [db (inject-special-movements (make-db date-filter))]
    (do
      (print-db db)
      ; (view (plot-movement-frequencies db))
      ;(print-date-stats db)
      (print-checked-in-enhancement db)
      (print-checked-in-bug db)
      ;(print-cards-that-moved db)
      ; (print-all-card-movements db)
      ;(view (histogram (get-lateness db) :nbins 12 :x-label "days late" :title "Lateness"))
      )
    )
  )


; go (for debugging now)
(time (main))
(def db (inject-special-movements (make-db date-filter)))


; TODO in progress
(comment

  (println (from-milli-to-days ((get-column-times m) in-progress-column-id 0)))
  (println (from-milli-to-days ((get-column-times m) next-column-id 0)))


  ;(def has-schedule (filter (fn [c] (not-empty (:due c))) (:cards json-map)))
  )