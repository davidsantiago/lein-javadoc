(ns leiningen.javadoc
  (:require [leiningen.core.main :refer [abort]]
            [leiningen.core.classpath :as lein-cp]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))

;;;; Obviate lein-jdk-tools

(defn tools-jar
  "Yield the canonical path to the JDK tools.jar file, or nil if not found."
  []
  (let [tj (io/file (System/getProperty "java.home") ".." "lib" "tools.jar")]
    (when (.exists (io/file tj))
      (.getCanonicalPath tj))))

;;;; lein-javadoc

(defn get-javadoc-opts
  "Create the map of javadoc options, using defaults where possible."
  [project]
  (let [javadoc-opts (:javadoc-opts project)]
    {:output-dir (get javadoc-opts :output-dir "javadoc/")
     :java-source-paths (get project :java-source-paths)
     :package-names (get javadoc-opts :package-names)
     :additional-args (get javadoc-opts :additional-args)
     :exact-command-line (get javadoc-opts :exact-command-line)
     :tools-jar-paths (get javadoc-opts :tools-jar-paths)}))

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

(defn opts->args
  [javadoc-opts]
  (or (:exact-command-line javadoc-opts)
      (concat ["-d"
               (:output-dir javadoc-opts)
               "-sourcepath"
               (str/join ":"
                         (:java-source-paths javadoc-opts))
               "-subpackages"
               (str/join ":"
                         (:package-names javadoc-opts))]
              (:additional-args javadoc-opts))))

(defn tools-classpath
  "Construct a tools.jar-containing classpath coll or die trying."
  [javadoc-opts]
  (if-let [paths (not-empty (:tools-jar-paths javadoc-opts))]
    (if (string? paths)
      (abort ":javadoc-opts :tools-jar-paths must be a collection of strings, not a single string. (May also be empty or nil.)")
      paths)
    (or [(tools-jar)]
        (abort "No tools.jar found in system or specified in project, cannot run javadoc."))))

(defn make-classpath
  [project javadoc-opts]
  (concat
   (lein-cp/get-classpath project)
   (tools-classpath javadoc-opts)))

(defn run-javadoc
  [sh-args]
  (let [pb (.inheritIO (ProcessBuilder. (into-array String sh-args)))
        p (try
            (.start pb)
            (catch java.io.IOException e
              (abort (str "Failed to find " (first sh-args)
                          " command.\n"
                          (.getMessage e)))))]
    (.waitFor p)
    (let [exit (.exitValue p)]
      (when (pos? exit)
        (abort (str "javadoc exited with exit code " exit))))))

(defn javadoc
  "Run javadoc"
  [project & args]
  (let [javadoc-opts (get-javadoc-opts project)]
    (when (check-options javadoc-opts)
      (let [jd-args (opts->args javadoc-opts)
            cp (make-classpath project javadoc-opts)
            sh-args (list* "java"
                           "-cp" (str/join \: cp)
                           "com.sun.tools.javadoc.Main"
                           jd-args)]
        (run-javadoc sh-args)))))
