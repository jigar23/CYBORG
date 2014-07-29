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

package edu.iitb.cyborg.fstMono;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to create a linked list of the final fst file.<br>
 * The left context is associated with only a string unlike the right context 
 * which is associated with string, id and marker. <br>
 * Refer to the linked list algorithm in the manual for more details.
 *
 */
public class FstLinkedList {
//	this is an array of String
	ArrayList<String> left;
//	this is an array of class Next
	ArrayList<Next> right;
	static FstLinkedList list[];
//	this has the final minimized list without the start and end markers.
	static FstLinkedList newList[];
	static ArrayList<Integer> ids;
	
	public FstLinkedList(){
		left = new ArrayList<>();
		right = new ArrayList<>();
	}
	public static void main(String[] args) throws IOException, InterruptedException {
//		FilesLoader filesLoader = new FilesLoader();
//		filesLoader.loadDict("resources\\Dictionary.dic");
////		FstCreateFiles fc = new FstCreateFiles();
//		Fst.getFST("laala basa","resources\\fillerDict.txt");
		
		ReadFinalDir rfd = new ReadFinalDir();
		rfd.readFile();
		FstLinkedList fll = new FstLinkedList();
		fll.setLeftContext();
		fll.setRightContext();
		fll.print();
		System.out.println("------------- New List ---------");
		fll.createNewList();
		fll.printNewList();
	}
	
	
	/**
	 * This function creates the static array list[]. <br>
	 * It assigns symbol/symbols to each of the  list elements.
	 * Size of the list array is the maximum number in final_dir.txt file. <br> 
	 * Each of the array index of list[] is associated with symbol/symbols in this function.
	 * In this function we just make use of second and third column of final_dir.txt file.
	 */
	public void setLeftContext(){
		
		System.out.println("max id : " + ReadFinalDir.maxStateID);
		list = new FstLinkedList[ReadFinalDir.maxStateID];
		newList = new FstLinkedList[ReadFinalDir.maxStateID];
		list[0] = new FstLinkedList();
		list[0].setLeftElement("NIL");
		
		for(ReadFinalDir fd: ReadFinalDir.finalDir){
			int pos = fd.getEnd();
			//check for final state
			if(fd.getEnd() == -1) continue; 
			list[pos] = new FstLinkedList();
//			newList[pos] = new FstLinkedList();
			if(list[pos].getLeft() != null && !(list[pos].getLeft().equals(fd.in)))
				list[pos].setLeftElement(fd.in);
		}
	}
	
	/**
	 * This function has to be called after setLeftContext() otherwise it will produce an null pointer exception
	 * because the static array list is not initialised. <br>
	 * 
	 * In this function we make use of first, second and third column of final_dir.txt file.
	 */
	public void setRightContext(){
		for(ReadFinalDir fd: ReadFinalDir.finalDir){
			int pos = fd.getStart();
			//check for final state
			if(fd.getEnd() == -1) {
				Next obj = new Next();
				obj.setId(fd.getEnd());
				obj.setSym("NIL");	
				list[pos].setRightElement(obj);
				continue; 
			}
			
			Next obj = new Next();
			obj.setId(fd.getEnd());
			obj.setSym(fd.getIn());	
			list[pos].setRightElement(obj);
		}
	}
	
	/**
	 * This function creates a new list from the old list. It replaces the start and end marker with 
	 * corresponding next entries and assigns the marker appropriately. <br>
	 * For detailed description on the creation of new list, refer to the the Manual.
	 */
	public void createNewList(){
		newList = new FstLinkedList[ReadFinalDir.maxStateID];
		ids = new ArrayList<>();

		for(int i = 0; i < ReadFinalDir.maxStateID; i++){
			
			//if marker found at this id, then skip it without adding it to the new list.
			if((ids.contains(i))) continue;
			
			newList[i] = new FstLinkedList();
			ArrayList<Next> rightArray = list[i].getRight(); 
			for(Next r : rightArray){
				String sym = r.getSym();
				int id = r.getId();
					//if last element
					if(id == -1){
					newList[i].setRightElement(r);
					continue;
				}
				ArrayList<Next> rightArr = list[id].getRight();
				if(sym.equals("#1")){
					for(Next rt : rightArr){

						Next rgt = new Next(rt);
						ids.add(id);
						rgt.setMarker("b");
						newList[i].setRightElement(rgt);
					}
				}
				else if(sym.equals("#0")) {
					list[i].setRight(rightArr);
					i--;
					ids.add(id);
				}
				else{
					int flag = 0;
					for(Next rt : rightArr){
						String nextSym = rt.getSym();
						if(nextSym.equals("#0"))flag = 1;
					}
					if(flag == 1) r.setMarker("e");
					else r.setMarker("i");
					newList[i].setRightElement(r);
					}
				}
			}
		}

	void print(){
		for(int i = 0 ; i < ReadFinalDir.maxStateID ; i++){
			
			for(String s: list[i].getLeft())
				System.out.print(s + " ");
			
			System.out.print(i + " ");
			
			for(Next r : list[i].getRight())
				System.out.print(r.getSym() + "" + r.getId() + " ");
			
			System.out.println("");
		}
	}
	
	public void printNewList(){
		for(int i = 0 ; i < ReadFinalDir.maxStateID ; i++){
			if(ids.contains(i))continue;
			for(String s: newList[i].getLeft())
				System.out.print(s + " ");
			
			System.out.print(i + " ");
			
			for(Next r : newList[i].getRight())
				System.out.print(r.getSym() + "" + r.getId() + " " + r.getMarker() + "\t");
			
			System.out.println("");
		}
	}
	void setRight(ArrayList<Next> right) {
		this.right = right;
	}
	ArrayList<String> getLeft() {
		return left;
	}

	void setLeftElement(String left) {
		this.left.add(left);
	}

	ArrayList<Next> getRight() {
		return right;
	}

	void setRightElement(Next right) {
		this.right.add(right);
	}	
	
}

class Next{
	String sym;
	int id;
	String marker;

	//copy constructor
	public Next(Next copyFrom){
		this.sym = copyFrom.sym;
		this.id = copyFrom.id;
		this.marker = copyFrom.marker;
	}
	
	Next(){
		
	}
	String getMarker() {
		return marker;
	}

	void setMarker(String marker) {
		this.marker = marker;
	}

	void setSym(String s){
		this.sym = s;
	}
	
	String getSym(){
		return this.sym;
	}
	
	void setId(int i){
		this.id = i;
	}

	int getId(){
		return this.id;
	}
}