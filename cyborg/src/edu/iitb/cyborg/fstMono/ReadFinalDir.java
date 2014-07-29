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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadFinalDir {
	int start;
	int end;
	String in;
	String op;
	static ArrayList<ReadFinalDir> finalDir;
	//this variable stores the maxID present in the Fst final_dir.txt file
	static int maxStateID;
	
	/**
	 * This function read the final_dir.txt file present in the fst folder line by line
	 * and stores it in an object of class ReadFinalDir. It stores it in following way: <br>
	 * Start End  In  Out <br>
	 * where <br> 
	 * start -> transition from <br>
	 * end -> transition to <br>
	 * in -> input symbol <br>
	 * out -> output symbol <br>
	 * @throws IOException
	 */
	public void readFile() throws IOException{
		maxStateID = 0;
		finalDir = new ArrayList<>();
		BufferedReader br = null;
		String line = null;
		String words[];
		try{
			br = new BufferedReader(new FileReader("fst//final_dir.txt"));
			while((line = br.readLine()) != null){
				words = line.split("\\s+");
				ReadFinalDir obj = new ReadFinalDir();
				if(words.length > 1){
					int start = Integer.parseInt(words[0]);
					if(maxStateID < start) maxStateID = start;
					int end= Integer.parseInt(words[1]);
					if(maxStateID < end) maxStateID = end;
					obj.setStart(start);
					obj.setEnd(end);
					obj.setIn(words[2]);
					obj.setOp(words[3]);
				}else{
					obj.setStart(Integer.parseInt(words[0]));
					//for the end of state
//					finalState = Integer.parseInt(words[0]);
					obj.setEnd(-1);
				}
				
				finalDir.add(obj);
			}
			maxStateID++;	
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br != null)br.close();
		}
		
	}

	int getStart() {
		return start;
	}

	void setStart(int start) {
		this.start = start;
	}

	int getEnd() {
		return end;
	}

	void setEnd(int end) {
		this.end = end;
	}

	String getIn() {
		return in;
	}

	void setIn(String in) {
		this.in = in;
	}

	String getOp() {
		return op;
	}

	void setOp(String op) {
		this.op = op;
	}
}
