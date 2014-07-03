package learner.mealy.alpha;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import tools.loggers.LogManager;
import drivers.Driver;
import drivers.mealy.MealyDriver;
import automata.Automata;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import learner.Learner;

public class AlphaLearner extends Learner{
	
	public MealyDriver driver;
	public CharTable currentCharTable;
	public CharTable fullCharTable;
	public List<InputSequence> z;
	public InputSequence traceI;
	public OutputSequence traceO;
	public DAGLabelling treeLabelling;
	public FSMConjecture conjecture;
	public List<String> inputs;
	
	public AlphaLearner(Driver driver,List<InputSequence> Z) {
		this.driver = (MealyDriver) driver;
		currentCharTable = new CharTable();
		fullCharTable = new CharTable();
		z=Z;
		traceI =new InputSequence();
		traceO = new OutputSequence();
		inputs = driver.getInputSymbols();
		conjecture = new FSMConjecture(inputs);
		treeLabelling = new DAGLabelling(null);
	}

	@Override
	public Automata createConjecture() {
		FSMConjecture conj = treeLabelling.mapDepthLabels.get(treeLabelling.mapDepthLabels.size()-1).get(0).conjecture;
		StateLabel initialState = treeLabelling.mapDepthLabels.get(treeLabelling.mapDepthLabels.size()-1).get(0).localLabelling.get(0);
		Mealy c =conj.convertToMealy(initialState) ;
		LogManager.logInfo("Conjecture have " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		for (MealyTransition t : c.getTransitions())
			LogManager.logTransition(t.toString());
		LogManager.logLine();
		
		System.out.print("      states : " + c.getStateCount() + "\r");
		System.out.flush();

		c.exportToDot();

		return c;
	}

	@Override
	public void learn() {

		driver.reset();
		System.out.println(driver.getAutomaton().getName());
		System.out.println("****Preliminary Loop********");
		try {
			executePreliminaryLoop();
		} catch (InconsistencyException e) {
			e.printStackTrace();
		}
		System.out.println("****End of Preliminary Loop********");
		InputSequence alpha1 = z.get(0);

		int oldTraceLength = 0;
		int k = 0;
		while (k < 30) {
			k++;

			System.out.println("beginning of a new round");
			System.out.println("treeLabelling :" + treeLabelling.mapToString());
			oldTraceLength = traceI.getLength();
			// Mealy result = (Mealy) createConjecture();
			List<DAGNode> currentNodeList = treeLabelling.mapDepthLabels
					.get(traceI.getLength());
			Set<StateLabel> possibleLabels = new HashSet<>();
			for (DAGNode pnode : currentNodeList) {
				possibleLabels
						.add(pnode.localLabelling.get(traceI.getLength()));
			}
			System.out.println("possible labels :" + printLabelling(possibleLabels));
			if (possibleLabels.size() == 1) {
				StateLabel uniqueLabel = possibleLabels.iterator().next();
				if (uniqueLabel.isQuestionMark) {
					System.out.println("apply alpha1 because question mark");
					executeInputSequence(alpha1);
				} else {
					
					System.out.println(treeLabelling.mapDepthLabels);
					System.out.println(treeLabelling.mapDepthLabels.size() - 1);
					String in = treeLabelling.mapDepthLabels.get(
							treeLabelling.mapDepthLabels.size() - 1).get(0).conjecture
							.firstInputUnknownFrom(uniqueLabel);
					System.out.println("after firstIU");
					System.out.println(conjecture);
					if (in != null) {
						executeSingleInput(in);
					} else {
						String transfert = treeLabelling.mapDepthLabels.get(
								treeLabelling.mapDepthLabels.size() - 1).get(0).conjecture
								.transfertToNearestUncertainty(uniqueLabel);
						if (transfert == null){
							System.out.println("verifyIfAllTransitionsHaveBeenChecked");
							InputSequence trIS = verifyIfAllTransitionsHaveBeenChecked(treeLabelling.mapDepthLabels.get(
								treeLabelling.mapDepthLabels.size() - 1).get(0).localLabelling, treeLabelling.mapDepthLabels.get(
								treeLabelling.mapDepthLabels.size() - 1).get(0).conjecture, uniqueLabel);
							System.out.println("trIS :"+trIS);
							if(trIS==null){
								break;
							}
							executeInputSequence(trIS);
						}
						else{
							executeSingleInput(transfert);
						}
							
						
					}

				}
			} else {
				boolean containsAQuestionMark =false;
				for(StateLabel pLabel : possibleLabels){
					if(pLabel.isQuestionMark){
						containsAQuestionMark=true;
					}
				}
				System.out.println("contains a question mark :"+containsAQuestionMark);
				if(containsAQuestionMark){
					executeInputSequence(alpha1);
				}
				else{
					//TODO find the best ds
					System.out.println(currentCharTable);
					InputSequence bestSeq = findMostDistinguishingSequence(possibleLabels);
					System.out.println(bestSeq);
					executeInputSequence(bestSeq);
				}
			}
			try {
				updateTree(traceI.getLength() - oldTraceLength);
			} catch (InconsistencyException e) {
				e.printStackTrace();
				treeLabelling.rebuildMapDepthLabels();
				System.out.println("learn on labelling :"+printLabelling(e.localLabelling));
				sureSplitStates(e.index,e.localLabelling);

			}
		}

		Mealy result = (Mealy) createConjecture();
		System.out.println(currentCharTable);

	}
	
	private InputSequence findMostDistinguishingSequence(Set<StateLabel> possibleLabels) {
		Set<InputSequence> pool = new HashSet<>();
		for(StateLabel plabel : possibleLabels){
			pool.addAll(currentCharTable.map.get(plabel).keySet());
		}
		Map<InputSequence,Set<OutputSequence>> inoutmap = new HashMap<>();
		for(InputSequence seq : pool){
			inoutmap.put(seq, new HashSet<OutputSequence>());
			for(StateLabel plabel : possibleLabels){
				if(currentCharTable.map.get(plabel).containsKey(seq)){
					inoutmap.get(seq).add(currentCharTable.map.get(plabel).get(seq));
				}
			}
		}
		int distinguished =0;
		InputSequence ds =null;
		for(Entry<InputSequence,Set<OutputSequence>> entry : inoutmap.entrySet()){
			if(entry.getValue().size()>distinguished){
				ds = entry.getKey().clone();
				distinguished=entry.getValue().size();
			}
		}
		
		
		return ds;
	}

	private void sureSplitStates(int inconsistencyIndex,List<StateLabel> labelling){
		try{
			splitStates(inconsistencyIndex,labelling);
		}
		catch(InconsistencyException e){
			System.out.println("here");
			sureSplitStates(e.index,e.localLabelling);
		}
	}
	
	private void splitStates(int inconsistencyIndex,List<StateLabel> labelling) throws InconsistencyException {
		System.out.println("traceI :"+traceI);
		System.out.println("traceO :"+traceO);
		IndexPair incompatiblePair = findFirstIncompatiblePairWithSameLabel(inconsistencyIndex,labelling);
		System.out.println("incompatible pair :"+incompatiblePair.first+", "+incompatiblePair.second);
		InputSequence ds = new InputSequence();
		int i =0;
		while(traceI.subSequence(incompatiblePair.first+i, incompatiblePair.first+i+1).equals(traceI.subSequence(incompatiblePair.second+i, incompatiblePair.second+i+1)) 
				&& traceO.subSequence(incompatiblePair.first+i, incompatiblePair.first+i+1).equals(traceO.subSequence(incompatiblePair.second+i, incompatiblePair.second+i+1))){
			ds.addInputSequence(traceI.subSequence(incompatiblePair.first+i, incompatiblePair.first+i+1));
			i++;
		}
		ds.addInputSequence(traceI.subSequence(incompatiblePair.first+i, incompatiblePair.first+i+1));
		i++;
		System.out.println("DS :"+ds);
		z.add(ds);
		currentCharTable.distinguishingSet.add(ds);
		StateLabel X = labelling.get(incompatiblePair.first);
		StateLabel X0 = new StateLabel(X, 0);
		StateLabel X1 = new StateLabel(X, 1);
		System.out.println("X :"+X.name);
		System.out.println(currentCharTable);
		Map<InputSequence,OutputSequence> old= currentCharTable.map.remove(X);
		Map<InputSequence,OutputSequence> map0 = new HashMap<InputSequence,OutputSequence>();
		map0.putAll(old);
		map0.put(ds.clone(), traceO.subSequence(incompatiblePair.first,incompatiblePair.first+i));
		currentCharTable.map.put(X0, map0);
		
		Map<InputSequence,OutputSequence> map1 = new HashMap<InputSequence,OutputSequence>();
		map1.putAll(old);
		map1.put(ds.clone(), traceO.subSequence(incompatiblePair.second,incompatiblePair.second+i));
		currentCharTable.map.put(X1, map1);
		System.out.println(currentCharTable);
		
		
		//delete all labels, relabel
		treeLabelling = new DAGLabelling(null);
		
		updateTree(traceI.getLength());
		
		
		
	}


	
	private void updateTree(int newInputsLength) throws InconsistencyException {
		System.out.println("");
		System.out.println("traceI :"+traceI);
		System.out.println("---------debut updateTree----------");
		int begin = traceI.getLength()-newInputsLength;
		InputSequence alpha1 = z.get(0);
		
		
		for(int i=begin;i<=traceI.getLength();i++){ //from where I stopped labelling to the end of the trace
			System.out.println(" ");
			System.out.println("int i = "+i);
			List<DAGNode> universe = new LinkedList<>();
			if(i == 0){
				universe.add(treeLabelling.root);
			}
			else{
				universe.addAll(treeLabelling.mapDepthLabels.get(i-1));
			}
			
			
			
			
			
			//bidouillage degueulasse
			boolean oneTime=true;
			
			//treat each possible way in universe
			for(DAGNode node : universe){
				if(oneTime){
					if(node.successors.isEmpty()){
						List<DAGNode> l = new LinkedList<>();
						treeLabelling.mapDepthLabels.add(i, l);
					}
					oneTime=false;
				}
				List<StateLabel> currentLabel = new LinkedList<>();
				
				
				//compute a list of impossible labels
				List<StateLabel> impossibleLabels = new LinkedList<>();
				for(InputSequence ds : currentCharTable.distinguishingSet){
					for(int j=1;j<=ds.getLength();j++){
						if(traceI.isFollowedBy(i, ds.clone().subSequence(0, j))){
							OutputSequence os = traceO.subSequence(i, i+j);
							for(Entry<StateLabel, Map<InputSequence, OutputSequence>> bentry : currentCharTable.map.entrySet()){
								for(Entry<InputSequence, OutputSequence> sentry : bentry.getValue().entrySet()){
									if(ds.equals(sentry.getKey())&&!os.equals(sentry.getValue().subSequence(0, j))){
										impossibleLabels.add(bentry.getKey());
									}
								}
							}
						}
					}
				}
				//System.out.println("impossible labels :"+ impossibleLabels.toString());
				//first step determine the possible labels
				if(traceI.isFollowedBy(i, alpha1.clone())){
					List<StateLabel> labelList = currentCharTable.getStatesforAlpha(alpha1, traceO.subSequence(i, i+alpha1.getLength()));
					if(labelList.size()==0){
						StateLabel label = new StateLabel(false);
						currentCharTable.map.put(label,new HashMap<InputSequence,OutputSequence>());
						currentCharTable.map.get(label).put(alpha1, traceO.subSequence(i, i+alpha1.getLength()));
						currentLabel.add(label);
					}else if(labelList.size()==1){
						
						
						currentLabel.add(labelList.get(0));
					}
					else {//case where several states can answer the same output for alpha1
						currentLabel.addAll(labelList);
					}
					
				}
				else{
					currentLabel.add(new StateLabel(true));
					
					
					
					
				}
				//second step use these labels
				
					//System.out.println("mdl size :"+treeLabelling.mapDepthLabels.size());
					//System.out.println(treeLabelling.mapToString());
					//if(i>=treeLabelling.mapDepthLabels.size()-1||i==0){
						if(node.successors.isEmpty()){
							Iterator<StateLabel> iterCur = currentLabel.iterator();
							while(iterCur.hasNext()){
								StateLabel label = iterCur.next();
								System.out.println("");
						DAGNode newNode = new DAGNode(label,node);
						List<StateLabel> newList = new LinkedList<>();
						newList.addAll(node.localLabelling);
						newList.add(label);
						newNode.localLabelling = newList;
						try{
							System.out.println("then");
							node.successors.add(newNode);
							newNode.conjecture = buildConjecture(newNode.localLabelling);
							if(impossibleLabels.contains(newNode.localLabelling.get(i))){
								throw new InconsistencyException();
							}
							
							treeLabelling.mapDepthLabels.get(i).add(newNode);
							//System.out.println("newNode local labelling : "+printLabelling(newNode.localLabelling));
							//System.out.println("newNode local conjecture : "+newNode.conjecture.toString());
						}
						catch(InconsistencyException e){
							DAGNode treatedNode = newNode;
							//System.out.println("exception on labelling :"+printLabelling(newNode.localLabelling));
							if(iterCur.hasNext()){
							}
							else{
								while(treatedNode.father.successors.size()<2){
									treatedNode = treatedNode.father;
									if(treatedNode == treeLabelling.root) break;
								}
								if(treatedNode == treeLabelling.root) {
									System.out.println("cut to the root");
									System.out.println("cut to the root on labelling :"+printLabelling(newNode.localLabelling));
									System.out.println("cut to the root on conjecture :"+newNode.conjecture);
									throw new InconsistencyException(i-1,newNode.localLabelling);
								}
								treatedNode.father.successors.remove(treatedNode);
								
							}
							
						}
						
						
						
							}
					}else {//if successor already exists modify in place
						System.out.println("else");
						Iterator<DAGNode> iter = node.successors.iterator();
						List<DAGNode> tobeAdded = new LinkedList<>();
						while(iter.hasNext()){
							DAGNode nodeToUpdate = iter.next();
							iter.remove();
							treeLabelling.mapDepthLabels.get(i).remove(nodeToUpdate);
							Iterator<StateLabel> iterCur = currentLabel.iterator();
							while(iterCur.hasNext()){
								StateLabel label = iterCur.next();
								DAGNode newNode = new DAGNode(label,node);
								List<StateLabel> newList = new LinkedList<>();
								newList.addAll(nodeToUpdate.localLabelling);
								newNode.localLabelling = newList;
								try{
									newNode.conjecture = buildConjecture(newNode.localLabelling);
									newNode.conjecture.merge(label, newNode.localLabelling.get(i), newNode.localLabelling);
									if(impossibleLabels.contains(newNode.localLabelling.get(i))){
										throw new InconsistencyException();
									}
									//System.out.println("newNode local labelling : "+printLabelling(newNode.localLabelling));
									//System.out.println("newNode local conjecture : "+newNode.conjecture.toString());
									
									tobeAdded.add(newNode);
									//node.successors.add(newNode);
									treeLabelling.mapDepthLabels.get(i).add(newNode);
								}
								catch(InconsistencyException e){
									DAGNode treatedNode = newNode;
									//System.out.println("exception on labelling :"+printLabelling(newNode.localLabelling));
									if(tobeAdded.size()>=1 || iterCur.hasNext()){
									}
									else{
										while(treatedNode.father.successors.size()<2){
											treatedNode = treatedNode.father;
											if(treatedNode == treeLabelling.root) break;
										}
										if(treatedNode == treeLabelling.root){
											System.out.println("cut to the root");
											System.out.println("cut to the root on labelling :"+printLabelling(newNode.localLabelling));
											System.out.println("cut to the root on conjecture :"+newNode.conjecture);
											throw new InconsistencyException(i-1,newNode.localLabelling);
										}
										treatedNode.father.successors.remove(treatedNode);
										
									}
									
								}
								
							}
						}
						node.successors.addAll(tobeAdded);
						
						
					}
				}
				
			treeLabelling.rebuildMapDepthLabels();
				
			}
		
		System.out.println("---------fin updateTree----------");
		System.out.println("");
	}

	private void executePreliminaryLoop() throws InconsistencyException{
		InputSequence alpha1 = z.get(0);
		OutputSequence out1 = executeInputSequence(alpha1);
		StateLabel label1 = new StateLabel(false);
		currentCharTable.map.put(label1,new HashMap<InputSequence,OutputSequence>());
		currentCharTable.map.get(label1).put(alpha1.clone(), out1.clone());
		OutputSequence out = executeInputSequence(alpha1);
		while(!currentCharTable.containsOutput(out)){
			StateLabel label = new StateLabel(false);
			currentCharTable.map.put(label,new HashMap<InputSequence,OutputSequence>());
			currentCharTable.map.get(label).put(alpha1.clone(), out.clone());
			out = executeInputSequence(alpha1);
		}
		
		System.out.println("charTable after preliminary loop : "+currentCharTable);
		updateTree(traceI.getLength());
		
		
		
	}
	
	private String executeSingleInput(String input){
		String output = ((MealyDriver) driver).execute(input);
		traceI.addInput(input);
		traceO.addOutput(output);
		System.out.println("   EXECUTION OF "+input+" GIVES OUTPUT "+output);
		return output;
	}
	
	private OutputSequence executeInputSequence(InputSequence inputSequence){
		OutputSequence outputSequence = new OutputSequence();
		for(String input : inputSequence.sequence){
			outputSequence.addOutput(executeSingleInput(input));
		}
		return outputSequence;
	}
	
	
	
	private FSMConjecture buildConjecture(List<StateLabel> labelling) throws InconsistencyException{
		FSMConjecture conj = new FSMConjecture(inputs);
		ConjectureTransition newTrans;
		for(int i=0;i<labelling.size()-1;i++){
			newTrans = new ConjectureTransition(labelling.get(i), labelling.get(i+1),
					traceI.subSequence(i,i+1).getFirstSymbol(), traceO.subSequence(i, i+1).getLastSymbol());
			try {
				conj.treatTransition(newTrans,labelling);
			} catch (InconsistencyException e) {
				throw new InconsistencyException();
			}
		}
		//System.out.println(conj);
		return conj;
	}
	
	
	
	
	
	private IndexPair findFirstIncompatiblePairWithSameLabel(int inconsistencyIndex2,List<StateLabel> localLabelling){ //implementation of Kella's algorithm
		System.out.println("findFirstIncompatiblePairWithSameLabel");
		System.out.println(currentCharTable);
		System.out.println(printLabelling(localLabelling));
		int inconsistencyIndex= localLabelling.size()-2;
		/*
		ArrayList<LinkedList<Integer>> incompatibilitiesList = new ArrayList<>(inconsistencyIndex+1);
		incompatibilitiesList.add(inconsistencyIndex, new LinkedList<Integer>());
		for(int i = inconsistencyIndex-1;i>=0;i--){
			incompatibilitiesList.add(i, new LinkedList<Integer>());
			for(int j = inconsistencyIndex;j>i;j--){
				if(traceI.subSequence(i, i+1).equals(traceI.subSequence(j, j+1))){ //same input
					if(!traceO.subSequence(i, i+1).equals(traceO.subSequence(j, j+1))){ //different outputs
						incompatibilitiesList.get(i).add(j);
					}
					else{
						if(j+1<inconsistencyIndex && incompatibilitiesList.get(i+1).contains(j+1)){
							incompatibilitiesList.get(i).add(j);
						}
					}
				}
			}
		}
		*/
		List<LinkedList<Integer>> incompatibilitiesList = new LinkedList<LinkedList<Integer>>();
		incompatibilitiesList.add( new LinkedList<Integer>());
		for(int i = inconsistencyIndex-1;i>=0;i--){
			incompatibilitiesList.add( new LinkedList<Integer>());
			for(int j = traceI.getLength()-1;j>i;j--){
				if(traceI.subSequence(i, i+1).equals(traceI.subSequence(j, j+1))){ //same input
					if(!traceO.subSequence(i, i+1).equals(traceO.subSequence(j, j+1))){ //different outputs
						incompatibilitiesList.get(incompatibilitiesList.size()-1).add(j);
					}
					else{
						if(j+1<=inconsistencyIndex && incompatibilitiesList.get(incompatibilitiesList.size()-2).contains(j+1)){
							System.out.println("###########################################################");
							System.out.println("###########################################################");
							incompatibilitiesList.get(incompatibilitiesList.size()-1).add(j);
						}
					}
				}
			}
		}
		System.out.println(incompatibilitiesList);
		Collections.reverse(incompatibilitiesList);
		System.out.println("inconsistency index :"+inconsistencyIndex);
		System.out.println("trace length:"+traceI.getLength());
		System.out.println("labelling length:"+localLabelling.size());
		System.out.println(incompatibilitiesList);
		IndexPair result=null;
		for(int i =0; i< inconsistencyIndex;i++){
			for(int j : incompatibilitiesList.get(i)){
				/*if(labelling.mapDepthLabels.get(i).size()==1 && labelling.mapDepthLabels.get(j).size()==1 
						&& labelling.getLabelAt(i).equals(labelling.getLabelAt(j))){*/
				if(j<localLabelling.size()){
					if(localLabelling.get(i).equals(localLabelling.get(j)) ){
						if(result==null || result.second>j){
							result = new IndexPair(i,j);
						}
					}
				}
				
			}
			if(result!=null && result.second<=i) break;
		}
		return result;
		
		
		
	}
	
	private static String printLabelling(Collection<StateLabel> l){
		String s =" labelling : { ";
		
		for(StateLabel label : l){
			s += "[";
			s += label.name+" ";
			s += "];";
		}
	return s;
	}
	
	private InputSequence verifyIfAllTransitionsHaveBeenChecked(List<StateLabel> localLabelling,FSMConjecture conj,StateLabel currentState){
		Map<StateLabel,Map<String,Set<InputSequence>>> checkingMap = new HashMap<>();
		for(StateLabel label : currentCharTable.map.keySet()){
			checkingMap.put(label, new HashMap<String,Set<InputSequence>>());
		}
		int maxLength =0;
		for(InputSequence seq : z){
			if(seq.getLength()>maxLength) maxLength = seq.getLength();
		}
		
		
		for(int i =0; i< traceI.getLength();i++){
			String input = traceI.subSequence(i, i+1).getFirstSymbol();
			for(int j =1;i+j<=traceI.getLength() && j<=maxLength+1;j++){
				for(InputSequence seq : z){
					if(traceI.subSequence(i+1, i+j).equals(seq)){
						if(checkingMap.get(localLabelling.get(i)).containsKey(input)){
							checkingMap.get(localLabelling.get(i)).get(input).add(seq);
						}
						else {
							Set<InputSequence> set = new HashSet<>();
							set.add(seq);
							checkingMap.get(localLabelling.get(i)).put(input,set);
						}
						
					}
				}
			}
		}
		String res = "checkingMap : ";
		for(Entry<StateLabel, Map<String, Set<InputSequence>>> bigEntry : checkingMap.entrySet()){
			res += bigEntry.getKey().name+" : {";
			for(Entry<String,Set<InputSequence>> smallEntry : bigEntry.getValue().entrySet()){
				res += smallEntry.getKey()+" : [";
				for(InputSequence is : smallEntry.getValue()){
					res += is+", ";
				}
				res += "] ";
			}
			res += "} ";
		}
			
		System.out.println(res);
		
		
		StateLabel origin =null;
		String inputUnchecked =null;
		InputSequence uncheckedSeq =null;
		outerloop:
		for(Entry<StateLabel, Map<String, Set<InputSequence>>> bigEntry : checkingMap.entrySet()){
			for(Entry<String,Set<InputSequence>> smallEntry : bigEntry.getValue().entrySet()){
				if(!smallEntry.getValue().containsAll(z)){
					origin = bigEntry.getKey();
					inputUnchecked = smallEntry.getKey();
					Set<InputSequence> s = new HashSet<>();
					s.addAll(z);
					
					
					String toPrint = "sE value : { ";
					for(InputSequence is : smallEntry.getValue()){
						toPrint += is+", ";
					}
					System.out.println(origin.name);
					System.out.println("input string :"+smallEntry.getKey());
					System.out.println(toPrint);
					
					s.removeAll(smallEntry.getValue());
					uncheckedSeq = s.iterator().next();
					break outerloop;
				}
			}
		}
		
		if(origin ==null){
			return null;
		}
		
		if(!currentState.equals(origin)){
			System.out.println("transfer");
			return new InputSequence(conj.findFirstTransferInputTo(currentState,origin));
		}
		System.out.println("can be checked directly");
		InputSequence result = new InputSequence(inputUnchecked);
		result.addInputSequence(uncheckedSeq.clone());
	return result;
		
		
		
	}

}
