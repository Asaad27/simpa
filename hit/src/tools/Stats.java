package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import tools.loggers.LogManager;

public class Stats{
	private BufferedWriter writer = null;
	private int recordCount;
	
	public Stats(String filename){
		try {
			recordCount = 0;
			writer = new BufferedWriter(new FileWriter(new File(filename)));
		} catch (IOException e) {
			LogManager.logException("Unable to create stats file ("+filename+")", e);
			writer = null;
		}			
	}
	
	public void setHeaders(List<String> headers){
		headers.add(0, "TestID");
		addHeaders(headers);
	}
	
	public void addHeaders(List<String> headers){
		if (writer != null){
			try {
				for(int i=0; i<headers.size(); i++){
					writer.write(headers.get(i));
					if (i<headers.size()-1) writer.write(",");					
				}
				writer.write("\n");
			} catch (IOException e) {
				LogManager.logException("Unable to write in stats file", e);
			}
		}
	}
	
	public void addRecord(List<String> records){
		records.add(0, String.valueOf(++recordCount));
		addHeaders(records);
	}
	
	public void close(){
		try {
			if (writer != null){
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
		}
	}
}