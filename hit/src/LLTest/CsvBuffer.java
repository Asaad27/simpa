package LLTest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import tools.loggers.LogManager;

/**
 * Buffer for the stats of the tests with learnlib
 * @author Laurent
 * 
 */
public class CsvBuffer {
	
	File f;
	BufferedWriter writer;
	String header;
	
	public CsvBuffer(String filename, String header){
		try {
			f = new File(filename);
			f.getAbsoluteFile().getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(f));
			writer.append(header);
			writer.newLine();
		} catch (IOException e) {
			LogManager.logException("Unable to create stats file (" + filename
					+ ")", e);
			writer = null;
		}
	}
	
	public void write(int... data){
		try {
			for(int i : data){
				writer.append(i+",");
			}
			writer.newLine();
		} catch (IOException e) {
			LogManager.logException("Unable to write in stats file ", e);
		}
	}
	
	public void close(){
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LogManager.logException("Unable to write in stats file ", e);
		}
		
	}

}
