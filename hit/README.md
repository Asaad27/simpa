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
    locate the directory ***simpa-clean***.
    - Under ***simpa-clean*** select the project or projects which you would
    like to import.
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
compile and launch SIMPA. The script is not optimized and the compilation will
take place each time you run the script.

You can add arguments to `launch_SIMPA.sh` and they will be passed to SIMPA.

#### Others

SIMPA has quite a lot of dependencies which have to be added in your `CLASSPATH`. You can add them by adapting and running the following code :
  
    export SIMPA="$HOME/SIMPA" # set this path to your local clone of SIMPA
    export CLASSPATH="$SIMPA/hit/bin:$SIMPA/hit/lib/json/jackson-mapper-asl-1.9.11.jar:$SIMPA/hit/lib/json/jackson-core-asl-1.9.11.jar\:$SIMPA/hit/lib/sipunit/namespace-1.0.1.jar:$SIMPA/hit/lib/sipunit/dom-2.3.0-jaxb-1.0.6.jar\:$SIMPA/hit/lib/sipunit/xsdlib-20060615.jar:$SIMPA/hit/lib/sipunit/jaxb-api-1.0.1.jar\:$SIMPA/hit/lib/sipunit/jaxb-libs-1.0.3.jar:$SIMPA/hit/lib/sipunit/isorelax-20030108.jar\:$SIMPA/hit/lib/sipunit/concurrent-1.3.3.jar:$SIMPA/hit/lib/sipunit/nist-sdp-1.0.jar\:$SIMPA/hit/lib/sipunit/log4j-1.2.8.jar:$SIMPA/hit/lib/sipunit/jax-qname-1.1.jar\:$SIMPA/hit/lib/sipunit/xalan-2.6.0.jar:$SIMPA/hit/lib/sipunit/relaxngDatatype-20020414.jar\:$SIMPA/hit/lib/sipunit/jaxb-impl-1.0.3.jar:$SIMPA/hit/lib/sipunit/sipunit-2.0.0.jar\:$SIMPA/hit/lib/sipunit/jain-sip-ri-1.2.164.jar:$SIMPA/hit/lib/sipunit/relaxngDatatype-2011.1.jar\:$SIMPA/hit/lib/sipunit/jaxb-xjc-1.0.7.jar:$SIMPA/hit/lib/sipunit/junit-4.8.2.jar\:$SIMPA/hit/lib/sipunit/stun4j-1.0.MOBICENTS.jar:$SIMPA/hit/lib/sipunit/jain-sip-api-1.2.1.jar\:$SIMPA/hit/lib/sipunit/xml-apis-1.0.b2.jar:$SIMPA/hit/lib/sipunit/xercesImpl-2.6.2.jar\:$SIMPA/hit/lib/weka.jar:$SIMPA/hit/lib/jsoup-1.8.2.jar:$SIMPA/hit/lib/htmlunit/httpmime-4.2.2.jar\:$SIMPA/hit/lib/htmlunit/serializer-2.7.1.jar:$SIMPA/hit/lib/htmlunit/commons-io-2.4.jar\:$SIMPA/hit/lib/htmlunit/htmlunit-core-js-2.11.jar:$SIMPA/hit/lib/htmlunit/htmlunit-2.11.jar\:$SIMPA/hit/lib/htmlunit/httpclient-4.2.2.jar:$SIMPA/hit/lib/htmlunit/xalan-2.7.1.jar\:$SIMPA/hit/lib/htmlunit/sac-1.3.jar:$SIMPA/hit/lib/htmlunit/commons-lang3-3.1.jar\:$SIMPA/hit/lib/htmlunit/cssparser-0.9.8.jar:$SIMPA/hit/lib/htmlunit/xercesImpl-2.10.0.jar\:$SIMPA/hit/lib/htmlunit/xml-apis-1.4.01.jar:$SIMPA/hit/lib/htmlunit/nekohtml-1.9.17.jar\:$SIMPA/hit/lib/htmlunit/httpcore-4.2.2.jar:$SIMPA/hit/lib/htmlunit/commons-codec-1.7.jar\:$SIMPA/hit/lib/htmlunit/commons-logging-1.1.1.jar:$SIMPA/hit/lib/htmlunit/commons-collections-3.2.1.jar\:$SIMPA/hit/lib/antlr-4.5.3-complete.jar:$CLASSPATH"

Then you can compile the sources :

    cd $SIMPA/hit/src
    javac -g -d ../bin  */*.java */*/*.java */*/*/*.java */*/*/*/*.java */*/*/*/*/*.java
    
And now you can launch SIMPA :

	java main.simpa.SIMPA

#  Usage

The basic arguments are :
    - `--help` display the list of available command line arguments.
    - `--gui` open graphical interface (other arguments can be provided to
    preset values in graphical interface).

There are some examples for **Program Arguments** :

	--mealy --driver drivers.mealy.SFM11StefenDriver --lm --mrBean --oracleSeed=auto --maxcelength=20 --maxceresets=30 --html --text

	--mealy --driver drivers.mealy.FromDotMealyDriver --lm --html --text --loadDotFile /Documents/workspace/DotParser/test2.dot
