package stats;


public class LineStyle {
	public static LineStyle APPROXIMATION = new LineStyle("dashtype 2 linewidth 2 linecolor \"red\"");
	public static LineStyle BOUND = new LineStyle("dashtype 4 linewidth 2 linecolor \"green\"");
	
	private static int lineStylesNb = 0;
	public final int index = ++lineStylesNb;
	public final String plotLine;
	
	public LineStyle(String plotLine){
		this.plotLine = plotLine;
	}
}