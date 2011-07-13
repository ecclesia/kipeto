# Kipeto
This is just a rough preliminary guide to get you started. Extensive documentation as well as a binary distribution will follow shortly.

## How to build

### Requirements
* Java 1.6+
* Maven 3.0+

### Build Instructions
1. checkout sources
2. run `mvn install`

The build is going to produce two artifacts of interest:
- kipeto/target/kipeto-1.0-SNAPSHOT-jar-with-dependencies.jar
- kipeto-tools/target/kipeto-tools-1.0-SNAPSHOT-jar-with-dependencies.jar

For the rest of this document we refer to them as `kipeto.jar` and `kipeto-tools.jar`.

## How to use
1. prepare an application directory suitable for distribution
2. create a blueprint `java -jar kipeto-tools.jar -b <blueprint-name> -d <kipeto-data-dir> -s <application-dir>`
3. publish the repository (`<kipeto-data-dir>/repos`) to a suitable web server such as apache.
3. install the created blueprint `java -jar kipeto.jar -g -b <blueprint-name> -d <kipeto-data-dir> -r <remote-repository-url> -t <target-dir>`

That's about it ;-)
