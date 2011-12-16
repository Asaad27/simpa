package tools;

import java.io.File;
import java.io.IOException;

import main.Options;
import tools.loggers.LogManager;

public class GraphViz{
   private final static String DOT = "dot";
   
   public static File dotToFile(String filename)
   {
	   File img = null;
	   if (Options.GRAPHVIZ){      
		   try {
			   File input = new File(filename);
			   img = new File(Utils.changeExtension(filename, "svg"));
			   Runtime rt = Runtime.getRuntime();  
			   String[] args = {DOT, "-Tsvg", input.getAbsolutePath(), "-o", img.getAbsolutePath()};
			   Process p = rt.exec(args);         
			   p.waitFor();         
		   }
		   catch (Exception e) {
			   LogManager.logException("Warning: converting dot to file", e);
			   return null;
		   }
	   }
	   return img;
   }
   
   public static int check(){
	   try {
	         Runtime rt = Runtime.getRuntime();  
	         String[] args = {DOT, "-V"};
	         Process p = rt.exec(args);
	         p.waitFor();
	         return p.exitValue();
	      }
	   	  catch (IOException e) {
	         return 1;
	      }
	      catch (Exception e) {
	         return 2;
	      }
   }
}

