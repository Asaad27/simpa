package stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import stats.attribute.Attribute;
import tools.GNUPlot;
import tools.loggers.LogManager;

public class Graph<T_ABS extends Comparable<T_ABS>, T_ORD extends Comparable<T_ORD>> {
	public enum PlotStyle {
		POINTS("with points"),
		AVERAGE("with linespoints"),
		AVERAGE_WITH_EXTREMA("with yerrorbars"),
		MEDIAN("with linespoint"),
		SMOOTH("with linespoints"),
		;
		public String plotLine;
		private PlotStyle(String plotLine) {
			this.plotLine = plotLine;
		}
	}

	private Attribute<T_ABS> abs;
	private Attribute<T_ORD> ord;
	private StatsSet stats;
	private StringBuilder plotLines;
	private String title;
	private String fileName;
	private Boolean forceOrdLogScale;

	private List<File> toDelete;

	public Graph(Attribute<T_ABS> abs, Attribute<T_ORD> ord){
		this.abs = abs;
		this.ord = ord;
		this.stats = new StatsSet();
		plotLines = new StringBuilder();
		toDelete = new ArrayList<File>();
	}

	protected void plot(StatsSet stats, PlotStyle style, String titleSuffix){
		if (stats.size() == 0)
			return;
		this.stats.getStats().addAll(stats.getStats());
		File tempPlot = makeDataFile(stats, style);
		StringBuilder plotTitle = new StringBuilder();
		plotTitle.append(style + " of " + stats.size() + " inferences ");
		plotTitle.append(titleSuffix);
		plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" " +
				style.plotLine +
				" title \"" + plotTitle + "\", ");
	}

	public void plot(StatsSet stats, PlotStyle style){
		plot(stats,style,"");
	}
	
	/**
	 * plot a theorical function
	 * @param f the function expression (must depends of x)
	 * @param title the title of the function
	 */
	public void plotFunc(String f, String title){
		plotLines.append(f + " with lines title \"" + title + "\", ");
	}

	public <T extends Comparable<T>> void plotGroup(StatsSet stats, Attribute<T> groupBy, PlotStyle style){
		Map <T,StatsSet> grouped = stats.sortByAtribute(groupBy);
		List<T>  keys = new ArrayList<T>(grouped.keySet());
		Collections.sort(keys);
		for (T key : keys){
			StringBuilder title = new StringBuilder();
			title.append(" ");
			title.append(groupBy.getName());
			title.append(" = ");
			title.append(key);
			plot(grouped.get(key),style,title.toString());
		}
	}

	public void setTitle(String title){
		this.title = title;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 
	 * @param forceOrdLogScale true to force logarithmic scale, false to force linear scale and null to use default scale
	 */
	public void setForceOrdLogScale(Boolean forceOrdLogScale) {
		this.forceOrdLogScale = forceOrdLogScale;
	}

	public void export(){
		if (plotLines.length() == 0){
			LogManager.logError("no data to plot");
			return;
		}
		StringBuilder r = new StringBuilder();

		r.append("set terminal png enhanced font \"Sans,10\"\n");

		String name = new String("relationship between "+ord+" and  "+abs);
		r.append("set title \"" + (title == null ? name : title) + "\"\n");

		StringBuilder totalFileName = new StringBuilder(Options.OUTDIR + File.separator);
		if (fileName == null)
			totalFileName.append(name + "(" + stats.hashCode() + ")");
		else
			totalFileName.append(fileName);
		totalFileName.append(".png");

		r.append("set output \"" + totalFileName + "\"\n");

		r.append("set xlabel \"" + abs.getName() + " (" + abs.getUnits().getSymbol() + ")\"\n");

		r.append("set ylabel \"" + ord.getName() + " (" + ord.getUnits().getSymbol() + ")\"\n");

		r.append("set label \"");
		r.append(makeDataDescritption(stats, new Attribute[]{ord,abs}).toString()
				.replace("\"", "\\\"")
				);
		r.append("\" at graph 1,0.5 right\n");

		boolean ordLogScale = ord.useLogScale();
		if (forceOrdLogScale != null)
			ordLogScale = forceOrdLogScale;
		r.append((ordLogScale? "set logscale y" : "unset logscale y") + "\n");
		r.append("plot "+plotLines.substring(0, plotLines.length()-2)+"\n");
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
			Comparable<?> min = s.attributeMin(a);
			Comparable<?> max = s.attributeMax(a);
			if (min.equals(max)){
				r.append(a.getName() + " : " + min + " " + a.getUnits().getSymbol() + separator);
			} else {
				r.append(min.toString() + " ≤ " + a.getName() + " ≤ " + max.toString() + " " + a.getUnits().getSymbol() + separator);
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
		case SMOOTH:{
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			int n = 0;
			String min = "";
			StatsSet tmpSet = new StatsSet();
			for (T_ABS key : keys){
				for (StatsEntry s : sorted.get(key).getStats()){
					if (n==0){
						min = s.get(abs).toString();
						tmpSet = new StatsSet();
					}
					tmpSet.add(s);
					n++;
					if (n == 5){
						float avg = tmpSet.attributeAVG(ord);
						String max = s.get(abs).toString();
						tempWriter.write(min + " " + avg + "\n");
						tempWriter.write(max + " " + avg + "\n");
						n = 0;
					}
				}
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
