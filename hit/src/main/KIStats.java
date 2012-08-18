package main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.Utils;
import tools.loggers.LogManager;

public class KIStats {
	public final static String name = "KIStat";
	
	private static void welcome() {
		System.out.println(name + " - " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();		
	}

	public static void main(String[] args) throws IOException {
		Options.STAT = true;
		welcome();
		String dir = Options.OUTDIR;
		
		Options.NBTEST = 50;
		
	try{
			for (int i=0; i<=100; i+= 10){
				Options.NDVGUARDPERCENT = i;
				KITestEFSM.main(args);

				System.out.println(i + "," + Utils.meanOfCSVField(Options.DIRTEST + File.separator + "stats.csv", 7));
				System.out.println(i + "," + Utils.percentOfCSVField(Options.DIRTEST + File.separator + "stats.csv", 9, "1"));
				Options.OUTDIR = dir;
				Utils.copyFile(new File(Options.DIRTEST + File.separator + "stats.csv"), new File("basic=" + i + ".csv"));
			}

		}catch(Exception e){
			LogManager.logException("Unexpected error at test", e);
		}		
	}
}
