(defproject miq "0.1.0-SNAPSHOT"
            :description "Trello Analytics for Minimally Invasive Quality"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/data.json "0.2.5"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [incanter "1.5.5"]

                           ]
            :plugins [[codox "0.8.10"]]
            :main miq.core
            :aot :all
            )
