(ns strava-report.core
  (:require [clj-strava.api :as strava]
            [clojure.core.async :refer [<!!]]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout put!]])
  (:gen-class))


(defn get-zones
  [id c access-token]
  (put! c (<!! (strava/activity-zones access-token {:id id}))))

(def zones-distribution (atom {:Z1  0 :Z2  0 :Z3  0 :Z4  0 :Z5  0 :total-time 0}))

(def zones-distribution-percentage (atom {:Z1  0 :Z2  0 :Z3  0 :Z4  0 :Z5  0}))


(defn increase-zones
  [zone-state activity-zone]
  (merge-with + zone-state {:Z1 ((get ((activity-zone 0) :distribution_buckets) 0) :time)
                            :Z2 ((get ((activity-zone 0) :distribution_buckets) 1) :time)
                            :Z3 ((get ((activity-zone 0) :distribution_buckets) 2) :time)
                            :Z4 ((get ((activity-zone 0) :distribution_buckets) 3) :time)
                            :Z5 ((get ((activity-zone 0) :distribution_buckets) 4) :time)
                            :total-time (reduce #(+ %1 (:time %2)) 0 ((activity-zone 0) :distribution_buckets))}))
(defn zones-by-total-time
  [zone-state zones-by-time]
  (merge-with + zone-state {:Z1 (/ (* (zones-by-time :Z1) 100.0) (zones-by-time :total-time))
                            :Z2 (/ (* (zones-by-time :Z2) 100.0) (zones-by-time :total-time))
                            :Z3 (/ (* (zones-by-time :Z3) 100.0) (zones-by-time :total-time))
                            :Z4 (/ (* (zones-by-time :Z4) 100.0) (zones-by-time :total-time))
                            :Z5 (/ (* (zones-by-time :Z5) 100.0) (zones-by-time :total-time))}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [access-token (strava/access-token "35b34365890d49e57aad496ab5209e1b4c7bb15a")
        last7DaysActivities (<!! (strava/activities access-token
                                                    {"per_page" 25 "after" (c/to-epoch (t/minus (t/today) (t/days 7)))}))
        activityIds (->> last7DaysActivities
                         (reduce-kv
                           #(assoc %1 %2 (:id %3))
                           {}))
        n (count activityIds)
        cs (repeatedly n chan)]
    (doseq [[idx c] (map-indexed #(vector %1 %2) cs)]
      (get-zones (get activityIds idx) c access-token))
    (dotimes [i n]
      (let [[v c] (alts!! (conj cs (timeout 10000)))]
        (println "handled zone" i " [->]" v )
        (swap! zones-distribution increase-zones v)))
    (swap! zones-distribution-percentage zones-by-total-time @zones-distribution)
    activityIds)
  (println @zones-distribution-percentage))
