(ns miq.plot
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.wordcloud :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set]
            )

  )

(use '(incanter core charts stats))


; Create a plot of frequencies
(defn plot-movement-frequencies [db]
  (let [movement-week-map (actions-to-by-week-frequency (:movements db))
        interuption-week-map (actions-to-by-week-frequency (:interruptions db))
        rework-week-map (actions-to-by-week-frequency (:reworks db))
        checked-in-week-map (actions-to-by-week-frequency (:checked-in db))
        first-week (week-of-year-from-trello (first (:movements db)))
        last-week (week-of-year-from-trello (last (:movements db)))
        date-range (vec (range first-week last-week))
        plot1 (line-chart date-range (get-vals movement-week-map date-range 0) :y-label "Count" :x-label "Weeks" :legend true :series-label "movements")]
    (do
      (add-categories plot1 date-range (get-vals interuption-week-map date-range 0) :legend true :series-label "interruptions")
      (add-categories plot1 date-range (get-vals rework-week-map date-range 0) :legend true :series-label "rework")
      (add-categories plot1 date-range (get-vals checked-in-week-map date-range 0) :legend true :series-label "checked in")

      )
    plot1
    )
  )

(defn plot-checked-in [db]
  (let
    [checked-in (:checked-in db)
     unique-cards (unique-cards-from-movements checked-in)
     enh (filter (fn [m] (is-movement-enhancement? m db)) unique-cards)
     bugs (filter (fn [m] (is-movement-bug? m db)) unique-cards)
     unknown (filter (fn [m] (is-movement-unknown? m db)) unique-cards)
     enh-week-map (actions-to-by-week-frequency enh)
     bug-week-map (actions-to-by-week-frequency bugs)
     unknown-week-map (actions-to-by-week-frequency unknown)
     ;       first-week (week-of-year-from-trello (first (:checked-in db)))
     first-week (dec (week-of-year-from-trello (first enh)))
     last-week (week-of-year-from-trello (last (:checked-in db)))
     date-range (vec (range first-week last-week))
     plot2 (line-chart date-range (get-vals enh-week-map date-range 0) :y-label "Count" :x-label "Weeks" :legend true :series-label "Enhancements")]
    (do
      (add-categories plot2 date-range (get-vals bug-week-map date-range 0) :legend true :series-label "Bugs")
      (add-categories plot2 date-range (get-vals unknown-week-map date-range 0) :legend true :series-label "Unknown")

      )

    plot2
    )
  )



(defn plot-word-cloud [db n]
     (let [f (word-cloud db n)
           c (map (fn[w] (first w)) f)
           v (map (fn[w] (last w)) f)
           bc (bar-chart c v :vertical false :x-label "task word" :y-label "frequency" :title "word-cloud")
           ]
       bc
       )
     )
