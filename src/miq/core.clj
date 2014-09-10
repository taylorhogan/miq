; Trello Analytics

(ns json.core
  (:require [clojure.data.json :as json])
  )
(import '(java.util Calendar))


(use '(incanter core charts stats ))

; define some constants for column ids
(def in-progress-column-id "53067ded5264b32b0bf1dbfa")
(def next-column-id "53067ded5264b32b0bf1dbf9")



; map a .json file into a hash map
(defn file-to-map [file-path]
  "Return a map of the json data from the file path"
  (json/read-str (slurp file-path) :key-fn keyword)
  )

; get resource path
(defn resource-path []
  (let [current-directory (System/getProperty "user.dir")]
    (if (.contains current-directory "/src") (str current-directory "/../../resources") (str current-directory "/resources"))
    ))


; some date stuff
(defn today []
  (.format (java.text.SimpleDateFormat. "MM/dd/yyyy")  (java.util.Date.))
)
(defn remove-from-end [s end]
  (if (.endsWith s end)
      (.substring s 0 (- (count s)
                         (count end)))
    s))


(defn to-date [trello-date]
  (.parse  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z"))
  )

(defn to-millis [trello-date]
  (.getTime (.parse  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (remove-from-end trello-date "Z")))
  )

(defn week-of-year-from-date
  [date]
	(let [cal (Calendar/getInstance)]
		(.setTime cal date)
		(.get cal Calendar/WEEK_OF_YEAR)))


(defn week-of-year-from-trello [card]
  (week-of-year-from-date(to-date (:date card ))))



;
(defn column-name-to-column-id [json-map column-name]
  (loop [in-data (:lists json-map)]
    (if (empty? in-data) nil
      (if (= column-name (:name (first in-data))) (:id (first in-data))
        (recur (rest in-data))

        )
      )
    )
  )



; Is this card in a column-id
(defn card-in-column? [card column-id]
  (= (:idList card) column-id)
  )

; How many cards are in a named column
(defn arity-of-column [json-map column-name]
  (loop [cards (:cards json-map)
         column-id (column-name-to-column-id json-map column-name)
         accum 0]
    (if (empty? cards) accum
      (recur
       (rest cards)
        column-id
       (if (card-in-column? (first cards) column-id)
         (inc accum)
         accum)

       )
      )
    )
  )



(defn card-movement [json-map]
  (filter is-card-movement? (:actions json-map))
  )
(defn is-update-card? [c]
  (= "updateCard" (:type c))
  )

(defn has-data-5? [c]
  (= (count (:data c)) 5)
  )

(defn has-list-before-after [c]
  (if (or (nil? (list-after c)) (nil? (list-before c))) false true)
  )

(defn is-card-movement? [c]
  (and (is-update-card? c) (has-data-5? c) (has-list-before-after c) (not= (list-before-id c) (list-after-id  c)))
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

(defn is-owner [c o]
  )
;http://www.jsoneditoronline.org
(defn make-blip [time delta]
  (conj [] (- time delta) time (+ time delta) ))


(defn blipper [xs]
  (loop
    [css xs
         result []]
    (if (empty? css) result
      (recur (rest css) (conj result (make-blip (first css) 1))))
    )
  )

(defn get-bucket [min max delta value]
  (/ (- value min) delta)
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


; start of analyis

(def file-path (str (resource-path) "/cd.json"))
(def json-map (file-to-map file-path))
(def movements (card-movement json-map))
(count movements)
(def reworks  (filter is-rework? movements))
(count reworks)
(def interruptions  (filter is-interruption? movements))





(def interruptions-on-week (map week-of-year-from-trello interruptions))
(view (histogram interruptions-on-week :nbins 20 :y-label "interruptions" :x-label "week number" :title "interrupions by week") )

(def rework-on-week (map week-of-year-from-trello reworks))
(view (histogram rework-on-week :nbins 20 :y-label "reworks" :x-label "week number" :title "reworks by week") )
















(def interruptions-by-person (group-by (fn[c] (:fullName (:memberCreator c)) ) interruptions))
(count  interruptions-by-person)



(def taylor-movements (filter (fn[c] (= (:fullName (:memberCreator c)) "taylor")) movements))


(def dates (reverse(map (fn[c] (to-millis (:date c))) taylor-movements)))


(def y (flatten(repeat  (count dates) '(0 1 0))))
 (comment
 (view (time-series-plot blipped-date y
                          :title "Taylor's Rejected bug fixes"
                          :x-label "Time"
                          :y-label "Rejections"))
 )


(- (last dates) (first dates))

(defn bucketize [seq-of-times bucket-size]
  (reduce (fn [m data]
                (let [min (last seq-of-times)
                      max (first seq-of-times)
                      bucket (get-bucket min max  bucket-size data)]

                  (update-in m [:buckets bucket] (fnil inc 0))))
              {}
              seq-of-times)
  )











