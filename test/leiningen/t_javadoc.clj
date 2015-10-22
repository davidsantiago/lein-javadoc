(ns leiningen.t-javadoc
  (:require [leiningen.javadoc :as jd]
            [clojure.test :refer :all]
            [clojure.string :as str]))

(deftest java-cmd-fallbacks
  ;; Command from option; JDK home from option (and whether tools.jar
  ;; is found there); JAVA_CMD from environment; expected output.
  (are [opt-cmd opt-home home-exists? env    output]
       (= (with-redefs [jd/getenv #(if (= %& ["JAVA_CMD"])
                                     env
                                     "UNEXPECTED")
                        jd/canonical-path #(when home-exists?
                                             (str/join "/" %))]
            (jd/java-cmd-path {:java-cmd opt-cmd
                               :jdk-home opt-home}))
          output)
       ;; Prefer :java-cmd
       "comj"   "HOME"    true        "envj" "comj"
       ;; Fall back to :jdk-home
       nil     "HOME"    true         "envj" "HOME/../bin/java"
       ;; Fall back to JAVA_CMD
       nil     "HOME"    false        "envj" "envj"
       nil     nil       nil          "envj" "envj"
       ;; Finally fall back to constant "java"
       nil     nil       nil          nil    "java"))

(deftest tools-jar-fallbacks
  ;; Paths from option; JDK home from option (and whether tools.jar is
  ;; found there); same for JDK home detected from current JVM;
  ;; expected output.
  (are [opt-paths opt-home opt-home? prop-home prop-home? output]
       (= (with-redefs
            [jd/getprop #(if (= %& ["java.home"])
                           prop-home
                           "UNEXPECTED")
             jd/canonical-path (fn [parts]
                                 (when (nil? (first parts))
                                   (throw "Unexpected nil for jdk home"))
                                 (when (or (and (= (first parts) opt-home)
                                                opt-home?)
                                           (and (= (first parts) prop-home)
                                                prop-home?))
                                   (str/join "/" parts)))]
            (jd/tools-classpath {:tools-jar-paths opt-paths
                                 :jdk-home opt-home}))
          output)
       ;; Prefer :tools-jar-paths
       ["opath"]  "OHOME"  true      "PHOME"   true ["opath"]
       ;; Fall back to :jdk-home
       []         "OHOME"  true      "PHOME"   true ["OHOME/../lib/tools.jar"]
       nil        "OHOME"  true      "PHOME"   true ["OHOME/../lib/tools.jar"]
       ;; Fall back to current JVM
       nil        "OHOME"  false     "PHOME"   true ["PHOME/../lib/tools.jar"]
       nil        nil      nil       "PHOME"   true ["PHOME/../lib/tools.jar"])
  (with-redefs [jd/tools-jar (constantly nil)]
    (with-redefs [leiningen.core.main/abort #(throw (Exception. %))]
      (is (thrown-with-msg? Exception #"must be a collection of strings"
                            (jd/tools-classpath {:tools-jar-paths "foo"})))
      (is (thrown-with-msg? Exception #"No tools[.]jar found"
                            (jd/tools-classpath {}))))))

(deftest get-javadoc-opts
  (testing "empty -- defaulting"
    (is (= (jd/get-javadoc-opts {:javadoc-opts {}})
           (jd/get-javadoc-opts {})
           {:output-dir "javadoc/"
            :java-source-paths nil
            :package-names nil
            :additional-args nil
            :exact-command-line nil
            :jdk-home nil
            :java-cmd nil
            :tools-jar-paths nil})))
  (testing "full + unrecognized"
    (is (= (jd/get-javadoc-opts {:java-source-paths ["jsp"]
                                 :javadoc-opts
                                 {:output-dir "od"
                                  :package-names ["pn"]
                                  :additional-args ["aa"]
                                  :exact-command-line ["ecl"]
                                  :jdk-home "jh"
                                  :java-cmd "jc"
                                  :tools-jar-paths ["tjp"]
                                  ;; confirm that unrecognized things
                                  ;; are not included
                                  :bogus-extra "be"}})
           {:output-dir "od"
            :java-source-paths ["jsp"]
            :package-names ["pn"]
            :additional-args ["aa"]
            :exact-command-line ["ecl"]
            :jdk-home "jh"
            :java-cmd "jc"
            :tools-jar-paths ["tjp"]}))))
