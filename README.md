This version of SIMPA software was written to generate experiments for an
article for JSS.
Others branch of this repository are probably more intersting for you if you
want to use SIMPA.


# Installation

SIMPA was mainly developed in Eclipse and it is easiest to use SIMPA directly in
this IDE. However, we also present below how to run SIMPA directly from
command-line.

## Eclipse

###  PREREQUISITES

	Java Runtime Environment >= 1.8         www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	Eclipse                  >= Mars        eclipse.org/ide/
	Gnuplot                  >= 5
###  INSTALLATION

- You can use the ***Import Wizard*** to  command link import an existing project into workspace.
    - From the main menu bar, select  command link ***File*** > ***Import***....
    The Import wizard opens.
    - Select ***General*** > ***Existing Project into Workspace*** and click
    Next.
    - Choose ***Select root directory***  and click the associated **Browse** to
    locate the directory ***SIMPA***.
    - Click Finish to start the import.
- The class to run is `main.simpa.JSS_figures`.

—> If you are getting a problem like (`The method getTextContent() is undefined for the type Node`) in Eclipse, my tested solution would be : ***Java Build Path*** > ***Order and Export***, select **JRE System Library** and move it to Top.

## Command-line

###  PREREQUISITES

	Java Runtime Environment >= 1.8        www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	Gnuplot                  >= 5

#### Unix

In this repository, you can find a script named `launch_SIMPA.sh` which will
compile and launch SIMPA.

#### Others

SIMPA has quite a lot of dependencies which have to be added in your `CLASSPATH`. You can add them by adapting and running the following code :
  
    export SIMPA="$HOME/SIMPA" # set this path to your local clone of SIMPA
    export CLASSPATH="$SIMPA/bin:$SIMPA/lib/paho.client.mqttv3-1.1.2-sources.jar:$SIMPA/lib/htmlunit/htmlunit-2.11.jar:$SIMPA/lib/htmlunit/xml-apis-1.4.01.jar:$SIMPA/lib/htmlunit/commons-logging-1.1.1.jar:$SIMPA/lib/htmlunit/commons-lang3-3.1.jar:$SIMPA/lib/htmlunit/httpclient-4.2.2.jar:$SIMPA/lib/antlr-4.5.3-complete.jar:$SIMPA/lib/sipunit/jain-sip-ri-1.2.164.jar:$SIMPA/lib/sipunit/jain-sip-api-1.2.1.jar:$SIMPA/lib/sipunit/sipunit-2.0.0.jar:$SIMPA/lib/jsoup-1.8.2.jar:$SIMPA/lib/json/jackson-core-asl-1.9.11.jar:$SIMPA/lib/json/jackson-mapper-asl-1.9.11.jar:$SIMPA/lib/paho.client.mqttv3-1.1.2.jar:$CLASSPATH"

Then you can compile the sources :

    cd $SIMPA/src
    java -jar "$SIMPA/lib/antlr-4.5.3-complete.jar" -no-visitor -package tools.antlr4.DotMealy "$SIMPA/src/tools/antlr4/DotMealy/DotMealy.g4"
    javac -g -d ../bin  */*.java */*/*.java */*/*/*.java */*/*/*/*.java */*/*/*/*/*.java
    
And now you can launch SIMPA :

	java main.simpa.JSS_figures

#  Usage

The experiments are based on random walk, but the seeds used to initialize the
random sources are pre-defined, so you should get the same results as us.

However, because of a mistake I made, some seeds were not initialized carefully
and thus, the experiments are hard to reproduce exactly.

In order to get exactly the same results, you should launch SIMPA several times :

    ./launch_SIMPA.sh r 1
    ./launch_SIMPA.sh r 2
    ./launch_SIMPA.sh r 3

this will produce the graphs of the article.

then run 

    ./launch_SIMPA b 1
    ./launch_SIMPA b 2
    ./launch_SIMPA b 3

this will produce the tables of the article

This is still under test to check whether we get exactly the same results or slightly different results due to random behavior.

