package learner.efsm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import main.Options;
import tools.ASLanEntity;
import tools.GraphViz;
import tools.Utils;
import tools.loggers.LogManager;
import weka.ARFF;
import weka.TreeNode;
import automata.State;
import automata.efsm.EFSMTransition;
import automata.efsm.EFSMTransition.Label;
import drivers.Driver;
import drivers.efsm.EFSMDriver;

public class LiConjecture extends automata.efsm.EFSM {
	private static final long serialVersionUID = 6592229434557819171L;

	private List<String> inputSymbols;
	private TreeMap<String, List<String>> paramNames;
	private	Map<String, Label> labels;
	public List<String> gSymbols;
	
	public LiConjecture(Driver d) {
		super(d.getSystemName());
		this.inputSymbols = d.getInputSymbols();
		this.paramNames = ((EFSMDriver)d).getParameterNames();
		if (paramNames == null){
			LogManager.logException("No parameter names defined", new Exception());
			System.exit(0);
		}
	}
	
	public List<String> getParamNames(String inputSymbol){
		return paramNames.get(inputSymbol);
	}
	
	public List<String> getInputSymbols(){
		return inputSymbols;
	}
	
	public Label getLabelForTransition(EFSMTransition t){
		return labels.get(t.toString());
	}
		
	public void exportToRawDot(){
		LogManager.logConsole("Exporting raw conjecture");
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {			
			if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("unable to create "+ dir.getName() +" directory");

			file = new File(dir.getPath() + File.separatorChar + name + ".raw.dot");
			writer = new BufferedWriter(new FileWriter(file));
            writer.write("digraph G {\n");
            for (EFSMTransition t : getTransitions()){
            	writer.write("\t" + t.toRawDot() + "\n");
            }
            writer.write("}\n");
            writer.close();
            LogManager.logInfo("Raw conjecture have been exported to " + file.getName());
            File imagePath = GraphViz.dotToFile(file.getPath());
            if (imagePath!= null) LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
            LogManager.logException("Error writing dot file", e);
        }		
	}
	
	public void fillVar(EFSMTransition t, Label label){
		String dataFile = ARFF.generateFileForVar(t, paramNames);
		dataFile = ARFF.filterFileForVar(dataFile);
		dataFile = ARFF.handleConstantOutput(dataFile, label);
		dataFile = ARFF.handleRelatedDataForOutput(dataFile);
		dataFile = ARFF.handleDifferentOutput(dataFile, label, t);
	}
	
	private void fillPredicate(List<EFSMTransition> list, Map<String, Label> labels) {
		if (list.size()>1){
			String dataFile = ARFF.generateFileForPredicate(list, paramNames);
			dataFile = ARFF.filterFileForPredicate(dataFile);
			dataFile = ARFF.handleRelatedDataForPredicate(dataFile);
			TreeNode node = ARFF.handlePredicate(dataFile);
			if(node != null){
				for(EFSMTransition t : list){
					for(String pred : node.getPredicatesFor(t.getTo()+t.getOutput())){
						labels.get(t.toString()).addPredicate(pred);
					}
				}
			}
		}
	}

	public void exportToDot(){
		LogManager.logConsole("Cleaning and exporting the final conjecture");
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		if (Options.WEKA){
			LogManager.logInfo("Exporting final conjecture to file");
			try {
				if (Utils.createDir(dir)){
					file = new File(dir.getPath() + File.separatorChar + name + ".dot");
					writer = new BufferedWriter(new FileWriter(file));
					writer.write("digraph G {\n");            
            
					labels = new HashMap<String, Label>();
					Label newLabel = null;

					for (EFSMTransition t : getTransitions()){
						newLabel = t.initializeLabel(paramNames);            	
						fillVar(t, newLabel);
						labels.put(t.toString(), newLabel);
					}

					for (State s : states){
						for(String input : inputSymbols){
							fillPredicate(getTransitionFromWithInput(s, input), labels);        
						}
					}
					gSymbols = ARFF.getGlobalSymbols();

					for (EFSMTransition t : getTransitions()){
						writer.write("\t" + t.getFrom() + " -> " + t.getTo() + "[label=\"" + labels.get(t.toString()) + "\"];" + "\n");            	
					}
					writer.write("}\n");
					if (writer != null) writer.close();
					LogManager.logInfo("Conjecture have been exported to " + file.getName());
					File imagePath = GraphViz.dotToFile(file.getPath());
					if (imagePath!= null) LogManager.logImage(imagePath.getPath());
				}else
					LogManager.logError("unable to create "+ dir.getName() +" directory");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}			
	}
	
	public static void serialize(LiConjecture o, String filename) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(Options.DIRGRAPH + File.separator + filename);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e){
			LogManager.logException("Error serializing generated EFSM", e);
		}
	}
	
	public static LiConjecture deserialize(String filename){
		Object o = null;
		File f = new File(Options.DIRGRAPH + File.separator + filename);
		LogManager.logStep(LogManager.STEPOTHER, "Loading LiConjecture from " + f.getName());
		try {
			FileInputStream fis = new FileInputStream(f.getAbsolutePath());
			ObjectInputStream ois= new ObjectInputStream(fis);
			o = ois.readObject();
			ois.close();
			fis.close();
			}catch (Exception e) {
				LogManager.logException("Error deserializing generated EFSM", e);
			}
		return (LiConjecture)o;
	}

	public void exportToAslan() {
		LogManager.logConsole("Converting conjecture to ASLan++");
		
		ASLanEntity entity = new ASLanEntity(name);
		entity.loadFromEFSM(this);
		
		serialize(this, "saved_efsm");
		
		Writer writer = null;
		File file = null;
		try {
			File dir = new File(Options.OUTDIR + Options.DIRASLAN);		
			if (Utils.createDir(dir)){
				file = new File(dir.getPath() + File.separatorChar + name.replace(" ", "_").toUpperCase() + ".aslan++");				
				writer = new BufferedWriter(new FileWriter(file));				
				writer.write(entity.toString());								
				if (writer != null) writer.close();
				LogManager.logInfo("Conjecture have been exported to " + file.getName());
			}else
				LogManager.logError("unable to create "+ dir.getName() +" directory");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
