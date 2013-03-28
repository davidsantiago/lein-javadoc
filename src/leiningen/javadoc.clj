(ns leiningen.javadoc
  (:require [leiningen.core.eval :as lein-core]
            [clojure.string :as str]))

(defn get-javadoc-opts
  "Create the map of javadoc options, using defaults where possible."
  [project]
  (let [javadoc-opts (:javadoc-opts project)]
    {:output-dir (get javadoc-opts :output-dir "javadoc/")
     :java-source-paths (get project :java-source-paths)
     :package-names (get javadoc-opts :package-names)
     :additional-args (get javadoc-opts :additional-args)
     :exact-command-line (get javadoc-opts :exact-command-line)}))

(defn check-options
  "Check the javadoc options and print a few diagnostics/warnings. Returns true
   if the javadoc command can be run."
  [javadoc-opts]
  (let [exact-cmd-line? (:exact-command-line javadoc-opts)
        missing-package-names? (empty? (:package-names javadoc-opts))]
    (if exact-cmd-line?
      (println "lein javadoc warning: `:exact-command-line` is set, using user-chosen command line. All other options in the project configuration are being ignored."))
    (if missing-package-names?
      (println "lein javadoc error: Required configuration key `:package-names` is empty or missing."))
    (not (or missing-package-names?))))

(defn javadoc
  "Run javadoc"
  [project & args]
  (let [javadoc-opts (get-javadoc-opts project)
        arg-list (-> (or (:exact-command-line javadoc-opts)
                         (concat ["-d"
                                  (:output-dir javadoc-opts)
                                  "-sourcepath"
                                  (str/join ":"
                                            (:java-source-paths javadoc-opts))
                                  "-subpackages"
                                  (str/join ":"
                                            (:package-names javadoc-opts))]
                                 (:additional-args javadoc-opts)))
                     vec)]
    (if (check-options javadoc-opts)
      (lein-core/eval-in-project
       project
       `(do
          (Main/execute (into-array String ~arg-list)))
       `(import '[com.sun.tools.javadoc ~'Main])))))
