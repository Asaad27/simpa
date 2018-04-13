package stats;

import stats.Graph.Color;
import stats.Graph.PlotStyle;
import stats.Graph.PointShape;
import stats.attribute.restriction.Restriction;

/**
 * This class aims to store the configuration of one type of curve to make it
 * visually identifiable when plotted on several graphs (otherwise color and
 * points shapes are dependent of the other curves logged)
 * 
 * @author Nicolas BREMOND
 *
 */
public class OneConfigPlotter {
	private final Restriction[] restrictions;
	private final PointShape pointShape;
	private String title;
	private final Color color;
	private PlotStyle plotStyle;

	/**
	 * create one configuration of plot.
	 * 
	 * @param restrictions
	 *            the restriction to apply on StatsSet before plotting
	 * @param pointShape
	 *            the wanted shape of points (can be null but this is
	 *            discouraged as it may lead to different representations of
	 *            curves)
	 * @param color
	 *            the wanted color (can be null but this is discouraged as it
	 *            may lead to different representations of curves)
	 * @param title
	 *            the default title for plotting.
	 */
	public OneConfigPlotter(Restriction[] restrictions, PointShape pointShape,
			Color color, String title) {
		super();
		this.restrictions = restrictions;
		this.pointShape = pointShape;
		this.title = title;
		this.color = color;
		if (color == null || pointShape == null)
			System.out.println(
					"Warning : one config plotter is not fully parameterized and apparence may change between two plot."
							+ "Please provide color and shape in constructor to avoid this message.");
	}

	public void setPlotStyle(PlotStyle plotStyle) {
		this.plotStyle = plotStyle;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void plotOn(Graph<?, ?> g, StatsSet set) {
		StatsSet s = new StatsSet(set);
		s.restrict(restrictions);
		g.plot(s, plotStyle, title, pointShape, color);
	}

	public void plotOn(Graph<?, ?> g, StatsSet set, String title) {
		StatsSet s = new StatsSet(set);
		s.restrict(restrictions);
		g.plot(s, plotStyle, title, pointShape, color);
	}

	public void plotOn(Graph<?, ?> g, StatsSet set, String title,
			PlotStyle plotStyle) {
		StatsSet s = new StatsSet(set);
		s.restrict(restrictions);
		g.plot(s, plotStyle, title, pointShape, color);
	}
}