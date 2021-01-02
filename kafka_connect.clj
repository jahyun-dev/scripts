#!/usr/bin/env bb
(ns kafka-connect
  (:require [cheshire.core :as json]))

(defn get-connectors
  [host]
  (->>
    @(org.httpkit.client/get (str host "/connectors"))
    :body
    json/parse-string))

(defn get-connector-config
  [host connector]
  (->> @(org.httpkit.client/get (str host "/connectors/" connector "/config"))
       :body
       json/parse-string))

(defn update-connector-config
  [host connector config]
  (->> @(org.httpkit.client/put
          (str host "/connectors/" connector "/config")
          {:body    (json/generate-string config)
           :headers {"Content-Type" "application/json"}})
       :body
       json/parse-string))

(defn get-all-connector-configs
  [host]
  (->> (get-connectors host)
       (map (partial get-connector-config host))))

(defn write-file
  [x file-path]
  (with-open [w (clojure.java.io/writer file-path)]
    (.write w (json/generate-string x {:pretty true}))))

(defn write-kafka-connectors
  [configs output-dir]
  (for [config configs]
    (let [file-name (str output-dir "/" (get config "name") ".json")
          sorted-config (into (sorted-map) config)]
      (write-file sorted-config file-name))))

(defn dump-kafka-connectors
  [host output-dir]
  (write-kafka-connectors (get-all-connector-configs host) output-dir))

(defn update-config
  [config update-config]
  (merge config update-config))

(let [[host output-dir] *command-line-args*]
  (dump-kafka-connectors host output-dir))
