# SIMPA

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