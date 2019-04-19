/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.simpa.Options;
import stats.attribute.Attribute;
import tools.GNUPlot;
import tools.loggers.LogManager;

public class Graph<T_ABS extends Comparable<T_ABS>, T_ORD extends Comparable<T_ORD>> {
	public enum PlotStyle {
		POINTS("with points"), AVERAGE("with linespoints ps 1.5"), AVERAGE_WITH_EXTREMA("with errorlines"), MEDIAN(
				"with linespoint ps 1.5"), SMOOTH("with linespoints"),
		BOXPLOT("with boxplot"),
		CANDLESTICK("with candlesticks whiskerbars"),
		;
		public String plotLine;

		private PlotStyle(String plotLine) {
			this.plotLine = plotLine;
		}
	}

	public enum EstimationMode {
		POWER(), EXPONENTIAL(), ;
	}

	public static class Color {
		private int r, g, b;

		public Color(int r, int g, int b) {
			super();
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public Color(String htmlColor) {
			assert htmlColor.startsWith("#");
			assert htmlColor.length() == 7;
			r = Integer.parseInt(htmlColor.substring(1, 3), 16);
			g = Integer.parseInt(htmlColor.substring(3, 5), 16);
			b = Integer.parseInt(htmlColor.substring(5, 7), 16);
		}

		public String toGnuplotString() {
			String r_ = Integer.toHexString(r);
			String g_ = Integer.toHexString(g);
			String b_ = Integer.toHexString(b);
			if (r_.length() < 2)
				r_ = "0" + r_;
			if (g_.length() < 2)
				g_ = "0" + g_;
			if (b_.length() < 2)
				b_ = "0" + b_;
			return "rgb '#" + r_ + g_ + b_ + "' ";
		}
	}

	static final Color BLACK = new Color(0, 0, 0);

	public static class PointShape {
		public final int style;

		public PointShape(int style) {
			this.style = style;
		}

		public final static PointShape PLUS_CROSS = new PointShape(1);
		public final static PointShape TIMES_CROSS = new PointShape(2);
		public final static PointShape EMPTY_SQUARE = new PointShape(4);
		public final static PointShape FILLED_SQUARE = new PointShape(5);
		public final static PointShape EMPTY_CIRCLE = new PointShape(6);
		public final static PointShape FILLED_CIRCLE = new PointShape(7);
		public final static PointShape EMPTY_TRIANGLE_UP = new PointShape(8);
		public final static PointShape FILLED_TRIANGLE_UP = new PointShape(9);
		public final static PointShape EMPTY_TRIANGLE_DOWN = new PointShape(10);
		public final static PointShape FILLED_TRIANGLE_DOWN = new PointShape(
				11);
		public final static PointShape EMPTY_DIAMOND = new PointShape(12);
		public final static PointShape FILLED_DIAMOND = new PointShape(13);

	}

	public static class KeyParameters {

		public static enum VerticalPosition {
			TOP("top"), BOTTOM("bottom"), V_CENTER("center");
			public String gnuplot;

			private VerticalPosition(String gnuplot) {
				this.gnuplot = gnuplot;
			}
		}

		public static enum HorizontalPosition {
			RIGHT("right"), LEFT("left"), H_CENTER("center");
			public String gnuplot;

			private HorizontalPosition(String gnuplot) {
				this.gnuplot = gnuplot;
			}
		}

		public KeyParameters() {
			this(null, null);
		}

		public KeyParameters(VerticalPosition vPosition,
				HorizontalPosition hPosition) {
			this.vPosition = vPosition;
			this.hPosition = hPosition;
		}

		public void disable() {
			isEnabled = false;
		}

		private VerticalPosition vPosition;
		private HorizontalPosition hPosition;
		private boolean outside = false;
		private boolean isEnabled = true;

		public String toGnuplotLine() {
			if (!isEnabled)
				return "set key off\n";
			return "set key"
					+ (outside ? " tmargin right"
							: (" inside"
									+ (hPosition == null ? ""
											: (" " + hPosition.gnuplot))
									+ (vPosition == null ? ""
											: (" " + vPosition.gnuplot))))
					+ "\n";
		}

		public void setOutside(boolean outside) {
			this.outside = outside;
		}

		public void setvPosition(VerticalPosition vPosition) {
			this.vPosition = vPosition;
		}

		public void sethPosition(HorizontalPosition hPosition) {
			this.hPosition = hPosition;
		}

		public void setPosition(VerticalPosition vPosition,
				HorizontalPosition hPosition) {
			this.vPosition = vPosition;
			this.hPosition = hPosition;
		}
	}

	private static boolean forcePoints = false; // set this to true in order to
												// plot points for each graph

	protected final Attribute<T_ABS> abs;
	protected final Attribute<T_ORD> ord;
	private KeyParameters keyParameters = new KeyParameters();
	private PlotStyle defaultStyle = PlotStyle.POINTS;
	private StatsSet stats;
	private StringBuilder plotLines;
	private String title;
	private String fileName;
	private Integer imageHeight;
	private Integer imageWidth;
	private Boolean forceOrdLogScale;
	private Boolean forceAbsLogScale;
	private Double minAbs = null;
	private Double maxAbs = null;
	private Double minOrd = null;
	private Double maxOrd = null;
	private Number xTics = null;
	private Number[] xTicsValues=null;
	private Number yTics = null;
	private Number[] yTicsValues=null;
	private List<Attribute<?>> dataDescriptionfields = null;
	private Set<LineStyle> linesStyles = new HashSet<LineStyle>();

	private List<File> toDelete;
	private boolean isForArticle = false;
	private boolean exportForLatex = false;

	public void setForArticle(boolean isForArticle) {
		this.isForArticle = isForArticle;
		exportForLatex = isForArticle;
	}

	private boolean fewData = false;// indicate that one set did not provide
									// enough points for one value in abscissa

	public Graph(Attribute<T_ABS> abs, Attribute<T_ORD> ord) {
		this.abs = abs;
		this.ord = ord;
		this.stats = new StatsSet();
		plotLines = new StringBuilder();
		toDelete = new ArrayList<File>();
	}

	public void plot(StatsSet stats, PlotStyle style, PointShape pointType) {
		plot(stats, style, ("" + style).replaceAll("_", " ") + " of "
				+ stats.size() + " inferences " + stats.getTitle(), pointType,
				null);
	}

	public void plot(StatsSet stats, PlotStyle style, String title) {
		plot(stats, style, title, null, null);
	}

	public void plot(StatsSet stats, PlotStyle style, String title,
			PointShape pointType, Color color) {
		if (stats.size() == 0)
			return;

		Map<T_ABS, StatsSet> setByAbs = stats.sortByAtribute(abs);
		for (Entry<T_ABS, StatsSet> e : setByAbs.entrySet()) {

			if (e.getValue().size() < 50) {
				fewData = true;
				if (isForArticle)
					System.err.println("few " + stats.getTitle() + " for value "
							+ e.getKey());
			}
		}

		if (style == null)
			style = defaultStyle;
		this.stats.getStats().addAll(stats.getStats());
		File tempPlot = makeDataFile(stats, style);
		StringBuilder plotTitle = new StringBuilder();
		plotTitle.append(title);
		plotLines.append("\"" + tempPlot.getAbsolutePath() + "\" ");
		String lineStyle = style.plotLine;
		if (stats.get(0).get(abs) instanceof String) {
			switch (style) {
			case SMOOTH:
			case POINTS:
			case MEDIAN:
			case AVERAGE:
				plotLines.append(" using 1:3:xticlabels(2)");
				break;
			case AVERAGE_WITH_EXTREMA:
				plotLines.append(" using 1:3:4:5:xticlabels(2)");
				break;
			case BOXPLOT:
				plotLines.append(" using (0):3:(0):1:xtic(2)");//TODO xticslabel ?
				break;
			case CANDLESTICK:
				plotLines.append(" using 1:5:5:5:5 with candlesticks lc "
						+ ((color == null ? BLACK : color).toGnuplotString())
						+ " notitle,\\\n '' using 1:4:3:7:6:xticlabels(2)");
			}
			lineStyle = lineStyle.replace("linespoint", "point");
			lineStyle = lineStyle.replace("errorlines", "errorbar");
		} else if (style == PlotStyle.BOXPLOT)
			plotLines.append(" using (0):2:(0):1");
		else if (style == PlotStyle.CANDLESTICK)
			plotLines.append(" using 1:4:4:4:4 with candlesticks lc "
					+ ((color == null ? BLACK : color).toGnuplotString())
					+ " notitle,\\\n '' using 1:3:2:6:5");
		plotLines.append(" " + lineStyle);
		if (pointType != null && style != PlotStyle.CANDLESTICK) {
			plotLines.append(" pt " + pointType.style);
		}
		if (color != null) {
			plotLines.append(" lc " + color.toGnuplotString());
		}
		plotLines.append(" title \"" + plotTitle + "\", ");
		if (forcePoints && style != PlotStyle.POINTS)
			plot(stats, PlotStyle.POINTS, title);
	}

	public void plot(StatsSet stats, PlotStyle style) {
		plot(stats, style, ("" + style).replaceAll("_", " ") + " of "
				+ stats.size() + " inferences " + stats.getTitle());
	}

	/**
	 * plot a theoretical function
	 * 
	 * @param f
	 *            the function expression (must depends of 'x')
	 * @param title
	 *            the title of the function
	 */
	public void plotFunc(String f, String title, LineStyle lineStyle) {
		linesStyles.add(lineStyle);
		// plotLines.append(f + " with lines lt 3 title \"" + title + "\", ");
		plotLines.append(f + " with lines linestyle " + lineStyle.index
				+ " title \"" + title + "\", ");
	}

	public String formatDouble(double d) {
		if (Math.abs(d) < 10000 && Math.abs(d) > 0.001) {
			double log = Math.log10(Math.abs(d));
			double roundingValue = Math.pow(10, Math.floor(log) - 2);
			d = Math.round(d / roundingValue) * roundingValue;
			if (d == (int) d)
				return String.format("%d", (int) d);
			return String.format("%s", d);
		}
		return String.format("%.2g", d);
	}

	public void plotEstimation(StatsSet set, EstimationMode estimationMode) {
		switch (estimationMode) {
		case POWER: {
			Utils.DataSet dataSet = new Utils.DataSet();
			Utils.DataSet dataSetLog = new Utils.DataSet();
			for (StatsEntry s : set.getStats()) {
				double x = s.getFloatValue(abs);
				double y = s.getFloatValue(ord);
				if (x <= 0 || y <= 0)
					continue;
				double weight = x * x;
				Utils.DataPoint p = new Utils.DataPoint();
				p.x = x;
				p.y = y;
				p.weight = weight;
				dataSet.add(p);
				Utils.DataPoint logP = new Utils.DataPoint();
				logP.x = Math.log(x);
				logP.y = Math.log(y);
				logP.weight = weight;
				dataSetLog.add(logP);
			}
			Utils.AffineRegressionResults affineR = Utils
					.affineRegression(dataSet);
			Utils.AffineRegressionResults affineRLog = Utils
					.affineRegression(dataSetLog);
			double a = affineRLog.a;
			double b = affineR.averageY / Math.pow(affineR.averageX, a);

			plotFunc(
					b + "*x**" + a,
					((set.getTitle().equals("")) ? "" : "estimation of "
							+ set.getTitle() + "(based on " + set.size()
							+ " points) : ")
							+ formatDouble(b)
							+ "×["
							+ abs.getName()
							+ "]^{"
							+ formatDouble(a) + "}",
					LineStyle.buildApproximation(linesStyles.size()));
			break;
		}
		case EXPONENTIAL: {
			Utils.DataSet dataSetLog = new Utils.DataSet();
			for (StatsEntry s : set.getStats()) {
				double x = s.getFloatValue(abs);
				double y = s.getFloatValue(ord);
				if (x <= 0 || y <= 0)
					continue;
				double weight = x;
				Utils.DataPoint logP = new Utils.DataPoint();
				logP.x = x;
				logP.y = Math.log(y);
				logP.weight = weight;
				dataSetLog.add(logP);
			}
			Utils.AffineRegressionResults affineRLog = Utils
					.affineRegression(dataSetLog);
			double logA = affineRLog.a;
			double a = Math.exp(logA);
			double b = Math.exp(affineRLog.b);
			;
			plotFunc(
					b + "*" + a + "**x",
					((set.getTitle().equals("")) ? "" : "estimation of "
							+ set.getTitle() + "(based on " + set.size()
							+ " points) : ")
							+ formatDouble(b)
							+ "×"
							+ formatDouble(a)
							+ "^{["
							+ abs.getName()
							+ "]}="
							+ formatDouble(b)
							+ "×e^{"
							+ formatDouble(logA) + "×[" + abs.getName() + "]}",
					LineStyle.buildApproximation(linesStyles.size()));
			break;
		}
		}
	}

	public <T extends Comparable<T>> void plotGroup(StatsSet stats, Attribute<T> groupBy, PlotStyle style) {
		Map<T, StatsSet> grouped = stats.sortByAtribute(groupBy);
		List<T> keys = new ArrayList<T>(grouped.keySet());
		Collections.sort(keys);
		for (T key : keys) {
			StringBuilder title = new StringBuilder();
			title.append(" ");
			title.append(groupBy.getName());
			title.append(" = ");
			title.append(key);
			plot(grouped.get(key), style, title.toString());
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSize(Integer width, Integer Height) {
		this.imageWidth = width;
		this.imageHeight = Height;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 
	 * @param forceOrdLogScale
	 *            true to force logarithmic scale, false to force linear scale
	 *            and null to use default scale
	 */
	public void setForceOrdLogScale(Boolean forceOrdLogScale) {
		this.forceOrdLogScale = forceOrdLogScale;
	}

	/**
	 * 
	 * @param forceAbsLogScale
	 *            true to force logarithmic scale, false to force linear scale
	 *            and null to use default scale
	 */
	public void setForceAbsLogScale(Boolean forceAbsLogScale) {
		this.forceAbsLogScale = forceAbsLogScale;
	}

	/**
	 * set the fields which must appear in data description.
	 * 
	 * @param fields
	 */
	public void setDataDescriptionFields(Attribute<?>[] fields) {
		dataDescriptionfields = Arrays.asList(fields);
	}

	/**
	 * you can put null to use gnuplot default
	 * 
	 * @param min
	 * @param max
	 */
	public void forceAbsRange(Number min, Number max) {
		minAbs = (min == null) ? null : min.doubleValue();
		maxAbs = (max == null) ? null : max.doubleValue();
	}

	/**
	 * you can put null to use gnuplot default
	 * 
	 * @param min
	 * @param max
	 */
	public void forceOrdRange(Number min, Number max) {
		minOrd = (min == null) ? null : min.doubleValue();
		maxOrd = (max == null) ? null : max.doubleValue();
	}
	public void setXTics(Number between) {
		xTics = between;
	}

	/**
	 * set the tics values for X axis. if the tics were already defined by
	 * {@link #setXTics(Number)}, those tics will be added to the others.
	 * 
	 * @param values
	 *            the values to be displayed on X axis
	 */
	public void setXTics(Number[] values) {
		xTicsValues = values;
	}

	public void setYTics(Number between) {
		yTics = between;
	}

	/**
	 * set the tics values for Y axis. if the tics were already defined by
	 * {@link #setYTics(Number)}, those tics will be added to the others.
	 * 
	 * @param values
	 *            the values to be displayed on Y axis
	 */
	public void setYTics(Number[] values) {
		yTicsValues = values;
	}



	/**
	 * Sets the style of plotting to use when it is not specified in plotLine
	 * 
	 * This must be set before the calls to plot.
	 * 
	 * @param defaultStyle
	 *            the default style of plotting for this graph.
	 */
	public void setDefaultPlotStyle(PlotStyle defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	public void export() {
		if (plotLines.length() == 0) {
			LogManager.logError("no data to plot" + ((fileName == null) ? "" : "(" + fileName + ")"));
			return;
		}
		if (exportForLatex) {
			int pxCmRatio = 100;
			Integer height = imageHeight;
			Integer width = imageWidth;
			exportForLatex = false;
			imageHeight = (height != null) ? height * pxCmRatio : null;
			imageWidth = (width != null) ? width * pxCmRatio : null;
			export();
			imageHeight = height;
			imageWidth = width;
			exportForLatex = true;
		}

		StringBuilder r = new StringBuilder();
		r.append("set style boxplot fraction 1 nooutliers sorted\n");

		if (exportForLatex) {
			r.append("set terminal epslatex ");
			if (imageHeight != null && imageWidth != null) {
				r.append(" size " + imageWidth + "cm," + imageHeight + "cm");
			}
			r.append("\n");
		} else {
			r.append("set terminal svg ");
			if (imageHeight != null && imageWidth != null) {
				r.append(" size " + imageWidth + "," + imageHeight);
			} else {
				r.append(" size 1200,800 dynamic");
			}
			r.append(" enhanced font \"Sans,12\"\n");
		}

		r.append(keyParameters.toGnuplotLine());

		String name = new String("relationship between " + ord + " and  " + abs);
		r.append("set title \"" + (title == null ? name : title) + "\"\n");

		
		StringBuilder totalFileName = new StringBuilder(
				Options.getStatsGraphDir().getAbsolutePath() + File.separator);
		List<Class<?>> classUsed=new ArrayList<>();
		for (StatsEntry stat:stats.getStats()) {
			Class<?> c=stat.getClass();
			if (!classUsed.contains(c)) {
				classUsed.add(c);
			}
		}
		for (int i=0;i<classUsed.size();i++) {
			Class<?> c=classUsed.get(i);
			totalFileName.append(c.getPackage().getName());
			if (i<classUsed.size()-1)
				totalFileName.append("_");
		}
		totalFileName.append(File.separator);
		if (fileName == null)
			totalFileName.append(name + "(" + stats.hashCode() + ")");
		else if (new File(fileName).isAbsolute())
			totalFileName = new StringBuilder(fileName);
		else
			totalFileName.append(fileName.replace(" ", "_"));
		if (exportForLatex) {
			totalFileName.append(".tex");
		} else
			totalFileName.append(".svg");
		new File(totalFileName.toString()).getParentFile().mkdirs();

		r.append("set output \"" + totalFileName + "\"\n");

		r.append("set xlabel \"" + abs.getName()
				+ (abs.shouldDisplayUnits()
						? " (" + abs.getUnits().getSymbol() + ")"
						: "")
				+ "\"\n");

		r.append("set ylabel \"" + ord.getName()
				+ (ord.shouldDisplayUnits()
						? " (" + ord.getUnits().getSymbol() + ")"
						: "")
				+ "\"\n");

		if (stats.size() > 0 && stats.get(0).get(abs) instanceof String)
			r.append("set xtics rotate by -30\n");
		r.append("set label \"");
		String dataDescription = makeDataDescritption(stats,
				new Attribute<?>[] { ord, abs }).toString();
		r.append(dataDescription.replace("\"", "\\\""));
		int linesNb = dataDescription.split("\\\\n").length;
		r.append("\" at graph 1,"+ (linesNb/24.) + " right\n");

		if (isForArticle && fewData)
			r.append(
					"set label \"DRAFT\" at graph 0.5,0.5 center font \",50\"\n");

		boolean absLogScale = abs.useLogScale();
		if (forceAbsLogScale != null)
			absLogScale = forceAbsLogScale;
		r.append((absLogScale ? "set logscale x" : "unset logscale x") + "\n");
		boolean ordLogScale = ord.useLogScale();
		if (forceOrdLogScale != null)
			ordLogScale = forceOrdLogScale;
		r.append((ordLogScale ? "set logscale y" : "unset logscale y") + "\n");

		r.append("set xrange [" + ((minAbs == null) ? "" : minAbs) + ":" + ((maxAbs == null) ? "" : maxAbs) + "]\n");
		r.append("set yrange [" + ((minOrd == null) ? "" : minOrd) + ":" + ((maxOrd == null) ? "" : maxOrd) + "]\n");
		if (xTics != null)
			r.append("set xtics " + xTics + "\n");
		if (xTicsValues != null) {
			r.append("set xtics ");
			if (xTics != null)
				r.append("add ");
			r.append("(");
			if (xTicsValues.length > 0)
				r.append(xTicsValues[0]);
			for (int i = 1; i < xTicsValues.length; i++)
				r.append(", " + xTicsValues[i]);
			r.append(")\n");
		}
		if (yTics != null)
			r.append("set ytics " + yTics + "\n");
		if (yTicsValues != null) {
			r.append("set ytics ");
			if (yTics != null)
				r.append("add ");
			r.append("(");
			if (yTicsValues.length > 0)
				r.append(yTicsValues[0]);
			for (int i = 1; i < yTicsValues.length; i++)
				r.append(", " + yTicsValues[i]);
			r.append(")\n");
		}

		for (LineStyle ls : linesStyles)
			r.append("set style line " + ls.index + " " + ls.plotLine + "\n");

		r.append("plot " + plotLines.substring(0, plotLines.length() - 2) + "\n");
		GNUPlot.makeGraph(r.toString());
	}

	private StringBuilder makeDataDescritption(StatsSet s, Attribute<?>[] ignoreFields) {
		return makeDataDescritption(s, Arrays.asList(ignoreFields));
	}

	private StringBuilder makeDataDescritption(StatsSet s, List<Attribute<?>> ignoreFields) {
		if (s.size() == 0) {
			return new StringBuilder("No Data");
		}
		StringBuilder r = new StringBuilder();
		String separator = "\\n";
		List<Attribute<?>> fields = (dataDescriptionfields == null) ? Arrays.asList(s.getStats().get(0).getAttributes())
				: dataDescriptionfields;
		for (Attribute<?> a : fields) {
			if (ignoreFields.contains(a) && dataDescriptionfields == null)
				continue;
			if ((!a.isParameter() || a.isVirtualParameter()) && dataDescriptionfields == null)
				continue;
			Comparable<?> min = s.attributeMin(a);
			Comparable<?> max = s.attributeMax(a);
			if (min.equals(max)) {
				r.append(a.getName() + " : " + min + " " + a.getUnits().getSymbol() + separator);
			} else {
				r.append(min.toString() + " $\\\\\\\\leq$ " + a.getName() + " $\\\\\\\\leq$ " + max.toString() + " "
						+ escapeTex(a.getUnits().getSymbol()) + separator);
			}
		}
		return r;
	}

	private Map<String, Integer> labels = new HashMap<>();

	private <T extends Comparable<T>> String getForDatafile(T value) {
		if (value instanceof Integer)
			return value.toString();
		if (value instanceof Float)
			return value.toString();
		if (value instanceof String) {
			Integer index = labels.get(value);
			if (index == null) {
				index = labels.size();
				labels.put((String) value, index);
			}
			return index.toString() + " \"" + ((String) value)
					.replaceAll("\"", "\\\\\"").replaceAll("_", "\\\\\\\\_")
					+ "\"";
		}
		throw new RuntimeException("not implemented");
	}

	private File makeDataFile(StatsSet stats, PlotStyle style) {
		assert style != null;
		if (stats.get(0).get(abs) instanceof String) {
			Map<String, Integer> sortingValues = new HashMap<>();
			for (StatsEntry s : stats.getStats()) {
				String absValue = (String) s.get(abs);
				if (sortingValues.containsKey(absValue))
					continue;
				Integer states = s.get(Attribute.STATE_NUMBER);
				Integer inputs = s.get(Attribute.INPUT_SYMBOLS);
				Integer sortingValue = states * inputs;
				sortingValues.put(absValue, sortingValue);
			}

			List<Integer> sorted = new ArrayList<Integer>(
					sortingValues.values());
			Collections.sort(sorted);
			String[] orderedLabels = new String[sorted.size()];
			for (Entry<String, Integer> entry : sortingValues.entrySet()) {
				for (int i = 0; i < sorted.size(); i++) {
					if (orderedLabels[i] != null)
						continue;
					if (entry.getValue() == sorted.get(i)) {
						orderedLabels[i] = entry.getKey();
						labels.put(entry.getKey(), i);
						break;
					}
				}
			}
		}
		File tempPlot;
		PrintWriter tempWriter;
		try {
			tempPlot = File.createTempFile("simpa_" + ord + "_" + abs + "_", ".dat");
			tempPlot.deleteOnExit();
			toDelete.add(tempPlot);
			tempWriter = new PrintWriter(tempPlot, "UTF-8");
		} catch (IOException ioe) {
			LogManager.logException("unable to create temporary file for gnuplot", ioe);
			return null;
		}
		switch (style) {
		case POINTS:
		case BOXPLOT:
			for (StatsEntry s : stats.getStats()) {
				tempWriter.write(getForDatafile(s.get(abs)) + " "
						+ getForDatafile(s.get(ord)) + "\n");
			}
			break;
		case AVERAGE: {
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys) {
				tempWriter.write(getForDatafile(key) + " "
						+ sorted.get(key).attributeAVG(ord) + "\n");
			}

		}
			break;
		case AVERAGE_WITH_EXTREMA: {
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys) {
				StatsSet entrie = sorted.get(key);
				tempWriter.write(getForDatafile(key) + " "
						+ getForDatafile(entrie.attributeAVG(ord)) + " "
						+ getForDatafile(entrie.attributeMin(ord)) + " "
						+ getForDatafile(entrie.attributeMax(ord)) + "\n");
			}
		}
			break;
		case CANDLESTICK: {
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys) {
				StatsSet entrie = sorted.get(key);
				tempWriter.write(getForDatafile(key) + " "
						+ getForDatafile(entrie.attributeMin(ord)) + " "
						+ getForDatafile(entrie.attributeFirstQuartille(ord))
						+ " " + getForDatafile(entrie.attributeMedian(ord))
						+ " "
						+ getForDatafile(entrie.attributeLastQuartille(ord))
						+ " " + getForDatafile(entrie.attributeMax(ord))
						+ "\n");
			}
		}
			break;
		case MEDIAN: {
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			for (T_ABS key : keys) {
				tempWriter.write(getForDatafile(key) + " "
						+ sorted.get(key).attributeMedian(ord) + "\n");
			}
		}
			break;
		case SMOOTH: {
			Map<T_ABS, StatsSet> sorted = stats.sortByAtribute(abs);
			List<T_ABS> keys = new ArrayList<T_ABS>(sorted.keySet());
			Collections.sort(keys);
			int n = 0;
			String min = "";
			StatsSet tmpSet = new StatsSet();
			for (T_ABS key : keys) {
				for (StatsEntry s : sorted.get(key).getStats()) {
					if (n == 0) {
						min = s.get(abs).toString();
						tmpSet = new StatsSet();
					}
					tmpSet.add(s);
					n++;
					if (n == 5) {
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
		}

		tempWriter.close();
		return tempPlot;
	}

	private String escapeTex(String s) {
		// s = s.replaceAll("\\\\", Matcher.quoteReplacement("\\\\"));
		s = s.replaceAll("%", "\\\\\\\\\\\\\\\\%");// yes, that's a lot of
													// backslash ! They must be
													// doubled for source code
													// in java, doubled for the
													// replaceAll method,
													// doubled for escape in
													// gnuplot
		// System.out.println(s);
		// s = s + "\\\\LaTeX ntest";
		return s;
	}

	public KeyParameters getKeyParameters() {
		return keyParameters;
	}

	public void setKeyParameters(KeyParameters keyPosition) {
		this.keyParameters = keyPosition;
	}
}
