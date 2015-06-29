package learner.mealy.noReset;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import tools.GNUPlot;
import tools.loggers.LogManager;
import automata.mealy.InputSequence;

public class NoResetStats {
	enum Atribute {
		W_SIZE("Size of W","sequence"),
		LOCALIZER_CALL_NB("Number of call to localizer",""),
		LOCALIZER_SEQUENCE_LENGTH("Length of localizer sequence","symbols"),
		TRACE_LENGTH("length of trace","symbols"),
		INPUT_SYMBOLS("number of input symbols",""),
		OUTPUT_SYMBOLS("number of output symbols",""),
		STATE_NUMBER("number of states","");
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
		AVERAGE_WITH_EXTREMA("with yerrorbars");
		public String plotLine;
		private PlotStyle(String plotLine) {
			this.plotLine = plotLine;
		}
	}
	private int WSize;
	private int localizeCallNb = 0;
	private int localizeSequenceLength;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	
	public NoResetStats(List<InputSequence> W, int inputSymbols, int outputSymbols){
		WSize = W.size();
		this.inputSymbols = inputSymbols;
		this.outputSymbols= outputSymbols;
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
