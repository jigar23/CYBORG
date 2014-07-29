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

package edu.iitb.cyborg.aligner.util.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class Results {
	
	public void printPhoneSegInConsole(int stateDuration[],String phonemSeq[], String triphoneSeq[], double[] scoreArray)
	{
		System.out.println();
		System.out.println("Phone segmentation:");
		//System.out.println();
		System.out.println("SFrm\tEFrm\tSegAScr\t\tPhone");
		int SFrm = 0;
		int EFrm = 0;
		int noOfFrames = 0;
		int totalNoOfFrames = 0;
		double totalScore = 0;
		double score = 0;
		String triPhone = "";
		//String phonemsOp = "";
		
		NumberFormat numberFormator = new DecimalFormat("#0.0000");
		
		for(int i = 0; i < phonemSeq.length/3; i++){
			
			//phonemsOp += phonemSeq[i]+" ";
			
			noOfFrames 	= stateDuration[i*3] + stateDuration[i*3+1] + stateDuration[i*3+2];
			score = scoreArray[i*3] + scoreArray[i*3+1] + scoreArray[i*3+2];
			totalScore+=score;
			totalNoOfFrames += noOfFrames;
			EFrm = totalNoOfFrames - 1;
			
			if(triphoneSeq[i].split("\t")[1].equals("-") && triphoneSeq[i].split("\t")[2].equals("-"))
				triPhone=triphoneSeq[i].split("\t")[0];
			else
				triPhone=triphoneSeq[i].replaceAll("\t", " ");
				
			System.out.println(SFrm+"\t"+EFrm+"\t"+numberFormator.format(score)+"\t"+triPhone);
			SFrm = totalNoOfFrames;
		}
		System.out.println("Total score:\t"+numberFormator.format(totalScore));
//		System.out.println();
//		System.out.println(phonemsOp);
	}
	
	public void printPhoneSegInFile(int stateDuration[],String phonemSeq[], String triphoneSeq[], double[] scoreArray, String phsegFolder, String fileId) throws IOException
	{
		File resultFile = new File(phsegFolder+"/"+fileId+".phseg");
		
		FileWriter resultFileWriter = new FileWriter(resultFile);

		resultFileWriter.write("SFrm\tEFrm\tSegAScr\t\tPhone");
		int SFrm = 0;
		int EFrm = 0;
		int noOfFrames = 0;
		int totalNoOfFrames = 0;
		double totalScore = 0;
		double score = 0;
		String triPhone = "";
		//String phonemsOp = "";
		
		NumberFormat numberFormator = new DecimalFormat("#0.0000");
		
		for(int i = 0; i < phonemSeq.length/3; i++){

			//phonemsOp += phonemSeq[i]+" ";
			
			noOfFrames 	= stateDuration[i*3] + stateDuration[i*3+1] + stateDuration[i*3+2];
			score = scoreArray[i*3] + scoreArray[i*3+1] + scoreArray[i*3+2];
			totalScore+=score;
			totalNoOfFrames += noOfFrames;
			EFrm = totalNoOfFrames - 1;
			
			if(triphoneSeq[i].split("\t")[1].equals("-") && triphoneSeq[i].split("\t")[2].equals("-"))
				triPhone=triphoneSeq[i].split("\t")[0];
			else
				triPhone=triphoneSeq[i].replaceAll("\t", " ");
				
			resultFileWriter.write("\n");
			resultFileWriter.write(SFrm+"\t"+EFrm+"\t"+numberFormator.format(score)+"\t"+triPhone);
			SFrm = totalNoOfFrames;
		}
		resultFileWriter.write("\n\n");
		resultFileWriter.write("Total score:\t"+numberFormator.format(totalScore));
//		resultFileWriter.write("\n\n");
//		resultFileWriter.write(phonemsOp);
		resultFileWriter.close();
	}
	
}
