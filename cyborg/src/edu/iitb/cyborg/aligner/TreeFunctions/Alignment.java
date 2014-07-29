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

package edu.iitb.cyborg.aligner.TreeFunctions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.cyborg.aligner.util.checkPoints.CheckPoint;
import edu.iitb.cyborg.performance.Performance;

public class Alignment {
	
	//parameters to set for alignment.
	//feature parameters
	public static String cmn = "yes"; // cepstral mean normalization, default value = "yes". Enter "no" to disable cmn.
	public static String useDelta = "yes"; // delta and double delta features, default value = "yes". Enter "no" to just use 13 features. 
	//other feature parameters have to be set in the file FeatureFileExtractor.java in the package edu.iitb.frontend.audio.feature.
	
	//model parameters
	public static float varFloor = (float) 0.0001; //variance flooring. By default value is 0.0001.
	public static float mixwFloor = (float) 0.0000001; //mixture weight flooring. By default value is 0.0000001.
	public static float tmatFloor = (float) 0.0001; //mixture weight flooring. By default value is 0.0001.
	
	//pruning parameters
	public static String prune = "yes"; // pruning the paths, default value = "yes". Enter "no" to disable pruning.
	public static float beamWidth = (float) -126.64; // beam width for pruning. default value is -126.64 (ln(10^-55)).
	
	public static void main(String[] args) throws IOException, IllegalArgumentException, UnsupportedAudioFileException, InterruptedException {

		Performance.logStartTime();
		
		//String home = System.getProperty("user.home");
		String currentDir = "./";
		String models = null;
		String fileID = null;
		String transFile = null;
		String dictionary = null;
		String inputFolder = currentDir;
		String featureFolder = currentDir;
		String phsegFolder = currentDir;
		String wdsegFolder = currentDir;
		String triPhnSearch = "yes";
		
		CheckPoint checkPoint = new CheckPoint();
		
//		if(checkPoint.checkArgs(args)){
			for(int i = 0; i < args.length ; i++){
				if(args[i].equals("-version")){
					System.out.println("Cyborg version 1.1");
					System.exit(0);
				}
				if(args[i].equals("-help")){
					System.out.println("Arguments list definition:");
					System.out.println("[NAME]\t[DEFLT]\t[DESCR]");
					
					System.out.println("-models\t\tInput model files directory");
					System.out.println("-ctl\t\tControl file listing utterances to be processed");
					System.out.println("-t\t\tInput transcriptions file corresponds to audio");
					System.out.println("-dict\t\tMain pronunciation dictionary (lexicon) input file");
					System.out.println("-in\t.\tInput audio wav file(s) directory");
					System.out.println("-cepdir\t.\tInput cepstrum files directory");
					System.out.println("-triphn\tyes\tDetermines whether to use triphone search or monophone search");
					System.out.println("-prune\tyes\tPruning some paths during viterbi search");	
					
					System.out.println("-phseg\t.\tOutput directory for phone segmentation files");
					System.out.println("-wdseg\t.\tOutput directory for word segmentation files");
					System.exit(0);
				}
				if(args[i].equals("-models")){
					models = args[++i];
				}
				if(args[i].equals("-ctl")){
					fileID = args[++i];
				}
				if(args[i].equals("-in")){
					inputFolder = args[++i];
				}
				if(args[i].equals("-t")){
					transFile = args[++i];
				}
				if(args[i].equals("-dict")){
					dictionary = args[++i];
				}
				if(args[i].equals("-cepdir")){
					featureFolder = args[++i];
				}
				if(args[i].equals("-phseg")){
					phsegFolder = args[++i];
				}
				if(args[i].equals("-wdseg")){
					wdsegFolder = args[++i];
				}
				if(args[i].equals("-triphn")){
					triPhnSearch = args[++i];
				}
				if(args[i].equals("-prune")){
					prune = args[++i];
				}				
	
			}
			
			if(models == null || fileID == null || transFile == null || dictionary == null){
				System.out.println("Arguments list definition:");
				System.out.println("[NAME]\t[DEFLT]\t[DESCR]");
				
				System.out.println("-models\t\tInput model files directory");
				System.out.println("-ctl\t\tControl file listing utterances to be processed");
				System.out.println("-t\t\tInput transcriptions file corresponds to audio");
				System.out.println("-dict\t\tMain pronunciation dictionary (lexicon) input file");
				System.out.println("-in\t.\tInput audio wav file(s) directory");
				System.out.println("-cepdir\t.\tInput cepstrum files directory");
				System.out.println("-triphn\tyes\tDetermines whether to use triphone search or monophone search");
				System.out.println("-prune\tyes\tPruning some paths during viterbi search");	
				
				System.out.println("-phseg\t.\tOutput directory for phone segmentation files");
				System.out.println("-wdseg\t.\tOutput directory for word segmentation files");
				System.out.println();
				System.out.println("ERROR: Incorrect argument list,\n -models, -ctl, -t, -dict are mendatory.");
				System.exit(0);
			}
			
			System.out.println(transFile);
			
			
		FilesLoader filesLoader = new FilesLoader();
		Aligner align = new Aligner();
		// load and initialise all the models (mean,variance, mixture weights and transition matrix) in the array
		filesLoader.initialize(models);
		filesLoader.loadDict(dictionary);
		filesLoader.loadMdef(models+"/mdef_tab");
		
		
//		To check if the fileIds and transcription file are proper
		BufferedReader readTransFile = null;
		BufferedReader readFileID = null;
		String fileIdLine = null;
		String transLine = null;
		try{
//			read the transcriptions and fileIDs file.
			readFileID = new BufferedReader(new FileReader(fileID));
			readTransFile = new BufferedReader(new FileReader(transFile));
			int lineNo = 0;
			while((fileIdLine = readFileID.readLine()) != null){
//				remove extra spaces at beginning and end
				String fileId = fileIdLine.trim();
				lineNo++;
				if((transLine = readTransFile.readLine()) != null){
					
//					the fileID in each line of transcription file which is in (*) should match
//					with the fileId in the fileIDs file otherwise produce an error.
					String[] s = transLine.split("\\(");
					//System.out.println(transLine);	
					String id = s[1].replace(")", "");
					if(fileId.equals(id)){
						String trans = s[0];
						//System.out.println(trans);
//						aligning the file one at a time.
						System.out.println("\n");
						align.doAlignment(fileId, trans, inputFolder, featureFolder, phsegFolder, wdsegFolder,triPhnSearch);

					}else{
						System.out.println("ERROR: transcription and fileID mismatch at line "
										+ lineNo + "!!!");
						System.exit(0);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out
					.println("ERROR: check your transcription/ fileIDS file");
		}
		finally{
			if(readFileID != null)readFileID.close();
			if(readTransFile != null)readTransFile.close();
		}

		
		Performance.logEndTime();
		Performance.logEndTimeInFile();
		Performance.memInfo();
		Performance.memInfoInFile();
		
	}
}

