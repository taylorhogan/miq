; Trello Analytics
; a helpful website to see json http://www.jsoneditoronline.org
(ns miq.main
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set])
  )



(use '(incanter core charts stats ))




; start of analyis

(def file-path (str (resource-path) "/cd.json"))
(def json-map (file-to-map file-path))


(def movements (card-movement json-map))
(def reworks  (filter is-rework? movements))
(def interruptions  (filter is-interruption? movements))





(def interruptions-on-week (map week-of-year-from-trello interruptions))
(view (histogram interruptions-on-week :nbins 20 :y-label "interruptions by week" :x-label "week number" :title "interruptions by week") )

(def rework-on-week (map week-of-year-from-trello reworks))
(view (histogram rework-on-week :nbins 20 :y-label "rework by week" :x-label "week number" :title "reworks by week") )

(def movements-on-week (map week-of-year-from-trello movements))
(view (histogram movements-on-week :nbins 20 :y-label "movements by week" :x-label "week number" :title "movements by week") )
















