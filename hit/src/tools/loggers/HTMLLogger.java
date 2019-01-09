package tools.loggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import learner.efsm.table.LiControlTable;
import learner.efsm.table.LiControlTableItem;
import learner.efsm.table.LiControlTableRow;
import learner.efsm.table.LiDataTable;
import learner.efsm.table.LiDataTableItem;
import learner.efsm.table.LiDataTableRow;
import learner.efsm.table.NBP;
import learner.efsm.table.NDF;
import learner.efsm.table.NDV;
import learner.efsm.tree.ZXObservationNode;
import learner.mealy.table.LmControlTable;
import learner.mealy.table.LmControlTableItem;
import learner.mealy.table.LmControlTableRow;
import learner.mealy.tree.ZObservationNode;
import main.simpa.Options;
import main.simpa.SIMPA;
import tools.Base64;
import tools.Utils;
import automata.State;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import automata.mealy.InputSequence;

public class HTMLLogger implements ILogger {
	private File file;
	private File dir;
	private DateFormat dfm;
	private DateFormat tfm;
	private DateFormat filenameFm;
	private Writer writer = null;
	
	private boolean htmlLogDivOpened = false;

	public HTMLLogger() {
		file = null;
		dir = Options.getLogDir();
		filenameFm = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		dfm = new SimpleDateFormat("MM/dd/yyyy");
		tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");
		try {
			if (!dir.isDirectory()) {
				if (!dir.mkdirs())
					throw new IOException("unable to create "
							+ dir.getAbsolutePath() + " directory");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logControlTable(LiControlTable l) {
		try {
			if (htmlLogDivOpened) {
				writer.write("</div>\n");
				htmlLogDivOpened = false;
			}
			int nbSymbols = l.inputSymbols.size();
			StringBuffer s = new StringBuffer(
					"<li class=\"controltable\">\n<table>\n<tr class=\"header\">\n<td></td>");
			for (int i = 0; i < nbSymbols; i++)
				s.append("<td>" + l.inputSymbols.get(i) + "</td>");
			s.append("\n</tr>\n");
			for (LiControlTableRow ctr : l.S)
				s.append(printRows(ctr, "#F9FFF9"));
			for (LiControlTableRow ctr : l.R)
				s.append(printRows(ctr, "#FFF9F9"));
			s.append("</table>\n</li>");
			writer.write(s.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logDataTable(LiDataTable l) {
		try {
			int nbSymbols = l.inputSymbols.size();
			StringBuffer s = new StringBuffer(
					"<li class=\"datatable\">\n<table>\n<tr class=\"header\">\n<td></td>");
			for (int i = 0; i < nbSymbols; i++)
				s.append("<td>" + l.inputSymbols.get(i) + "</td>");
			s.append("\n</tr>\n");
			for (LiDataTableRow ctr : l.S)
				s.append(printRows(ctr, "#F9FFF9"));
			for (LiDataTableRow ctr : l.R)
				s.append(printRows(ctr, "#FFF9F9"));
			s.append("</table>\n</li><br/>");
			writer.write(s.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logEnd() {
		try {
			writer.flush();
			writer.write("</ul>\n");
			writer.write("</body>\n");
			writer.write("</html>\n");
			writer.flush();
			writer.close();
			if (Options.LOG_HTML && Options.AUTO_OPEN_HTML)
				Utils.browse(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logError(String s) {
		try {
			writer.write("<li class=\"error\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">Error : "
					+ Utils.escapeHTML(s) + "</span>");
			writer.write("</li>\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logException(String m, Exception e) {
	}

	@Override
	public void logInfo(String s) {
		try {
			writer.flush();
			writer.write("<li class=\"info\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + Utils.escapeHTML(s)
					+ "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logWarning(String s) {
		try {
			writer.flush();
			writer.write("<li class=\"warning\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">Warning : "
					+ Utils.escapeHTML(s) + "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(ParameterizedInput pi, ParameterizedOutput po) {
		try {
			writer.flush();
			writer.write("<li class=\"request\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\"><span class=\"pi\">"
					+ Utils.escapeHTML(pi.toString())
					+ "</span> -> <span class=\"po\">"
					+ Utils.escapeHTML(po.toString())
					+ "</span></span>\n</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStart() {
		try {
			file = new File(dir.getAbsolutePath() + File.separator
					+ filenameFm.format(new Date()) + ".html");
			writer = new BufferedWriter(new FileWriter(file));
			writer.flush();
			writer.write("<html>\n");
			writer.write("<head>\n");
			writer.write("<title>" + SIMPA.name + " - " + dfm.format(new Date())
					+ "</title>\n");
			writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n");
			writer.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + new File("log").getAbsolutePath()
					+ File.separator + "style.css\">\n");
			writer.write("<script src=\"" + new File("log").getAbsolutePath()
					+ File.separator + "jquery.min.js\"></script>\n");
			writer.write("<script src=\"" + new File("log").getAbsolutePath()
					+ File.separator + "script.js\"></script>\n");
			writer.write("</head>\n");
			writer.write("<body>\n");
			writer.write("<div id=\"info\">\n");
			writer.write(SIMPA.name + " - " + dfm.format(new Date()));
			writer.write("</div>\n");
			writer.write("<div id=\"controls\">\n" +
					"<p>Show tables: <input type=\"checkbox\" id=\"showTables\" onclick=\"table_event(this)\" /></p>\n" +
					"<p>Show logs: <input type=\"checkbox\" id=\"showLogs\" onclick=\"log_event(this)\" /></p>\n" +
					"<p>Show graphs: <input type=\"checkbox\" id=\"showGraphs\" onclick=\"graph_event(this)\" /></p>\n" +
					"<p>Show data: <input type=\"checkbox\" id=\"showData\" onclick=\"data_event(this)\" /></p>\n" +
					"</div>\n");
			writer.write("<ul>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuffer printRows(LmControlTableRow ctr, String color) {

		StringBuffer s = new StringBuffer("<tr style=\"background-color:"
				+ color + "\">\n<td class=\"header\">" + ctr.getIS().toString()
				+ "</td>");
		for (int i = 0; i < ctr.getColumnCount(); i++) {
			LmControlTableItem acti = ctr.getColumn(i);
			s.append("<td><table class=\"cti\">");
			s.append("<tr>");
			s.append("<td><span class=\"outputsymbol\">"
					+ (acti.isOmegaSymbol() ? Options.SYMBOL_OMEGA_UP : acti
							.getOutputSymbol()) + "</span></td>");
			s.append("</tr>");
			s.append("</table></td>");
		}
		s.append("</tr>");
		return s;
	}

	private StringBuffer printRows(LiControlTableRow ctr, String color) {

		StringBuffer s = new StringBuffer("<tr style=\"background-color:"
				+ color + "\">\n<td class=\"header\">"
				+ ctr.getPIS().toString() + "</td>");
		for (int i = 0; i < ctr.getColumnCount(); i++) {
			ArrayList<LiControlTableItem> acti = ctr.getColumn(i);
			s.append("<td><table class=\"cti\">");
			for (LiControlTableItem cti : acti) {
				s.append("<tr>");
				s.append("<td><span class=\"inputparam\">"
						+ cti.getParameters().toString()
						+ "<span>,</td>"
						+ "<td><span class=\"outputsymbol\">"
						+ (cti.isOmegaSymbol() ? Options.SYMBOL_OMEGA_UP : cti
								.getOutputSymbol()) + "</span></td>");
				s.append("</tr>");
			}
			s.append("</table></td>");
		}
		s.append("</tr>");
		return s;
	}

	private StringBuffer printRows(LiDataTableRow dtr, String color) {
		StringBuffer s = new StringBuffer("<tr style=\"background-color:"
				+ color + "\"><td class=\"header\">" + dtr.getPIS().toString()
				+ "</td>");
		for (int i = 0; i < dtr.getColumnCount(); i++) {
			ArrayList<LiDataTableItem> acti = dtr.getColumn(i);
			s.append("<td><table class=\"dti\">");
			for (LiDataTableItem cti : acti) {
				s.append("<tr>");
				s.append("<td>(<span class=\"inputparam\">"
						+ cti.getInputParameters().toString()
						+ "</span>,</td>"
						+ "<td><span class=\"state\">"
						+ cti.getAutomataState().values().toString()
						+ "</span>) -></td>"
						+ "<td><span class=\"outputparam\">"
						+ (cti.getOutputParameters().isEmpty() ? Options.SYMBOL_OMEGA_LOW
								: cti.getOutputParameters().toString())
						+ "</span></td>");
				s.append("</tr>");
			}
			s.append("</table></td>");
		}
		s.append("</tr>");
		return s;
	}

	@Override
	public void logReset() {
		try {
			writer.flush();
			if (!htmlLogDivOpened) {
				writer.write("<div class=\"testing\">\n");
				htmlLogDivOpened = true;
			}
			writer.write("<li class=\"reset\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">reset</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStat(String s) {
		try {
			writer.flush();
			writer.write("<li class=\"stat\">\n");
			writer.write("<div id=\"stats\">\n");

			writer.write("<span>" + s + "</span>");

			writer.write("</div>\n");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStep(int step, Object o) {
		String s = null;
		try {
			writer.flush();
			switch (step) {
			case LogManager.STEPNDV:
				writer.write("<li class=\"step ndv\">\n");
				s = "NonDeterministicValue : " + NDV.class.cast(o).toString();
				break;
			case LogManager.STEPNBP:
				writer.write("<li class=\"step nbp\">\n");
				s = "NonBalancedParameter : " + NBP.class.cast(o).toString();
				break;
			case LogManager.STEPNCR:
				writer.write("<li class=\"step ncr\">\n");
				if (o instanceof ParameterizedInputSequence)
					s = "NonClosedRow : "
							+ ParameterizedInputSequence.class.cast(o)
									.toString();
				else
					s = "NonClosedRow : "
							+ InputSequence.class.cast(o).toString();
				break;
			case LogManager.STEPNDF:
				writer.write("<li class=\"step ndf\">\n");
				s = "NonDisputedFree : " + NDF.class.cast(o).toString();
				break;
			case LogManager.STEPOTHER:
				writer.write("<li class=\"step stepunknown\">\n");
				s = (String) o;
				break;
			}
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + s + "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logData(String data) {
		try {
			writer.flush();
			writer.write("<li class=\"data\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + data + "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logTransition(String trans) {
		try {
			writer.flush();
			writer.write("<li class=\"transition\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + trans + "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logLine() {
		try {
			writer.flush();
			writer.write("<hr/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logImage(String path) {
		try {
			writer.flush();
			writer.write("<li class=\"image\">\n");
			writer.write("<center><img src=\"data:image/svg+xml;base64,"
					+ Base64.encodeFromFile(path) + "\"/></center>");
			writer.write("</li>\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logConcrete(String data) {
		try {
			writer.flush();
			writer.write("<li class=\"concrete\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span>Concrete :<br/><span class=\"content\">"
					+ Utils.escapeHTML(data) + "<br/></span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logParameters(Map<String, Integer> params) {
		try {
			logSpace();
			StringBuffer s = new StringBuffer(
					"<li class=\"symbols_and_params\">\n<table>\n<tr class=\"header\">\n");
			s.append("<td>Name</td>");
			ArrayList<String> keys = new ArrayList<String>(params.keySet());
			Collections.sort(keys);
			for (String k : keys) {
				s.append("<td>" + k + "</td>");
			}
			s.append("\n</tr><tr class=\"value\">\n");
			s.append("<td>Nb of parameters</td>");
			for (String k : keys) {
				s.append("<td>" + params.get(k) + "</td>");
			}
			s.append("\n</tr>\n");
			s.append("</table>\n</li>");
			writer.write(s.toString());
			logSpace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logSpace() {
		try {
			writer.write("<li>&nbsp;</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(String input, String output) {
		try {
			writer.flush();
			writer.write("<li class=\"request\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + LogManager.getPrefix()
					+ "<span class=\"pi\">"
					+ input + "</span> -&gt <span class=\"po\">"
					+ (output.length() > 0 ? output : Options.SYMBOL_OMEGA_LOW)
					+ "</span></span>\n</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(String input, String output, int n) {
		try {
			writer.flush();
			writer.write("<li class=\"request\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + LogManager.getPrefix()
					+ "transition n°" + n + " : "
					+ "<span class=\"pi\">"
					+ input + "</span> -&gt <span class=\"po\">"
					+ (output.length() > 0 ? output : Options.SYMBOL_OMEGA_LOW)
					+ "</span></span>\n</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(String input, String output, int n, State before,
			State after) {
		try {
			writer.flush();
			writer.write("<li class=\"request\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date())
					+ "</span><span class=\"content\">" + LogManager.getPrefix()
					+ "transition n°" + n + " : (" + before
					+ ") --<span class=\"pi\">" + input
					+ "</span>/<span class=\"po\">"
					+ (output.length() > 0 ? output : Options.SYMBOL_OMEGA_LOW)
					+ "</span>--&gt (" + after + ")</span>\n</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logControlTable(LmControlTable ct) {
		try {
			int nbCols = ct.getColsCount();
			StringBuffer s = new StringBuffer(
					"<li class=\"controltable\">\n<table>\n<tr class=\"header\">\n<td></td>");
			for (int i = 0; i < nbCols; i++)
				s.append("<td>" + ct.getColSuffix(i) + "</td>");
			s.append("\n</tr>\n");
			for (LmControlTableRow ctr : ct.S)
				s.append(printRows(ctr, "#F9FFF9"));
			for (LmControlTableRow ctr : ct.R)
				s.append(printRows(ctr, "#FFF9F9"));
			s.append("</table>\n</li><li>&nbsp;</li>");
			writer.write(s.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logObservationTree(ZObservationNode root) {
		logImage(root.toDot().getAbsolutePath());
	}

	@Override
	public void logXObservationTree(ZXObservationNode root) {
		logImage(root.toDot().getAbsolutePath());		
	}

	@Override
	public void logFatalError(String s) {
		try {
			writer.write("<li>"+s+"</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
