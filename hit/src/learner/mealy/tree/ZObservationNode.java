/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import learner.mealy.Node;
import main.simpa.Options;
import tools.GraphViz;
import tools.loggers.LogManager;

public class ZObservationNode extends Node<ZObservationNode> {
	public int state = -1;
	public int label = -1;

	public ZObservationNode() {
		super();
	}

	public ZObservationNode(String input, String output) {
		super(input, output);
	}

	public boolean isState() {
		return state > -1;
	}

	public boolean isLabelled() {
		return label > -1;
	}

	public boolean isInitial() {
		return state == 0 && input == null;
	}

	private void toDotCreateNodes(Writer w) throws IOException {
		if (state == -1)
			w.write("    node" + id
					+ " [style=\"rounded,filled\", fillcolor=\"#" + "FFFFFF"
					+ "\", color=\"#" + (isLabelled() ? "FF6666" : "666666")
					+ "\", shape=record, label=\"{" + label + "}\"]\n");
		else
			w.write("    node" + id
					+ " [style=\"rounded,filled\", fillcolor=\"#" + "E0FEEE"
					+ "\", color=\"#666666" + "\", shape=record, label=\"{"
					+ state + " | " + label + "}\"]\n");
		for (ZObservationNode n : children.values()) {
			n.toDotCreateNodes(w);
		}
	}

	public File toDot() {
		Writer writer = null;
		File file = null;
		File imagePath = null;
		File dir = Options.getDotDir();
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
						+ " directory");

			file = new File(dir.getPath() + File.separatorChar + "tmp" + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			toDotCreateNodes(writer);
			toDotWrite(writer);
			writer.write("}\n");
			writer.close();
			imagePath = GraphViz.dotToFile(file.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
		return imagePath;
	}

	@Override
	public String toString() {
		return "[" + id + ", " + state + ", " + label + "]";
	}

	@Override
	protected ZObservationNode thisAsT() {
		return this;
	}
}
