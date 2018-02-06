package learner.mealy.localizerBased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import automata.Transition;
import automata.mealy.InputSequence;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import tools.GNUPlot;
import tools.loggers.LogManager;

public class LocalizerBasedStats {
	enum Atribute {
		W_SIZE("Size of W",							"sequences",false,	true,	"W",	false),
		W1_LENGTH("Length of first W element",		"symbols",	false,	true,	"w1",	false),
		LOCALIZER_CALL_NB("Number of call to localizer","",		false,	false,	"lc",	false),
		LOCALIZER_SEQUENCE_LENGTH("Length of localizer sequence","symbols",false,false,"lsl",false),
		TRACE_LENGTH("length of trace",				"symbols",	true,	false,	"tl",	false),
		INPUT_SYMBOLS("number of input symbols",	"",			false,	true,	"f",	false),
		OUTPUT_SYMBOLS("number of output symbols",	"",			false,	true,	"o",	false),
		STATE_NUMBER("number of states",			"",			false,	true,	"s",	false),
		STATE_NUMBER_BOUND("bound of state number",	"states",	false,	true,	"n",	false),
		STATE_BOUND_OFFSET("difference between bound and real state number","states",false,true,"dns",true),
		LOOP_RATIO("percentage of loop transitions","%",		false,	true,	"lr",	false),
		;
		
		public final String units;
		public final String name;
		public final boolean logScale;
		public final boolean isParameter;
		public final String id;
		public final boolean isVirtual;
		private Atribute(String name, String units, boolean logScale,boolean isParameter,String id,boolean isVirtual) {
			this.units = units;
			this.name = name;
			this.logScale = logScale;
			this.isParameter = isParameter;
			this.id = id;
			this.isVirtual = isVirtual;
		}
		public String ToString(){
			return name;
		}
	}
	enum PlotStyle {
		POINTS("with points"),
		AVERAGE("with linespoints"),
		AVERAGE_WITH_EXTREMA("with yerrorbars"),
		MEDIAN("with linespoint"),
		;
		public String plotLine;
		private PlotStyle(String plotLine) {
			this.plotLine = plotLine;
		}
	}
	private int WSize;
	private int w1Length;
	private int localizeCallNb = 0;
	private int localizeSequenceLength;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int n;
	private int loopTransitionPercentage;
	
	
	public LocalizerBasedStats(List<InputSequence> W, int inputSymbols, int outputSymbols, int n){
		WSize = W.size();
		w1Length = W.get(0).getLength();
		this.inputSymbols = inputSymbols;
		this.outputSymbols= outputSymbols;
		this.n = n;
	}
	
	private LocalizerBasedStats(){
	}

	protected void setLocalizeSequenceLength(int length){
		localizeSequenceLength = length;
	}
	
	protected void increaseLocalizeCallNb(){
		localizeCallNb ++;
	}

	public int getTraceLength() {
		return traceLength;
	}

	protected void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	public int getWLength() {
		return WSize;
	}

	public int getLocalizeCallNb() {
		return localizeCallNb;
	}

	public int getLocalizeSequenceLength() {
		return localizeSequenceLength;
	}
	
	public int getStatesNumber() {
		return statesNumber;
	}

	protected void setStatesNumber(int statesNumber) {
		this.statesNumber = statesNumber;
	}

	public void updateWithConjecture(LmConjecture conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions=0;
		for (Transition t : conjecture.getTransitions()){
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		loopTransitionPercentage = ((100*loopTransitions)/conjecture.getTransitionCount());
	}
	
	/**
	 * export stat in CSV format
	 */
	public String toCSV(){
		StringBuilder r = new StringBuilder();
		r.append(WSize + ",");
		r.append(w1Length + ",");
		r.append(localizeCallNb + ",");
		r.append(localizeSequenceLength + ",");
		r.append(traceLength + ",");
		r.append(inputSymbols + ",");
		r.append(outputSymbols + ",");
		r.append(statesNumber + ",");
		r.append(statesNumber + ",");
		r.append(loopTransitionPercentage + ",");
		r.append(n);
		return r.toString();
	}

	/**
	 * get the Header for CSV data
	 */
	public static String CSVHeader(){
		return Atribute.W_SIZE.name + ","
				+ Atribute.W1_LENGTH.name + ","
				+ Atribute.LOCALIZER_CALL_NB.name + ","
				+ Atribute.LOCALIZER_SEQUENCE_LENGTH.name + ","
				+ Atribute.TRACE_LENGTH.name + ","
				+ Atribute.INPUT_SYMBOLS.name + ","
				+ Atribute.OUTPUT_SYMBOLS.name + ","
				+ Atribute.STATE_NUMBER.name + ","
				+ Atribute.STATE_NUMBER_BOUND.name + ","
				+ Atribute.LOOP_RATIO.name + ","
				;
	}
	
	/**
	 * rebuild a LocalizerBasedStats object from a CSV line
	 * @param line the line to parse
	 */
	public static LocalizerBasedStats entrieFromCSV(String line){
		LocalizerBasedStats stats = new LocalizerBasedStats();
		StringTokenizer st = new StringTokenizer(line, ",");
		stats.WSize = Integer.parseInt(st.nextToken());
		stats.w1Length = Integer.parseInt(st.nextToken());
		stats.localizeCallNb = Integer.parseInt(st.nextToken());
		stats.localizeSequenceLength = Integer.parseInt(st.nextToken());
		stats.traceLength = Integer.parseInt(st.nextToken());
		stats.inputSymbols = Integer.parseInt(st.nextToken());
		stats.outputSymbols = Integer.parseInt(st.nextToken());
		stats.statesNumber = Integer.parseInt(st.nextToken());
		stats.n = Integer.parseInt(st.nextToken());
		stats.loopTransitionPercentage = Integer.parseInt(st.nextToken());
		return stats;
	}

	/**
	 * build a list of localizerBasedStats from a CSV file
	 * @param filename the name of the file to parse
	 */
	public static List<LocalizerBasedStats> setFromCSV(String filename){
		List<LocalizerBasedStats> r = new ArrayList<LocalizerBasedStats>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String strLine;
			if (!(strLine = br.readLine()).equals(CSVHeader())){
				br.close();
				System.out.println(CSVHeader());
				System.out.println(strLine);
				throw new RuntimeException("the csv file do not have the good headings");
			}
			while ((strLine = br.readLine()) != null) {
				r.add(entrieFromCSV(strLine));
			}
			br.close();
			return r;
		} catch (Exception e) {
			return null;
		}
	}

	private static Map<Integer,List<LocalizerBasedStats>> sortByAtribute(List<LocalizerBasedStats> allStats, Atribute a){
		Map<Integer,List<LocalizerBasedStats>> sorted = new HashMap<Integer,List<LocalizerBasedStats>>();
		for (LocalizerBasedStats s : allStats){
			List<LocalizerBasedStats> Entry = sorted.get(s.getAtribute(a));
			if (Entry == null){
				Entry = new ArrayList<LocalizerBasedStats>();
				sorted.put(s.getAtribute(a), Entry);
			}
			Entry.add(s);
		}
		return sorted;
	}
	
	private static List<LocalizerBasedStats> selectFromRange(List<LocalizerBasedStats> allStats, Atribute a, int min, int max){
		List<LocalizerBasedStats> kept = new ArrayList<LocalizerBasedStats>();
		for (LocalizerBasedStats s : allStats){
			if (s.getAtribute(a) <= max && s.getAtribute(a) >= min)
				kept.add(s);
		}
		return kept;
	}

	private static List<LocalizerBasedStats> selectFromValues(List<LocalizerBasedStats> allStats, Atribute a, List<Integer> values){
		List<LocalizerBasedStats> kept = new ArrayList<LocalizerBasedStats>();
		for (LocalizerBasedStats s : allStats){
			if (values.contains(s.getAtribute(a)))
				kept.add(s);
		}
		return kept;
	}
	
	private static List<LocalizerBasedStats> selectFromValues(List<LocalizerBasedStats> allStats, Atribute a, Integer[] values){
		return selectFromValues(allStats, a, Arrays.asList(values));
	}

	private static float AtributeAvg(List<LocalizerBasedStats> allStats, Atribute a){
		int sum = 0;
		for (LocalizerBasedStats s : allStats)
			sum += s.getAtribute(a);
		return (float) sum / allStats.size();
	}

	private static Integer AtributeMin(List<LocalizerBasedStats> allStats, Atribute a) {
		int min = allStats.get(0).getAtribute(a);
		for (LocalizerBasedStats s : allStats)
			if (min > s.getAtribute(a))
				min = s.getAtribute(a);
		return min;
	}

	private static Integer AtributeMax(List<LocalizerBasedStats> allStats, Atribute a) {
		int max = allStats.get(0).getAtribute(a);
		for (LocalizerBasedStats s : allStats)
			if (max < s.getAtribute(a))
				max = s.getAtribute(a);
		return max;
	}
	
	private static Integer AtributeMedian(List<LocalizerBasedStats> allStats, Atribute a){
		Integer[] values = new Integer[allStats.size()];
		for (int i = 0; i < allStats.size(); i++){
			values[i] = allStats.get(i).getAtribute(a);
		}
		Arrays.sort(values);
		return values[allStats.size()/2];
	}

	public static String makeTextStats(List<LocalizerBasedStats> statsCol) {
		Map<Integer, List<LocalizerBasedStats>> sorted = sortByAtribute(statsCol, Atribute.W_SIZE);

		StringBuilder r = new StringBuilder();
		for (Integer WSize : sorted.keySet()){
			List<LocalizerBasedStats> entry = sorted.get(WSize);
			r.append("for W sets of size " + WSize + " (" + entry.size() + " inference(s)) :\n");
			r.append("\tcalls to localizer :\t" + AtributeAvg(entry, Atribute.LOCALIZER_CALL_NB) + " calls\n");
			r.append("\tlength of localizer :\t" + AtributeAvg(entry, Atribute.LOCALIZER_SEQUENCE_LENGTH) + " symbols\n");
			r.append("\ttotal length of trace :\t" + AtributeAvg(entry, Atribute.TRACE_LENGTH) + " symbols\n");
			r.append("\tinput symbols :\t\t" + AtributeAvg(entry, Atribute.INPUT_SYMBOLS) + " symbols\n");
			r.append("\toutput symbols :\t" + AtributeAvg(entry, Atribute.OUTPUT_SYMBOLS) + " symbols\n");
			r.append("\tstates number :\t\t" + AtributeAvg(entry, Atribute.STATE_NUMBER) + " states\n");
			r.append("\n");
		}
		return r.toString();
	}

	private int getAtribute(Atribute a){
		switch (a) {
		case W_SIZE:
			return WSize;
		case W1_LENGTH:
			return w1Length;
		case LOCALIZER_CALL_NB:
			return localizeCallNb;
		case LOCALIZER_SEQUENCE_LENGTH:
			return localizeSequenceLength;
		case TRACE_LENGTH:
			return traceLength;
		case INPUT_SYMBOLS:
			return inputSymbols;
		case OUTPUT_SYMBOLS:
			return outputSymbols;
		case STATE_NUMBER:
			return statesNumber;
		case STATE_NUMBER_BOUND:
			return n;
		case STATE_BOUND_OFFSET:
			return n-statesNumber;
		case LOOP_RATIO:
			return loopTransitionPercentage;
		default :
			throw new RuntimeException();
		}
	}
	
	private static File makeDataFile(List<LocalizerBasedStats> allStats, Atribute ord, Atribute abs, PlotStyle style){
		File tempPlot;
		PrintWriter tempWriter;
		try {
			tempPlot = File.createTempFile("simpa_"+ord+"_"+abs+"_", ".dat");
			tempPlot.deleteOnExit();
			tempWriter = new PrintWriter(tempPlot,"UTF-8");
		}catch (IOException ioe){
			LogManager.logException("unable to create temporary file for gnuplot", ioe);
			return null;
		}
		switch (style) {
		case POINTS:
			for (LocalizerBasedStats s : allStats){
				tempWriter.write(s.getAtribute(abs) + " " + s.getAtribute(ord) + "\n");	
			}
			break;
		case AVERAGE:{
			Map<Integer,List<LocalizerBasedStats>> sorted = sortByAtribute(allStats, abs);
			List<Integer> keys = new ArrayList<Integer>(sorted.keySet());
			Collections.sort(keys);
			for (Integer key : keys){
				tempWriter.write(key + " " + AtributeAvg(sorted.get(key), ord) + "\n");
			}
		}
		break;
		case AVERAGE_WITH_EXTREMA:{
			Map<Integer,List<LocalizerBasedStats>> sorted = sortByAtribute(allStats, abs);
			List<Integer> keys = new ArrayList<Integer>(sorted.keySet());
			Collections.sort(keys);
			for (Integer key : keys){
				List<LocalizerBasedStats> entrie = sorted.get(key);
				tempWriter.write(key + " " + AtributeAvg(entrie, ord) + 
						" " + AtributeMin(entrie, ord) + " " + AtributeMax(entrie, ord) + "\n");
			}
		}
		break;
		case MEDIAN:{
			Map<Integer,List<LocalizerBasedStats>> sorted = sortByAtribute(allStats, abs);
			List<Integer> keys = new ArrayList<Integer>(sorted.keySet());
			Collections.sort(keys);
			for (Integer key : keys){
				tempWriter.write(key + " " + AtributeMedian(sorted.get(key), ord) + "\n");
			}
		}
		break;
		default:
			break;
		}

		tempWriter.close();
		return tempPlot;
	}

	private static String makeTitle(List<LocalizerBasedStats> allStats, Atribute ord, PlotStyle style){
		StringBuilder r = new StringBuilder();
		r.append(style + " of " + allStats.size() + " inferences ");
		return r.toString();
	}
	
	private static String makeTitle(List<LocalizerBasedStats> allStats, Atribute ord, Atribute group, Integer key, PlotStyle style){
		StringBuilder r = new StringBuilder();
		r.append(makeTitle(allStats, ord, style));
		r.append("(" + group.name + " : " + key + " " + group.units + ")");
		return r.toString();
	}
	
	private static String makeDataId(List<LocalizerBasedStats> allStats){
		if (allStats.size() == 0)
			return "_empty_";
		StringBuilder r = new StringBuilder();
		for (Atribute a : Atribute.class.getEnumConstants()){
			int min = AtributeMin(allStats, a);
			int max = AtributeMax(allStats, a);
			if (min == max){
				r.append("_" + a.id + min);
			} else {
				r.append("_" + a.id + min + "-" + max);
			}
		}
		return r.toString();
	}
	
	private static String makeDataDescritption(List<LocalizerBasedStats> allStats, List<Atribute> ignorefields){
		StringBuilder r = new StringBuilder();
		String separator = "\\n";
		for (Atribute a : Atribute.class.getEnumConstants()){
			if (ignorefields.contains(a))
				continue;
			if (!a.isParameter || a.isVirtual)
				continue;
			int min = AtributeMin(allStats, a);
			int max = AtributeMax(allStats, a);
			if (min == max){
				r.append(a.name + " : " + min + " " + a.units + separator);
			} else {
				r.append(min + " ≤ " + a.name + " ≤ " + max + " " + a.units + separator);
			}
		}
		return r.toString();
	}
	
	private static String makeDataDescritption(List<LocalizerBasedStats> allStats, Atribute[] ignorefields){
		return makeDataDescritption(allStats, Arrays.asList(ignorefields));
	}
	
	/**
	 * 
	 * @param allStats
	 * @param ord
	 * @param abs
	 * @param group may be null if data are not grouped
	 * @param style
	 * @param plotLines
	 * @return
	 */
	private static String makeGnuPlotInstructions(List<LocalizerBasedStats> allStats, Atribute ord, Atribute abs, Atribute group, PlotStyle style, String plotLines){
		StringBuilder r = new StringBuilder();

		r.append("set terminal png enhanced font \"Sans,10\"\n");
		
		String name = new String("relationship between "+ord+" and  "+abs+(group == null ? "" : " grouped by " + group));
		r.append("set title \"" + name + "\"\n");
		
		String filename = new String(Options.getStatsGraphDir() + File.separator
				+ name + "(" + makeDataId(allStats) + ").png");
		r.append("set output \"" + filename + "\"\n");
		
		r.append("set xlabel \"" + abs.name + " (" + abs.units + ")\"\n");
		
		r.append("set ylabel \"" + ord.name + " (" + ord.units + ")\"\n");
		
		r.append("set label \"");
		if (group == null)
			r.append(makeDataDescritption(allStats, new Atribute[]{ord,abs}));
		else
			r.append(makeDataDescritption(allStats, new Atribute[]{ord,group,abs}));
		r.append("\" at graph 1,0.25 right\n");

	
		r.append((ord.logScale ? "set logscale y" : "unset logscale y") + "\n");
	
		r.append(plotLines+"\n");
		
		return r.toString();
	}

	public static void makeMap(List<LocalizerBasedStats> allStats, Atribute ord, Atribute abs, Atribute watch,int watchMin ,int watchMax){
		if (allStats.isEmpty())
			return;
		int maxSize = 0;
		Set<Integer> absKeys = new HashSet<Integer>();
		Map<Integer, List<LocalizerBasedStats>> groupORD = sortByAtribute(allStats, ord);
		Map<Integer,Map<Integer, List<LocalizerBasedStats>>> groupMAP = new HashMap<Integer,Map<Integer,List<LocalizerBasedStats>>>();
		for (Integer key : groupORD.keySet()){
			Map<Integer, List<LocalizerBasedStats>> groupABS = sortByAtribute(groupORD.get(key), abs);
			for (Integer keyAbs : groupABS.keySet()){
				List<LocalizerBasedStats> values = groupABS.get(keyAbs);
				if (values.size() > maxSize)
					maxSize = values.size();
			}
			groupMAP.put(key, groupABS);
			absKeys.addAll(groupABS.keySet());
		}
		maxSize++;
		
		File tempPlot;
		PrintWriter tempWriter;
		try {
			tempPlot = File.createTempFile("simpa_"+ord+"_"+abs+"_", ".dat");
			//tempPlot.deleteOnExit();
			tempWriter = new PrintWriter(tempPlot,"UTF-8");
		}catch (IOException ioe){
			LogManager.logException("unable to create temporary file for gnuplot", ioe);
			return;
		}
		List<Integer> keyValuesOrd = new ArrayList<Integer>(groupORD.keySet());
		Collections.sort(keyValuesOrd);
		List<Integer> keyValuesAbs = new ArrayList<Integer>(absKeys);
		Collections.sort(keyValuesAbs);
		for (Integer keyOrd : keyValuesOrd){
			Map<Integer, List<LocalizerBasedStats>> groupABS = groupMAP.get(keyOrd);
			for (Integer keyAbs : keyValuesAbs){
				float RatioSize = 0;
				float RatioWatch = 0;
				List<LocalizerBasedStats> pointValues = groupABS.get(keyAbs);
				if (pointValues != null){
					RatioSize = ((float)pointValues.size())/maxSize;
					//to increase visibility 
//					RatioSize *= 3./4.;
//					RatioSize += .25;
					List<LocalizerBasedStats> watchValues = selectFromRange(pointValues, watch, watchMin, watchMax);
					RatioWatch = ((float)watchValues.size())/pointValues.size();
				}
				tempWriter.append(keyOrd + " " + keyAbs + " " + RatioSize + " " + RatioWatch + "\n");
			}
			tempWriter.append("\n");
		}
		tempWriter.close();
		
		StringBuilder r = new StringBuilder();

		r.append("set terminal png enhanced font \"Sans,10\"\n");
		
		String name = new String("Map of value in "+ord+" and  "+abs+" observing " + watch);
		r.append("set title \"" + name + "\"\n");
		
		String filename = new String(Options.getStatsGraphDir() + File.separator
				+ name + "(" + makeDataId(allStats) + ").png");
		r.append("set output \"" + filename + "\"\n");
		
		r.append("set xlabel \"" + abs.name + " (" + abs.units + ")\"\n");
		
		r.append("set ylabel \"" + ord.name + " (" + ord.units + ")\"\n");
		
		r.append("set label \"");
			r.append(makeDataDescritption(allStats, new Atribute[]{ord,abs}));
		r.append("\" at graph 1,0.25 right front\n");

		r.append("RGB(R,G,B) =  int(255.*R) * 2**16 + int(255.*G) * 2**8  + int(255.*B)\n");
		r.append("myColor(s,w) =  RGB((1-s)+s*w,1-s,(1-s)+s*(1-w))\n");
		//r.append("plot \""+tempPlot.getAbsolutePath()+"\" using 2:1:(myColor($3,$4)) with image lc rgb variable\n");
		r.append("plot \""+tempPlot.getAbsolutePath()+"\" using 2:1:(myColor($3,$4)) with points pt 7 ps 2 lc rgb variable\n");
		
		GNUPlot.makeGraph(r.toString());
		
	}
	
	public static void makeGraph(List<LocalizerBasedStats> allStats, Atribute ord, Atribute abs, Atribute sort, PlotStyle style){
		if (allStats.isEmpty())
			return;
		StringBuilder plotLines = new StringBuilder("plot ");
		Map<Integer, List<LocalizerBasedStats>> sorted = sortByAtribute(allStats, sort);
		List<Integer> keyValues = new ArrayList<Integer>(sorted.keySet());
		Collections.sort(keyValues);
		for (Integer Size : keyValues){
			File tempPlot = makeDataFile(sorted.get(Size), ord, abs,style);
			plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
					style.plotLine +
					" title \"" + makeTitle(sorted.get(Size), ord, sort, Size, style) + "\", ");
		}
		GNUPlot.makeGraph(makeGnuPlotInstructions(allStats, ord, abs, sort, style, plotLines.toString()));
	}
	
	public static void makeGraph(List<LocalizerBasedStats> allStats, Atribute ord, Atribute abs, PlotStyle style){
		if (allStats.isEmpty())
			return;
		StringBuilder plotLines = new StringBuilder("plot ");
		File tempPlot = makeDataFile(allStats, ord, abs,style);
		plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
				style.plotLine +
				" title \"" + makeTitle(allStats, ord, style) + "\", ");
		GNUPlot.makeGraph(makeGnuPlotInstructions(allStats, ord, abs, null, style, plotLines.toString()));
	}

	public static void makeGraph(List<LocalizerBasedStats> allStats){
		//utils : points repartition
//		makeGraph(selectFromRange(selectFromRange(selectFromRange(selectFromRange(allStats, 
//				Atribute.INPUT_SYMBOLS, 0, 500),
//				Atribute.W_SIZE, 1, 3),
//				Atribute.STATE_NUMBER, 0, 500),
//				Atribute.OUTPUT_SYMBOLS, 0, 500),
//				Atribute.INPUT_SYMBOLS, Atribute.STATE_NUMBER, Atribute.OUTPUT_SYMBOLS,PlotStyle.POINTS);

		List<LocalizerBasedStats> chosenStats;
		
		//not enough data
//		chosenStats = allStats;
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS,15,15);
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS,10,10);
//		chosenStats = selectFromRange(chosenStats, Atribute.W_SIZE,2,2);
//		chosenStats = selectFromValues(chosenStats, Atribute.STATE_NUMBER_BOUND,new Integer[]{5,10,15,20,30,50});
//		makeGraph(chosenStats,
//				Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER, Atribute.STATE_NUMBER_BOUND, PlotStyle.POINTS);
		
		//seems ok but not enough data for STATE_NUMBER != 10
//		chosenStats = allStats;
//		chosenStats = selectFromRange(chosenStats, Atribute.W_SIZE,2,2);
//		chosenStats = selectFromRange(chosenStats, Atribute.W1_LENGTH, 1, 1);
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS,15,15);
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS,10,10);
//		chosenStats = selectFromValues(chosenStats, Atribute.STATE_NUMBER,new Integer[]{10});
//		makeGraph(chosenStats,
//				Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER_BOUND, Atribute.STATE_NUMBER, PlotStyle.MEDIAN);
	
		//not enough data...
//		chosenStats = allStats;
//		chosenStats = selectFromValues(chosenStats, Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50});
//		//chosenStats = selectFromRange(chosenStats, Atribute.W_SIZE, 2, 2);
//		//chosenStats = selectFromValues(chosenStats, Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50});
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 5, 5);
//		chosenStats = selectFromRange(chosenStats, Atribute.STATE_NUMBER, 10, 10);
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS, 5, 5);
//		makeGraph(chosenStats,
//				Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER_BOUND, Atribute.W_SIZE, PlotStyle.POINTS);
		
		
		//not enough data...
//		chosenStats = allStats;
//		chosenStats = selectFromRange(chosenStats, Atribute.W_SIZE, 2, 2);
//		chosenStats = selectFromValues(chosenStats, Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50});
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 5, 5);
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS, 5, 5);
//		makeGraph(chosenStats,
//				Atribute.TRACE_LENGTH, Atribute.STATE_BOUND_OFFSET, Atribute.STATE_NUMBER, PlotStyle.POINTS);
	
	
		//group by output symbols gave a curious result
//		chosenStats = allStats;
//		chosenStats = selectFromRange(chosenStats, Atribute.STATE_BOUND_OFFSET, 0, 0);
//		chosenStats = selectFromRange(chosenStats, Atribute.STATE_NUMBER, 5, 5);
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 3, 3);
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS, 5, 5);
//		makeGraph(chosenStats,
//				Atribute.TRACE_LENGTH, Atribute.W_SIZE, PlotStyle.POINTS);
		
//		makeGraph(selectFromRange(selectFromRange(selectFromRange(allStats, 
//		Atribute.INPUT_SYMBOLS, 5, 5),
//		Atribute.OUTPUT_SYMBOLS, 5, 5),
//		Atribute.STATE_NUMBER, 0, 500),
//		Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER, Atribute.W_SIZE,PlotStyle.POINTS);

		

		// not good
//		chosenStats = allStats;
//		//chosenStats = selectFromRange(chosenStats, Atribute.STATE_BOUND_OFFSET, 0, 0);
//		chosenStats = selectFromRange(chosenStats, Atribute.STATE_NUMBER, 11, 11);
//		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 5, 5);
//		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS, 5, 5);
//		makeGraph(chosenStats, 
//				Atribute.TRACE_LENGTH, Atribute.W1_LENGTH, Atribute.W_SIZE, PlotStyle.POINTS);
		// help to choose the right number of states		
//		makeGraph(selectFromRange(selectFromRange(selectFromRange(allStats, 
//				Atribute.INPUT_SYMBOLS, 5, 5),
//				Atribute.OUTPUT_SYMBOLS, 5, 5),
//				Atribute.STATE_NUMBER, 0, 500),
//				Atribute.W1_LENGTH, Atribute.STATE_NUMBER, Atribute.W_SIZE,PlotStyle.POINTS);
	
		//OK		
		chosenStats = allStats;
		chosenStats = selectFromRange(chosenStats, Atribute.STATE_BOUND_OFFSET, 0, 0);
		chosenStats = selectFromRange(chosenStats, Atribute.STATE_NUMBER, 6, 6);
		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 5, 5);
		makeGraph(chosenStats,
				Atribute.TRACE_LENGTH, Atribute.INPUT_SYMBOLS, Atribute.W_SIZE, PlotStyle.MEDIAN);
		
		chosenStats = allStats;
		chosenStats = selectFromRange(chosenStats, Atribute.STATE_BOUND_OFFSET, 0, 500);
		chosenStats = selectFromRange(chosenStats, Atribute.STATE_NUMBER, 0, 500);
		chosenStats = selectFromRange(chosenStats, Atribute.INPUT_SYMBOLS, 0, 500);
		chosenStats = selectFromRange(chosenStats, Atribute.OUTPUT_SYMBOLS, 0, 500);
		makeMap(chosenStats, Atribute.INPUT_SYMBOLS, Atribute.STATE_NUMBER, Atribute.W_SIZE, 3, 3);
		
	}
}
