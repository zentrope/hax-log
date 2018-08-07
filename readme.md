# hax-logger

A simple `stdout` hash-map-to-JSON logger for pure structured logging.

## Usage

Cut and paste the code in the `core.clj` namespace into your app, then
tweak as desired.

Required `clojure.data.json` as a dep:

    {:deps {org.clojure/data.json {:mvn/version "0.2.6"}}}

You can use it by passing in Clojure maps.

``` clojure
(ns my.app
  (:require
    [clojure.stacktrace :as st]
    [zentrope.haxlogger.core :as log]))

;; Use of ':message' is arbitrary, here.
(log/info {:message "Application started."})
(log/info {:message "Initial set." :count 23 :names [:a :b]})

(log/warn {:message "Not actually implemented." :epic "23" :sprint "46"})

(try
  (something-risky)
  (throw (Exception. "Oops"))
  (catch Throwable t
    (log/error {:exception (str (class t))
                :stacktrace (with-out-str (st/print-stack-trace t))
                :message (.getMessage t)})))

;; If you want to make sure all messages are printed when your
;; app terminates, try:

(log/wait)

;; or

(.addShutdownHook (Runtime/getRuntime) (Thread. #(log/wait 2000)))

```

## Limitations

- Stdout only
- Maps only
- Pretty printed JSON only


## Output example

Note that `timestamp`, `level`, `namespace` and `thread` are
automatically injected into the structured data output by the system.


``` clojure
{"timestamp":"2018-08-07T00:26:36.151735Z",
 "level":"INFO",
 "namespace":"zentrope.haxlogger.main",
 "thread":"main",
 "message":"Not implemented."}

{"timestamp":"2018-08-07T00:26:36.152347Z",
 "level":"WARN",
 "namespace":"zentrope.haxlogger.main",
 "thread":"main",
 "message":"Lovely day",
 "count":23}

{"timestamp":"2018-08-07T00:26:36.154178Z",
 "level":"INFO",
 "namespace":"zentrope.haxlogger.main",
 "thread":"sim-log",
 "message":"This is from a thread!",
 "tangle":"weave",
 "foo":{"bar":"baz", "quo":"quux"}}

{"timestamp":"2018-08-07T00:37:53.906361Z",
 "level":"ERROR",
 "namespace":"zentrope.haxlogger.main",
 "thread":"sim-log",
 "exception":"class java.lang.Exception",
 "stacktrace": "java.lang.Exception: oops!\n at zentrope.haxlogger.main$sim_log$fn__399.invoke (main.clj:37)\n    zentrope.haxlogger.main$sim_log.invokeStatic (main.clj:37)\n    zentrope.haxlogger.main$sim_log.invoke (main.clj:31)\n    zentrope.haxlogger.main$_main$fn__405.invoke (main.clj:56)\n    clojure.lang.AFn.run (AFn.java:22)\n    java.lang.Thread.run (Thread.java:844)\n",
 "message":"oops!"}

{"timestamp":"2018-08-07T00:26:36.154275Z",
 "level":"INFO",
 "namespace":"zentrope.haxlogger.main",
 "thread":"sim-log",
 "message":"Pausing for 2 seconds"}

{"timestamp":"2018-08-07T00:26:38.154384Z",
 "level":"INFO",
 "namespace":"zentrope.haxlogger.main",
 "thread":"sim-log",
 "message":"Delivering release."}

{"timestamp":"2018-08-07T00:26:38.154648Z",
 "level":"INFO",
 "namespace":"zentrope.haxlogger.main",
 "thread":"main",
 "message":"done"}
```

## License

Copyright (c) 2018-present Keith Irwin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published
by the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see
[http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
