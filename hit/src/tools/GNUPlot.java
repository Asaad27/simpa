package tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tools.loggers.LogManager;

public class GNUPlot{
   private static String GNUPLOT = "gnuplot";
   
   public static void makeGraph(String instructions)
   {    		
	   try {
		   Runtime rt = Runtime.getRuntime();
		   Process p = rt.exec(GNUPLOT);
		   p.getOutputStream().write(instructions.getBytes());
		   p.getOutputStream().close();
		    try {
	            final BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(p.getErrorStream()));
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                System.out.println(line);
	            }
	            reader.close();
	        } catch (final Exception e) {
	            e.printStackTrace();
	        }
		   p.waitFor();         
	   }
	   catch (Exception e) {
		   LogManager.logException("Warning: creating GNUPlot graph", e);
		   return;
	   }
   }
}

