package datamining.free;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import learner.efsm.table.LiDataTableItem;
import main.simpa.Options;
import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.EFSMTransition;
import automata.efsm.EFSMTransition.Label;
import automata.efsm.Parameter;
import drivers.efsm.EFSMDriver.Types;

public class Classifier {

	private static int iOut = 0;
	
	public static int getIOut() {
		return iOut;
	}
	
	public static TreeNode Classify(String dataFile, int indexClass) {
		try {
			int index_class = 0;
			
			ArffReader reader = new ArffReader(dataFile);
			
			//Get Attributes and enum
			Map<String, Nominal<String>> map_attributes = reader.getMapAttributes();
			
			//Get Attributes List 
			Map<Integer, String> array_attributes = reader.getArrayAttributes();

			//Get Data columns
			List<LinkedList<String>> columns_attributes = reader.getColumnsAttributes();
			
			if (indexClass < 0)
				index_class = reader.getIndexClass();
			
			//Create Col_class
			LinkedList<String> col_class = new LinkedList<String>(columns_attributes.get(index_class));
			
			//ID3
			TreeNode DT = ID3.Run(columns_attributes, col_class, map_attributes, array_attributes, index_class);			
			
			//Optimization
			DT = TreeNode.Optimize(DT);
			
			return DT;
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
		return null;
	}
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
			return "s" + filterSymName(param.value) + "";
		case NUMERIC:
			return param.value;
		case STRING:
			gSymbols.add("s" + filterSymName(param.value));
			return "s" + filterSymName(param.value) + "";
		default:
			LogManager.logException("", new Exception("Undefined type"));
		}			
		return null;
	}
	
	private static String convertInit(Parameter param, Types type) {
		switch (type) {
		case NOMINAL:
			gSymbols.add("s" + filterSymName(param.value));
			return "s" + filterSymName(param.value) + "";
		case NUMERIC:
			return String.valueOf(Integer.MIN_VALUE);
		case STRING:
			gSymbols.add("s" + filterSymName(param.value));
			return "s" + filterSymName(param.value) + "";
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
		File dir = new File(Options.OUTDIR + Options.DIRARFF);
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
				writer.write("@ATTRIBUTE " + Utils.capitalize(paramNames.get(list.get(0).getInput()).get(i))	+ " "
						+ convertTypes(list.get(0).getParamsData(0)
								.getInputParameters().get(i).type) + "\n");
			i=0; currentParamIndex = 0;
			List<Types> realTypes = getTypesOfParams(list);
			for (Map.Entry<String, List<Parameter>> entry : list.get(0).getParamsData(0).getAutomataState().entrySet()) {
				for (int j = 0; j < entry.getValue().size(); j++) {
					writer.write("@ATTRIBUTE saved" + Utils.capitalize(paramNames.get(entry.getKey()).get(j))  +"_" + Math.abs((new SecureRandom().nextLong()))+ " "
							+ convertTypes(realTypes.get(currentParamIndex++)) + "\n");
				}
				i++;
			}
			writer.write("@ATTRIBUTE class NOMINAL {");
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
						line.append(convertValue(p) + ",");
					i = 0;
					for (Parameter p  : dti.getAutomataStateParams()) {
						if (p.type == realTypes.get(i)) line.append(convertValue(p) + ",");
						else line.append(convertInit(p, realTypes.get(i)) + ",");
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
		File dir = new File(Options.OUTDIR + Options.DIRARFF);
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
				writer.write("@ATTRIBUTE "+ Utils.capitalize(paramNames.get(transition.getInput()).get(i))	+ " "
						+ convertTypes(transition.getParamsData(0)
								.getInputParameters().get(i).type) + "\n");
			i = 0;
			List<Types> realTypes = getTypesOfParams(transition);
			
			for (Map.Entry<String, List<Parameter>> entry : transition.getParamsData(0).getAutomataState().entrySet()) {
				for (int j = 0; j < entry.getValue().size(); j++) {
					if (!paramNames.get(entry.getKey()).isEmpty())
						writer.write("@ATTRIBUTE saved" + Utils.capitalize(paramNames.get(entry.getKey()).get(j)) +"_" + Math.abs((new Random().nextLong())) + " "
							+ convertTypes(realTypes.get(i)) + "\n");
					i++;
				}				
			}
			for (i = 0; i < transition.getParamsData(0).getOutputParameters()
					.size(); i++)
				writer.write("@ATTRIBUTE "+ Utils.capitalize(paramNames.get(transition.getOutput()).get(i)) + " "
						+ convertTypes(transition.getParamsData(0).getOutputParameters().get(i).type) + "\n");

			writer.write("@DATA\n");
			for (LiDataTableItem dti : transition.getParamsData()) {
				StringBuffer line = new StringBuffer();
				for (Parameter p : dti.getInputParameters())
					line.append(convertValue(p) + ",");
				i = 0;
				for (Parameter p  : dti.getAutomataStateParams()) {
					if (p.type == realTypes.get(i)) line.append(convertValue(p) + ",");
					else line.append(convertInit(p, realTypes.get(i)) + ",");
					i++;
				}
				for (Parameter p : dti.getOutputParameters())
					line.append(convertValue(p) + ",");
				writer.write(line.substring(0, line.length() - 1) + "\n");
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
	
	public static String filterDoubleLines(String dataFile) {
		File fileNew = null;
		
		fileNew = new File(Options.OUTDIR + File.separator +"arff" +File.separator
				+ filename(dataFile) + "_filtered.arff");
		
		try {
			//Set up writting 
			FileWriter fw = new FileWriter (fileNew);
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter fileOutput = new PrintWriter (bw); 
				 	
			//Set up reading 
			InputStream ips=new FileInputStream(dataFile); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne = "";
			
			//Process
			while (!ligne.toUpperCase().equals("@DATA")) {
				ligne=br.readLine();
				fileOutput.println (ligne);
			}
			ArrayList<String> Lines = new ArrayList<String>();
			
			while ((ligne=br.readLine())!=null) {
				if (!Lines.contains(ligne)) {
					Lines.add(ligne);
					fileOutput.println (ligne);
				}
			}
			
			br.close();
			fileOutput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fileNew.getPath();
	}
	
	private static int rel (List<String> col_a, List<String> col_b) {
		int corr = 0;
		Iterator<String> itr = col_a.iterator();
		Iterator<String> itr2 = col_b.iterator();
		while (itr.hasNext()) {
			String i1 = itr.next();
			String i2 = itr2.next();
			if (i1.equals(i2) && !i1.contains("sinit")) {
				corr++;
			}
		}
		float res = corr;
		res = (res/(float)col_a.size()) *100;
		return (int) res;
	}
	
	private static LinkedList<String> createColRel(LinkedList<String> col_a, LinkedList<String> col_b) {
		LinkedList<String> NewCol = new LinkedList<String>();
		Iterator<String> itr = col_a.iterator();
		Iterator<String> itr2 = col_b.iterator();
		while (itr.hasNext()) {
			if (itr.next().equals(itr2.next()))
				NewCol.add("true");
			else
				NewCol.add("False");
		}
		
		return NewCol;
	}
	
	private static String filename(String dataFile) {
		int index = dataFile.lastIndexOf(File.separatorChar);
		if (index < 0)
			index = 0;
		return dataFile.substring(index, dataFile.length()-5);
	}
	
	public static String addRel(String dataFile) {
		try {
			String NewFile = dataFile;
			ArffReader reader = new ArffReader(dataFile);
			List<LinkedList<String>> columns_attributes = reader.getColumnsAttributes();
			Map<Integer, String> array_attributes = reader.getArrayAttributes();
			List<LinkedList<String>> L_col = new ArrayList<LinkedList<String>>();
			List<String> L_att = new ArrayList<String>();
			
			for (int i = 0; i < columns_attributes.size(); i++) {
				for (int j = 0; j < i; j++) {
					if (rel(columns_attributes.get(i), columns_attributes.get(j)) > Options.SUPPORT_MIN) {
						String att1 = array_attributes.get(i);
						String att2 = array_attributes.get(j);
						String newAtt = new String(att1 +".equals."+ att2);
						LinkedList<String> NewCol = createColRel(columns_attributes.get(i), columns_attributes.get(j));
						if (!L_att.contains(newAtt)) {
							L_col.add(NewCol);
							L_att.add(newAtt);
						}
					}
				}
			}

			NewFile = writeRelForOutput(NewFile, L_att, L_col, array_attributes.size());
			return NewFile;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String filterArff(String dataFile) {
		dataFile = filterDoubleLines(dataFile);
		dataFile = addRel(dataFile);
		return dataFile;
	}
	
	public static String RemoveCol(String dataFile, ArrayList<Integer> nbCol) {
		File fileNew = new File(Options.OUTDIR + File.separatorChar + "arff" +File.separatorChar
				+ filename(dataFile) + "_step1.arff");
		try {
			//Set up writting 
			FileWriter fw = new FileWriter (fileNew);
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter fileOutput = new PrintWriter (bw); 
					 	
			//Set up reading 
			InputStream ips=new FileInputStream(dataFile); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne = "";
			
			while (!ligne.toUpperCase().contains("@ATTRIBUTE")) {
				fileOutput.println (ligne);
				ligne = br.readLine();
			}
			
			int i = 0;
			while (!ligne.toUpperCase().contains("@DATA")) {
				if (!nbCol.contains(i)) {
					fileOutput.println(ligne);
				}
				i++;
				ligne = br.readLine();
			}
			
			fileOutput.println(ligne);
			
			while ((ligne=br.readLine())!=null) {
				String [] line = ligne.split(",");
				String str = "";
				for (i = 0; i < line.length; i++) {
					if (!nbCol.contains(i)) {
						str += line[i];
						str += ",";
					}
				}
				str = str.substring(0, str.length()-1);
				fileOutput.println(str);
			}
			
			br.close();
			fileOutput.close();
			return fileNew.toString();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String setIOut(String dataFile) {
		try {
			ArffReader reader = new ArffReader(dataFile);
			Map<Integer, String> array_attributes = reader.getArrayAttributes();
			
			iOut = array_attributes.size() - 1;
			while (!array_attributes.get(iOut).startsWith("saved")) 
				iOut--;

			return dataFile;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String handleConstantOutput(String dataFile, Label label) {
		try {
			String NewFile = setIOut(dataFile);
			
			ArffReader reader = new ArffReader(dataFile);
			Map<String, Nominal<String>> map_attributes = reader.getMapAttributes();
			Map<Integer, String> array_attributes = reader.getArrayAttributes();
			
			InputStream ips=new FileInputStream(dataFile); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne = "";
			ArrayList<Integer> L_idx = new ArrayList<Integer>();
			
			while (!ligne.toUpperCase().contains("@ATTRIBUTE")) {
				ligne = br.readLine();
			}
			
			int i = 0;
			while (!ligne.toUpperCase().contains("@DATA")) {
				String [] tab_line = ligne.split(" ");
				if(tab_line != null) {
					if (tab_line[0].toUpperCase().contains("@ATTRIBUTE")) {
						String str = tab_line[1];
						if (map_attributes.get(str).size() == 1 && i > iOut) {
							L_idx.add(i);
							label.addVar(array_attributes.get(i) + " = "
									+ map_attributes.get(str).getLinkedList().get(0));
						}
					}
				}
			
				ligne = br.readLine();
				i++;
			}
			br.close();
			

			NewFile = RemoveCol(NewFile, L_idx);
			return NewFile;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String handleDifferentOutput(String dataFile, Label label) {	
		try {
			ArffReader reader = new ArffReader(dataFile);
			Map<String, Nominal<String>> map_attributes = reader.getMapAttributes();
			Map<Integer, String> array_attributes = reader.getArrayAttributes();
			List<LinkedList<String>> columns_attributes = reader.getColumnsAttributes();
			boolean rel_find = false;
			
			Iterator<Map.Entry<Integer, String>> it = array_attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, String> pairs = it.next();
				String [] str = pairs.getValue().split(".equals.");
				if (str.length < 2)
					continue;
				if (str[0].contains("saved") || str [1].contains("saved"))
					continue;
				LinkedList<String> L_class = new LinkedList<String>();
				for(int i = iOut; i < columns_attributes.size(); i++)
					L_class.add(array_attributes.get(i));
				if (L_class.contains(str[0]) || L_class.contains(str[1])) {
					rel_find = true;
					label.addVar(str[0] + " = " + str[1]);
				}
			}
			
			if (rel_find)
				return dataFile;
			
			for(int i  = array_attributes.size() - 1; i > iOut; i--) {
				
				LinkedList<String> col_class = new LinkedList<String>(columns_attributes.get(i));
				TreeNode DT = ID3.Run(columns_attributes, col_class, map_attributes, array_attributes, i);
				DT = TreeNode.Optimize(DT);
			
				String className = array_attributes.get(i);
				LinkedList<String> diffvalues = map_attributes.get(className).getLinkedList();
				
				Iterator<String> itr = diffvalues.iterator();
				while(itr.hasNext()) {
					String value = itr.next();
					for(String pred : DT.getPredicatesFor(value)){
						if (pred != null) {
							label.addVar(pred + " => " + className + " = " + value);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataFile;
	}
	
	private static String writeRelForOutput(String dataFile, List<String> L_att, List<LinkedList<String>> L_col, int nb_att) {
		File fileNew = null;
		
		fileNew = new File(Options.OUTDIR + File.separatorChar+"arff" +File.separatorChar
				+ filename(dataFile) + "_related.arff");
		try {
			//Set up writting 
			FileWriter fw = new FileWriter (fileNew);
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter fileOutput = new PrintWriter (bw); 
					 	
			//Set up reading 
			InputStream ips=new FileInputStream(dataFile); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne = "";
			
			//process
			
			while (!ligne.toUpperCase().contains("@ATTRIBUTE")) {
				ligne = br.readLine();
				fileOutput.println(ligne);
			}
			
			int nb = 1;
			while (nb < nb_att - 1) {
				ligne = br.readLine();
				fileOutput.println(ligne);
				nb++;
			}
			Iterator<String> itr = L_att.iterator();
			while(itr.hasNext()){
				fileOutput.println("@ATTRIBUTE "+itr.next()+" STRING");
			}
			ligne = br.readLine();
			fileOutput.println(ligne);

			ligne = br.readLine();
			fileOutput.println(ligne);
			
			LinkedList<Iterator<String>> it = new LinkedList<Iterator<String>>();
			for (int i = 0; i < L_col.size(); i++) {
				it.add(L_col.get(i).iterator());
			}
			
			
			while ((ligne=br.readLine())!=null) {
				String [] t_str = ligne.split(",");
				String res = "";
				if (t_str.length > 0) {
					for(int j = 0; j < t_str.length - 1; j++) {
						res += t_str[j] + ",";
					}
					
					for (int i = 0; i < it.size(); i++) {
						res += it.get(i).next() + ",";
					}
					
					res += t_str[t_str.length -1];
					fileOutput.println(res);
				}
			}
			
			br.close();
			fileOutput.close();
			return fileNew.toString();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String handleRelatedDataForOutput(String dataFile) {
		try {
			ArffReader reader = new ArffReader(dataFile);
			List<LinkedList<String>> columns_attributes = reader.getColumnsAttributes();
			Map<Integer, String> array_attributes = reader.getArrayAttributes();
		
			int outIndex = array_attributes.size() - 1;

			ArrayList<LinkedList<String>> L_col = new ArrayList<LinkedList<String>>();
			ArrayList<String> L_att = new ArrayList<String>();
			
			while (outIndex > iOut) {
				Map <String, List<List<String>>> allinsts = new TreeMap<String, List<List<String>>>();
				InputStream ips=new FileInputStream(dataFile); 
				InputStreamReader ipsr=new InputStreamReader(ips);
				BufferedReader br=new BufferedReader(ipsr);
				String ligne = "";
				while (!ligne.toUpperCase().contains("@DATA")) {
					ligne = br.readLine();
				}
				
				while ( (ligne=br.readLine()) != null) {
					String [] t_str = ligne.split(",");
					if (t_str.length == 0)
						continue;

					String c = t_str[outIndex];
					List<String> f_str = Arrays.asList(t_str);
					List<String> l_str = new ArrayList<String>();
					for(int i = 0; i < f_str.size(); i++) {
						//if (i != outIndex)
							l_str.add(new String(f_str.get(i)));
					}
					
					List<List<String>> l = allinsts.get(c);
					if (l == null) {
						l = new ArrayList<List<String>>();
						l.add(l_str);
						allinsts.put(c, l);
					}
					else {
						l.add(l_str);
					}
				}
				br.close();
				
				for (String dest : allinsts.keySet()) {
					List<List<String>> l = allinsts.get(dest);
					ArrayList<LinkedList<String>> New = new ArrayList<LinkedList<String>>();
					for(int col = 0; col < l.get(0).size(); col++) {
						New.add(new LinkedList<String>());
						for(int i = 0; i < l.size(); i++) {
							New.get(col).add(l.get(i).get(col));
						}
					}
					
					for (int i = 0; i < New.size(); i++) {
						for (int j = i+1; j < New.size(); j++) {
							if (rel(New.get(i), New.get(j)) > Options.SUPPORT_MIN) {
								String att1 = array_attributes.get(i);
								String att2 = array_attributes.get(j);
								String newAtt = new String(att1 +".equals."+ att2);
								LinkedList<String> NewCol = createColRel(columns_attributes.get(i), columns_attributes.get(j));
								if (!L_att.contains(newAtt)) {
									L_col.add(NewCol);
									L_att.add(newAtt);
									iOut++;
								}
							}
						}
					}
				}
				outIndex--;
			}
			

			return writeRelForOutput(dataFile, L_att, L_col, array_attributes.size());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
