(ns wibeeer.core
  (:gen-class)
  (:require [clojure.xml :refer [parse]]
            [clojure.string :as s]
            [config.core :refer [load-env]]))

(def formater-date-time (new java.text.SimpleDateFormat "yyyy-MM-dd HH:mm:ss"))
(def formater-date (new java.text.SimpleDateFormat "yyyy-MM-dd"))

(defn extract-value [data value]
  (->> data
   :content
   (filter #(= (:tag %) value))
   first
   :content
   first
   read-string))

(defn get-status-url [ip]
  (str "http://" ip "/en/status.xml?rnd=" (rand-int 1000)))

(defn get-values-ip [ip values]
  (let [data (parse (get-status-url ip))]
    (map #(extract-value data %) values)))

(defn get-values [requests]
  (flatten
   (map #(apply get-values-ip %) requests)))

(defn totals [requests]
  (try
    (let [dt (java.util.Date.)]
      (spit
       (str (.format formater-date dt)".log")
       (str
        (.format formater-date-time dt) ";"
        (s/join ";" (get-values requests))
        "\n")
       :append true))
    (catch Exception e
      (.printStackTrace e))))

(defn monitor [requests seconds]
  (while true
    (do
      (totals requests)
      (Thread/sleep (* seconds 1000)))))

(defn -main [& args]
  (let [{:keys [requests frequency-seconds]}
        (:wibeeer (load-env))]
    (monitor requests
             frequency-seconds)))

