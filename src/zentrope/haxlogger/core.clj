;;;
;;; Copyright (c) 2018-present Keith Irwin
;;;
;;; This program is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published
;;; by the Free Software Foundation, either version 3 of the License,
;;; or (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with this program.  If not, see
;;; <http://www.gnu.org/licenses/>.
;;;

(ns zentrope.haxlogger.core
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string])
  (:import
   (java.util.concurrent.atomic AtomicLong)
   (java.util.concurrent Executors ThreadFactory TimeUnit)
   (java.time Instant)
   (java.time.format DateTimeFormatter)))

(defn- thread-name []
  (.getName (Thread/currentThread)))

(defn- thread-factory
  [stub counter]
  (proxy [ThreadFactory] []
    (newThread [f]
      (doto (Thread. f)
        (.setDaemon true)
        (.setName (str stub "." (.getAndIncrement counter)))))))

(def ^:private EXECUTOR
  (Executors/newFixedThreadPool
    (+ 2 (.availableProcessors (Runtime/getRuntime)))
    (thread-factory "haxlog" (AtomicLong. 0))))

(def ^:private AGENT
  (agent nil :error-mode :continue))

;;; LOGGER utilities

(defn- timestamp []
  (.format DateTimeFormatter/ISO_INSTANT (Instant/now)))

(def ^:private levels
  {:info  "INFO"
   :warn  "WARN"
   :error "ERROR"})

(defn- metadata [ns level]
  {:timestamp (timestamp)
   :level     (levels level)
   :namespace (str ns)
   :thread    (thread-name)})

;;; Interface

(defn log [ns level m]
  (let [data (merge (metadata ns level) m)]
    (send-via EXECUTOR AGENT (fn [_] (json/pprint data)))))

(defmacro info
  [m]
  `(log ~*ns* :info ~m))

(defmacro error
  [m]
  `(log ~*ns* :error ~m))

(defmacro warn
  [m]
  `(log ~*ns* :warn ~m))

(defn wait
  ([timeout-ms]
   (await-for timeout-ms AGENT))
  ([]
   (wait 4000)))
