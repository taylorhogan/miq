; Trello Analytics
; a helpful website to see json http://www.jsoneditoronline.org
(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set])

  )


(use '(incanter core charts stats))





; print out the edges of the graph


(defn print-movement-stats [movements]
  (do
    (println "movements:" (count movements))

    (println "next -> in progress:" (count (filter (fn [a] (move-from-to a next-column-id in-progress-column-id)) movements)))
    (println "in progress -> next:" (count (filter (fn [a] (move-from-to a in-progress-column-id next-column-id)) movements)))
    (println "next -> checked in" (count (filter (fn [a] (move-from-to-multiple a next-column-id all-checked-in)) movements)))
    (println "checked in -> next:" (count (filter (fn [a] (move-from-to-multiple a all-checked-in next-column-id)) movements)))

    (println "in progress -> checked in :" (count (filter (fn [a] (move-from-to-multiple a in-progress-column-id all-checked-in)) movements)))
    (println "checked in -> in progress:" (count (filter (fn [a] (move-from-to-multiple a all-checked-in in-progress-column-id)) movements)))


    )
  )

; look at all the .json files in the resource directory and create a sequence of all unique actions
(def files (get-json-files))
(def all (build-map files))
(def movements (distinct all))
(println "distinct" (count movements))








;(def has-schedule (filter (fn [c] (not-empty (:due c))) (:cards json-map)))
(def sorted-movements (sort-by milli-time movements))

(def reworks (filter is-rework? sorted-movements))
(def interruptions (filter is-interruption? sorted-movements))
(def checked-in (filter is-checked-in? sorted-movements))

(println "start of analysis date:" (trello-date (first sorted-movements)))
(println "end of analysis date:" (trello-date (last sorted-movements)))
(println "reworks: " (count reworks))
(println "interruptions " (count interruptions))
(println "check-in" (count checked-in))

(print-movement-stats movements)




(def interruptions-on-week (map week-of-year-from-trello interruptions))
;(view (histogram interruptions-on-week :nbins 20 :y-label "interruptions by week" :x-label "week number" :title "interruptions by week"))

(def rework-on-week (map week-of-year-from-trello reworks))
;(view (histogram rework-on-week :nbins 20 :y-label "rework by week" :x-label "week number" :title "reworks by week"))

(def movements-on-week (map week-of-year-from-trello movements))
;(view (histogram movements-on-week :nbins 20 :y-label "movements by week" :x-label "week number" :title "movements by week"))

(def checked-in-on-week (map week-of-year-from-trello checked-in))
;(view (histogram checked-in-on-week :nbins 20 :y-label "check in by week" :x-label "week number" :title "checked in by week"))
;


(defn add-to-map [map  the-type-of-action]
  (let [cur (the-type-of-action map 0)]
    (into map '{the-type-of-action (inc cur)})
    )
  )



(def weeks (distinct interruptions-on-week))

(comment
  (view (bar-chart weeks
                   interruptions-on-week
                   :group-by weeks
                   :title "Interruptions"
                   :legend true
                   :y-label "Count"
                   :x-label "Week"))
  )





(distinct interruptions-on-week)



(println (week-of-year-from-trello (first movements)) (week-of-year-from-trello (last movements)))




(defn date-card-moved-to-check-in [c movement-actions]


  (filter (fn [action] (and
                         (is-checked-in? action)
                         (= (:id c) (card-id-of-action action)))) movement-actions))


















