package tools;

import java.io.File;
import java.io.IOException;

import main.Options;
import tools.loggers.LogManager;

public class GraphViz{
   private static String DOT = "C:\\Users\\max\\Downloads\\graphviz-2.30.1\\release\\bin\\dot";
   
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
   
	public static int check() {
		Runtime rt = Runtime.getRuntime();
		Process p;
		try {
			String[] args = { DOT, "-V" };
			p = rt.exec(args);
			p.waitFor();
			return p.exitValue();
		} catch (IOException e) {
			try {
				DOT = Utils.exec("which dot");
				String[] newargs = { DOT, "-V" };
	            if (DOT != null){
	            	p = rt.exec(newargs);
	    			p.waitFor();
	    			return p.exitValue();
	            }
	            return 1;
			}catch (Exception f) {
				return 1;
			}
		} catch (Exception e) {
			return 2;
		}
	}
}

