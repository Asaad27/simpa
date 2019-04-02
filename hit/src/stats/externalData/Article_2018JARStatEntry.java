/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package stats.externalData;

import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;

/**
 * From https://link.springer.com/article/10.1007/s10817-018-9486-0#Sec21
 * 
 * doi : https://doi.org/10.1007/s10817-018-9486-0
 * 
 * @author Nicolas BREMOND
 *
 */
public class Article_2018JARStatEntry extends StatsEntry {
	static public final String emqttDot = "dot_file(BenchmarkMQTT_emqtt__two_client_will_retain)";
	static public final String hbmqttDot = "dot_file(BenchmarkMQTT_hbmqtt__two_client_will_retain)";
	static public final String mosquittoDot = "dot_file(BenchmarkMQTT_mosquitto__two_client_will_retain)";
	static public final String vernemqDot = "dot_file(BenchmarkMQTT_VerneMQ__two_client_will_retain)";
	static public final String mutationOracle = "Mutation";

	public static StatsSet getSet() {
		StatsSet set = new StatsSet();
		set = new StatsSet();
		set.add(new Article_2018JARStatEntry(emqttDot, mutationOracle, 175, 253,
				11058, 1647, 13655));
		set.add(new Article_2018JARStatEntry(hbmqttDot, mutationOracle, 200,
				255, 10986, 1067, 9116));
		set.add(new Article_2018JARStatEntry(mosquittoDot, mutationOracle, 150,
				187, 8201, 1309, 10686));
		set.add(new Article_2018JARStatEntry(vernemqDot, mutationOracle, 125,
				183, 7983, 1295, 10410));
		return set;
	}

	private Article_2018JARStatEntry(String automata, String oracle, int nsel,
			int test_eq, int step_eq, int test_mem, int step_mem) {
		super();
		this.automata = automata;
		this.oracle = oracle;
		this.nsel = nsel;
		this.test_eq = test_eq;
		this.step_eq = step_eq;
		this.test_mem = test_mem;
		this.step_mem = step_mem;
	}

	final String automata;
	final String oracle;
	final int nsel;
	final int test_eq;
	final int step_eq;
	final int test_mem;
	final int step_mem;

	@Override
	protected Attribute<?>[] getAttributesIntern() {
		return new Attribute<?>[] { Attribute.TRACE_LENGTH,
				Attribute.RESET_CALL_NB, Attribute.AUTOMATA,
				Attribute.ORACLE_USED };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == Attribute.AUTOMATA) {
			return (T) automata;
		}
		if (a == Attribute.RESET_CALL_NB) {
			return (T) Integer.valueOf(test_mem + test_eq - nsel);
		}
		if (a == Attribute.TRACE_LENGTH) {
			Integer lastOracleSteps = step_eq * nsel / test_eq;
			return (T) Integer.valueOf(step_eq + step_mem - lastOracleSteps);
		}
		if (a == Attribute.ORACLE_RESET_NB)
			return (T) Integer.valueOf(test_eq - nsel);
		if (a == Attribute.ORACLE_USED)
			return (T) oracle;
		throw new RuntimeException("invalid attribute " + a);
	}

	@Override
	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == Attribute.ORACLE_USED || a == Attribute.AUTOMATA)
			throw new RuntimeException();
		return ((Integer) getStaticAttribute(a)).floatValue();
	}

	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new GraphGenerator() {
			@Override
			public void generate(StatsSet s) {
			}
		};
	}

}
