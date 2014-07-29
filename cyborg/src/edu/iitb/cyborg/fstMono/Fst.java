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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class Fst {
	
	
	
	/**
	 *This function takes the dictionary and transcription as input and 
	 *creates a creates an FST folder.<br>
	 *Fst folder contains <b>final_dir.txt</b>. This file is created after many FST operations.<br>
	 * For list of operations and procedure, refer to the manual.
	 *   
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */		
public static void getFST(String transcription, String fillerDict) throws IOException, InterruptedException{
		
		//-------- stores the dictionary file in hashmap ---------//
//		FilesLoader filesLoader = new FilesLoader();
//		filesLoader.loadDict(dictionary);
//		The dictionary has been loaded in the main file.
	
	
		File file = new File("fst");
		if(!file.exists()){
			if(file.mkdir()) System.out.println("fst directory created");
		}else{System.out.println("fst directory exists.");}
		
//		if(file.exists()){
//			file.delete();
//			if(file.mkdir()) System.out.println("fst directory created");
//		}
		
		long time1 = System.currentTimeMillis();
		//FstGrammar fg = new FstGrammar();
		FstCreateFiles fg = new FstCreateFiles();
		fg.getFstTxtFiles(transcription, fillerDict);
		
		//creating the fst file for wordGraph..
		ProcessBuilder builder = new ProcessBuilder("cmd.exe","/c","fstcompile " +
				"--isymbols=fst\\lexicon.syms --osymbols=fst\\lexicon.syms fst\\wordGraph.stxt" +
				" | fstrmepsilon | fstarcsort > fst\\wordGraph_dir.fst");
		builder.redirectErrorStream(true);
		Process p = builder.start();
		int status = p.waitFor();
		if(status == 1){
			//reading the command lines
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String line;
	        while (true) {
	            line = br.readLine();
	            if (line == null) { 	
	            	break; }
	            System.out.println(line);
	        }
			System.out.println("ERROR: fst file for wordGraph not created..!!!");
			System.exit(0);
		} 
//		else{
//			System.out.println("ERROORRR in the wordGraph_dir.fst file :( ...!!!!!");
//		}

		
      //creating the fst file for lexicon..
      		builder = new ProcessBuilder("cmd.exe","/c","fstcompile --isymbols=fst\\lexicon.syms --osymbols=fst\\lexicon.syms " +
      				"fst\\lexicon.stxt | fstclosure | fstarcsort > fst\\lexicon_dir.fst");
      		builder.redirectErrorStream(true);
      		p = builder.start();
      		status = p.waitFor();
      		if(status == 1){
      			//reading the command lines
      			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      	        String line;
      	        while (true) {
      	            line = br.readLine();
      	            if (line == null) { 
      	            	
      	            	break; }
      	            System.out.println(line);
      	        }
      			System.out.println("ERROR: fst file for lexicon not created..!!!");
      			System.exit(0);
//      			System.out.println("lexicon_dir.fst file created sucessfully :)\n");
      		} 
//      		else{
//      			System.out.println("ERROORRRRRR in the lexicon_dir.fst file :( ...!!!!!");
//      		}
              
 
            //composing the fst file for lexicon..
        		builder = new ProcessBuilder("cmd.exe","/c","fstcompose fst\\lexicon_dir.fst fst\\wordGraph_dir.fst | " +
        				"fstrmepsilon | fstdeterminize | fstminimize > fst\\final_dir.fst");
        		builder.redirectErrorStream(true);
        		p = builder.start();
        		status = p.waitFor();
        		if(status == 1){
        			//reading the command lines
        			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	        String line;
        	        while (true) {
        	            line = br.readLine();
        	            if (line == null) { 
        	            	
        	            	break; }
        	            System.out.println(line);
        	        }
        			System.out.println("ERROR: composed final fst not created..!!!");
        			System.exit(0);
        		} 

                
           //print the final  fst file
                builder = new ProcessBuilder("cmd.exe","/c","fstprint --isymbols=fst\\lexicon.syms " +
                		"--osymbols=fst\\lexicon.syms fst\\final_dir.fst fst\\final_dir.txt");
        		builder.redirectErrorStream(true);
        		p = builder.start();
        		p.waitFor();
        		if(status == 1){
        			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	        String line;
        	        while (true) {
        	            line = br.readLine();
        	            if (line == null) { 
        	            	
        	            	break; }
        	            System.out.println(line);
        	        }
        			System.out.println("ERROR: composed final fst not created..!!!");
        			System.exit(0);
        		}
//        		System.out.println("final_dir.txt file created sucessfully :)");
        		 System.out.println(System.currentTimeMillis() - time1);
	}

}

