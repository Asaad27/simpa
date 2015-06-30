package learner.mealy.noReset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import main.simpa.Options;
import tools.GNUPlot;
import tools.loggers.LogManager;
import automata.mealy.InputSequence;

public class NoResetStats {
	enum Atribute {
		W_SIZE("Size of W",							"sequence",	false,	true,	"W",	false),
		W1_LENGTH("Length of first W element",		"symbols",	false,	true,	"w1",	false),
		LOCALIZER_CALL_NB("Number of call to localizer","",		false,	false,	"lc",	false),
		LOCALIZER_SEQUENCE_LENGTH("Length of localizer sequence","symbols",false,false,"lsl",false),
		TRACE_LENGTH("length of trace",				"symbols",	true,	false,	"tl",	false),
		INPUT_SYMBOLS("number of input symbols",	"",			false,	true,	"f",	false),
		OUTPUT_SYMBOLS("number of output symbols",	"",			false,	true,	"o",	false),
		STATE_NUMBER("number of states",			"",			false,	true,	"s",	false),
		STATE_NUMBER_BOUND("bound of state number",	"states",	false,	true,	"n",	false),
		STATE_BOUND_OFFSET("difference between bound and real state number","states",false,true,"dns",false),
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
	
	
	public NoResetStats(List<InputSequence> W, int inputSymbols, int outputSymbols, int n){
		WSize = W.size();
		w1Length = W.get(0).getLength();
		this.inputSymbols = inputSymbols;
		this.outputSymbols= outputSymbols;
		this.n = n;
	}
	
	private NoResetStats(){
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
		r.append(n);
		return r.toString();
	}

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
				;
	}
	
	public static NoResetStats entrieFromCSV(String line){
		NoResetStats stats = new NoResetStats();
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
		return stats;
	}

	public static List<NoResetStats> setFromCSV(String filename){
		List<NoResetStats> r = new ArrayList<NoResetStats>();
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

	private static Map<Integer,List<NoResetStats>> sortByAtribute(List<NoResetStats> allStats, Atribute a){
		Map<Integer,List<NoResetStats>> sorted = new HashMap<Integer,List<NoResetStats>>();
		for (NoResetStats s : allStats){
			List<NoResetStats> Entry = sorted.get(s.getAtribute(a));
			if (Entry == null){
				Entry = new ArrayList<NoResetStats>();
				sorted.put(s.getAtribute(a), Entry);
			}
			Entry.add(s);
		}
		return sorted;
	}
	
	private static List<NoResetStats> selectFromRange(List<NoResetStats> allStats, Atribute a, int min, int max){
		List<NoResetStats> kept = new ArrayList<NoResetStats>();
		for (NoResetStats s : allStats){
			if (s.getAtribute(a) <= max && s.getAtribute(a) >= min)
				kept.add(s);
		}
		return kept;
	}

	private static List<NoResetStats> selectFromValues(List<NoResetStats> allStats, Atribute a, List<Integer> values){
		List<NoResetStats> kept = new ArrayList<NoResetStats>();
		for (NoResetStats s : allStats){
			if (values.contains(s.getAtribute(a)))
				kept.add(s);
		}
		return kept;
	}
	
	private static List<NoResetStats> selectFromValues(List<NoResetStats> allStats, Atribute a, Integer[] values){
		return selectFromValues(allStats, a, Arrays.asList(values));
	}

	private static float AtributeAvg(List<NoResetStats> allStats, Atribute a){
		int sum = 0;
		for (NoResetStats s : allStats)
			sum += s.getAtribute(a);
		return (float) sum / allStats.size();
	}

	private static Integer AtributeMin(List<NoResetStats> allStats, Atribute a) {
		int min = allStats.get(0).getAtribute(a);
		for (NoResetStats s : allStats)
			if (min > s.getAtribute(a))
				min = s.getAtribute(a);
		return min;
	}

	private static Integer AtributeMax(List<NoResetStats> allStats, Atribute a) {
		int max = allStats.get(0).getAtribute(a);
		for (NoResetStats s : allStats)
			if (max < s.getAtribute(a))
				max = s.getAtribute(a);
		return max;
	}
	
	private static Integer AtributeMedian(List<NoResetStats> allStats, Atribute a){
		Integer[] values = new Integer[allStats.size()];
		for (int i = 0; i < allStats.size(); i++){
			values[i] = allStats.get(i).getAtribute(a);
		}
		Arrays.sort(values);
		return values[allStats.size()/2];
	}

	public static String makeTextStats(List<NoResetStats> statsCol) {
		Map<Integer, List<NoResetStats>> sorted = sortByAtribute(statsCol, Atribute.W_SIZE);

		StringBuilder r = new StringBuilder();
		for (Integer WSize : sorted.keySet()){
			List<NoResetStats> entry = sorted.get(WSize);
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
		default :
			throw new RuntimeException();
		}
	}
	
	private static File makeDataFile(List<NoResetStats> allStats, Atribute ord, Atribute abs, PlotStyle style){
		File tempPlot;
		PrintWriter tempWriter;
		try {
			tempPlot = File.createTempFile("simpa_"+ord+"_"+abs+"_", ".dat");
			tempWriter = new PrintWriter(tempPlot,"UTF-8");
		}catch (IOException ioe){
			LogManager.logException("unable to create temporary file for gnuplot", ioe);
			return null;
		}
		switch (style) {
		case POINTS:
			for (NoResetStats s : allStats){
				tempWriter.write(s.getAtribute(abs) + " " + s.getAtribute(ord) + "\n");	
			}
			break;
		case AVERAGE:{
			Map<Integer,List<NoResetStats>> sorted = sortByAtribute(allStats, abs);
			for (Integer key : sorted.keySet()){
				tempWriter.write(key + " " + AtributeAvg(sorted.get(key), ord) + "\n");
			}
		}
		break;
		case AVERAGE_WITH_EXTREMA:{
			Map<Integer,List<NoResetStats>> sorted = sortByAtribute(allStats, abs);
			for (Integer key : sorted.keySet()){
				List<NoResetStats> entrie = sorted.get(key);
				tempWriter.write(key + " " + AtributeAvg(entrie, ord) + 
						" " + AtributeMin(entrie, ord) + " " + AtributeMax(entrie, ord) + "\n");
			}
		}
		break;
		case MEDIAN:{
			Map<Integer,List<NoResetStats>> sorted = sortByAtribute(allStats, abs);
			for (Integer key : sorted.keySet()){
				tempWriter.write(key + " " + AtributeMedian(sorted.get(key), ord) + "\n");
			}
		}
		break;
		default:
			break;
		}

		tempWriter.close();
		tempPlot.deleteOnExit();
		return tempPlot;
	}

	private static String makeTitle(List<NoResetStats> allStats, Atribute ord, PlotStyle style){
		StringBuilder r = new StringBuilder();
		r.append(style + " of " + allStats.size() + " inferences ");
		return r.toString();
	}
	
	private static String makeTitle(List<NoResetStats> allStats, Atribute ord, Atribute group, Integer key, PlotStyle style){
		StringBuilder r = new StringBuilder();
		r.append(makeTitle(allStats, ord, style));
		r.append("(" + group.name + " : " + key + " " + group.units + ")");
		return r.toString();
	}
	
	private static String makeDataId(List<NoResetStats> allStats){
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
	
	private static String makeDataDescritption(List<NoResetStats> allStats, List<Atribute> ignorefields){
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
	
	private static String makeDataDescritption(List<NoResetStats> allStats, Atribute[] ignorefields){
		return makeDataDescritption(allStats, Arrays.asList(ignorefields));
	}

	public static void makeGraph(List<NoResetStats> allStats, Atribute ord, Atribute abs, Atribute sort, PlotStyle style){
		if (allStats.size() == 0)
			return;
		StringBuilder plotLines = new StringBuilder("plot ");
		Map<Integer, List<NoResetStats>> sorted = sortByAtribute(allStats, sort);
		List<Integer> keyValues = new ArrayList<Integer>(sorted.keySet());
		Collections.sort(keyValues);
		for (Integer Size : keyValues){
			File tempPlot = makeDataFile(sorted.get(Size), ord, abs,style);
			plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
					style.plotLine +
					" title \"" + makeTitle(sorted.get(Size), ord, sort, Size, style) + "\", ");
		}
		String filename = new String(Options.OUTDIR + File.pathSeparator + "relationship between "+ord+" and  "+abs+" sorted by " + sort + makeDataId(allStats) + ".png");
		GNUPlot.makeGraph(
				"set terminal png enhanced font \"Sans,10\"\n"+
				"set output \"" + filename + "\"\n"+
				"set xlabel \"" + abs.name + " (" + abs.units + ")\"\n" +
				"set ylabel \"" + ord.name + " (" + ord.units + ")\"\n" +
				"set label \"" + makeDataDescritption(allStats, new Atribute[]{ord,sort,abs}) + "\" at graph 1,0.5 right\n" +
				(ord.logScale ? "set logscale y" : "unset logscale y") + "\n" +
				plotLines+"\n");
	}
	
	public static void makeGraph(List<NoResetStats> allStats, Atribute ord, Atribute abs, PlotStyle style){
		StringBuilder plotLines = new StringBuilder("plot ");
			File tempPlot = makeDataFile(allStats, ord, abs,style);
			plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
					style.plotLine +
					" title \"" + makeTitle(allStats, ord, style) + "\", ");
		String filename = new String(Options.OUTDIR + "relationship between "+ord+" and  "+abs+makeDataId(allStats)+".png");
		GNUPlot.makeGraph(
				"set terminal png enhanced font \"Sans,10\"\n"+
				"set output \"" + filename + "\"\n"+
				"set xlabel \"" + abs.name + " (" + abs.units + ")\"\n" +
				"set ylabel \"" + ord.name + " (" + ord.units + ")\"\n" +
				(ord.logScale ? "set logscale y" : "unset logscale y") + "\n" +
				plotLines+"\n");
	}

	public static void makeGraph(List<NoResetStats> allStats){
		makeGraph(selectFromValues(selectFromRange(allStats, Atribute.W_SIZE, 2, 2),
				Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50}),
				Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER_BOUND, Atribute.STATE_NUMBER, PlotStyle.POINTS);
		makeGraph(selectFromValues(selectFromRange(allStats, Atribute.W_SIZE, 1, 1),
				Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50}),
				Atribute.TRACE_LENGTH, Atribute.STATE_BOUND_OFFSET, Atribute.STATE_NUMBER, PlotStyle.POINTS);
		makeGraph(selectFromValues(allStats,
				Atribute.STATE_NUMBER,new Integer[]{5,10,15,20,30,50}),
				Atribute.TRACE_LENGTH, Atribute.STATE_NUMBER_BOUND, Atribute.STATE_NUMBER, PlotStyle.POINTS);
		makeGraph(allStats, Atribute.TRACE_LENGTH, Atribute.W_SIZE, PlotStyle.MEDIAN);
		makeGraph(allStats, Atribute.TRACE_LENGTH, Atribute.W1_LENGTH, Atribute.W_SIZE, PlotStyle.MEDIAN);
		makeGraph(allStats, Atribute.TRACE_LENGTH, Atribute.INPUT_SYMBOLS, Atribute.W_SIZE, PlotStyle.MEDIAN);
	}
}
