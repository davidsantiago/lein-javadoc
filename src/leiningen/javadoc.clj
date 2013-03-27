(ns leiningen.javadoc
  (:require [leiningen.core.eval :as lein-core]
            [leiningen.core.project :as proj]
            [lein-jdk-tools.plugin :as jdk-tools])
  #_(:import [com.sun.tools.javadoc Main]))

(defn javadoc
  "I don't do a lot."
  [project & args]
  (let [jdk-tools-profile {:resource-paths (vec (jdk-tools/jpda-jars))}]
    (lein-core/eval-in-project (proj/merge-profiles project
                                                    [jdk-tools-profile])
                               (prn (System/getProperty "java.class.path"))
                               (import '[com.sun.tools.javadoc Main])))
  #_(Main/execute []))
