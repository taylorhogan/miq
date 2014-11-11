(ns miq.plot
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
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

