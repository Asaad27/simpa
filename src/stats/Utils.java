/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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

import java.util.ArrayList;

public class Utils {
	static class DataPoint {
		double x;
		double y;
		double weight;
	}

	static class DataSet extends ArrayList<DataPoint> {
		private static final long serialVersionUID = 1L;
	}

	static class AffineRegressionResults {
		double a;
		double b;
		double averageX;
		double averageY;

	}

	static AffineRegressionResults affineRegression(DataSet set) {
		AffineRegressionResults results = new AffineRegressionResults();
		double sumWeights = 0;
		double avgX = 0;
		double avgY = 0;
		double varX = 0;
		double coVar = 0;
		for (DataPoint p : set) {
			double x = p.x;
			double y = p.y;
			double weight = p.weight;
			avgX += x * weight;
			avgY += y * weight;
			varX += x * x * weight;
			coVar += x * y * weight;
			sumWeights += weight;
		}

		varX /= sumWeights;
		coVar /= sumWeights;
		avgX /= sumWeights;
		avgY /= sumWeights;

		varX -= avgX * avgX;
		coVar -= avgX * avgY;
		if (varX == 0)
			results.a = 0;
		else
			results.a = coVar / varX;
		results.b = avgY - results.a * avgX;
		results.averageX = avgX;
		results.averageY = avgY;
		return results;
	}

}
