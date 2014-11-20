(ns miq.wordcloud
  (:require [clojure.data.json :as json]
            [miq.util :refer :all]
            [miq.trello :refer :all]
            [clojure.set :as set]
            (:gen-class))

  )




(defn get-card-text [db]
  (loop [unique-cards (unique-cards-from-movements (:checked-in db))
         words ()]
    (if (empty? unique-cards) (flatten words)
                              (recur (rest unique-cards) (conj words (card-title-words (card-id-of-action (first unique-cards)) db))))

    )
  )
(defn word-in-domain [word domain]

  )

(defn filter-card-text [names]
  (let [lowercase (clojure.string/lower-case names)]

    )
  )

(require '[clojure.string :as str])
(defn word-cloud [db n]
  (let [words (map clojure.string/lower-case (flatten (get-card-text db)))
        domain (str/split (slurp  (str (resource-path) "/domainwords.txt")) #"\n")
        real (filter (fn[w] (in? domain w)) words)
        frequent (frequencies-n n real)
        ]
    frequent
    )
  )


