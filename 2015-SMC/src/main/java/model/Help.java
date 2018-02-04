package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * @author Humberto Corona 
 *
 */
public class Help {


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

	public static String stemmWord(String input){
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(input.replaceAll("[^a-zA-Z\\s]", ""));
		stemmer.stem();
		return stemmer.getCurrent();
	}


}
