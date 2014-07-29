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
 * @author  : Jigar Gada
 * @contact : jigargada23@yahoo.com
 */

	package edu.iitb.cyborg.fst;

	import java.io.IOException;
import java.util.ArrayList;
import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.cyborg.io.TriphoneDoesntExistsException;

	public class NewTriphone {
		
		Next left;
		Next center;
		Next right;
		int obsState;
		int tmatState;
		boolean existsB = true;
		boolean existsI = true;
		boolean existsE = true;
		
		public NewTriphone() {
			existsB = true;
			existsI = true;
			existsE = true;
		}
		//copy constructor
		public NewTriphone(NewTriphone copyFrom){
			this.left = copyFrom.left;
			this.center = copyFrom.center;
			this.right = copyFrom.right;
			this.obsState = copyFrom.obsState;
			this.tmatState = copyFrom.tmatState;
			this.existsB = copyFrom.existsB;
			this.existsI = copyFrom.existsI;
			this.existsE = copyFrom.existsE;
		}
		public static void main(String[] args) throws IOException, InterruptedException, TriphoneDoesntExistsException {

			//-------- load dictionary and mdef file ------------//
			FilesLoader filesLoader = new FilesLoader();
			filesLoader.loadDict("resources//Dictionary.dic");
			filesLoader.loadMdef("ComIrva_FA_CMN_s1000_g16.cd_cont_1000/mdef_tab");
			//----- create the FST file----//
			Fst.getFST("laala basa", "resources//fillerDict.txt");
			//--Read the FST file--//
			ReadFinalDir rfd = new ReadFinalDir();
			rfd.readFile();
			//--create a linked list of the fst file--//
			FstLinkedList fll = new FstLinkedList();
			fll.setLeftContext();
			fll.setRightContext();
//			fll.print();
			fll.createNewList();
//			System.out.println("-------new list------");
//			fll.printNewList();
//			System.out.println("-------------");
			NewTriphone tr = new NewTriphone();
			ArrayList<NewTriphone> trArray = tr.getStartState();
			tr.printTriphones(trArray);
			
			NewTriphone current = trArray.get(0);
			trArray = tr.getNextState(current);
			
			while(trArray.size() > 0){	
				System.out.print("Current Triphone : " + current.getTriphone() + "\t" + current.getObsState() + "\t");
				System.out.println("Next Triphone : " + trArray.get(0).getTriphone() + "\t" + trArray.get(0).getObsState());
				if(trArray.size() > 3){
//					System.out.println(trArray.size());
					current = trArray.get(3);
				}
				else
					current = trArray.get(0);
				trArray = tr.getNextState(current);
			}
			
		}
		
		/**
		 * This function returns the start triphone of the FST after SIL.
		 * @return
		 * @throws TriphoneDoesntExistsException 
		 */
		ArrayList<NewTriphone> getStartTriphone() throws TriphoneDoesntExistsException{
			
			ArrayList<NewTriphone> tArray = new ArrayList<>();
			Next left = FstLinkedList.newList[0].getRight().get(0);
			FilesLoader fl = new FilesLoader();
			ArrayList<Next> rightContext = FstLinkedList.newList[1].getRight();
			for(Next r: rightContext){
				Next center = r;
				int id2 = r.getId();
				ArrayList<Next> obj = FstLinkedList.newList[id2].getRight();
				for(Next rt: obj){
					NewTriphone t = new NewTriphone();
					Next right = rt;
					t.setLeft(left);
					t.setCenter(center);
					t.setRight(right);
					//int states[] = fl.getStates(t.getTriphone());
					
					//Falling back to monophone if triphone dosen't exists
					int states[];
					try{
						states = fl.getStates(t.getTriphone());
					}catch(Exception e){
						
						if(t.getTriphone().split("\t")[3].equals("b"))
							t.setExistsB(false);
						else if(t.getTriphone().split("\t")[3].equals("i"))
							t.setExistsI(false);
						else if(t.getTriphone().split("\t")[3].equals("e"))
							t.setExistsE(false);
						
						try{
							states = fl.getStates(t.getTriphone());
						}catch(Exception e1){
							
							if(t.getTriphone().split("\t")[3].equals("b"))
								t.setExistsB(false);
							else if(t.getTriphone().split("\t")[3].equals("i"))
								t.setExistsI(false);
							else if(t.getTriphone().split("\t")[3].equals("e"))
								t.setExistsE(false);
							try{
								states = fl.getStates(t.getTriphone());
							}catch(Exception e2){
								
								if(t.getTriphone().split("\t")[3].equals("b"))
									t.setExistsB(false);
								else if(t.getTriphone().split("\t")[3].equals("i"))
									t.setExistsI(false);
								else if(t.getTriphone().split("\t")[3].equals("e"))
									t.setExistsE(false);
								
									states = fl.getStates(t.getTriphone());
								
							}
						}
						
					}
					
					t.setObsState(states[2]);
					t.setTmatState(states[1]);
					tArray.add(t);
				}
			}
			return tArray;
		}
		
		/**
		 * This function gives the set of start states.<br>
		 * Assuming that the start is always from silence, this function will return 
		 * the start state of 'SIL	-	-	-'.
		 * @return
		 * @throws TriphoneDoesntExistsException 
		 */
		public ArrayList<NewTriphone> getStartState() throws TriphoneDoesntExistsException {
			ArrayList<NewTriphone> list = new ArrayList<>();
			NewTriphone tr = new NewTriphone();
			String triphone = "SIL\t-\t-\t-";
			FilesLoader fl = new FilesLoader();
			int states[] = fl.getStates(triphone);
			Next rt = new Next();
			//set id as 1 for start state
			rt.setSym("SIL");
			rt.setId(1);
			tr.setCenter(rt);
			tr.setObsState(states[2]);
			tr.setTmatState(states[1]);
			list.add(tr);
			return list;
			
		}
		

		/**
		 * This function returns the next state given the current state.<br>
		 * Each triphone is assumed to have 3 states. e.g. the triphone 
		 * 'a	SIL	k	b' has states 275, 302 and 312. <br>
		 * If the current triphone is 'a	SIL	k	b' with stateId 275 then this function 
		 * will return 'a	SIL	k	b' with stateId 302.<br>
		 * When the input triphone is the last state of a particular triphone, then it calls the function
		 * getNextTriphone() to get the next triphone and returns the first state of that triphone.<br>
		 * Incase the getNextTriphone() returns 2 triphones then this function will return first state
		 * of both the triphones.
		 * @param t
		 * @return
		 * @throws IOException
		 * @throws TriphoneDoesntExistsException 
		 */
		public ArrayList<NewTriphone> getNextState(NewTriphone t) throws IOException, TriphoneDoesntExistsException {
			ArrayList<NewTriphone> list = new ArrayList<>();
			FilesLoader fl = new FilesLoader();
			//int states[] = fl.getStates(t.getTriphone());
			
			//Falling back to monophone if triphone dosen't exists
			int states[];
			try{
				states = fl.getStates(t.getTriphone());
			}catch(Exception e){
				
				if(t.getTriphone().split("\t")[3].equals("b"))
					t.setExistsB(false);
				else if(t.getTriphone().split("\t")[3].equals("i"))
					t.setExistsI(false);
				else if(t.getTriphone().split("\t")[3].equals("e"))
					t.setExistsE(false);
				
				try{
					states = fl.getStates(t.getTriphone());
				}catch(Exception e1){
					
					if(t.getTriphone().split("\t")[3].equals("b"))
						t.setExistsB(false);
					else if(t.getTriphone().split("\t")[3].equals("i"))
						t.setExistsI(false);
					else if(t.getTriphone().split("\t")[3].equals("e"))
						t.setExistsE(false);
					try{
						states = fl.getStates(t.getTriphone());
					}catch(Exception e2){
						
						if(t.getTriphone().split("\t")[3].equals("b"))
							t.setExistsB(false);
						else if(t.getTriphone().split("\t")[3].equals("i"))
							t.setExistsI(false);
						else if(t.getTriphone().split("\t")[3].equals("e"))
							t.setExistsE(false);
					
							states = fl.getStates(t.getTriphone());

						
					}
				}
				
			}
			
			NewTriphone tr = new NewTriphone(t);
			//if the current triphone is the first state, then return the second state.
			if(tr.getObsState() == states[2]){
				tr.setObsState(states[3]);
				list.add(tr);
//				return list;
			}
			//if the current triphone is the second state, then return the third state.
			else if(tr.getObsState() == states[3]){
				tr.setObsState(states[4]);
				list.add(tr);
//				return list;
			}
			//if the current triphone is the third state, then return the first states of the next triphone.
			else if(tr.getObsState() == states[4]){
				if(tr.getCenter().getId() == 1){
					list = tr.getStartTriphone();
//					return list;
				}
				else{
					list = tr.getNextTriphone(t);
//					return list;
				}
				
			}
			return list;
		}
			
		
		/**
		 * This function returns the set of next triphones given the current triphone.
		 * The working of this function is mentioned in detail in the manual.
		 * @param tr
		 * @return
		 * @throws TriphoneDoesntExistsException 
		 */
		ArrayList<NewTriphone> getNextTriphone(NewTriphone tr) throws TriphoneDoesntExistsException{
			Next left = tr.getCenter();
			Next center = tr.getRight();
			ArrayList<NewTriphone> tArray = new ArrayList<>();
			FilesLoader fl = new FilesLoader();
			//check for last state
			if(center.getSym().equals("NIL")){
				return tArray;
			}
			
			ArrayList<Next> rightContext = FstLinkedList.newList[tr.getRight().getId()].getRight();
			for(Next rt: rightContext){
				NewTriphone t = new NewTriphone();
				Next right = rt;
				t.setLeft(left);
				t.setCenter(center);
				t.setRight(right);
				//int states[] = fl.getStates(t.getTriphone());
				
				//Falling back to monophone if triphone dosen't exists
				int states[];
				try{
					states = fl.getStates(t.getTriphone());
				}catch(Exception e){
					
					if(t.getTriphone().split("\t")[3].equals("b"))
						t.setExistsB(false);
					else if(t.getTriphone().split("\t")[3].equals("i"))
						t.setExistsI(false);
					else if(t.getTriphone().split("\t")[3].equals("e"))
						t.setExistsE(false);
					
					try{
						states = fl.getStates(t.getTriphone());
					}catch(Exception e1){
						
						if(t.getTriphone().split("\t")[3].equals("b"))
							t.setExistsB(false);
						else if(t.getTriphone().split("\t")[3].equals("i"))
							t.setExistsI(false);
						else if(t.getTriphone().split("\t")[3].equals("e"))
							t.setExistsE(false);
						
						try{
							states = fl.getStates(t.getTriphone());
						}catch(Exception e2){
							
							if(t.getTriphone().split("\t")[3].equals("b"))
								t.setExistsB(false);
							else if(t.getTriphone().split("\t")[3].equals("i"))
								t.setExistsI(false);
							else if(t.getTriphone().split("\t")[3].equals("e"))
								t.setExistsE(false);
							
								states = fl.getStates(t.getTriphone());
							
						}
					}
					
				}
				
				t.setObsState(states[2]);
				t.setTmatState(states[1]);
				tArray.add(t);
			}
			return tArray;
		}
		
		public void printTriphones(ArrayList<NewTriphone> tArray){
			for(NewTriphone t: tArray){
				System.out.println(t.getTriphone());
			}
		}
		
		void printTriphone(NewTriphone t){
			System.out.print(t.getTriphone());
		}

		/**
		 * This fucntion returns the triphone in the form 
		 * stored in the mdef file.
		 * @return
		 */
		public String getTriphone() {
			
			if(!this.isExistsB() && this.isExistsI() && this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "b");				
			}
			else if(this.isExistsB() && !this.isExistsI() && this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "e");				
			}
			else if(this.isExistsB() && this.isExistsI() && !this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "i");
			}	
			else if(this.isExistsB() && !this.isExistsI() && !this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "b");			
			}
			else if(!this.isExistsB() && this.isExistsI() && !this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "i");				
			}
			else if(!this.isExistsB() && !this.isExistsI() && this.isExistsE()){
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + "e");
			}
			else if(!this.isExistsB() && !this.isExistsI() && !this.isExistsE()){
				return (this.getCenter().getSym()+"\t-\t-\t-");
			}
			else if(FstCreateFiles.fillerWords.contains(this.getCenter().getSym())){
				return (this.getCenter().getSym()+"\t-\t-\t-");
			}
			else if(this.center.getSym().equals("SIL")){
				return ("SIL\t-\t-\t-");
			}
			//If the left/right context of a phone is a filler then replace it with SIL.
			else if(FstCreateFiles.fillerWords.contains(this.getLeft().getSym())){
				return 	(this.getCenter().getSym() + "\t" + "SIL" + "\t" + 
						this.getRight().getSym() + "\t" + this.getCenter().getMarker());
			}
			else if(FstCreateFiles.fillerWords.contains(this.getRight().getSym())){
				return 	(this.getCenter().getSym() + "\t" +  this.getLeft().getSym()+ "\t" + 
						"SIL" + "\t" + this.getCenter().getMarker());
			}
			else{
				return 	(this.getCenter().getSym() + "\t" + this.getLeft().getSym() + "\t" + 
						this.getRight().getSym() + "\t" + this.getCenter().getMarker());
			}
			

		}

		public Next getLeft() {
			return left;
		}

		public void setLeft(Next left) {
			this.left = left;
		}

		public Next getCenter() {
			return center;
		}

		public void setCenter(Next center) {
			this.center = center;
		}

		public Next getRight() {
			return right;
		}

		public void setRight(Next right) {
			this.right = right;
		}


		public int getObsState() {
			return obsState;
		}

		public void setObsState(int obsState) {
			this.obsState = obsState;
		}

		public int getTmatState() {
			return tmatState;
		}

		public void setTmatState(int tmatState) {
			this.tmatState = tmatState;
		}
		
		public boolean isExistsB() {
			return existsB;
		}
		public void setExistsB(boolean existsB) {
			this.existsB = existsB;
		}
		public boolean isExistsI() {
			return existsI;
		}
		public void setExistsI(boolean existsI) {
			this.existsI = existsI;
		}
		public boolean isExistsE() {
			return existsE;
		}
		public void setExistsE(boolean existsE) {
			this.existsE = existsE;
		}
	}

