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


public class LineStyle {
	public static LineStyle APPROXIMATION = new LineStyle("dashtype 2 linewidth 2 linecolor \"red\"");
	public static LineStyle BOUND = new LineStyle("dashtype 4 linewidth 2 linecolor \"green\"");
	
	private static int lineStylesNb = 0;
	public final int index = ++lineStylesNb;
	public final String plotLine;
	
	public LineStyle(String plotLine){
		this.plotLine = plotLine;
	}
	
	public static LineStyle buildApproximation(int approximationIndex){
		return new LineStyle("dashtype "+(2+approximationIndex)+" linewidth 2 linecolor \"red\"");
	}
}