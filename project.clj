(defproject strava-report "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ekimber/clj-strava "0.1.1"]
                 [clj-time "0.14.2"]]
  :main ^:skip-aot strava-report.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
