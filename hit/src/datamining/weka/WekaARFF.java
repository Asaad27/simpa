package datamining.weka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import learner.efsm.table.LiDataTableItem;
import main.simpa.Options;
import tools.Utils;
import tools.loggers.LogManager;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnassignedDatasetException;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToNominal;
import automata.Transition;
import automata.efsm.EFSMTransition;
import automata.efsm.EFSMTransition.Label;
import automata.efsm.Parameter;
import drivers.efsm.EFSMDriver.Types;

public class WekaARFF {
	private static int iOut = 0;
	private static List<String> gSymbols = new ArrayList<String>();
	
	public static List<String> getGlobalSymbols(){
		return gSymbols;
	}
	
	private static String convertTypes(Types type) {
		switch (type) {
		case NOMINAL:
			return "STRING";
		case NUMERIC:
			return "NUMERIC";
		case STRING:
			return "STRING";
		default:
			LogManager.logException("", new Exception("Undefined type"));
		}
		return null;
	}

	private static String convertValue(Parameter param) {
		switch (param.type) {
		case NOMINAL:
			gSymbols.add("s" + filterSymName(param.value));
			return "'s" + filterSymName(param.value) + "'";
		case NUMERIC:
			return param.value;
		case STRING:
			gSymbols.add("s" + filterSymName(param.value));
			return "'s" + filterSymName(param.value) + "'";
		default:
			LogManager.logException("", new Exception("Undefined type"));
		}			
		return null;
	}
	
	private static String convertInit(Parameter param, Types type) {
		switch (type) {
		case NOMINAL:
			gSymbols.add("s" + filterSymName(param.value));
			return "'s" + filterSymName(param.value) + "'";
		case NUMERIC:
			return String.valueOf(Integer.MIN_VALUE);
		case STRING:
			gSymbols.add("s" + filterSymName(param.value));
			return "'s" + filterSymName(param.value) + "'";
		default:
			LogManager.logException("", new Exception("Undefined type"));	
		}
		return null;
	}
	
	private static String filterSymName(String name){
		String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String result = "";
		for (int i=0; i<name.length(); i++){
			if (allowed.indexOf(name.charAt(i)) != -1) result += name.charAt(i);
		}
		return result;
	}

	public static String generateFileForPredicate(
			List<EFSMTransition> list, TreeMap<String, List<String>> paramNames) {
		File file = null;
		File dir = Options.getArffDir();
		Writer writer = null;
		int i = 0; int currentParamIndex = 0;
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
					throw new IOException("unable to create " + dir.getName() + " directory");
			file = new File(dir.getAbsolutePath() + File.separator 	+ "from" + list.get(0).getFrom() + ".arff");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("@RELATION 'from" + list.get(0).getFrom() + "'\n");
			for (i = 0; i < list.get(0).getParamsData(0).getInputParameters()
					.size(); i++)
				writer.write("@ATTRIBUTE "
						+ Utils.capitalize(paramNames.get(list.get(0).getInput()).get(i)) 
						+ " "
						+ convertTypes(list.get(0).getParamsData(0)
								.getInputParameters().get(i).type) 
						+ "\n");
			i=0; currentParamIndex = 0;
			List<Types> realTypes = getTypesOfParams(list);
			for (Map.Entry<String, List<Parameter>> entry : list.get(0).getParamsData(0).getAutomataState().entrySet()) {
				for (int j = 0; j < entry.getValue().size(); j++) {
					writer.write("@ATTRIBUTE saved" + Utils.capitalize(paramNames.get(entry.getKey()).get(j))  +"_" + Math.abs((new SecureRandom().nextLong()))+ " "
							+ convertTypes(realTypes.get(currentParamIndex++)) + "\n");
				}
				i++;
			}
			writer.write("@ATTRIBUTE class {");
			Set<String> dests = new HashSet<String>();
			for(EFSMTransition t : list) dests.add(t.getTo() + t.getOutput());
			Object[] destsA = dests.toArray(); 
			writer.write((String)destsA[0]);
			for (i = 1; i < destsA.length; i++) {
				writer.write("," + (String)destsA[i]);
			}
			writer.write("}\n");
			
			writer.write("@DATA\n");
			for (EFSMTransition t : list) {
				for (LiDataTableItem dti : t.getParamsData()) {
					StringBuffer line = new StringBuffer();
					for (Parameter p : dti.getInputParameters())
						line.append(convertValue(p) + ", ");
					i = 0;
					for (Parameter p  : dti.getAutomataStateParams()) {
						if (p.type == realTypes.get(i)) line.append(convertValue(p) + ", ");
						else line.append(convertInit(p, realTypes.get(i)) + ", ");
						i++;
					}
					line.append(t.getTo()+t.getOutput() + "\n");
					writer.write(line.toString());
				}
			}
			writer.flush();
			writer.close();
			LogManager.logTransition("Generate raw datafile : "
					+ file.getName());
			return file.getPath();
		} catch (IndexOutOfBoundsException e){
			LogManager.logException("Error in generating datafile ("+file.getName()+")", e);
			System.exit(0);
		} catch (IOException e) {
			LogManager.logException("Unable to generate arff file", e);
			System.exit(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static List<Types> getTypesOfParams(Object transList) {
		List<EFSMTransition> list = null;
		if (transList instanceof EFSMTransition){
			list = new ArrayList<EFSMTransition>();
			list.add((EFSMTransition)transList);
		}else{
			list = (ArrayList<EFSMTransition>)transList;
		}
		
		List<ArrayList<Parameter>> simplifiedList = new ArrayList<ArrayList<Parameter>>();
		for(EFSMTransition trans : list){
			for (LiDataTableItem dti : trans.getParamsData()){
				simplifiedList.add((ArrayList<Parameter>)dti.getAutomataStateParams());
			}
		}
		
		List<Types> typeList = new ArrayList<Types>();
		
		for (int i=0; i<simplifiedList.get(0).size(); i++){
			Parameter param = simplifiedList.get(0).get(i);
			if (param.isInit()){
				for(int j=1; j<simplifiedList.size(); j++){
					if (!simplifiedList.get(j).get(i).isInit()){
						param = simplifiedList.get(j).get(i);
						break;
					}
				}
			}
			typeList.add(param.type);
		}	
		return typeList;
	}

	public static String generateFileForVar(EFSMTransition transition, TreeMap<String, List<String>> paramNames) {
		File file = null;
		File dir = Options.getArffDir();
		Writer writer = null;
		int i = 0;
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
					throw new IOException("unable to create " + dir.getName()
							+ " directory");
			file = new File(dir.getAbsolutePath() + File.separator
					+ transition.getFrom() + "-" + transition.getTo() + ".arff");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("@RELATION '" + transition.getFrom() + "-" + transition.getTo()
					+ "'\n");
			for (i = 0; i < transition.getParamsData(0).getInputParameters()
					.size(); i++)
				writer.write("@ATTRIBUTE "
						+ Utils.capitalize(paramNames.get(transition.getInput()).get(i))
						+ " "
						+ convertTypes(transition.getParamsData(0)
								.getInputParameters().get(i).type) 
						+ "\n");
			i = 0;
			List<Types> realTypes = getTypesOfParams(transition);
			
			for (Map.Entry<String, List<Parameter>> entry : transition.getParamsData(0).getAutomataState().entrySet()) {
				for (int j = 0; j < entry.getValue().size(); j++) {
					if (paramNames.get(entry.getKey()) != null 
							&& !paramNames.get(entry.getKey()).isEmpty())
						writer.write("@ATTRIBUTE saved" + Utils.capitalize(paramNames.get(entry.getKey()).get(j)) +"_" + Math.abs((new SecureRandom().nextLong())) + " "
							+ convertTypes(realTypes.get(i)) + "\n");
					i++;
				}				
			}
			for (i = 0; i < transition.getParamsData(0).getOutputParameters()
					.size(); i++)
				writer.write("@ATTRIBUTE "
						+ Utils.capitalize(paramNames.get(transition.getOutput()).get(i))
						+ " "
						+ convertTypes(transition.getParamsData(0).getOutputParameters().get(i).type) 
						+ "\n");

			writer.write("@DATA\n");
			for (LiDataTableItem dti : transition.getParamsData()) {
				StringBuffer line = new StringBuffer();
				for (Parameter p : dti.getInputParameters())
					line.append(convertValue(p) + ", ");
				i = 0;
				for (Parameter p  : dti.getAutomataStateParams()) {
					if (p.type == realTypes.get(i)) line.append(convertValue(p) + ", ");
					else line.append(convertInit(p, realTypes.get(i)) + ", ");
					i++;
				}
				for (Parameter p : dti.getOutputParameters())
					line.append(convertValue(p) + ", ");
				writer.write(line.substring(0, line.length() - 2) + "\n");
			}
			writer.flush();
			writer.close();
			LogManager.logTransition("Generate raw datafile : "
					+ file.getName());
			return file.getPath();
		} catch (IOException e) {
			LogManager.logException("Unable to generate arff file", e);
		}
		return null;
	}

	public static String handleConstantOutput(String dataFile, Label label) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			ArrayList<String> constantOutputs = new ArrayList<String>();
			for (int i = data.numAttributes() - 1; i > iOut; i--) {
				if (data.numDistinctValues(i) == 1) {
					if (data.attribute(i).type() == Attribute.NUMERIC)
						label.addVar(data.attribute(i).name() + " = "
								+ data.instance(0).value(i));
					else
						label.addVar(data.attribute(i).name() + " = "
								+ data.instance(0).stringValue(i));
					constantOutputs.add(data.attribute(i).name());
					Remove r = new Remove();
					r.setOptions(weka.core.Utils.splitOptions("-R "
							+ String.valueOf(i + 1)));
					r.setInputFormat(data);
					data = Filter.useFilter(data, r);
				}
			}
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			newFile = new File(
					Options.getArffDir().getAbsolutePath() + File.separatorChar
							+ Utils.removeExtension(dataFile) + "_step1.arff");
			saver.setFile(newFile);
			saver.writeBatch();
			LogManager.logData(constantOutputs.size()
					+ " constant outputs handled : " + constantOutputs);
			return newFile.getPath();
		} catch (FileNotFoundException e) {
			LogManager.logException("No such a file : " + dataFile, e);
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		return dataFile;
	}

	public static String filterFileForVar(String dataFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			int nbAttrFilteredNom = 0;
			int nbAttrFilteredRem = 0;
			
			iOut = data.numAttributes() - 1;
			while (!data.attribute(iOut).name().startsWith("saved")) iOut--;

			for (int i = data.numAttributes() - 1; i >= 0; i--) {
				if (data.attribute(i).isString()) {
					nbAttrFilteredNom++;
					StringToNominal f = new StringToNominal();
					f.setOptions(weka.core.Utils.splitOptions("-R "
							+ String.valueOf(i + 1)));
					f.setInputFormat(data);
					data = Filter.useFilter(data, f);
				}
			}

			for (int i = data.numAttributes() - 1; i >= 0; i--) {
				if (data.numDistinctValues(i) == 1
						&& (data.attribute(i).type() != Attribute.NUMERIC)
						&& data.instance(0).stringValue(i).equals("s"+Parameter.PARAMETER_INIT_VALUE)) {
					nbAttrFilteredRem++;
					iOut--;
					Remove r = new Remove();
					r.setOptions(weka.core.Utils.splitOptions("-R "
							+ String.valueOf(i + 1)));
					r.setInputFormat(data);
					data = Filter.useFilter(data, r);
				}
			}
			
			if (data.numAttributes() > 0) {
				ArffSaver saver = new ArffSaver();
				saver.setInstances(data);
				newFile = new File(Options.getArffDir().getAbsolutePath()
						+ File.separatorChar + Utils.removeExtension(dataFile)
						+ "_filtered.arff");
				saver.setFile(newFile);
				saver.writeBatch();
				LogManager.logData("Filtered attributes : "
						+ nbAttrFilteredNom + " String->Nominal and "
						+ nbAttrFilteredRem + " removed");
				return newFile.getPath();
			}
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		LogManager.logData("Keeping datafile unfiltered");
		return dataFile;
	}

	public static String filterFileForPredicate(String dataFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			int nbAttrFilteredNom = 0;
			int nbAttrFilteredRem = 0;

			for (int i = data.numAttributes() - 1; i >= 0; i--) {
				if (data.attribute(i).isString()) {
					nbAttrFilteredNom++;
					StringToNominal f = new StringToNominal();
					f.setOptions(weka.core.Utils.splitOptions("-R "
							+ String.valueOf(i + 1)));
					f.setInputFormat(data);
					data = Filter.useFilter(data, f);
				}
			}

			for (int i = data.numAttributes() - 2; i >= 0; i--) {
				if (data.numDistinctValues(i) == 1) {
					nbAttrFilteredRem++;
					Remove r = new Remove();
					r.setOptions(weka.core.Utils.splitOptions("-R "
							+ String.valueOf(i + 1)));
					r.setInputFormat(data);
					data = Filter.useFilter(data, r);
				}
			}
			
			if (data.numAttributes() > 0) {
				ArffSaver saver = new ArffSaver();
				saver.setInstances(data);
				newFile = new File(Options.getArffDir().getAbsolutePath()
						+ File.separatorChar + Utils.removeExtension(dataFile)
						+ "_filtered.arff");
				saver.setFile(newFile);
				saver.writeBatch();
				LogManager.logData("Filtered attributes : "
						+ nbAttrFilteredNom + " String->Nominal and "
						+ nbAttrFilteredRem + " removed");
				return newFile.getPath();
			}
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		LogManager.logData("Keeping datafile unfiltered");
		return dataFile;
	}

	public static WekaTreeNode handlePredicate(String dataFile) {
		WekaTreeNode node = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();

			data.setClassIndex(data.attribute("class").index());
			
			List<Instance> listUniqInstance = checkOnlyOneInstance(data);
			if (!listUniqInstance.isEmpty()){
				for(Instance inst : listUniqInstance){
					LogManager.logInfo("Only one instance of " + inst.stringValue(data.classIndex()) + " in " + dataFile);
				}
			}else{
				node = new WekaTreeNode();
				Classifier cl = null;
				try{
					cl = AbstractClassifier.forName("J48", weka.core.Utils.splitOptions("-C 0.25 -M 1"));
					cl.buildClassifier(data);				
					node.loadFromString(cl);
				}catch(StringIndexOutOfBoundsException e){
					try{
						cl = AbstractClassifier.forName("J48", weka.core.Utils.splitOptions("-R -N 3 -Q 1 -M 2"));
						cl.buildClassifier(data);
						node = new WekaTreeNode();
						node.loadFromString(cl);
					}catch(StringIndexOutOfBoundsException f){
						try{
							cl = AbstractClassifier.forName("J48", weka.core.Utils.splitOptions("-R -N 3 -Q 1 -B -M 2"));
							cl.buildClassifier(data);
							node = new WekaTreeNode();
							node.loadFromString(cl);
						}catch(StringIndexOutOfBoundsException g){
							LogManager.logError("Unable to get a tree with data in " + new File(dataFile).getName());
							node = null;
						}
					}
				}
			}

			LogManager.logData(data.numDistinctValues(data.numAttributes() - 1)
					+ " transitions handled");
		} catch (FileNotFoundException e) {
			LogManager.logException("No such a file : " + dataFile, e);
			node = null;
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
			node = null;
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
			node = null;
		}
		return node;
	}

	private static List<Instance> checkOnlyOneInstance(Instances data) {
		List<Instance> res = new ArrayList<Instance>();		
		Map<String, List<Instance>> allinsts = new TreeMap<String, List<Instance>>();
		for(int i=0; i<data.numInstances(); i++){
			String c = data.instance(i).stringValue(data.classIndex());
			List<Instance> l = allinsts.get(c);
			if (l == null){
				l = new ArrayList<Instance>();
				l.add(data.instance(i));
				allinsts.put(c, l);
			}else{
				l.add(data.instance(i));
			}
		}
		for(String dest : allinsts.keySet()){
			if (allinsts.get(dest).size()==1) res.add(allinsts.get(dest).get(0));
		}
		return res;
	}

	public static String handleDifferentOutput(String dataFile, Label label,
			Transition t, boolean forceJ48) {
		WekaTreeNode node = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			List<String> differentOutputs = new ArrayList<String>();
			for (int i = data.numAttributes() - 1; i > iOut; i--) {
				data.setClassIndex(i);
				differentOutputs.add(data.attribute(i).name());

				if (data.attribute(i).type() == Attribute.NUMERIC
						&& !forceJ48) {
					M5P m5 = new M5P();
					m5.setOptions(weka.core.Utils.splitOptions("-M 4.0"));
					m5.buildClassifier(data);

					WekaExpressionTreeNode en = new WekaExpressionTreeNode();
					en.loadFromString(m5);

					label.addVar(data.attribute(i).name() + " = " + en.toString());
				} else {					
					if (data.attribute(i).isNumeric()) {
						NumericToNominal f = new NumericToNominal();
						f.setOptions(weka.core.Utils.splitOptions("-R "
								+ String.valueOf(i + 1)));
						f.setInputFormat(data);
						data = Filter.useFilter(data, f);
					}				
					
					Classifier cl = AbstractClassifier.forName("J48", weka.core.Utils.splitOptions("-C 0.25 -M 2"));
					cl.buildClassifier(data);

					node = new WekaTreeNode();
					try{
						node.loadFromString(cl);

						ArrayList<String> diffValues = new ArrayList<String>();
						for(int j=0; j<data.attribute(i).numValues(); j++){
							if (!diffValues.contains(data.attribute(i).value(j))) diffValues.add(data.attribute(i).value(j));
						}					
						for(String val : diffValues){
							for(String pred : node.getPredicatesFor(val)){
								label.addVar(pred + " => " + data.attribute(i).name() + " = " + val);
							}
						}
					}catch (StringIndexOutOfBoundsException e){
						LogManager.logException("Warning : Unable to find more than one class (" + dataFile +")", e);
					}
				}
				Remove r = new Remove();
				r.setOptions(weka.core.Utils.splitOptions("-R "
						+ String.valueOf(i + 1)));
				r.setInputFormat(data);
				data = Filter.useFilter(data, r);
			}

			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			newFile = new File(
					Options.getArffDir().getAbsolutePath() + File.separatorChar
							+ Utils.removeExtension(dataFile) + "_step2.arff");
			saver.setFile(newFile);
			saver.writeBatch();
			LogManager.logData(differentOutputs.size()
					+ " different outputs handled : " + differentOutputs);
			return newFile.getPath();
		} catch (FileNotFoundException e) {
			LogManager.logException("No such a file : " + dataFile, e);
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		return dataFile;
	}

	public static String handleRelatedDataForPredicate(String dataFile,
			int supportMin) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			int nbEquals = 0;
			int outIndex = data.numAttributes()-1;
			
			Map<String, List<Instance>> allinsts = new TreeMap<String, List<Instance>>();
			for(int i=0; i<data.numInstances(); i++){
				String c = data.instance(i).stringValue(outIndex);
				List<Instance> l = allinsts.get(c);
				if (l == null){
					l = new ArrayList<Instance>();
					l.add(data.instance(i));
					allinsts.put(c, l);
				}else{
					l.add(data.instance(i));
				}
			}
		
			Set<WekaRelation> allRelation = new HashSet<WekaRelation>();
					
			for(String dest : allinsts.keySet()){
				List<Instance> l = allinsts.get(dest);
				allRelation.addAll(WekaRelation.findOn(l, supportMin));
			}
			
			Instances newData = new Instances(data);
			int relCount = 0;
			
			for(WekaRelation re : allRelation){
				switch(re.getType()){
				case EQUALS : nbEquals++;
							break;
				}
				newData.insertAttributeAt(new Attribute("rel" + (relCount++), re.getValues()), newData.numAttributes()-1);
				for (int i=0; i<newData.numInstances(); i++) {
					if (re.isTrueOn(newData.instance(i))) newData.instance(i).setValue(newData.numAttributes()-2, re.toString());
					else newData.instance(i).setValue(newData.numAttributes()-2, re.toNotString());
				}
			}

			if (newData.numAttributes() > 0 && !allRelation.isEmpty()) {
				ArffSaver saver = new ArffSaver();
				saver.setInstances(newData);
				newFile = new File(Options.getArffDir().getAbsolutePath()
						+ File.separatorChar + Utils.removeExtension(dataFile)
						+ "_related.arff");
				saver.setFile(newFile);
				saver.writeBatch();
				LogManager.logData("Relation found : "
						+ nbEquals + " Equals");
				return newFile.getPath();
			}
		} catch (IllegalArgumentException e) {
			LogManager.logException("Error reading instance (IllegalType)", e);
		} catch (UnassignedDatasetException e) {
			LogManager.logException("Error reading instance (No Dataset)", e);
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		return dataFile;
	}
	
	public static String handleRelatedDataForOutput(String dataFile,
			int supportMin) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			Instances data = new Instances(reader);
			reader.close();
			File newFile = null;
			int nbEquals = 0;
			int outIndex = data.numAttributes()-1;
			
			List<WekaRelation> allRelation = new ArrayList<WekaRelation>();
			
			while(outIndex > iOut && !data.attribute(outIndex).isNumeric()){			
				Map<String, List<Instance>> allinsts = new TreeMap<String, List<Instance>>();
				for(int i=0; i<data.numInstances(); i++){
					String c = data.instance(i).stringValue(outIndex);
					List<Instance> l = allinsts.get(c);
					if (l == null){
						l = new ArrayList<Instance>();
						l.add(data.instance(i));
						allinsts.put(c, l);
					}else{
						l.add(data.instance(i));
					}
				}			
						
				for(String dest : allinsts.keySet()){
					List<Instance> l = allinsts.get(dest);
					allRelation.addAll(WekaRelation.findOn(l, supportMin));
				}	
				
				outIndex--;
			}
			
			Instances newData = new Instances(data);
			int relCount = 0;
			
			for(WekaRelation re : allRelation){
				switch(re.getType()){
				case EQUALS : nbEquals++;
							break;
				}
				newData.insertAttributeAt(new Attribute("rel" + (relCount++), re.getValues()), newData.numAttributes()-1);
				for (int i=0; i<newData.numInstances(); i++) {
					if (re.isTrueOn(newData.instance(i))) newData.instance(i).setValue(newData.numAttributes()-2, re.toString());
					else newData.instance(i).setValue(newData.numAttributes()-2, re.toNotString());
				}
			}
			
			iOut += allRelation.size();

			if (newData.numAttributes() > 0 && !allRelation.isEmpty()) {
				ArffSaver saver = new ArffSaver();
				saver.setInstances(newData);
				newFile = new File(Options.getArffDir().getAbsolutePath()
						+ File.separatorChar + Utils.removeExtension(dataFile)
						+ "_related.arff");
				saver.setFile(newFile);
				saver.writeBatch();
				LogManager.logData("Relation found : "
						+ nbEquals + " Equals");
				return newFile.getPath();
			}
		} catch (IllegalArgumentException e) {
			LogManager.logException("Error reading instance (IllegalType)", e);
		} catch (UnassignedDatasetException e) {
			LogManager.logException("Error reading instance (No Dataset)", e);
		} catch (IOException e) {
			LogManager.logException("Error reading arff file ("+dataFile+")", e);
		} catch (Exception e) {
			LogManager.logException("Unknown error in processing file ("+dataFile+")", e);
		}
		return dataFile;
	}
}
