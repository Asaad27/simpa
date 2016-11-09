# SIMPA (Eclipse)

### o PREREQUISITES

	Java Runtime Environment	>= 1.7 		 www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	Eclipse 				   >= Mars      eclipse.org/ide/
	GraphViz				   >= 2	  	    www.graphviz.org/Download.php
### o INSTALLATION

- Install Eclipse Plugins, following plugins may be necessary:
  - **Eclox** is a simple doxygen frontend plug-in for eclipse, it is used to generate the documentation from sources. You can install with the Help ==> Eclipse Marketplace
  - **XText** is needed by ANTLR 4. ***Only version 2.7.3 of Xtext works with ANTLR 4***. The Xtext versions 2.8.0 to 2.10.0 fail. Documentation for installation is at http://www.eclipse.org/Xtext/download.html. Don't forget to uncheck "show only the latest version".
  - **ANTLR** (ANother Tool for Language Recognition) is used for the compilation of .g4 files containing the grammar to generate a parser. SIMPA use ANTLR to analyse .dot file and import into SIMPA. Install ANTLR 4 IDE, ***Go to Help*** > ***Eclipse Marketplace*** and search for antlr. Choose ***ANTLR 4 IDE*** (make sure it's ANTLR 4 IDE not ANTLR IDE) and click Install. Let the installer finish clicking ok if it prompts and restart Eclipse.
- You can use the ***Import Wizard*** to  command link import an existing project into workspace.
  - From the main menu bar, select  command link ***File*** > ***Import***.... The Import wizard opens.
  - Select ***General*** > ***Existing Project into Workspace*** and click Next.
  - Choose ***Select root directory***  and click the associated **Browse** to locate the directory ***simpa-clean***.
  - Under ***simpa-clean*** select the project or projects which you would like to import.
  - Click Finish to start the import.


If you are getting a problem like (`The method getTextContent() is undefined for the type Node`) in Eclipse, my tested solution would be : ***Java Build Path*** > ***Order and Export***, select **JRE System Library** and move it to Top.

### o USAGE

	- SIMPA - 07/11/2016
		Checking environment and options, please specify the driverClass
		Usage : SIMPA driverClass [Options]
	
	Options
	  > General
	          --help | -h   : Show help
	          --seed        : Use NN as seed for random generator
	          --loadDotFile : load the specified dot file
	                           use with drivers.mealy.FromDotMealyDriver
	          --interactive : algorithms may ask user to choose a sequence, a counter example or something else
	  > Inference Algorithm
	          --tree             : Use tree inference (if available) instead of table
	          --lm               : Use lm inference
	          --noReset          : use noReset Algorithm
	          --cutCombinatorial : use the combinatorial inference with cutting
	          --combinatorial    : use the combinatorial inference
	          --rivestSchapire   : use the RivestSchapire inference (must be used with an other learner)
	                               This option let you to run an inference algorithm with resets on a driver without reset.
	  > Algorithm ZQ
	          --stopatce         : Stop inference when a counter exemple is asked
	          --maxcelength (20) : Maximal length of random walk for counter example search
	          --maxceresets (10) : Maximal number of random walks for counter example search
	          -I              () : Initial input symbols (a,b,c)
	          -Z              () : Initial distinguishing sequences (a-b,a-c,a-c-b))
	          -I=X               : Initial input symbols set to X
	  > Algorithm noReset (ICTSS2015)
	          --stateBound      (0) : a bound of states number in the infered automaton
	                                          n  → use n as bound of state number
	                                          0  → use exact states number (need to know the automaton)
	                                          -n → use a random bound between the number of states and the number of states plus n (need to know the automaton)
	          --characterizationSeq : use the given charcacterization sequences
	                                          usage : '--characterizationSeq a,b,c;b,c,d' or '--characterizationSeq a,b,c --characterizationSeq b,c,d'
	          --noSpeedUp           : Don't use speedUp (deduction from trace based on state incompatibilities)
	                                  this is usefull if you don't know the real state number but only the bound.
	  > Algorithm EFSM
	          --generic         : Use generic driver
	          --scan            : Use scan driver
	          --reuseop         : Reuse output parameter for non closed row
	          --forcej48        : Force the use of J48 algorithm instead of M5P for numeric classes
	          --weka            : Force the use of weka
	          --supportmin (20) : Minimal support for relation (1-100)
	  > Random Mealy Generator
	          --minstates    (5) : Minimal number of states for random automatas
	          --maxstates    (5) : Maximal number of states for random automatas
	          --mininputsym  (5) : Minimal number of input symbols for random automatas
	          --maxinputsym  (5) : Maximal number of input symbols for random automatas
	          --minoutputsym (5) : Minimal number of output symbols for random automatas
	                               That is the minimal number used for output symbol genration but it is possible that less symbols are used
	          --maxoutputsym (5) : Maximal number of output symbols for random automatas
	          --transitions (90) : percentage of loop in random automatas
	                               Some other loop may be generated randomly so it's a minimal percentage
	  > Output
	          --html     : Use HTML logger
	          --text     : Use the text logger
	          --openhtml : Open HTML log automatically
	          --outdir (/Users/wang/simpa-clean/hit/bin) : Where to save arff and graph files
	  > Stats
	          --nbtest (1) : number of execution of the algorithm
	          --makeGraph  : create graph based on csv files
	          --stats      : enable stats mode
	                          - save results to csv
	                          - disable some feature
	  > Test EFSM (should be group with random Generator ?)
	          --minparameter (1) : Minimal number of parameter by symbol
	          --maxparameter (1) : Maximal number of parameter by symbol
	          --domainsize  (10) : Size of the parameter's domain
	          --simpleguard (25) : % of simple guard by transitions
	          --ndvguard    (25) : % of generating NDV by transitions
	          --ndvmintrans  (1) : Minimum number of states before checking NDV value
	          --ndvmaxtrans  (1) : Maximum number of states before checking NDV value


### o Detailed Instructions for Command Line Arguments in Eclipse

- Right-click on your project **simpa-clean**.
- Go to **Debug As > Java Application** or **Run As > Java Application**.
- Find the class *SIMPA (main.simpa)*.
- Go to **Debug As > Debug Configurations** or **Run As > Run Configurations**, and then click that says **Arguments**.
- Enter in your **Program Arguments**
- Click **Apply** or **Debug**

There are some examples for **Program Arguments** :

	drivers.mealy.SFM11StefenDriver --lm --html --text

	drivers.mealy.FromDotMealyDriver --lm --html --text --loadDotFile /Documents/workspace/DotParser/test2.dot --outdir /SIMPA_out

	drivers.mealy.transparent.RandomAndCounterMealyDriver --noReset --minstates 2 --maxstates 2 --mininputsym 2 --minoutputsym 2 --maxinputsym 2 --maxoutputsym 3 --seed 5 --nbtest 5 --stats --makeGraph --noSpeedUp



# SIMPA (*command*-line)

### o PREREQUISITES

	Java Runtime Environment	>= 1.7 		 www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	GraphViz				   >= 2	  	    www.graphviz.org/Download.php
### o INSTALLATION

- **Unix**
  After your download, On Unix shells, the following definitions are needed (SIMPA variable containing the location where the SIMPA checkout was done) : 
  	export SIMPA="$HOME/clone-simpa/simpa-clean"
  	export CLASSPATH=".:$SIMPA/hit/lib/json/jackson-mapper-asl-1.9.11.jar:$SIMPA/hit/lib/json/jackson-core-asl-1.9.11.jar\:$SIMPA/hit/lib/sipunit/namespace-1.0.1.jar:$SIMPA/hit/lib/sipunit/dom-2.3.0-jaxb-1.0.6.jar\:$SIMPA/hit/lib/sipunit/xsdlib-20060615.jar:$SIMPA/hit/lib/sipunit/jaxb-api-1.0.1.jar\:$SIMPA/hit/lib/sipunit/jaxb-libs-1.0.3.jar:$SIMPA/hit/lib/sipunit/isorelax-20030108.jar\:$SIMPA/hit/lib/sipunit/concurrent-1.3.3.jar:$SIMPA/hit/lib/sipunit/nist-sdp-1.0.jar\:$SIMPA/hit/lib/sipunit/log4j-1.2.8.jar:$SIMPA/hit/lib/sipunit/jax-qname-1.1.jar\:$SIMPA/hit/lib/sipunit/xalan-2.6.0.jar:$SIMPA/hit/lib/sipunit/relaxngDatatype-20020414.jar\:$SIMPA/hit/lib/sipunit/jaxb-impl-1.0.3.jar:$SIMPA/hit/lib/sipunit/sipunit-2.0.0.jar\:$SIMPA/hit/lib/sipunit/jain-sip-ri-1.2.164.jar:$SIMPA/hit/lib/sipunit/relaxngDatatype-2011.1.jar\:$SIMPA/hit/lib/sipunit/jaxb-xjc-1.0.7.jar:$SIMPA/hit/lib/sipunit/junit-4.8.2.jar\:$SIMPA/hit/lib/sipunit/stun4j-1.0.MOBICENTS.jar:$SIMPA/hit/lib/sipunit/jain-sip-api-1.2.1.jar\:$SIMPA/hit/lib/sipunit/xml-apis-1.0.b2.jar:$SIMPA/hit/lib/sipunit/xercesImpl-2.6.2.jar\:$SIMPA/hit/lib/weka.jar:$SIMPA/hit/lib/jsoup-1.8.2.jar:$SIMPA/hit/lib/htmlunit/httpmime-4.2.2.jar\:$SIMPA/hit/lib/htmlunit/serializer-2.7.1.jar:$SIMPA/hit/lib/htmlunit/commons-io-2.4.jar\:$SIMPA/hit/lib/htmlunit/htmlunit-core-js-2.11.jar:$SIMPA/hit/lib/htmlunit/htmlunit-2.11.jar\:$SIMPA/hit/lib/htmlunit/httpclient-4.2.2.jar:$SIMPA/hit/lib/htmlunit/xalan-2.7.1.jar\:$SIMPA/hit/lib/htmlunit/sac-1.3.jar:$SIMPA/hit/lib/htmlunit/commons-lang3-3.1.jar\:$SIMPA/hit/lib/htmlunit/cssparser-0.9.8.jar:$SIMPA/hit/lib/htmlunit/xercesImpl-2.10.0.jar\:$SIMPA/hit/lib/htmlunit/xml-apis-1.4.01.jar:$SIMPA/hit/lib/htmlunit/nekohtml-1.9.17.jar\:$SIMPA/hit/lib/htmlunit/httpcore-4.2.2.jar:$SIMPA/hit/lib/htmlunit/commons-codec-1.7.jar\:$SIMPA/hit/lib/htmlunit/commons-logging-1.1.1.jar:$SIMPA/hit/lib/htmlunit/commons-collections-3.2.1.jar\:$SIMPA/hit/lib/antlr-4.5.3-complete.jar:$CLASSPATH"

- **Cygwin**

  On Cygwin, as java is a Windows application, it does not recognize Cygwin paths. We must translate them when calling it.

  alias javac="javac -g -classpath \`cygpath -w -p \"\$CLASSPATH\"\` -Xlint:unchecked"

- **Windows**
  When installing Graphviz on Windows, put the path to dot.exe in the *PATH* system variable.

- **Compilation**

  	cd $SIMPA/hit/src
  	javac -g -d ../bin  */*.java */*/*.java */*/*/*.java */*/*/*/*.java */*/*/*/*/*.java

- **Run examples**

  The Class files have been generated in the bin/ directory.

  - Launching the executable file in its directory

    	cd $SIMPA/hit/bin
    	rm -f out/*
    	java main.simpa.SIMPA drivers.mealy.SFM11StefenDriver --lm --html --text

  - Launching the executable file from another directory
    	export CLASSPATH="$SIMPA/hit/bin:$CLASSPATH"
    	rm -f out/*
    	java main.simpa.SIMPA drivers.mealy.SFM11StefenDriver --lm --html --text

  - Output

    	SIMPA - 06/23/2016
    	Checking environment and options
    	you can try to do this learning again by running something like 'java main.simpa.SIMPA drivers.mealy.SFM11StefenDriver --lm --html --text --seed 1977387548829879471 '
    	System : SFM11Stefen
    	Inferring the system
    	Building the conjecture
    	Exporting conjecture
    	End
    	[+] End

  - Result

    SIMPA creates two files in the out/ directory (.dot and .svg)
    SFM11Stefen_inf.dot

    	digraph G {
    		S0 -> S1 [label="button/err"];
    		S0 -> S2 [label="pod/ok"];
    		S1 -> S1 [label="clean/err"];
    		S1 -> S1 [label="water/err"];
    	    S1 -> S1 [label="button/err"];
    	    S2 -> S3 [label="water/ok"];
    	    S2 -> S1 [label="button/err"];
    	    S2 -> S2 [label="pod/ok"];
    	    S4 -> S1 [label="water/err"];
    	    S1 -> S1 [label="pod/err"];
    	    S4 -> S0 [label="clean/ok"];
    	    S4 -> S1 [label="pod/err"];
    	    S3 -> S3 [label="pod/ok"];
    	    S3 -> S3 [label="water/ok"];
    	    S0 -> S0 [label="water/ok"];
    	    S3 -> S4 [label="button/coffee"];
    	    S4 -> S1 [label="button/err"];
    	    S2 -> S0 [label="clean/ok"];
    	    S3 -> S0 [label="clean/ok"];
    	    S0 -> S0 [label="clean/ok"];
    	    S0 [shape=doubleoctagon]
    	}
    The graphic SVG file generated is SFM11Stefen_inf.svg:

    ![SFM11Stefen_inf.svg](https://forge.imag.fr/plugins/mediawiki/wiki/simpa/images/a/a9/Example.jpg)