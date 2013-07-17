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

import main.Options;
import tools.Base64;
import tools.Utils;

public class HTMLLogger implements ILogger {
	private File file;
	private File dir;
	private DateFormat dfm;
	private DateFormat tfm;
	private DateFormat filenameFm;
	private Writer writer = null;
	
	public HTMLLogger() {
		file = null;
		dir = new File(Options.OUTDIR + "log");
		filenameFm = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		dfm = new SimpleDateFormat("MM/dd/yyyy");
		tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");
		try {
			if (!dir.isDirectory()) {
				if (!dir.mkdirs())
					throw new IOException("unable to create " + dir.getAbsolutePath()
							+ " directory");
			}
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
				if (Options.LOG && Options.OPEN_LOG) Utils.browse(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logError(String s) {}

	@Override
	public void logException(String m, Exception e) {}

	@Override
	public void logInfo(String s) {
		try {
			writer.flush();
			writer.write("<li class=\"info\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date()) + "</span><span class=\"content\">" + Utils.escapeHTML(s) + "</span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logStart() {
		try {
			file = new File(dir.getAbsolutePath() + File.separator
					+ filenameFm.format(new Date()) + "_" +  Options.INPUT + ".html");
			writer = new BufferedWriter(new FileWriter(file));
			writer.flush();
			writer.write("<html>\n");
			writer.write("<head>\n");
			writer.write("<title>" + Options.NAME + " - " + dfm.format(new Date()) + " - " + Options.INPUT + "</title>\n");
			writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n");
			writer.write("<style type=\"text/css\">\n");
			writer.write(Utils.fileContentOf(new File("log").getAbsolutePath() + File.separator + "style.css"));
			writer.write("</style>\n");
			writer.write("</head>\n");
			writer.write("<body>\n");
			writer.write("<div id=\"info\">\n");
			writer.write(Options.NAME + " - " + dfm.format(new Date()) + " - " +  Options.INPUT);
			writer.write("</div>\n");
			writer.write("<ul>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logReset() {
		try {
			writer.flush();
			writer.write("<li class=\"reset\">\n");
			writer.write("<span class=\"date\">" + tfm.format(new Date()) + "</span><span class=\"content\">reset</span>");
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

			writer.write("<span>" +s +"</span>");
			
			writer.write("</div>\n");
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
			writer.write("<span class=\"date\">" + tfm.format(new Date()) + "</span><span class=\"content\">" + data + "</span>");
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
			writer.write("<span class=\"date\">" + tfm.format(new Date()) + "</span><span class=\"content\">" + trans + "</span>");
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
			writer.write("<center><img src=\"data:image/svg+xml;base64," + Base64.encodeFromFile(path) + "\"/></center>");
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
			writer.write("<span class=\"date\">" + tfm.format(new Date()) + "</span>Concrete :<br/><span class=\"content\">" + Utils.escapeHTML(data) + "<br/></span>");
			writer.write("</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logParameters(Map<String, Integer> params) {
		try {
			logSpace();
			StringBuffer s = new StringBuffer("<li class=\"symbols_and_params\">\n<table>\n<tr class=\"header\">\n");			
			s.append("<td>Name</td>");
			ArrayList<String> keys = new ArrayList<String>(params.keySet());
			Collections.sort(keys);
			for(String k : keys){
				s.append("<td>"+k+"</td>");
			}
			s.append("\n</tr><tr class=\"value\">\n");
			s.append("<td>Nb of parameters</td>");
			for(String k : keys){
				s.append("<td>"+params.get(k)+"</td>");
			}
			s.append("\n</tr>\n");			
			s.append("</table>\n</li>");
			writer.write(s.toString());
			logSpace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void logSpace(){
		try {
			writer.write("<li>&nbsp;</li>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
