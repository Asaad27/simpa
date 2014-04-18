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
import java.util.List;
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
import learner.efsm.tree.XObservationNode;
import learner.mealy.table.LmControlTable;
import learner.mealy.table.LmControlTableItem;
import learner.mealy.table.LmControlTableRow;
import learner.mealy.tree.ObservationNode;
import main.simpa.Options;
import main.simpa.SIMPA;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import automata.mealy.InputSequence;

public class TextLogger implements ILogger {
	private File file;
	private File dir;
	private DateFormat tfm;
	private DateFormat dfm;
	private DateFormat filenameFm;
	private Writer writer = null;
	private String ret = null;

	public TextLogger() {
		ret = System.getProperty("line.separator");
		dir = new File(Options.OUTDIR + "log");
		filenameFm = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");
		dfm = new SimpleDateFormat("MM/dd/yyyy");
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
	
	public void logFatalError(String s) {
		System.err.flush();
		System.err.println("[!] "+ s);
		System.exit(1);
	}

	public void logControlTable(LiControlTable l) {
		try {
			int nbSymbols = l.inputSymbols.size();
			ArrayList<Integer> width = new ArrayList<Integer>();
			for (int i = 0; i < nbSymbols + 1; i++)
				width.add(0);
			final List<LiControlTableRow> allRows = l.getAllRows();
			for (LiControlTableRow ctr : allRows) {
				width.set(0, Math.max(ctr.getPIS().toString().length(),
						width.get(0)));
				for (int i = 0; i < nbSymbols; i++) {
					int maxwidth = 0;
					for (LiControlTableItem cti : ctr.getColum(i)) {
						maxwidth = Math.max(cti.toString().length(), maxwidth);
					}
					if (maxwidth > width.get(i + 1))
						width.set(i + 1, maxwidth);
				}
			}
			StringBuffer s = new StringBuffer(ret
					+ printLine(width, l.inputSymbols.size()));
			s.append(tfm.format(new Date()) + "|" + pad("", width.get(0)) + "|");
			for (int i = 0; i < nbSymbols; i++)
				s.append(pad(l.inputSymbols.get(i), width.get(i + 1)) + "|");
			s.append(ret + printLine(width, nbSymbols));
			for (LiControlTableRow ctr : l.S)
				s.append(printRows(ctr, width));
			s.append(printLine(width, nbSymbols));
			for (LiControlTableRow ctr : l.R)
				s.append(printRows(ctr, width));
			s.append(printLine(width, nbSymbols));
			writer.write(s.toString() + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logControlTable(LmControlTable l) {
		try {
			int nbCols = l.getColsCount();
			ArrayList<Integer> width = new ArrayList<Integer>();
			for (int i = 0; i < nbCols + 1; i++)
				width.add(0);
			final List<LmControlTableRow> allRows = l.getAllRows();
			for (int i = 0; i < nbCols; i++) {
				int suffixLength = l.getColSuffix(i).toString().length();
				if (width.get(i + 1) < suffixLength)
					width.set(i + 1, suffixLength);
			}
			for (LmControlTableRow ctr : allRows) {
				width.set(0,
						Math.max(ctr.getIS().toString().length(), width.get(0)));
				for (int i = 0; i < nbCols; i++) {
					int maxwidth = ctr.getColumn(i).getOutputSymbol().length();
					if (maxwidth > width.get(i + 1))
						width.set(i + 1, maxwidth);
				}
			}
			StringBuffer s = new StringBuffer(ret + printLine(width, nbCols));
			s.append(tfm.format(new Date()) + "|" + pad("", width.get(0)) + "|");
			for (int i = 0; i < nbCols; i++)
				s.append(pad(l.getColSuffix(i).toString(), width.get(i + 1))
						+ "|");
			s.append(ret + printLine(width, nbCols));
			for (LmControlTableRow ctr : l.S)
				s.append(printRows(ctr, width));
			s.append(printLine(width, nbCols));
			for (LmControlTableRow ctr : l.R)
				s.append(printRows(ctr, width));
			s.append(printLine(width, nbCols));
			writer.write(s.toString() + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logDataTable(LiDataTable l) {
		try {
			int nbSymbols = l.inputSymbols.size();
			ArrayList<Integer> width = new ArrayList<Integer>();
			for (int i = 0; i < nbSymbols + 1; i++)
				width.add(0);
			final List<LiDataTableRow> allRows = l.getAllRows();
			for (LiDataTableRow dtr : allRows) {
				width.set(0, Math.max(dtr.getPIS().toString().length(),
						width.get(0)));
				for (int i = 0; i < nbSymbols; i++) {
					int maxwidth = 0;
					for (LiDataTableItem dti : dtr.getColum(i)) {
						maxwidth = Math.max(dti.toString().length(), maxwidth);
					}
					if (maxwidth > width.get(i + 1))
						width.set(i + 1, maxwidth);
				}
			}
			StringBuffer s = new StringBuffer(printLine(width, nbSymbols));
			s.append(tfm.format(new Date()) + "|" + pad(" ", width.get(0))
					+ "|");
			for (int i = 0; i < nbSymbols; i++)
				s.append(pad(l.inputSymbols.get(i), width.get(i + 1)) + "|");
			s.append(ret);
			s.append(printLine(width, nbSymbols));
			for (LiDataTableRow dtr : l.S)
				s.append(printRows(dtr, width));
			s.append(printLine(width, nbSymbols));
			for (LiDataTableRow dtr : l.R)
				s.append(printRows(dtr, width));
			s.append(printLine(width, nbSymbols));
			writer.write(s.toString() + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logEnd() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logError(String s) {
	}

	@Override
	public void logException(String m, Exception e) {
	}

	@Override
	public void logInfo(String s) {
		try {
			writer.write(tfm.format(new Date()) + s + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(ParameterizedInput pi, ParameterizedOutput po) {
		try {
			writer.write(tfm.format(new Date()) + pi + " -> " + po + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStart() {
		try {
			file = new File(dir.getAbsolutePath() + File.separator
					+ filenameFm.format(new Date()) + "_" + Options.SYSTEM
					+ ".txt");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(SIMPA.name + " - " + dfm.format(new Date()) + " - "
					+ Options.SYSTEM + ret + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuffer pad(String s, int size) {
		StringBuffer res = new StringBuffer();
		int before = size - s.length() + 1;
		for (int i = 0; i < before; i++)
			res.append(" ");
		return res.append(s + " ");
	}

	private StringBuffer printLine(ArrayList<Integer> width, int NbSymbols) {
		StringBuffer s = new StringBuffer(tfm.format(new Date()) + "|");
		for (int i = 0; i < NbSymbols + 1; i++) {
			for (int j = 0; j < width.get(i) + 2; j++)
				s.append("-");
		}
		return s.append("--|" + ret);
	}

	private StringBuffer printRows(LmControlTableRow ctr,
			ArrayList<Integer> width) {
		int height = 0;
		StringBuffer s = new StringBuffer(tfm.format(new Date()) + "|"
				+ pad(ctr.getIS().toString(), width.get(0)) + "|");
		for (int i = 0; i < ctr.getColumCount(); i++) {
			LmControlTableItem acti = ctr.getColumn(i);
			s.append(pad(acti.toString(), width.get(i + 1)) + "|");
			height = 1;
		}
		s.append(ret);
		for (int i = 1; i < height; i++) {
			s.append(tfm.format(new Date()) + "|" + pad("", width.get(0)) + "|");
			for (int j = 0; j < ctr.getColumCount(); j++) {
				s.append(pad("", width.get(j + 1)) + "|");
			}
			s.append(ret);
		}
		return s;
	}

	private StringBuffer printRows(LiControlTableRow ctr,
			ArrayList<Integer> width) {
		int height = 0;
		StringBuffer s = new StringBuffer(tfm.format(new Date()) + "|"
				+ pad(ctr.getPIS().toString(), width.get(0)) + "|");
		for (int i = 0; i < ctr.getColumCount(); i++) {
			ArrayList<LiControlTableItem> acti = ctr.getColum(i);
			if (acti.size() > 0)
				s.append(pad(acti.get(0).toString(), width.get(i + 1)) + "|");
			else
				s.append(pad("", width.get(i + 1)) + "|");
			height = Math.max(height, acti.size());
		}
		s.append(ret);
		for (int i = 1; i < height; i++) {
			s.append(tfm.format(new Date()) + "|" + pad("", width.get(0)) + "|");
			for (int j = 0; j < ctr.getColumCount(); j++) {
				ArrayList<LiControlTableItem> acti = ctr.getColum(j);
				if (acti.size() > i)
					s.append(pad(acti.get(i).toString(), width.get(j + 1))
							+ "|");
				else
					s.append(pad("", width.get(j + 1)) + "|");
			}
			s.append(ret);
		}
		return s;
	}

	private StringBuffer printRows(LiDataTableRow dtr, ArrayList<Integer> width) {
		int height = 0;
		StringBuffer s = new StringBuffer(tfm.format(new Date()) + "|"
				+ pad(dtr.getPIS().toString(), width.get(0)) + "|");
		for (int i = 0; i < dtr.getColumCount(); i++) {
			ArrayList<LiDataTableItem> adti = dtr.getColum(i);
			if (adti.size() > 0)
				s.append(pad(adti.get(0).toString(), width.get(i + 1)) + "|");
			else
				s.append(pad("", width.get(i + 1)) + "|");
			height = Math.max(height, adti.size());
		}
		s.append(ret);
		for (int i = 1; i < height; i++) {
			s.append(tfm.format(new Date()) + "|" + pad("", width.get(0)) + "|");
			for (int j = 0; j < dtr.getColumCount(); j++) {
				ArrayList<LiDataTableItem> adti = dtr.getColum(j);
				if (adti.size() > i)
					s.append(pad(adti.get(i).toString(), width.get(j + 1))
							+ "|");
				else
					s.append(pad("", width.get(j + 1)) + "|");
			}
			s.append(ret);
		}
		return s;
	}

	@Override
	public void logReset() {
		try {
			writer.write(tfm.format(new Date()) + "reset" + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStat(String s) {
		try {
			writer.write(ret + tfm.format(new Date()) + s);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStep(int step, Object o) {
		String s = null;
		switch (step) {
		case LogManager.STEPNDV:
			s = "NDV : " + NDV.class.cast(o).toString();
			break;
		case LogManager.STEPNBP:
			s = "NBP : " + NBP.class.cast(o).toString();
			break;
		case LogManager.STEPNCR:
			if (o instanceof ParameterizedInputSequence)
				s = "NCR : "
						+ ParameterizedInputSequence.class.cast(o).toString();
			else
				s = "NCR : " + InputSequence.class.cast(o).toString();
			break;
		case LogManager.STEPNDF:
			s = "NDF : " + NDF.class.cast(o).toString();
			break;
		case LogManager.STEPOTHER:
			s = (String) o;
			break;
		}
		try {
			writer.write(tfm.format(new Date()) + s + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logData(String data) {
		try {
			writer.write(tfm.format(new Date()) + "        " + data + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logTransition(String trans) {
		try {
			writer.write(tfm.format(new Date()) + trans + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logLine() {
		try {
			writer.write(ret
					+ "-----------------------------------------------------------------------"
					+ ret + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logImage(String path) {
		try {
			writer.write(tfm.format(new Date()) + "See image here : " + path
					+ ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ASCII art ? :)
	}

	@Override
	public void logConcrete(String data) {
		try {
			writer.write(tfm.format(new Date()) + "Concrete : " + ret
					+ "------------" + ret + data + ret + "------------" + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logParameters(Map<String, Integer> params) {
		try {
			writer.write(tfm.format(new Date()) + "Symbols and parameters :"
					+ ret + ret);
			ArrayList<String> keys = new ArrayList<String>(params.keySet());
			Collections.sort(keys);
			for (String k : keys) {
				writer.write(tfm.format(new Date()) + "Symbol " + k + "has "
						+ params.get(k) + " parameters" + ret);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRequest(String input, String ouput) {
		try {
			writer.write(tfm.format(new Date()) + input + " -> " + ouput + ret);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logObservationTree(ObservationNode root) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logXObservationTree(XObservationNode root) {
		// TODO Auto-generated method stub
		
	}

}
