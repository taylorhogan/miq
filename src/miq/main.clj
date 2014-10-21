; Trello Analytics
; a helpful website to see json http://www.jsoneditoronline.org
(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set])

  )



(use '(incanter core charts stats))




; start of analyis
(def files (get-json-files))
(def all (build-map files))
(def movements (distinct all))
(println "distinct" (count movements))





(def reworks (filter is-rework? movements))
(def interruptions (filter is-interruption? movements))
(def checked-in (filter is-checked-in? movements))
;(def has-schedule (filter (fn [c] (not-empty (:due c))) (:cards json-map)))

(defn print-movement-stats [movements]
  (do
    (println "movements:" (count movements))

    (println "next -> in progress:" (count (filter (fn [a] (move-from-to a next-column-id in-progress-column-id)) movements)))
    (println "in progress -> next:" (count (filter (fn [a] (move-from-to a in-progress-column-id next-column-id)) movements)))
    (println "next -> checked in dev:" (count (filter (fn [a] (move-from-to a next-column-id checked-into-dev)) movements)))
    (println "checked in dev: -> next:" (count (filter (fn [a] (move-from-to a checked-into-dev next-column-id)) movements)))
    (println "next -> checked in stable:" (count (filter (fn [a] (move-from-to a next-column-id checked-into-stable)) movements)))
    (println "checked in stable: -> next" (count (filter (fn [a] (move-from-to a checked-into-stable next-column-id)) movements)))

    (println "in progress -> checked in dev:" (count (filter (fn [a] (move-from-to a in-progress-column-id checked-into-dev)) movements)))
    (println "checked in dev ->in progress:" (count (filter (fn [a] (move-from-to a checked-into-dev in-progress-column-id)) movements)))
    (println "in progress -> checked in stable:" (count (filter (fn [a] (move-from-to a in-progress-column-id checked-into-stable)) movements)))
    (println "check in stable -> in progress:" (count (filter (fn [a] (move-from-to a checked-into-stable in-progress-column-id)) movements)))


    (println "checked in dev -> checked in stable:" (count (filter (fn [a] (move-from-to a checked-into-dev checked-into-stable)) movements)))
    (println "check in stable -> checked in dev:" (count (filter (fn [a] (move-from-to a checked-into-stable checked-into-dev)) movements)))

    )
  )


(def sorted-movements (sort-by milli-time movements))

(println "start of analysis date:" (trello-date (first sorted-movements)))
(println "end of analysis date:" (trello-date (last sorted-movements)))

(print-movement-stats movements)




(println "total interruptions" (count interruptions))
(def interruptions-on-week (map week-of-year-from-trello interruptions))
(view (histogram interruptions-on-week :nbins 20 :y-label "interruptions by week" :x-label "week number" :title "interruptions by week"))

(def rework-on-week (map week-of-year-from-trello reworks))
(view (histogram rework-on-week :nbins 20 :y-label "rework by week" :x-label "week number" :title "reworks by week"))

(def movements-on-week (map week-of-year-from-trello movements))
(view (histogram movements-on-week :nbins 20 :y-label "movements by week" :x-label "week number" :title "movements by week"))

(def checked-in-on-week (map week-of-year-from-trello checked-in))
(view (histogram checked-in-on-week :nbins 20 :y-label "check in by week" :x-label "week number" :title "checked in by week"))




(println (week-of-year-from-trello (first movements)) (week-of-year-from-trello (last movements)))




(defn date-card-moved-to-check-in [c movement-actions]


  (filter (fn [action] (and
                         (is-checked-in? action)
                         (= (:id c) (card-id-of-action action)))) movement-actions))


















