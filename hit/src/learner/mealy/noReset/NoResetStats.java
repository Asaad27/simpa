package learner.mealy.noReset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
		W_SIZE("Size of W","sequence"),
		W1_LENGTH("Length of first W element","symbols"),
		LOCALIZER_CALL_NB("Number of call to localizer",""),
		LOCALIZER_SEQUENCE_LENGTH("Length of localizer sequence","symbols"),
		TRACE_LENGTH("length of trace","symbols"),
		INPUT_SYMBOLS("number of input symbols",""),
		OUTPUT_SYMBOLS("number of output symbols",""),
		STATE_NUMBER("number of states",""),
		STATE_NUMBER_BOUND("bound of state number","states"),
		;
		
		public final String units;
		public final String name;
		private Atribute(String name, String units) {
			this.units = units;
			this.name = name;
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
		default:
			break;
		}

		tempWriter.close();
		tempPlot.deleteOnExit();
		return tempPlot;
	}
	
	public static void makeGraph(List<NoResetStats> allStats, Atribute ord, Atribute abs, Atribute sort, PlotStyle style){
		StringBuilder plotLines = new StringBuilder("plot ");
		Map<Integer, List<NoResetStats>> sorted = sortByAtribute(allStats, sort);
		for (Integer Size : sorted.keySet()){
			File tempPlot = makeDataFile(sorted.get(Size), ord, abs,style);
			plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
					style.plotLine +
					" title \"" + sort.name + " " + Size + " " + sort.units +"\", ");
		}
		String filename = new String(Options.OUTDIR + File.pathSeparator + "relationship between "+ord+" and  "+abs+" sorted by " + sort + ".png");
		GNUPlot.makeGraph(
				"set terminal png enhanced font \"Sans,10\"\n"+
				"set output \"" + filename + "\"\n"+
				"set xlabel \"" + abs.name + " (" + abs.units + ")\"\n" +
				"set ylabel \"" + ord.name + " (" + ord.units + ")\"\n" +
				plotLines+"\n");
	}
	
	public static void makeGraph(List<NoResetStats> allStats, Atribute ord, Atribute abs, PlotStyle style){
		StringBuilder plotLines = new StringBuilder("plot ");
			File tempPlot = makeDataFile(allStats, ord, abs,style);
			plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
					style.plotLine);
		String filename = new String(Options.OUTDIR + File.pathSeparator + "relationship between "+ord+" and  "+abs+".png");
		GNUPlot.makeGraph(
				"set terminal png enhanced font \"Sans,10\"\n"+
				"set output \"" + filename + "\"\n"+
				"set xlabel \"" + abs.name + " (" + abs.units + ")\"\n" +
				"set ylabel \"" + ord.name + " (" + ord.units + ")\"\n" +
				plotLines+"\n");
	}

	public static void makeGraph(List<NoResetStats> allStats){
		makeGraph(allStats,Atribute.TRACE_LENGTH,Atribute.LOCALIZER_CALL_NB,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.TRACE_LENGTH,Atribute.INPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.TRACE_LENGTH,Atribute.OUTPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.TRACE_LENGTH,Atribute.STATE_NUMBER,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.LOCALIZER_SEQUENCE_LENGTH,Atribute.INPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.LOCALIZER_SEQUENCE_LENGTH,Atribute.OUTPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.LOCALIZER_SEQUENCE_LENGTH,Atribute.STATE_NUMBER,Atribute.W_SIZE,PlotStyle.POINTS);
		makeGraph(allStats,Atribute.LOCALIZER_CALL_NB,Atribute.STATE_NUMBER,Atribute.W_SIZE,PlotStyle.AVERAGE_WITH_EXTREMA);
		makeGraph(allStats,Atribute.LOCALIZER_CALL_NB,Atribute.INPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.AVERAGE_WITH_EXTREMA);
		makeGraph(allStats,Atribute.LOCALIZER_CALL_NB,Atribute.OUTPUT_SYMBOLS,Atribute.W_SIZE,PlotStyle.AVERAGE_WITH_EXTREMA);
		makeGraph(allStats, Atribute.W_SIZE, Atribute.OUTPUT_SYMBOLS, PlotStyle.AVERAGE);
	}
}
