package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import tools.loggers.LogManager;

public class Stats{
	private BufferedWriter writer = null;
	
	public Stats(String filename){
		try {
			writer = new BufferedWriter(new FileWriter(new File(filename)));
		} catch (IOException e) {
			LogManager.logException("Unable to create stats file ("+filename+")", e);
			writer = null;
		}			
	}
	
	public void setHeader(String... headers){
		addRecord(headers);
	}
	
	public void addRecord(String... records){
		if (writer != null){
			try {
				for(int i=0; i<records.length; i++){
					writer.write(records[i]);
					if (i<records.length-1) writer.write(",");					
				}
				writer.write("\n");
			} catch (IOException e) {
				LogManager.logException("Unable to write in stats file", e);
			}
		}
	}
	
	public void addRecord(List<String> records){
		if (writer != null){
			try {
				for(int i=0; i<records.size(); i++){
					writer.write(records.get(i));
					if (i<records.size()-1) writer.write(",");					
				}
				writer.write("\n");
			} catch (IOException e) {
				LogManager.logException("Unable to write in stats file", e);
			}
		}
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