package main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.Stats;
import tools.Utils;
import tools.loggers.LogManager;

public class SIMPAStats {
	public final static String name = "KIStat";
	
	private static void welcome() {
		System.out.println(name + " - " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();		
	}

	public static void main(String[] args) throws IOException {
		Options.STAT = true;
		welcome();
		String dir = Options.OUTDIR;
		

	try{
			Stats stat = new Stats("global.csv");
			stat.setHeaders(Utils.createArrayList("State", "Requests", "Duration"));
			for (int i=18; i<=18; i++){
				Options.MINSTATES = i;
				Options.MAXSTATES = i;
				
				System.out.println("State = " + i);
				
				SIMPATestMealy.main(args);

				stat.addRecord(Utils.createArrayList(String.valueOf(i),
						String.valueOf(Utils.meanOfCSVField(Options.DIRTEST + File.separator + "stats.csv", 5)),
						String.valueOf(Utils.meanOfCSVField(Options.DIRTEST + File.separator + "stats.csv", 6))));
							
				Options.OUTDIR = dir;
			}

		}catch(Exception e){
			LogManager.logException("Unexpected error at test", e);
		}		
	}
}
