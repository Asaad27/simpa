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
package stats.table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import stats.StatsSet;
import tools.Utils;

public class Table {
	File baseFile;
	StatsSet stats;
	List<TableColumn> columns;
	List<TableRow> rows;
	Integer rotateHeader = 90;
	public String topCell = "";

	public Table(File file, StatsSet stats, List<TableColumn> columns,
			List<TableRow> rows) {
		this.baseFile = file;
		this.stats = stats;
		this.columns = columns;
		this.rows = rows;
	}

	public static String defaultFormat(String in, TableOutputFormat format) {
		switch (format) {
		case HTML:
			return Utils.escapeHTML(in);
		case LATEX:
			return Table.stringToLatex(in);
		}
		throw new RuntimeException("not implemented");
	}

	public static String comment(String in, TableOutputFormat format) {
		switch (format) {
		case HTML:
			return "<!--" + defaultFormat(in, format) + "-->";
		case LATEX:
			return "%" + in.replace("\n", "\n%");
		}
		throw new RuntimeException("not implemented");
	}

	public void setRotateHeader(Integer rot) {
		rotateHeader = rot;
	}

	private List<List<TableCell>> createCells(TableOutputFormat format) {
		List<List<TableCell>> cells = new ArrayList<>();
		List<TableCell> currentRow = new ArrayList<>();
		TableCell cell = new TableCell();
		cell.setHeader(true);
		cell.setFormatedContent(defaultFormat(topCell, format));
		currentRow.add(cell);
		for (TableColumn col : columns) {
			cell = new TableCell();
			cell.setHeader(true);
			if (col != null)
				cell.setFormatedContent(col.getFormatedTitle(format));
			currentRow.add(cell);
		}
		cells.add(currentRow);
		for (TableRow row : rows) {
			currentRow = new ArrayList<>();
			StatsSet automaton = row.getData(stats);
			if (automaton.size() == 0)
				continue;
			cell = new TableCell();
			cell.setHeader(true);
			cell.setFormatedContent(row.getFormattedHeader(format));
			currentRow.add(cell);
			for (TableColumn c : columns) {
				cell = new TableCell();
				currentRow.add(cell);
				if (c == null)
					continue;
				StatsSet cellStats = c.restrict(automaton);
				int nb = cellStats.size();
				if (nb == 0)
					cell.setFormatedContent(defaultFormat("-", format));
				else {
					cell.setFormatedContent(
							c.getFormatedData(cellStats, format));
				}
			}
			cells.add(currentRow);
		}
		return cells;
	}

	public void export(TableOutputFormat format) {
		File file = baseFile;
		switch (format) {
		case HTML:
			file = new File(file.toString() + ".html");
			break;
		case LATEX:
			file = new File(file.toString() + ".tex");
			break;
		}
		PrintStream out = null;
		try {
			file.getParentFile().mkdirs();
			out = new PrintStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (out == null)
			out = System.out;
		List<List<TableCell>> cells = createCells(format);
		out.println(comment("File automatically generated with SIMPA.\n"
				+ "Hand-made changes might be overriten.", format));

		switch (format) {
		case HTML:
			out.println("<table>");
			for (List<TableCell> currentRow : cells) {
				out.println("\t<tr>");
				for (TableCell cell : currentRow) {
					out.print("\t\t");
					if (cell.isHeader())
						out.print("<th>");
					else
						out.print("<td>");
					out.print(cell.getFormatedContent());
					if (cell.isHeader())
						out.print("</th>");
					else
						out.print("</td>");
					out.println();
				}
				out.println("\t</tr>");
			}
			out.println("</table>");
			break;
		case LATEX:
			out.println();
			out.print("\\begin{tabular}{|");
			for (int i = 0; i < cells.get(0).size(); i++)
				if (i != 0 && columns.get(i - 1) == null)
					out.print('|');
				else
				out.print("l|");
			out.println("}");
			out.println("\\hline");
			boolean addhline = false;
			for (List<TableCell> currentRow : cells) {
				int pos = 0;
				addhline = false;
				for (TableCell cell : currentRow) {
					if (pos != 0 && columns.get(pos - 1) == null) {
						pos++;
						continue;
					}
					if (pos != 0) {
						out.print("& ");
						if (cell.isHeader())
							addhline = true;
					}
					if (pos != 0 && cell.isHeader() && rotateHeader != null)
						out.println("\\rotatebox{" + rotateHeader + "}{"
								+ cell.getFormatedContent() + "}");
					else
						out.println(cell.getFormatedContent());
					pos++;
				}
				out.println("\\\\");
				if (addhline)
					out.println("\\hline");
			}
			if (!addhline)
				out.println("\\hline");

			out.println("\\end{tabular}");
			break;
		}

		out.close();

	}

	public static String stringToLatex(String str) {
		str = str.replaceAll("_", "\\\\_").replaceAll("#", "\\\\#")
				.replaceAll("≥", "\\$\\\\geq\\$")
				.replaceAll("≤", "\\$\\\\leq\\$");
		if (str.contains("\n"))
			str = "\\shortstack{" + str.replaceAll("\n", "\\\\\\\\ \\\\relax ")
					+ "}";
		return str;
	}
}
