(ns miq.print
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set]
            )

  )


; print out the edges of the transition graph
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

; go through all cards that moved and just print out the name
(defn print-all-card-movements [db]
  (loop
    [cards (:card-idxs-that-moved db)]

    (if (empty? cards)
      nil
      (do
        (println (apply str (take 16 (get-card-name (first cards) (:cards db)))))
        (recur (rest cards)))
      )
    )
  )

; go through all cards that moved and just print out the name
(defn print-due-dates [db]
  (let
    [card-idxs (:card-idxs-that-moved db)
     cards (map (fn [c] (card-from-id c db)) card-idxs)
     has-due-date (filter (fn [c] (if (nil? (get-due-date c)) false true)) cards)
     ]
    (doseq [c has-due-date]
      (spit "due.txt" (str (:id c) "label:" (:label c) " " (days-late c db) " " (get-card-name c) "\n") :append true)
      )
    )
  )

(defn print-labels [db]
  (let
    [card-idxs (:card-idxs-that-moved db)
     cards (map (fn [c] (card-from-id c db)) card-idxs)
     ]
    (doseq [c cards]
      (spit "label.txt" (str "label: " (:name (first (:labels c))) " name:" (get-card-name c) "\n") :append true)
      )
    )
  )


(defn print-checked-in-enhancement [db]
  (let
    [checked-in (:checked-in db)
     unique-cards (unique-cards-from-movements checked-in)
     filtered (filter (fn [m] (is-movement-enhancement? m db)) unique-cards)
     ]
    (doseq [c filtered]
      (spit "enhancements.txt" (str (get-card-name (card-from-id (card-id-of-action c) db)) "\n") :append true)
      )
    )
  )

(defn print-checked-in-bug [db]
  (let
    [checked-in (:checked-in db)
     unique-cards (unique-cards-from-movements checked-in)
     filtered (filter (fn [m] (is-movement-bug? m db)) unique-cards)
     ]
    (doseq [c filtered]
      (spit "bugs.txt" (str (get-card-name (card-from-id (card-id-of-action c) db)) "\n") :append true)
      )
    )
  )
(defn print-checked-in [db]
  (let
    [checked-in (:checked-in db)
     unique-cards (unique-cards-from-movements checked-in)

     ]
    (doseq [c unique-cards]
      (spit "checkin.txt" (str "week: " (week-of-year-from-trello c) " " (get-card-name (card-from-id (card-id-of-action c) db)) "\n") :append true)
      )
    )
  )

(defn print-cards-that-moved [db]
  (let
    [cards (:card-idxs-that-moved db)
     ]
    (doseq [c cards]
      (let [card (card-from-id c db)]
        (spit "moved.txt" (str c " " (:due card) " " (:name card) "\n") :append true)
        ))
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


(defn print-csv [start stop col1 col2 col3 col4]
  (loop
    [idx start]
    (if (<= idx stop)
      (do
        (println idx "," (col1 idx 0) "," (col2 idx 0) "," (col3 idx 0) "," (col4 idx 0))
        (recur (inc idx))
        )
      )
    )
  )

(defn count-movements [from to all]
  (count (filter (fn [a] (move-from-to a from to)) all))
  )
(defn create-movement-matrix [db movements]

  )

(defn print-movement-matrix [db]
  (let [v [next-column-id creating-test-case in-progress-column-id checked-into-dev checked-into-stable checked-into-sip]
        w ["request" "test case" "working" "dev" "stable" "sip"]
        s (count v)
       ]


    (doseq [m (for [x (range s) y (range s) :when (not (= x y))] [ x y])]
           (println (w (m 0)) "->" (w (m 1))
           (count-movements (v (m 0)) (v (m 1)) (:movements db))))
    )

  )

