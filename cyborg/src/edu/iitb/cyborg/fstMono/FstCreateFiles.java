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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class FstCreateFiles {
	//lexiconPos 0->1 are reserved for silence and fillers
		static int lexiconPos = 2;
		static int symsPos = 0;
		static BufferedWriter wordGraph = null;
		static BufferedWriter lexicon = null;
		static BufferedWriter syms = null;
		static Map<String, String> hashMapSyms;
		//store the fillers in this arraylist
		static ArrayList<String> fillerWords;
		
		/**
		 * This function creates three text files in the current directory 
		 * <b>wordGraph.stxt</b>, <b>lexicon.stxt</b> and <b>lexicon.syms</b> from the transcription and the 
		 * dictionary.<br>
		 * The function will run provided the dictionary file has been loaded in the hashmap.<br>
		 * The format of the file is in accordance with the openFST open source project.</br>
		 * For more details of the openFST library, visit the site 
		 * <a href = "http://www.openfst.org/twiki/bin/view/FST/WebHome"> OpenFST </a> <br> <br>
		 * The mapping for &lts>, &lt/s> and &ltsil> has to be 'SIL'. This is hardcoded in the function.<br>
		 * Start silence has to be &lts>, end silence &lt/s>. <br>
		 * Start markers (#1) and end word markers (#0) have been added to get the start and end triphone.
		 *
		 * The mapping from filler to filler has to be same.
		 *  e.g. +bn+	+bn+
		 * 
		 * @param transcription
		 * @throws IOException
		 */
		public void getFstTxtFiles(String transcription,String fillerDict) throws IOException {
			//static variables have to be initialised again in  the function otherwise for 
			//multiple file alignment, the various values will come into effect.
			lexiconPos = 2;
			symsPos = 0;
			FstCreateFiles fg = new FstCreateFiles();
			hashMapSyms = new HashMap<>();
			//read the fillerDict file
			BufferedReader br = null;
			String phoneme;
			try{
				br = new BufferedReader(new FileReader(fillerDict));
				wordGraph = new BufferedWriter(new FileWriter("fst//wordGraph.stxt"));
				lexicon = new BufferedWriter(new FileWriter("fst//lexicon.stxt"));
				syms = new BufferedWriter(new FileWriter("fst//lexicon.syms"));
//				if((line = br.readLine()) == null) {
//					System.out.println("empty transcription file");
//					System.exit(0);
//				}
				
				//read the fillers and store it in the static array *fillerWords*.
				fg.readFillers(br);

				//write the silences, epsilon and fillers in the symbol file
				fg.printSilencesAndFillers();
				
				//write start and end word markers in the symbol file
				printSyms("#1");
				printSyms("#0");
				
				String words[] = string2array(transcription);	
				int totalWords = words.length;
				
				int pos = 0;
//				print start silence in wordGraph.
				printWordGraph(pos, "<s>");
				pos++;
				printFillerInWordGraph(pos);
				pos++;
				for (int i = 0; i < totalWords; i++){
					int no = 2;
					printWordGraph(pos, words[i]);
					if ((phoneme = FilesLoader.getPhonemes(words[i])) == null){
						System.out.println(words[i]
								+ " not present is the dictionary");
						System.exit(0);
					}
					fg.createHash(words[i],phoneme);
					fg.writeLexicon(phoneme,words[i]);
					//alternate pronunciations are marked as (2), (3), ...
					//e.g. hello(2), hello(3),..
					String altPronunciation = words[i] + "(" + no +")"; 
					while((phoneme = FilesLoader.getPhonemes(altPronunciation)) != null ){
						//printing the alternate pronunciation
						printWordGraph(pos, altPronunciation);
						no++;
						fg.createHash(altPronunciation,phoneme);
						fg.writeLexicon(phoneme,altPronunciation);
						altPronunciation = words[i] + "(" + no +")";
					}
					pos++;
					if(i < (totalWords-1) ){
						//printing intermediate silence/ epsilon
						printWordGraph(pos, "<eps>");
						printWordGraph(pos, "<sil>");
						pos++;	
						printFillerInWordGraph(pos);
						pos++;
					}
					
				}
//				print end fillers in wordGraph
				printFillerInWordGraph(pos);
				pos++;
				
//				print end silence in wordGraph.
				printWordGraph(pos, "</s>");
				pos++;
				
//				print silence and fillers in the lexicon file.
				printSilAndFillersInLexicon();
				
//				printing the end position in lexicon and wordGraph file in accordance with the OpenFst notations.
				lexicon.write(Integer.toString(1));
				wordGraph.write(Integer.toString(pos));

//				System.out.println("total time = " + (System.currentTimeMillis() - time1) + "ms");
			}
			finally{
				if(br != null)br.close();
				if(wordGraph != null) wordGraph.close();
				if(lexicon != null) lexicon.close();
				if(syms != null) syms.close();
				}
			
		}
		
		private void printSilAndFillersInLexicon() throws IOException {
			//printing the silences in the lexicon
			printLexicon(0, 1, "SIL", "<s>");
			printLexicon(0,1, "SIL", "</s>");
			printLexicon(0,1, "SIL", "<sil>");
			printLexicon(0, 1, "#0", "#0");
			//0,1,2 reserved for silences
			for(int i = 3 ; i < fillerWords.size(); i++)
				printLexicon(0,1, fillerWords.get(i), fillerWords.get(i));
		}

		/**
		 * This function will print the fillers in the wordGraph file
		 * in addition to the <eps> as fillers are not compulsory.
		 * @param pos
		 * @throws IOException
		 */
		private void printFillerInWordGraph(int pos) throws IOException {
			
			//0,1,2 reserved for silences
			for(int i = 3 ; i < fillerWords.size(); i++)
				printWordGraph(pos, fillerWords.get(i));
				
			printWordGraph(pos, "<eps>");
		}

		private void readFillers(BufferedReader br) throws IOException {
			
			String s[];
			fillerWords = new ArrayList<>();
			String line = null;
			if((line = br.readLine()) == null) {
				System.out.println("ERROR: empty filler Dictionary file");
				System.exit(0);
			}
			s = line.split("\\s+");
			fillerWords.add(s[0]);
			while((line = br.readLine()) != null){
				s = line.split("\\s+");
				fillerWords.add(s[0]);
			}
			
			
		}

		void printEndSilences(int pos) throws IOException{
			//printing the ending silences in the word graph
			printWordGraph(pos, "</s>");
			pos++;
			//printing the silences in the lexicon
			printLexicon(0, 1, "SIL", "<s>");
			printLexicon(0,1, "SIL", "</s>");
			printLexicon(0,1, "SIL", "<sil>");
			
			//printing the end position 
			lexicon.write(Integer.toString(1));
			wordGraph.write(Integer.toString(pos));
		}
		
		static String[] string2array(String s){
			//trim the starting the ending spaces.
			s = s.trim();
			// trim intermediate spaces to one space
			s = s.replaceAll("\\s+", " ");
			String words[] = s.split(" ");
			return words;
		}
			
		static void printWordGraph(int pos, String c) throws IOException{
			wordGraph.write(pos + " " + (pos+1) + " " + c + " " + c + "\n");
			//System.out.print(pos + " " + nextPos + " " + c + " " + d + "\n");	
		}
		
		static void printLexicon(int pos,int nextPos, String c, String d) throws IOException{
			lexicon.write(pos + " " + nextPos + " " + c + " " + d + "\n");
			//System.out.print(pos + " " + nextPos + " " + c + " " + d + "\n");	
		}
		
		static void printSyms(String c) throws IOException{
			syms.write(c + " " + symsPos + "\n");
			//System.out.print(symsPos + " " +  c + "\n");	
			symsPos++;
		}
		
		void writeLexicon(String phoneme, String word) throws IOException{
			String monophone[] = string2array(phoneme);
			int length = monophone.length;
//			#1 is the start of word marker
			printLexicon(0, lexiconPos, "#1", word);

			for(int i = 0; i < length; i++){
				printLexicon(lexiconPos, lexiconPos+1, monophone[i], "<eps>");
				lexiconPos++;
			}
//			#0 is end of word marker
			printLexicon(lexiconPos, 1, "#0", "<eps>");	
			lexiconPos++;
		}
	
		/**
		 * This function will write the silences, epsilon and fillers in the symbol file
		 * @throws IOException
		 */
		void printSilencesAndFillers() throws IOException {
			printSyms("<eps>");
			printSyms("SIL");
			
			for(String s: fillerWords){
				printSyms(s);
			}
		}

		
		void createHash(String word, String phoneme) throws IOException{
			
			if(getSymbol(word) == null) {
				putSymbol(word);
				printSyms(word);
			}
			String monophone[] = string2array(phoneme);
			for(String s:monophone){
				if(getSymbol(s) == null) {
					putSymbol(s);
					printSyms(s);
				}
			}
		}
		
		static String getSymbol(String word){
			return hashMapSyms.get(word);
		}
		
		static void putSymbol(String word){
			hashMapSyms.put(word, "1");
		}
		
		public static void main(String[] args) throws IOException, InterruptedException {
			FilesLoader filesLoader = new FilesLoader();
			filesLoader.loadDict("resources\\Dictionary.dic");
//			FstCreateFiles fc = new FstCreateFiles();
			Fst.getFST("laala basa","resources\\fillerDict.txt");
		}
}