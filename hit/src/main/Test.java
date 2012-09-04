package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import learner.efsm.LiConjecture;
import tools.ASLanEntity;
import tools.Utils;
import tools.loggers.LogManager;

public class Test {

	public static void main(String[] args) {
		LiConjecture c = LiConjecture.deserialize("saved_efsm");
		ASLanEntity e = new ASLanEntity("bob");
		e.loadFromEFSM(c);
		Writer writer = null;
		File file = null;
		try {
			File dir = new File(Options.OUTDIR + File.separator + Options.DIRASLAN);		
			if (Utils.createDir(dir)){
				file = new File(dir.getPath() + File.separator + "bob" + ".aslan++");				
				writer = new BufferedWriter(new FileWriter(file));				
				writer.write(e.toString());								
				if (writer != null) writer.close();
				LogManager.logInfo("Conjecture have been exported to " + file.getName());
			}else
				LogManager.logError("unable to create "+ dir.getName() +" directory");
		} catch (IOException d) {
			d.printStackTrace();
		}		
    }

}
