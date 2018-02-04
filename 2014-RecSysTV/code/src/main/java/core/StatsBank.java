package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Word;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * @author hcorona
 *
 */
public class StatsBank {


	/**
	 * @author Humberto Corona
	 * Calculates the ANEW features based on a String input file. 
	 * @param negativeTerms 
	 * @param positiveTerms 
	 * 
	 */

	public static double[] calcANEWVals(String fullText, List<Word> anew, List<String> dictionary, ArrayList<String> positiveTerms, ArrayList<String> negativeTerms) throws IOException {

		// READ THE LYRICS BODY AND CALC STATS
		DescriptiveStatistics statsValence 		= new DescriptiveStatistics();
		DescriptiveStatistics statsArousal 		= new DescriptiveStatistics();
		DescriptiveStatistics statsDominance 	= new DescriptiveStatistics();
		String lyricsStemmed 					= fullText; //stemmString(lyricsBody); // stemm the whole string
		//System.out.println(lyricsStemmed);
		double[] tf_values 						= new double[dictionary.size()];
		tf_values 								= StatsBank.TF(lyricsStemmed, dictionary);
		List<String> fields						= Arrays.asList(lyricsStemmed.toLowerCase().split(" "));
		int counter = 0;

		// initialize
		double [] stats = new double[29];


		for(String wordX : fields){

			if (dictionary.indexOf(wordX) != -1 ) {

				counter++;				
				int	x = dictionary.indexOf(wordX);	
				double a 	= anew.get(x).getArousalMean()/9;
				double v 	= anew.get(x).getValenceMean()/9;
				double d 	= anew.get(x).getDominanceMean()/9;

				statsArousal.addValue(a);
				statsValence.addValue(v);
				statsDominance.addValue(d);

			}
		}

		List<String> uniqueterms = new ArrayList<String>();
		for (String w : fields){
			if (!uniqueterms.contains(w))
				uniqueterms.add(w);
		}

		int uniqueWords = uniqueterms.size();
		uniqueterms.retainAll(dictionary);
		int uniqueAnew = uniqueterms.size();

		double sent [] = StatsBank.sentiment(lyricsStemmed, positiveTerms, negativeTerms);

		stats[0] 	= Arrays.asList(lyricsStemmed.split(" ")).size(); //number of words in total
		stats[1] 	= counter; //number of anew words
		stats[2]   	= uniqueWords; //number of unique words in text
		stats[3] 	= uniqueAnew; // number of unique anew words
		stats[4]   	= findMax(tf_values); //number of times the max word appears 

		stats[5] 	= statsValence.getMean();
		stats[6] 	= statsValence.getStandardDeviation();
		stats[7]	= statsValence.getMax();
		stats[8] 	= statsValence.getMin();
		stats[9]	= 0;
		stats[10]	= statsValence.getVariance();
		stats[11] 	= statsValence.getPercentile(50);

		stats[12] 	= statsArousal.getMean();
		stats[13] 	= statsArousal.getStandardDeviation();
		stats[14] 	= statsArousal.getMax();
		stats[15]	= statsArousal.getMin();
		stats[16]	= 0;
		stats[17]	= statsArousal.getVariance();
		stats[18] 	= statsArousal.getPercentile(50);

		stats[19] 	= statsDominance.getMean();
		stats[20] 	= statsDominance.getStandardDeviation();
		stats[21] 	= statsDominance.getMax();
		stats[22]	= statsDominance.getMin();
		stats[23]	= 0;
		stats[24]	= statsDominance.getVariance();
		stats[25] 	= statsDominance.getPercentile(50);

		stats[26] 	= sent[0];
		stats[27]   = sent[1];
		stats[28] 	= sent[2]+1;

		// if i can't find the stats just set them to zero
		if(counter == 0) for (int n = 0 ; n< stats.length ; n++) {stats[n] =0;}


		return stats;
	}



	/**
	 * @param input a string with lyrics
	 * @return output : a string with the stemmed lyrics
	 */


	public static String stemmString(String input){

		PorterStemmer stemmer = new PorterStemmer();
		String inputII =	input.replaceAll("[^a-zA-Z\\s]", "");

		List<String> fields = Arrays.asList(inputII.toLowerCase().split(" "));
		String output = "";
		for (String x : fields){
			stemmer.setCurrent(x);
			stemmer.stem();
			//System.out.println(x +"->"+stemmer.getCurrent());
			String stemmedWord = stemmer.getCurrent();
			output+=stemmedWord+" ";	
		}
		return output;
	}



	/**
	 * @return a list with the anew dataset to use in the calculation of features
	 * @throws IOException
	 * Reads the original ANEW dataset, extends it with wordnet (using JWI) synonims (using stemming) 
	 * giving around 3K words
	 */
	public static List<Word> buildANEW () throws IOException{

		// inits
		BufferedReader 	br 		= new BufferedReader(new FileReader("files/ANEW2010All.txt"));
		String strLine 			= "";
		List<String> fields 	= new ArrayList<String>();
		List<Word> anew 		= new ArrayList<Word>();
		List<String> stop 		= new ArrayList<String>();


		BufferedReader 	brStop 		= new BufferedReader(new FileReader("files/StopWords.txt"));
		while ((strLine = brStop.readLine()) != null)   {
			stop.add(strLine);
		}




		// read the ANEW dataset FIRST!
		//FILE FORMAT: Word,Wdnum,ValMn,ValSD,AroMn,AroSD,DomMn,DomSD,WordStemmed
		br.readLine();
		while ((strLine = br.readLine()) != null)   {
			fields 		= Arrays.asList(strLine.split("\t"));
			Word wordI 	= new Word();
			wordI.setWord(fields.get(0));
			wordI.setId(Integer.parseInt(fields.get(1)));
			wordI.setValenceMean(Float.parseFloat(fields.get(2)));
			wordI.setValenceStd(Float.parseFloat(fields.get(3)));
			wordI.setArousalMean(Float.parseFloat(fields.get(4)));
			wordI.setArousalStd(Float.parseFloat(fields.get(5)));
			wordI.setDominanceMean(Float.parseFloat(fields.get(6)));
			wordI.setDominanceStd(Float.parseFloat(fields.get(7)));
			wordI.setTf(1.0);
			wordI.setIdf(1.0);
			// there are repeated words in the dataset. why ? 
			if (!anew.contains(wordI)){
				anew.add(wordI);
			}
		}

		br.close();
		brStop.close();
		return anew;

	}


	// calculate TF
	public static double[]  TF (String document, List<String> dictionary){

		// stem the lyrics and split them 
		List<String>  fields = Arrays.asList(stemmString(document).split(" "));
		double[] tf_values = new double[dictionary.size()];
		for (int x=0 ; x<dictionary.size();x++){
			tf_values[x]=0;
		}

		// store each word
		for(String fieldI : fields){
			if (dictionary.indexOf(fieldI) != -1){
				int index = dictionary.indexOf(fieldI);
				tf_values[index]++;
			}
		}

		return tf_values;
	}


	public static double findMax (double[] myArray){
		double max = 0;
		for(int x = 0 ; x < myArray.length ; x++){
			if (myArray[x] > max) max = myArray[x];
		}
		return max;
	}

	public static double[]  sentiment(String lyrics, ArrayList<String> positiveTerms, ArrayList<String> negativeTerms) throws IOException{

		double positive = 0;
		double negative = 0;
		double [] sent = new double[3];

		sent[0]=0;
		sent[1]=0;
		sent[2]=0;


		for (String x : Arrays.asList(lyrics.split(" "))){
			if (positiveTerms.contains(x)) positive++;
			if (negativeTerms.contains(x)) negative++;
			//System.out.println(x+ " "+ sentiwordnet.extract(x, "n"));
		}

		//System.out.println(positive+","+negative+","+(positive+(-1)*negative)/(positive+negative)*2.0);
		if (positive+negative > 0){
			sent[0]=positive;
			sent[1]=negative;
			sent[2]=(positive+(-1)*negative)/(positive+negative);
		}
		return sent;
	}

	public static ArrayList<String> readSentimentFiles(boolean positive) throws IOException{

		String whichFile = "positive";
		if (!positive) whichFile = "negative";

		BufferedReader file 	= new BufferedReader(new FileReader("files/opinion-lexicon-English/"+whichFile+"-words.txt"));
		ArrayList<String> terms = new ArrayList<String>();
		String strLine = "";
		while ((strLine = file.readLine()) != null){

			if (!strLine.equals(";")){
				String stemmed = stemmString(strLine);
				stemmed = stemmed.substring(0, stemmed.length()-1);
				terms.add(stemmed);
			}
		}
		file.close();
		return terms;
	}

}
