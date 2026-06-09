(ns slag.test.utils.runner
  (:require
    [aleph.http :as http]
    [clojure.java.io :as io]
    [etaoin.api :as eta]
    [etaoin.impl.util :as ueta]
    [hiccup2.core :as hiccup]))

(defn html [cljs]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "ClojureScript Tests"]
    [:style "body {
              font-family: monospace;
              padding: 20px;
              background-color: #f5f5f5;
          }
          #test-output {
              background-color: white;
              border: 1px solid #ddd;
              padding: 15px;
              border-radius: 4px;
              white-space: pre-wrap;
              word-wrap: break-word;
              min-height: 200px;
          }
          .pass {
              color: green;
          }
          .fail {
              color: red;
          }"]]
   [:body
    [:h1 "ClojureScript Tests"]
    [:div#test-output "Running tests..."]
    [:script {:src cljs}]]])

(defn ^:private handler [request resource cljs]
  (let [uri (:uri request)]
    (cond
      (= uri resource)
      {:status  200
       :headers {"Content-Type" "application/javascript"}
       :body    (io/file cljs)}

      (= uri "/")
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (->> resource
                     html
                     hiccup/html
                     (str "<!doctype html>"))}

      :else
      {:status 404
       :body   "Not Found"})))

(defn ^:private colorize [s color]
  (str color s "\u001b[0m"))

(defn red [s]
  (colorize s "\u001b[31m"))

(defn green [s]
  (colorize s "\u001b[32m"))

(defn yellow [s]
  (colorize s "\u001b[33m"))

(defn ^:private print-logs! [driver]
  (when-let [logs (seq (eta/get-logs driver))]
    (println "\nBrowser Console:")
    (doseq [{:keys [level message]} logs
            :let [[_ msg] (re-find #"\"(.+)\"" message)]
            :let [msg (cond-> (str msg)
                        (= :severe level) red
                        (= :warning level) yellow)]]
      (println msg))))

(defn ^:private print-results! [server driver]
  (let [results (eta/js-execute driver "return window.testResults")]
    (eta/quit driver)
    (.close server)
    (if (and results
             (zero? (:failures results 0))
             (zero? (:errors results 0)))
      (do
        (println (green "✓ All tests passed!"))
        (System/exit 0))
      (do
        (println (red "✗ Tests failed or no results!"))
        (System/exit 1)))))

(defn ^:private run-on-rnd-port [resource cljs]
  (let [port (ueta/get-free-port)]
    [port (http/start-server #(handler % resource cljs) {:port port})]))

(defn run-tests! [server-resource cljs-loc]
  (let [[port server] (try
                        (run-on-rnd-port server-resource cljs-loc)
                        (catch Throwable _
                          (run-on-rnd-port server-resource cljs-loc)))
        driver (try
                 (eta/chrome {:headless true})
                 (catch Throwable _
                   (eta/chrome {:headless true})))
        url (str "http://localhost:" port)]
    (try
      (println (str "Starting test server on port " port))
      (println (str "Opening test runner: " url))
      (eta/go driver url)
      (eta/wait-visible driver {:css ".complete"})

      (print-logs! driver)
      (print-results! server driver)
      (catch Throwable ex
        (println (str "\nError retrieving test results: " (.getMessage ex)))
        (.printStackTrace ex)
        (eta/quit driver)
        (.close server)
        (System/exit 1)))))
