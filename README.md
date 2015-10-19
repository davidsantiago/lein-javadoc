# lein-javadoc

Use Leiningen to build the javadoc for the java source code in your projects.

## Usage

Put `[lein-javadoc "0.2.0"]` into the `:plugins` vector of your project.clj.

For this plugin to do anything, you need to add a map of configuration
options to the `:javadoc-opts` key of your project map (or a profile
if you prefer). The map can have the following keys:

- `:package-names` *(Required)*: This key should be a vector of
  strings containing the names of the Java packages that should be
  included in the Javadoc. Since this cannot be deduced from a project
  regularly, it is required.
- `:output-dir` *(Default: "javadoc/")*: This key should have a string
  containing the path to the directory the Javadoc output will be
  written to.
- `:java-source-paths` *(Default: The value of `:java-source-paths` in
  the Leiningen project)*: This key is a vector of strings containing
  the paths to the project directories containing the Java sources
  that will have Javadoc run on them. This value defaults to the same
  key from the project itself, which is presumably set to something
  meaningful if you want to run a Javadoc task. There is probably not
  much need to set this one, unless you have a very specific desire to
  Javadoc a set of source code different somehow from the source code
  you want to compile.
- `:additional-args`: This key should have a vector of strings, as if
  they had been parsed off of the command line, for passing to Javadoc
  in addition to the usual options automatically set by this
  task. This task only directly supports convenient usage of a small
  number of the flags and options that the Javadoc tool supports. If
  you wish to use any of the ones not directly supported, you can set
  them here. Also, if you feel a particularly useful flag should be
  supported by this task, go ahead and send a note or (even better) a
  pull request.
- `:exact-command-line`: This key is a vector of strings, as if they
  had been parsed off of the command line, for passing to Javadoc as
  the *only* options it will see. If this key is set, all other flags
  and options are ignored when the Javadoc tool is invoked. You will
  also be warned, to head off potential frustration. This option
  exists as a safety valve, in case this task does not currently
  support some combination of configuration options you really need.
- `:jdk-home`: This key is a string indicating the path to the JDK
  home, used for determining the location of the `java` command and
  tools.jar. This should include the `jre` directory, as in
  `"/usr/lib/jvm/jdk-8-oracle-x64/jre"`.
- `:java-cmd`: This key is a string indicating the path to the `java`
  command. If not supplied, defaults to these in order:
    - `../bin/java` relative to `:jdk-home`, if present;
    - value of `JAVA_CMD` in the environment;
    - the constant `java`.
- `:tools-jar-paths`: This key is a vector of strings pointing to
  possible locations of tools.jar. If empty or missing, defaults to
  these in order:
    - `../lib/tools.jar` relative to `:jdk-home`, if present;
    - `../lib/tools.jar` relative to current JVM's `java.home`
      property.

Also note that you must have the JDK installed for this task to work,
as Javadoc is a part of the JDK's lib/tools.jar. This plugin should
add that jar to the classpath automatically, but it must be present.

Once the plugin is configured for your project, you can invoke the
`javadoc` task to write the javadoc output to the configured
directory.

    $ lein javadoc

To use an alternative JDK, use `JAVA_CMD` with lein:

    $ JAVA_CMD=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java lein javadoc

By default this will have the effect of using both the java binary and
the tools.jar from the specified JDK. (In other uses, be sure to align
the source of the java binary and the source of tools.jar to ensure
compatibility.)

## Development

If you want to hack on this code, note that it does not currently work
when invoked from its own project directory. You should `lein install`
a SNAPSHOT version of it, and then test it from another project. Sorry
about that, but there appears to be some difficulty getting the
middleware required to work in its own project directory.

## License

Copyright © 2013 David Santiago

Other contributors:

- Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.
