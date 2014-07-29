package edu.iitb.cyborg.fstMono;

import java.io.IOException;
import java.util.ArrayList;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class Monophone {
	Next center;
	int obsState;
	int tmatState;
	
	//copy constructor
			private Monophone(Monophone copyFrom){
				this.center = copyFrom.center;
				this.obsState = copyFrom.obsState;
				this.tmatState = copyFrom.tmatState;
			}
			
			public Monophone(){};
			
			
	public static void main(String[] args) throws Exception {
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
//		fll.print();
		fll.createNewList();
		
		
		Monophone mn = new Monophone();
		ArrayList<Monophone> trArray = mn.getStartState();
		Monophone current = trArray.get(0);
//		mn.printTriphone(current);
		System.out.println("");
		while(trArray.size() > 0){
			System.out.print("Current Triphone : " + current.getTriphone() + "\t" + current.getObsState() + "\t");
			System.out.println("Next Triphone : " + trArray.get(0).getTriphone() + "\t" + trArray.get(0).getObsState());
//			if(trArray.size() > 3){
////				System.out.println(trArray.size());
//				current = trArray.get(3);
//			}
//			else
				current = trArray.get(0);
			trArray = mn.getNextState(current);
		}
	}
	
	/**
	 * This function gives the set of start states.<br>
	 * Assuming that the start is always from silence, this function will return 
	 * the start state of 'SIL	-	-	-'.
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<Monophone> getStartState() throws Exception {
		ArrayList<Monophone> list = new ArrayList<>();
		Monophone mn = new Monophone();
		String triphone = "SIL\t-\t-\t-";
		FilesLoader fl = new FilesLoader();
		int states[] = fl.getStates(triphone);
		Next rt = new Next();
		//set id as 1 for start state
		rt.setSym("SIL");
		rt.setId(1);
		mn.setCenter(rt);
		mn.setObsState(states[2]);
		mn.setTmatState(states[1]);
		list.add(mn);
		return list;
		
	}
	
	/**
	 * This function returns the set of next monophones given the current monophone.
	 * The working of this function is mentioned in detail in the manual.
	 * @param tr
	 * @return
	 * @throws Exception 
	 */
	ArrayList<Monophone> getNextTriphone(Monophone mn) throws Exception{
		ArrayList<Monophone> tArray = new ArrayList<>();
		FilesLoader fl = new FilesLoader();

		//get the next monophones
		ArrayList<Next> rightContext = FstLinkedList.newList[mn.getCenter().getId()].getRight();
		for(Next rt: rightContext){
			//check for last state
			if(rt.getSym().equals("NIL")){
				return tArray;
			}
			Monophone m = new Monophone();
			m.setCenter(rt);
			int states[] = fl.getStates(m.getTriphone());
			m.setObsState(states[2]);
			m.setTmatState(states[1]);
			tArray.add(m);
		}
		return tArray;

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
	 * @throws Exception 
	 */
	public ArrayList<Monophone> getNextState(Monophone m) throws Exception {
		ArrayList<Monophone> list = new ArrayList<>();
		FilesLoader fl = new FilesLoader();
		int states[] = fl.getStates(m.getTriphone());
		Monophone mn = new Monophone(m);
		//if the current monophone is the first state, then return the second state.
		if(m.getObsState() == states[2]){
			mn.setObsState(states[3]);
			list.add(mn);
//			return list;
		}
		//if the current monophone is the second state, then return the third state.
		else if(m.getObsState() == states[3]){
			mn.setObsState(states[4]);
			list.add(mn);
//			return list;
		}
		//if the current monophone is the third state, then return the first states of the next monophone.
		else if(m.getObsState() == states[4]){
				list = mn.getNextTriphone(m);

			}
		return list;	
		}
	

	public String getTriphone() {
		return (center.getSym() + "\t-\t-\t-");
	}
	
	void printTriphone(Monophone m){
		System.out.print(m.getTriphone());
	}
	
	public Next getCenter() {
		return center;
	}

	public void setCenter(Next center) {
		this.center = center;
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
	
}
