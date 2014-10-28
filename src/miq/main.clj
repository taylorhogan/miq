;
; Trello Analytics
; Extract a hashmap from the .json delievered by Trello
; From this extract various statistics about how/when cards are moved
;


; a helpful website to see json http://www.jsoneditoronline.org
; a helpful plotting page http://data-sorcery.org/category/plotting/


(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set])

  )

(use '(incanter core charts stats))




; look at all the .json files in the resource directory and create a sequence of all unique movements
(def files (get-json-files))
(def all-movements (all-card-movements files))
(def all-cards (get-all-cards files))
(def movements (distinct all-movements))
(def cards (distinct all-cards))



(def sorted-movements (sort-by milli-time movements))

(def reworks (filter is-rework? sorted-movements))
(def interruptions (filter is-interruption? sorted-movements))
(def checked-in (filter is-checked-in? sorted-movements))

(println "start of analysis date:" (trello-date (first sorted-movements)))
(println "end of analysis date:" (trello-date (last sorted-movements)))
(println "reworks: " (count reworks))
(println "interruptions " (count interruptions))
(println "check-in" (count checked-in))

(print-movement-stats sorted-movements)






(def interuption-week-map (actions-to-by-week-frequency interruptions))
(def rework-week-map (actions-to-by-week-frequency reworks))
(def movement-week-map (actions-to-by-week-frequency movements))
(def checked-in-week-map (actions-to-by-week-frequency checked-in))
(def first-week (week-of-year-from-trello (first sorted-movements)))
(def last-week (week-of-year-from-trello (last sorted-movements)))
(def date-range (vec (range first-week last-week)))

;(print-csv first-week last-week movement-week-map interuption-week-map rework-week-map checked-in-week-map)
(def plot1 (line-chart date-range (get-vals movement-week-map date-range) :legend true :series-label "movements"))
(add-categories plot1  date-range (get-vals interuption-week-map date-range) :legend true :series-label "interruptions")
(add-categories plot1  date-range (get-vals rework-week-map date-range) :legend true :series-label "rework")
(add-categories plot1  date-range (get-vals checked-in-week-map date-range) :legend true :series-label "checked in")
(view plot1)







(def cards-that-moved (get-distinct-cards sorted-movements))






(def card-name (get-card-name (last cards-that-moved) all-cards))
(println card-name)
(def m (get-movements-for-card (last cards-that-moved) sorted-movements))
(println (from-milli-to-days ((get-column-times m) in-progress-column-id)))
(println (from-milli-to-days ((get-column-times m) next-column-id)))

(defn print-all-card-movements [cards-that-moved sorted-movements]
  (map (fn [c] (get-movements-for-card c) sorted-movements) cards-that-moved)
  )

(movement-week-map (range 32 35))





;(def has-schedule (filter (fn [c] (not-empty (:due c))) (:cards json-map)))
