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
   (java.time Instant)
   (java.time.format DateTimeFormatter)
   (java.util.concurrent ArrayBlockingQueue)))

;;; THREAD utilities

(defn- thread-name []
  (.getName (Thread/currentThread)))

(defn- spawn! [f]
  (doto (Thread. f)
    (.setName (str "hax.log.q." (rand-int 100)))
    (.setDaemon true)
    (.start)))

;;; QUEUE utilities

(defn- handle [queue f]
  (try
    (loop []
      (let [value (.take queue)]
        (f value))
      (recur))
    (catch InterruptedException e
      (println "Queue [%s] terminated." (thread-name)))
    (catch Throwable t
      (println "Queue [%s] terminated (%s)." (thread-name) (str t)))))

(defn- put! [{:keys [queue]} value]
  (doto queue
    ;; Use .offer if you want to drop messages if backed up.
    (.put value)))

(defn- worker-queue [size f]
  (let [queue (ArrayBlockingQueue. size)
        thread (spawn! #(handle queue f))]
    {:queue queue :thread thread}))

;;; LOGGER utilities

(defn- timestamp []
  (.format DateTimeFormatter/ISO_INSTANT (Instant/now)))

(def ^:private levels
  {:info "INFO"
   :warn "WARN"
   :error "ERROR"})

(defn- metadata [ns level]
  {:timestamp (timestamp)
   :level (levels level)
   :namespace (str ns)
   :thread (thread-name)})

(defn- printer [m]
  (json/pprint m) (flush))

(defonce ^:private QUEUE
  (worker-queue 1024 #'printer))

;;; Interface

(defn log [ns level m]
  (put! QUEUE (merge (metadata ns level) m)))

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
  []
  (let [{:keys [queue]} QUEUE]
    (loop []
      (when-not (.isEmpty queue)
        (Thread/sleep 100)
        (recur)))))
