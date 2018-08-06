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
   (java.time.format DateTimeFormatter)))

(defn- timestamp []
  (.format DateTimeFormatter/ISO_INSTANT (Instant/now)))

(defn- thread-name []
  (.getName (Thread/currentThread)))

(def ^:private levels
  {:info "INFO"
   :warn "WARN"
   :error "ERROR"})

(defn- metadata
  [ns level]
  {:timestamp (timestamp)
   :level (levels level)
   :namespace (str ns)
   :thread (thread-name)})

(defonce ^:private LOCK (Object.))

(defn log [ns level m]
  (let [m2 (merge (metadata ns level) m)]
    (locking LOCK
      (json/pprint m2)
      (flush))))

(defmacro info
  [m]
  `(log ~*ns* :info ~m))

(defmacro error
  [m]
  `(log ~*ns* :error ~m))

(defmacro warn
  [m]
  `(log ~*ns* :warn ~m))
