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

(ns zentrope.haxlogger.main
  (:require
   [zentrope.haxlogger.core :as log]))

(defn spawn
  [name f]
  (doto (Thread. f)
    (.setName name)
    (.setDaemon false)
    (.start)))

(defn sim-log
  [lock]
  (log/info {:message "This is from a thread!"
             :tangle :weave
             :foo {:bar "baz" :quo :quux}})
  (deliver lock :done))

;; This exists mainly to exercise/demo the lib.

(defn -main
  [& args]
  (log/info {:message "Not implemented."})
  (log/warn {:message "Lovely day" :count 23})
  (let [lock (promise)]
    (spawn "sim-log" #(sim-log lock))
    @lock
    (log/info {:message "done"})))