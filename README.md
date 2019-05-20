# Installation

SIMPA was mainly developed in Eclipse and it is easiest to use SIMPA directly in
this IDE. However, we also present below how to run SIMPA directly from
command-line.

## Eclipse

###  PREREQUISITES

	Java Runtime Environment >= 1.8         www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	Eclipse                  >= Mars        eclipse.org/ide/
	GraphViz                 >= 2           www.graphviz.org/Download.php
###  INSTALLATION

- Install Eclipse Plugins, following plugins may be interesting:
    - **Eclox** is a simple doxygen frontend plug-in for eclipse, it is used to
    generate the documentation from sources. You can install with the Help ==>
    Eclipse Marketplace
- You can use the ***Import Wizard*** to  command link import an existing project into workspace.
    - From the main menu bar, select  command link ***File*** > ***Import***....
    The Import wizard opens.
    - Select ***General*** > ***Existing Project into Workspace*** and click
    Next.
    - Choose ***Select root directory***  and click the associated **Browse** to
    locate the directory ***SIMPA***.
    - Click Finish to start the import.
- The class to run is `main.simpa.SIMPA`.

—> If you are getting a problem like (`The method getTextContent() is undefined for the type Node`) in Eclipse, my tested solution would be : ***Java Build Path*** > ***Order and Export***, select **JRE System Library** and move it to Top.

###  Detailed Instructions for Command Line Arguments in Eclipse

- Right-click on your project **SIMPA**.
- Go to **Debug As > Java Application** or **Run As > Java Application**.
- Find the class *SIMPA (main.simpa)*.
- Go to **Debug As > Debug Configurations** or **Run As > Run Configurations**, and then click that says **Arguments**.
- Enter in your **Program Arguments**
- Click **Apply** or **Debug**

## Command-line

###  PREREQUISITES

	Java Runtime Environment >= 1.8        www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	GraphViz                 >= 2          www.graphviz.org/Download.php

#### Unix

In this repository, you can find a script named `launch_SIMPA.sh` which will
compile and launch SIMPA.

You can add arguments to `launch_SIMPA.sh` and they will be passed to SIMPA.

#### Others

SIMPA has quite a lot of dependencies which have to be added in your `CLASSPATH`. You can add them by adapting and running the following code :
  
    export SIMPA="$HOME/SIMPA" # set this path to your local clone of SIMPA
    export CLASSPATH="$SIMPA/bin:$SIMPA/lib/paho.client.mqttv3-1.1.2-sources.jar:$SIMPA/lib/htmlunit/htmlunit-2.11.jar:$SIMPA/lib/htmlunit/xml-apis-1.4.01.jar:$SIMPA/lib/htmlunit/commons-logging-1.1.1.jar:$SIMPA/lib/htmlunit/commons-lang3-3.1.jar:$SIMPA/lib/htmlunit/httpclient-4.2.2.jar:$SIMPA/lib/antlr-4.5.3-complete.jar:$SIMPA/lib/sipunit/jain-sip-ri-1.2.164.jar:$SIMPA/lib/sipunit/jain-sip-api-1.2.1.jar:$SIMPA/lib/sipunit/sipunit-2.0.0.jar:$SIMPA/lib/jsoup-1.8.2.jar:$SIMPA/lib/json/jackson-core-asl-1.9.11.jar:$SIMPA/lib/json/jackson-mapper-asl-1.9.11.jar:$SIMPA/lib/paho.client.mqttv3-1.1.2.jar:$CLASSPATH"

Then you can compile the sources :

    cd $SIMPA/src
    java -jar "$SIMPA/lib/antlr-4.5.3-complete.jar" -no-visitor -package tools.antlr4.DotMealy "$SIMPA/src/tools/antlr4/DotMealy/DotMealy.g4"
    javac -g -d ../bin  */*.java */*/*.java */*/*/*.java */*/*/*/*.java */*/*/*/*/*.java
    
And now you can launch SIMPA :

	java main.simpa.SIMPA

#  Usage

The basic arguments are :
- `--help` display the list of available command line arguments.
- `--gui` open graphical interface (other arguments can be provided
  to preset values in graphical interface).

Some example are shown when you use the `--help` argument.

# Inferring a real system (writing a new Driver)

To write a new driver, you must extends the class `drivers.mealy.MealyDriver` or
`drivers.efsm.EMFSDriver`. You must define the following methods :

	@Override
	public Output execute_implem(Input input) {
	}
which must execute the given input on system under inference and return the
output observed (`Input` and `Output` types depends on type of system : Mealy or
EFMS),

	@Override
	public void reset_implem() {
	}
which reset the driver to its initial state (if you plan to use only no-reset
algorithms, you can just put a `throw new RuntimeException();` in the body to be
sure that the method is never called),

	@Override
	public List<String> getInputSymbols() {
	}
which return a list of input symbols,

You must also add a constructor which takes no argument.


As an example, `drivers.mealy.real.HeatingSystem` is a driver which simply send
inputs to standard input of an executable an reads outputs from standard output
of the same software.
