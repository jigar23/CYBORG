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
 * @author  : Nicool
 * @contact : nicool@iitb.ac.in
 */

package edu.iitb.cyborg.aligner.util.checkPoints;

public class CheckPoint {

	String models = null;
	String fileName = null;
	String transcription = null;
	String dictionary = null;
	String inputFolder = null;
	String featureFolder = null;
	
	public boolean checkArgs(String args[]){
	
		for(int i = 0; i < args.length ; i++){
			if(args[i].equals("-models")){
				models = args[++i];
			}
			if(args[i].equals("-name")){
				fileName = args[++i];
			}
			if(args[i].equals("-in")){
				inputFolder = args[++i];
			}
			if(args[i].equals("-t")){
				transcription = args[++i];
			}
			if(args[i].equals("-dict")){
				dictionary = args[++i];
			}
			if(args[i].equals("-feat")){
				featureFolder = args[++i];
			}
			
			
		}
		if(models == null || fileName == null || inputFolder == null || transcription == null 
				|| dictionary == null || featureFolder == null){
			System.out.println("Insuffient arguments\n Usage ---> \n" +
			"java -jar \n -models <folder path which has all model files>\n " +
			"-name <input audio file without .wav extension>\n " +
			"-in <folder in which wav files are stored>\n " +
			"-feat <folder in which feature files will be stored>\n " +
			"-t <transcription file>\n " +
			"-dict <dictionary file>");
			
			return false;
		}
		
		return true;
	}
	
}
