# Changes

## (pending)

- Feature: Allow use of alternative JDKs.
    - Add `:java-cmd` option to specify Java command, with `JAVA_CMD`
      environment variable as fallback
    - Add `:jdk-home` for autodetection of tools.jar and java binary
      in alternative JDK

## 0.2.0: 2015-05-03

- Feature: Add `:tools-jar-paths` option to specify location of
  tools.jar; otherwise fall back on auto-locating it.
- Bugfix: Exclude tools.jar from output by shelling out instead of
  including as dependency.
- Dependencies: Removed lein-jdk-tools.

## 0.1.1: 2013-04-20

- Bugfix (attempted): Bump lein-jdk-tools dep version to exclude
  tools.jar from jar output. (Did not succeed, see 0.2.0.)

## 0.1.0: 2013-03-28

- Initial version, supporting `:package-names`, `:output-dir`,
  `:java-source-paths`, `:additional-args`, and `:exact-command-line`
