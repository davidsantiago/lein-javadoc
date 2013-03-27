(ns lein-javadoc.plugin
  (:require [lein-jdk-tools.plugin :as jdk-tools]))

;; This just applies the lein-jdk-tools middleware automatically.
(defn middleware
  [project]
  (jdk-tools/middleware project))
