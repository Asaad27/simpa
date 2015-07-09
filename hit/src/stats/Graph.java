package stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import stats.attribute.Attribute;
import tools.GNUPlot;
import tools.loggers.LogManager;

public class Graph<T_ABS extends Comparable<T_ABS>, T_ORD extends Comparable<T_ORD>> {
	public enum PlotStyle {
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
	
	private Attribute<T_ABS> abs;
	private Attribute<T_ORD> ord;
	private StatsSet stats;
	StringBuilder plotLines;
	String title;
	
	private List<File> toDelete;
	
	public Graph(Attribute<T_ABS> abs, Attribute<T_ORD> ord){
		this.abs = abs;
		this.ord = ord;
		this.stats = new StatsSet();
		plotLines = new StringBuilder("plot ");
		toDelete = new ArrayList<File>();
	}
	
	public void plot(StatsSet stats, PlotStyle style){
		this.stats.getStats().addAll(stats.getStats());
		File tempPlot = makeDataFile(stats, style);
		StringBuilder plotTitle = new StringBuilder();
		plotTitle.append(style + " of " + stats.size() + " inferences ");
		plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
				style.plotLine +
				" title \"" + plotTitle + "\", ");
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void export(){
		StringBuilder r = new StringBuilder();
		r.append(makeDataDescritption(stats, new Attribute[]{ord,abs}));


		r.append("set terminal png enhanced font \"Sans,10\"\n");
		
		String name = new String("relationship between "+ord+" and  "+abs);
		r.append("set title \"" + (title == null ? name : title) + "\"\n");
		
	//	String filename = new String(Options.OUTDIR + File.separator + name + "(" + makeDataId(stats) + ").png");
		String filename = "test.png";
		r.append("set output \"" + filename + "\"\n");
		
		r.append("set xlabel \"" + abs.getName() + " (" + abs.getUnits().getSymbol() + ")\"\n");
		
		r.append("set ylabel \"" + ord.getName() + " (" + ord.getUnits().getSymbol() + ")\"\n");
		
		r.append("set label \"");
		r.append(makeDataDescritption(stats, new Attribute[]{ord,abs}));
		r.append("\" at graph 1,0.25 right\n");
	
		boolean ordLogScale = ord.useLogScale();
		r.append((ordLogScale? "set logscale y" : "unset logscale y") + "\n");
	
		r.append(plotLines+"\n");
		
		GNUPlot.makeGraph(r.toString());
	}
	
	private StringBuilder makeDataDescritption(StatsSet s, Attribute<?>[] ignoreFields) {
		return makeDataDescritption(s, Arrays.asList(ignoreFields));
	}
	
	private StringBuilder makeDataDescritption(StatsSet s, List<Attribute<?>> ignoreFields) {
		if (s.size() == 0){
			return new StringBuilder("No Data");
		}
		StringBuilder r = new StringBuilder();
		String separator = "\\n";
		for (Attribute<?> a : s.getStats().get(0).getAttributes()){
			if (ignoreFields.contains(a))
				continue;
			if (!a.isParameter() || a.isVirtualParameter())
				continue;
			Float min = ((Integer)s.attributeMin(a)).floatValue();//TODO add a method getFloatValue in the Stats Class in order to make a cast properly and throw an exception.
			Float max = ((Integer)s.attributeMax(a)).floatValue();
			if (min == max){
				r.append(a.getName() + " : " + min + " " + a.getUnits().getSymbol() + separator);
			} else {
				r.append(min + " ≤ " + a.getName() + " ≤ " + max + " " + a.getUnits().getSymbol() + separator);
			}
		}
		return r;
	}

	private File makeDataFile(StatsSet stats, PlotStyle style){
		File tempPlot;
		PrintWriter tempWriter;
		try {
			tempPlot = File.createTempFile("simpa_"+ord+"_"+abs+"_", ".dat");
			tempPlot.deleteOnExit();
			toDelete.add(tempPlot);
			tempWriter = new PrintWriter(tempPlot,"UTF-8");
		}catch (IOException ioe){
			LogManager.logException("unable to create temporary file for gnuplot", ioe);
			return null;
		}
		switch (style) {
		case POINTS:
			for (StatsEntry s : stats.getStats()){
				tempWriter.write(s.get(abs) + " " + s.get(ord) + "\n");	
			}
			break;
		case AVERAGE:{
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys){
				tempWriter.write(key + " " + sorted.get(key).attributeAVG(ord) + "\n");
			}
	
		}
		break;
		case AVERAGE_WITH_EXTREMA:{
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys){
				StatsSet entrie = sorted.get(key);
				tempWriter.write(key + " " + entrie.attributeAVG(ord) + 
						" " + entrie.attributeMin(ord) + " " + entrie.attributeMax(ord) + "\n");
			}
		}
		break;
		case MEDIAN:{
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys){
				tempWriter.write(key + " " + sorted.get(key).attributeMedian(ord) + "\n");
			}
		}
		break;
		default:
			break;
		}

		tempWriter.close();
		return tempPlot;
	}

	
}
