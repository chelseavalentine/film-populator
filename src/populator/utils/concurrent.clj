(ns populator.utils.concurrent
  (:import
   [java.util.concurrent ExecutorService Executors]))

(defn do-concurrently
  [f]
  (let [^ExecutorService pool (Executors/newFixedThreadPool 1024)
        ^Callable clbl        (cast Callable f)]
    (.submit pool clbl)))
