/*
 * 
 * Copyright 2013 Digital Audio Processing Lab, Indian Institute of Technology.  
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

/**
 *
 * @author  : Nikul Prajapati
 * @contact : nikulprajapati90@gmai.com
 */

/**
 *
 * @author  : Jigar Gada
 * @contact : jigargada23@yahoo.com
 */

package edu.iitb.cyborg.aligner.TreeFunctions;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.cyborg.fst.ReadFinalDir;
import edu.iitb.cyborg.aligner.util.results.Results;
import edu.iitb.cyborg.fst.Fst;
import edu.iitb.cyborg.fst.FstLinkedList;
import edu.iitb.cyborg.fst.NewTriphone;
import edu.iitb.cyborg.fst.Monophone;
import edu.iitb.frontend.audio.feature.FeatureFileExtractor;

public class Aligner {

	static HashMap<String, String> hashMapStates = new HashMap<String, String>(); 
	Results results;
	static FilesLoader filesLoader;
	public Aligner() throws IOException{
		results = new Results();
		filesLoader = new FilesLoader();
	}
		
	public void doAlignment(String fileName, String transcription, String inputFolder, String featureFolder, String phsegFolder, String wdsegFolder, String triPhnSearch) throws Exception {

		//filesLoader = new FilesLoader();

		//extracting mfcc features
		FeatureFileExtractor.computeFeatures(fileName, inputFolder, featureFolder);
		String featureFile = featureFolder + "/" + fileName + ".mfc";
		
		//To check if MFC file is created
		File ft = new File(featureFile);
		if(ft.exists()){
			System.out.println("MFC file created.. :)");
		}
		else{
			System.out.println("ERROR creating MFC file");
			System.exit(0);
		}
		
		//loads the feature file in the array feat
		filesLoader.readFeat(featureFile);
		
		int totalTimeFrames = FilesLoader.FEAT.length;
		
		//Storing the features in a 2-dimensional array x[][]
		float x[][] = FilesLoader.FEAT;
		
		//Creates/Overwrite fst directory and store all the fst files
		Fst.getFST(transcription, "resources//fillerDict.txt");
		
		Probability p = new Probability();
		
		//Read the FST file
		ReadFinalDir rfd1 = new ReadFinalDir();
		rfd1.readFile();
		//rfd.readFile();
		
		//Creates a linkedlist of FST file
		FstLinkedList fll2 = new FstLinkedList();
		fll2.setLeftContext();
		fll2.setRightContext();
		fll2.createNewList();
		//System.out.println("-------new list------");
		//fll2.printNewList();
		//System.out.println("-------------");

		
		/*
		 * Viterbi algo with Tree structure starts here
		 */

		//HashMap for branchId's
		HashMap<String, ArrayList<Integer>> branchingHashMap = new HashMap<String,ArrayList<Integer>>();
		
		//Global branch Id
		int branchId = 1;
		
		//Creating and setting root node
		TreeNode nodeRoot = new TreeNode();
		
		if(triPhnSearch.equals("yes")){
			//Creating triPhone object as a temporary container
			NewTriphone triphoneObj = new NewTriphone();
			triphoneObj.printTriphones(triphoneObj.getStartState());
			
			//Lists to keep track of last two levels nodes while creating tree
			ArrayList<TreeNode> parentListLevelN = new ArrayList<TreeNode>();
			ArrayList<TreeNode> parentListLevelNplus1 = new ArrayList<TreeNode>();
			
			//Configuring root node
			nodeRoot.setParent(null);
			
			int stateInfoTemp[] = new int[3];
			stateInfoTemp[0] = 0;
			stateInfoTemp[1] = triphoneObj.getStartState().get(0).getObsState();
			stateInfoTemp[2] = triphoneObj.getStartState().get(0).getTmatState();
			
			nodeRoot.setTriPhone(triphoneObj.getStartState().get(0).getTriphone());
			nodeRoot.setStateInfo(stateInfoTemp);
			nodeRoot.setCostOfNode(p.b(nodeRoot.getStateInfo()[1], x[0]));
			nodeRoot.setCostOfPath(nodeRoot.getCostOfNode());		
			nodeRoot.setBranchId(0);
			nodeRoot.setNewTriphoneObj(triphoneObj.getStartState().get(0));
			
			parentListLevelN.add(nodeRoot);
			parentListLevelNplus1.add(nodeRoot);
					
			for(int indexI = 1; indexI < totalTimeFrames; indexI++){
				
				parentListLevelN.clear();
				parentListLevelN.addAll(parentListLevelNplus1);
				parentListLevelNplus1.clear();
				
				Iterator<TreeNode> iteratorParent = parentListLevelN.iterator();
			
				while(iteratorParent.hasNext()){
					
					TreeNode parent = iteratorParent.next(); //Getting parent from parent list

		
					ArrayList<NewTriphone> triPhoneList = new ArrayList<NewTriphone>();
					
					//Getting next triphone list states 
					triPhoneList = triphoneObj.getNextState(parent.getNewTriphoneObj());
			
//					System.out.print("\nInput : "+triphoneObj.getTriphone()+" "+triphoneObj.getObsState());
//					System.out.print("\tOutput : ");
//					for(Triphone tri : triPhoneList)
//						System.out.print("\t"+tri.getTriphone()+" "+tri.getObsState());
//					System.out.println();
//					System.out.print("-----------------");

					if(branchingHashMap.get(String.valueOf(parent.getBranchId())) == null && triPhoneList.size() > 1){
						for(int i = 0; i< triPhoneList.size();i++)
							parent.siblingsBranchIds.add(branchId++);
						
						branchingHashMap.put(String.valueOf(parent.getBranchId()), parent.siblingsBranchIds);
					}
					
					
					TreeNode node[] = new TreeNode[triPhoneList.size()+1]; // Creating child nodes

					//Configuring left child node
					node[0] = new TreeNode();
					
					node[0].setParent(parent);
					
					node[0].setTriPhone(parent.getTriPhone());
					node[0].setStateInfo(parent.getStateInfo());
					node[0].setCostOfNode(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0]));
					node[0].setCostOfPath(node[0].getCostOfNode()+parent.getCostOfPath());
					node[0].setBranchId(parent.getBranchId());
					node[0].setSiblingsBranchIds(parent.getSiblingsBranchIds());
					node[0].setNewTriphoneObj(parent.getNewTriphoneObj());
					parentListLevelNplus1.add(node[0]);
					
						
					
//					if(triPhoneList.size() == 0)
//						System.out.println("\ntriPhoneList with size 0 "+node[0].getTriPhone()+" Id"+node[0].getStateInfo()[0]);
					
					//Configuring child nodes in left to right order
					for(int i=1; i< (triPhoneList.size()+1); i++){
						node[i] = new TreeNode();
						
						node[i].setParent(parent);
						
						int stateInfoTempInside[] = new int[3];
						stateInfoTempInside[0] = node[0].getStateInfo()[0]+1;
						stateInfoTempInside[1] = triPhoneList.get(i-1).getObsState();
						stateInfoTempInside[2] = triPhoneList.get(i-1).getTmatState();
						
						node[i].setTriPhone(triPhoneList.get(i-1).getTriphone());
						node[i].setStateInfo(stateInfoTempInside);
						node[i].setCostOfNode(p.b(node[i].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[i].getStateInfo()[0]));
						node[i].setCostOfPath(node[i].getCostOfNode()+parent.getCostOfPath());				
						//System.out.println("Cost : "+node[i].getCostOfPath());
						
						
						//Assign different branch Id in case of total more then two branches
						if(triPhoneList.size() > 1 && branchingHashMap.get(String.valueOf(parent.getBranchId()))!=null){	
							node[i].setBranchId(branchingHashMap.get(String.valueOf(parent.getBranchId())).get(i-1));
							//node[i].setBranchId(parent.siblingsBranchIds.get(i-1));	
							//System.out.print(node[i].getBranchId()+" ");
							
						}
						else{
							node[i].setBranchId(parent.getBranchId());
							//System.out.print(node[i].getBranchId()+" ");
						}
						
						node[i].setNewTriphoneObj(triPhoneList.get(i-1));
						
						parentListLevelNplus1.add(node[i]);
						
					}			
					
				}
				
				
				
				//Merging the nodes
				ArrayList<TreeNode> parentListFinal = new ArrayList<>();
				parentListFinal = mergeNodes(parentListLevelNplus1);

//				System.out.println("Parent list levelNplus1");
//				for(TreeNode child : parentListLevelNplus1)
//					System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());
//				System.out.println("Parent list final");
//				for(TreeNode child : parentListFinal)
//					System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());

				//Pruning the paths
				if(Alignment.prune.equals("yes")){
					parentListLevelNplus1 = prunePaths(parentListFinal);
				}
				else if(Alignment.prune.equals("no")){
					parentListLevelNplus1 = parentListFinal;
				}
				else{
					System.out.println("improper argument assigned to 'prune' = "
							+ Alignment.prune);
					System.exit(0);
				}
				
				
			}
			
			
			
			//Back trace from last state of SIL only
			//TreeNode backTraceChild = getBackTraceChild(parentListLevelNplus1);
			
			//Backtrace from last state of any phone.
			TreeNode backTraceChild = getBackTraceChildFree(parentListLevelNplus1);			
				
	        // Backtracing path
			int N = backTraceChild.getStateInfo()[0]+1;
			System.out.println("Value of N : "+N);
			int stateDuration[] = new int[N];
			double scoreArray[] = new double[N];
			String triPhoneSeq[] = new String[N];
			TreeNode parent;
			
			while(backTraceChild.getParent() != null){
				parent = backTraceChild.getParent();

				//System.out.println("State index : "+backTraceChild.getStateInfo()[0]);
				
				stateDuration[backTraceChild.getStateInfo()[0]]++;
				//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
				scoreArray[backTraceChild.getStateInfo()[0]] += backTraceChild.getCostOfNode();
				triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();
				backTraceChild = parent;
			}

			stateDuration[backTraceChild.getStateInfo()[0]]++;
			//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
			scoreArray[backTraceChild.getStateInfo()[0]] += backTraceChild.getCostOfNode();
			triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();
			
			//Getting phoneme sequence
			String phonemSeq[] = new String[N];
			String triphoneSeq[] = new String[N];
			int j=0;
			for(int i = 0; i< N ;i++){
				//System.out.println(stateDuration[i][0]+" "+stateDuration[i][1]+"\t"+numberFormator.format(scoreArray[i])+"\t"+triPhoneSeq[i]);
				if(i%3 == 0){
				 phonemSeq[j] = triPhoneSeq[i].split("\t")[0];
				 triphoneSeq[j] = triPhoneSeq[i];
				 j++;
				}
				//System.out.println(triPhoneSeq[i].split("\t")[0]+" ");
			}

//			System.out.println();
//			System.out.println("Phonem sequence");
//			for(int i = 0; i<N/3 ;i++)
//						System.out.println(phonemSeq[i]+" ");
//			
//			System.out.println();
//			System.out.println("FST file content");
//			for(Fst2Triphones obj : Fst2Triphones.arrayObj)
//			    System.out.println(obj.getIn()+" "+obj.getOp()); 
			
			System.out.println();
			System.out.println("Utterence : "+transcription+" "+"("+fileName+")");
			results.printPhoneSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
			results.printPhoneSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray, phsegFolder, fileName);
			//results.printWordSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
			//results.printWordSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray, wdsegFolder, fileName);		
			
//			stateDuration[s]++;
//			
//			while(child.getParent() != null){
//				Node parent = child.getParent();
	//
//				if(parent.getStateInfo()[1] != child.getStateInfo()[1]) s--;
//				stateDuration[s]++;
//				child = parent;
	//
//			}
		
			
			/*
			 * Tree structure ends for Triphone
			 */
			
		}else if(triPhnSearch.equals("no")){
			
			//Creating triPhone object as a temporary container
			Monophone monophoneObj = new Monophone();
			
			//Lists to keep track of last two levels nodes while creating tree
			ArrayList<TreeNode> parentListLevelN = new ArrayList<TreeNode>();
			ArrayList<TreeNode> parentListLevelNplus1 = new ArrayList<TreeNode>();
			
			//Configuring root node
			nodeRoot.setParent(null);
			
			int stateInfoTemp[] = new int[3];
			stateInfoTemp[0] = 0;
			stateInfoTemp[1] = monophoneObj.getStartState().get(0).getObsState();
			stateInfoTemp[2] = monophoneObj.getStartState().get(0).getTmatState();
			
			nodeRoot.setTriPhone(monophoneObj.getStartState().get(0).getTriphone());
			nodeRoot.setStateInfo(stateInfoTemp);
			nodeRoot.setCostOfNode(p.b(nodeRoot.getStateInfo()[1], x[0]));
			nodeRoot.setCostOfPath(nodeRoot.getCostOfNode());		
			nodeRoot.setBranchId(0);
			nodeRoot.setMonophoneObj(monophoneObj.getStartState().get(0));
			
			parentListLevelN.add(nodeRoot);
			parentListLevelNplus1.add(nodeRoot);
					
			for(int indexI = 1; indexI < totalTimeFrames; indexI++){
				
				parentListLevelN.clear();
				parentListLevelN.addAll(parentListLevelNplus1);
				parentListLevelNplus1.clear();
				
				Iterator<TreeNode> iteratorParent = parentListLevelN.iterator();
			
				while(iteratorParent.hasNext()){
					
					TreeNode parent = iteratorParent.next(); //Getting parent from parent list

		
					ArrayList<Monophone> monoPhoneList = new ArrayList<Monophone>();
					
					//Getting next triphone list states 
					monoPhoneList = monophoneObj.getNextState(parent.getMonophoneObj());
			
//					System.out.print("\nInput : "+monophoneObj.getTriphone()+" "+monophoneObj.getObsState());
//					System.out.print("\tOutput : ");
//					for(Triphone tri : monoPhoneList)
//						System.out.print("\t"+tri.getTriphone()+" "+tri.getObsState());
//					System.out.println();
//					System.out.print("-----------------");

					if(branchingHashMap.get(String.valueOf(parent.getBranchId())) == null && monoPhoneList.size() > 1){
						for(int i = 0; i< monoPhoneList.size();i++)
							parent.siblingsBranchIds.add(branchId++);
						
						branchingHashMap.put(String.valueOf(parent.getBranchId()), parent.siblingsBranchIds);
					}
					
					
					TreeNode node[] = new TreeNode[monoPhoneList.size()+1]; // Creating child nodes

					//Configuring left child node
					node[0] = new TreeNode();
					
					node[0].setParent(parent);
					
					node[0].setTriPhone(parent.getTriPhone());
					node[0].setStateInfo(parent.getStateInfo());
					node[0].setCostOfNode(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0]));
					node[0].setCostOfPath(node[0].getCostOfNode()+parent.getCostOfPath());
					node[0].setBranchId(parent.getBranchId());
					node[0].setSiblingsBranchIds(parent.getSiblingsBranchIds());
					node[0].setMonophoneObj(parent.getMonophoneObj());
					parentListLevelNplus1.add(node[0]);
					
						
					
//					if(monoPhoneList.size() == 0)
//						System.out.println("\nmonoPhoneList with size 0 "+node[0].getTriPhone()+" Id"+node[0].getStateInfo()[0]);
					
					//Configuring child nodes in left to right order
					for(int i=1; i< (monoPhoneList.size()+1); i++){
						node[i] = new TreeNode();
						
						node[i].setParent(parent);
						
						int stateInfoTempInside[] = new int[3];
						stateInfoTempInside[0] = node[0].getStateInfo()[0]+1;
						stateInfoTempInside[1] = monoPhoneList.get(i-1).getObsState();
						stateInfoTempInside[2] = monoPhoneList.get(i-1).getTmatState();
						
						node[i].setTriPhone(monoPhoneList.get(i-1).getTriphone());
						node[i].setStateInfo(stateInfoTempInside);
						node[i].setCostOfNode(p.b(node[i].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[i].getStateInfo()[0]));
						node[i].setCostOfPath(node[i].getCostOfNode()+parent.getCostOfPath());				
						//System.out.println("Cost : "+node[i].getCostOfPath());
						
						
						//Assign different branch Id in case of total more then two branches
						if(monoPhoneList.size() > 1 && branchingHashMap.get(String.valueOf(parent.getBranchId()))!=null){	
							node[i].setBranchId(branchingHashMap.get(String.valueOf(parent.getBranchId())).get(i-1));
							//node[i].setBranchId(parent.siblingsBranchIds.get(i-1));	
							//System.out.print(node[i].getBranchId()+" ");
							
						}
						else{
							node[i].setBranchId(parent.getBranchId());
							//System.out.print(node[i].getBranchId()+" ");
						}
						
						node[i].setMonophoneObj(monoPhoneList.get(i-1));
						
						parentListLevelNplus1.add(node[i]);
						
					}			
					
				}
				
				
				
				//Merging the nodes
				ArrayList<TreeNode> parentListFinal = new ArrayList<>();
				parentListFinal = mergeNodes(parentListLevelNplus1);

//				System.out.println("Parent list levelNplus1");
//				for(Node child : parentListLevelNplus1)
//					System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());
//				System.out.println("Parent list final");
//				for(Node child : parentListFinal)
//					System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());

				//Pruning the paths
				if(Alignment.prune.equals("yes")){
					parentListLevelNplus1 = prunePaths(parentListFinal);
				}
				else if(Alignment.prune.equals("no")){
					parentListLevelNplus1 = parentListFinal;
				}
				else{
					System.out.println("improper argument assigned to 'prune' = "
							+ Alignment.prune);
					System.exit(0);
				}
				
				
			}
			
			
			//Back trace from last state of SIL only
			//TreeNode backTraceChild = getBackTraceChild(parentListLevelNplus1);
			
			//Backtrace from last state of any phone.
			TreeNode backTraceChild = getBackTraceChildFree(parentListLevelNplus1);			
				
	        // Backtracing path
			int N = backTraceChild.getStateInfo()[0]+1;
			System.out.println("Value of N : "+N);
			int stateDuration[] = new int[N];
			double scoreArray[] = new double[N];
			String triPhoneSeq[] = new String[N];
			TreeNode parent;
			
			while(backTraceChild.getParent() != null){
				parent = backTraceChild.getParent();

				//System.out.println("State index : "+backTraceChild.getStateInfo()[0]);
				
				stateDuration[backTraceChild.getStateInfo()[0]]++;
				//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
				scoreArray[backTraceChild.getStateInfo()[0]] += backTraceChild.getCostOfNode();
				triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();
				backTraceChild = parent;
			}

			stateDuration[backTraceChild.getStateInfo()[0]]++;
			//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
			scoreArray[backTraceChild.getStateInfo()[0]] += backTraceChild.getCostOfNode();
			triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();
			
			//Getting phoneme sequence
			String phonemSeq[] = new String[N];
			String triphoneSeq[] = new String[N];
			int j=0;
			for(int i = 0; i< N ;i++){
				//System.out.println(stateDuration[i][0]+" "+stateDuration[i][1]+"\t"+numberFormator.format(scoreArray[i])+"\t"+triPhoneSeq[i]);
				if(i%3 == 0){
				 phonemSeq[j] = triPhoneSeq[i].split("\t")[0];
				 triphoneSeq[j] = triPhoneSeq[i];
				 j++;
				}
				//System.out.println(triPhoneSeq[i].split("\t")[0]+" ");
			}

//			System.out.println();
//			System.out.println("Phonem sequence");
//			for(int i = 0; i<N/3 ;i++)
//						System.out.println(phonemSeq[i]+" ");
//			
//			System.out.println();
//			System.out.println("FST file content");
//			for(Fst2Triphones obj : Fst2Triphones.arrayObj)
//			    System.out.println(obj.getIn()+" "+obj.getOp()); 
			
			System.out.println();
			System.out.println("Utterence : "+transcription+" "+"("+fileName+")");
			results.printPhoneSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
			results.printPhoneSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray, phsegFolder, fileName);
			//results.printWordSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
			//results.printWordSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray, wdsegFolder, fileName);		
			
//			stateDuration[s]++;
//			
//			while(child.getParent() != null){
//				Node parent = child.getParent();
	//
//				if(parent.getStateInfo()[1] != child.getStateInfo()[1]) s--;
//				stateDuration[s]++;
//				child = parent;
	//
//			}
		
			
			/*
			 * Tree structure ends for Monophone
			 */
		}
		else{
			System.out.println("Error : Invalide argument for \"-triphn\"");
			System.exit(0);
		}
		

	}	// end of main
		
	
	

	public static TreeNode max(ArrayList<TreeNode> elements) {
		TreeNode m = elements.get(0);
		for(int i = 1 ; i < elements.size() ; i++){
			if(elements.get(i).getCostOfPath() > m.getCostOfPath()) m = elements.get(i);
		}
		return m;
	}
	
	public static ArrayList<TreeNode> mergeNodes(ArrayList<TreeNode> parentListLevelNplus1){
		
		ArrayList<TreeNode> parentListFinal = new ArrayList<>();
		ArrayList<String> stateSeqAndObjId = new ArrayList<>();
		
		for(int i = 0; i < parentListLevelNplus1.size(); i++){
			ArrayList<TreeNode> temp = new ArrayList<>();
			List<TreeNode> bList = parentListLevelNplus1.subList(i, parentListLevelNplus1.size());
			if(!(stateSeqAndObjId.contains(parentListLevelNplus1.get(i).getStateInfo()[0]+"\t"+parentListLevelNplus1.get(i).getStateInfo()[1]+"\t"+parentListLevelNplus1.get(i).getBranchId()))){
				stateSeqAndObjId.add(parentListLevelNplus1.get(i).getStateInfo()[0]+"\t"+parentListLevelNplus1.get(i).getStateInfo()[1]+"\t"+parentListLevelNplus1.get(i).getBranchId());
				for(int j = 0; j < bList.size(); j++)			
					if((bList.get(j).getStateInfo()[0] == parentListLevelNplus1.get(i).getStateInfo()[0]) && 
							(bList.get(j).getStateInfo()[1] == parentListLevelNplus1.get(i).getStateInfo()[1]))
							temp.add(bList.get(j));
					
				if(temp.size() > 1)
					parentListFinal.add(Aligner.max(temp));
				else
					parentListFinal.add(temp.get(0));

			}
			
		 
		}
		return parentListFinal;
	}
	
	public static ArrayList<TreeNode> prunePaths(ArrayList<TreeNode> parentListFinal){
		
		//get the node with maximum cost.
		double maxCost = Aligner.max(parentListFinal).getCostOfPath();
		//System.out.println("maximum cost " + maxCost);
		double pruneLimit = Alignment.beamWidth;
		double beam = maxCost + pruneLimit;
		//System.out.println("beam cost " + beam);
		ArrayList<TreeNode> prunedList = new ArrayList<>();
			
		//System.out.println("info for pruned nodes");
		for(TreeNode n : parentListFinal){
			//pruning 
			if(n.getCostOfPath() > beam){
				prunedList.add(n);
				TreeNode parent = n.getParent();
				parent.setChild(n);
			}			
		}
		
		return prunedList;
		
	}
	
	//Backtrace best likely path considering silence as a last state 	
	public static TreeNode getBackTraceChild(ArrayList<TreeNode> parentListLevelNplus1) throws Exception{
		//Assuming silence as a last state and backtracing it from silence.  
		int states[] = filesLoader.getStates("SIL\t-\t-\t-");
		//int states[] = filesLoader.getStates("s\ta\tSIL\te");

		TreeNode backTraceChild = new TreeNode();
		double max=0; 
		
		for(TreeNode child : parentListLevelNplus1)
			if(states[4] == child.getStateInfo()[1]){
				max = child.getCostOfPath();
				//System.out.println("Max cost : "+max);
				break;
			}
		
		for(TreeNode child: parentListLevelNplus1){
			if(states[4] == child.getStateInfo()[1]){
			    if(max <= child.getCostOfPath()){
			    	max = child.getCostOfPath();
			    	backTraceChild = child;
			    }					
			}
		}
		
		return backTraceChild;
	}
	
	//This function will backtrace from best likely path without considering last state should be of silence.
	public static TreeNode getBackTraceChildFree(ArrayList<TreeNode> parentListLevelNplus1) throws Exception{
		//Assuming silence as a last state and backtracing it from silence.  
//		int states[] = filesLoader.getStates("SIL\t-\t-\t-");
		//int states[] = filesLoader.getStates("s\ta\tSIL\te");

		TreeNode backTraceChild = new TreeNode();
		double max=parentListLevelNplus1.get(0).getCostOfPath();
		//double max=-0.000000000000000000001;
		
//		for(TreeNode child : parentListLevelNplus1)
//			if(states[4] == child.getStateInfo()[1]){
//				max = child.getCostOfPath();
//				//System.out.println("Max cost : "+max);
//				break;
//			}
//		System.out.println("Total Nodes at last level:"+parentListLevelNplus1.size());
		for(TreeNode child: parentListLevelNplus1){
//			if(states[4] == child.getStateInfo()[1]){
			    if(max <= child.getCostOfPath()){
			    	max = child.getCostOfPath();
			    	backTraceChild = child;			    	
			    }					
//			}
		}
		
		return backTraceChild;
	}	
}
