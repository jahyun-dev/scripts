#!/usr/bin/env bb
(ns kafka-connect-load
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

(defn write-file
  [x file-path]
  (with-open [w (clojure.java.io/writer file-path)]
    (.write w (json/generate-string x {:pretty true}))))

(let [[host output-dir] *command-line-args*
      configs (->>
                (get-connectors host)
                (map (partial get-connector-config host)))]
  (for [config configs]
    (let [file-name (str output-dir "/" (get config "name") ".json")
          sorted-config (into (sorted-map) config)]
      (write-file sorted-config file-name))))
