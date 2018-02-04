package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import model.Word;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;



public class DataGenerator {

	private static final String channels = "us-fox,us-cnnhd,us-eentertainment,us-syfy,us-fnchd,us-cart,us-msnbc,us-discovery";


	// CALL ANY OF THE METHODS TO GENERATE THE DATA 
	public static void main(String[] args) throws ParseException, ClientProtocolException, IOException, org.json.simple.parser.ParseException {

		//APICalls.getAuthorization();
		getProgramStats();
		//createWekaFile();
		//getWeeklyStats();

	}

	// get the statistics per program 
	public static void getProgramStats() throws IOException{

		// INIT 
		List <Word> anew 					= StatsBank.buildANEW();
		List<String> dictionaryAnew 		= new ArrayList<String>();
		PrintWriter writer 					= new PrintWriter(new BufferedWriter(new FileWriter("files/result-programStats.csv",true)));	
		List<String> items 					= Arrays.asList(channels.split(","));
		for (Word wi : anew) dictionaryAnew.add(anew.indexOf(wi), wi.getWord()); // create the index

		for(String channel : items){

			try{

				ArrayList <Program> programList 	= new ArrayList <Program>();

				// QUERY PARAMETERS FOR API
				// TWO WEEKS IN FEBRUARY 2014 .
				// GET THE EPG!
				String url 				= "/epg/channels/"+channel+"/boxfish/72283b8f-cddd-40b1-b41c-c837a56df2f5/2014-02-07T00:00:00/2014-02-14T00:00:00/All?upcoming=true";

				// PROCESSING JSON RESULT
				JSONArray json 			= APICalls.doHttpRequest(url);
				JsonObject jsonA 		= JsonObject.readFrom(json.get(0).toString());
				JsonArray nestedArray 	= jsonA.get( "timetable" ).asArray();


				// DOING ALL THE FANCY STUFF : PROGRAM MERGING AND ANEW VALUES
				for(JsonValue jsonI : nestedArray){
					String genre ="";
					JsonObject a 	= (JsonObject) jsonI;
					if(!a.get("genre").isNull())  genre = a.get("genre").asString();
					String start 	= a.get("start").asString();
					String stop 	= a.get("stop").asString();
					String idText 	= a.get("id").asString().replaceAll("\"", "");
					int id 			= Integer.parseInt(idText);
					String text 	= APICalls.getFeatures(start, stop, anew,channel);

					Program program = new Program(channel, genre, id, text);
					// if the program was already seen add the new text
					if (programList.contains(program)){
						int index 			= programList.indexOf(program);
						Program oldProgram 	= programList.get(index);
						oldProgram.addText(text);
						programList.set(index, oldProgram);
					}
					else programList.add(program);
				}

				System.out.println(programList.size()+" programs found in channel "+channel);


				// PRINT THE RESULTS INTO A FILE
				for (Program programI : programList){
					double [] values = StatsBank.calcANEWVals(programI.getText(), anew, dictionaryAnew, StatsBank.readSentimentFiles(true), StatsBank.readSentimentFiles(false));
					String valuesText ="";
					System.out.println(programI.getText());
					for (int w = 0 ; w < 26 ; w++){
						valuesText+=values[w]+",";
					}
					System.out.println(channel+","+programI.getGenre()+","+programI.getId()+","+valuesText+" text");//+programI.getText());
					writer.println(channel+","+programI.getGenre()+","+programI.getId()+","+valuesText+programI.getText());//+programI.getText());
					writer.flush();
				}
			}
			catch(Exception e ) {System.out.println("an exception was caught!");}
		}
		writer.close();
	}



	// gets the weekly statistics per each channel
	// based on search entity counts from the boxfish API
	public static void getWeeklyStats() throws IOException{

		String dt = "2013-01-01";  // Start date 1st jan 2013
		PrintWriter writer 		= new PrintWriter(new BufferedWriter(new FileWriter("files/result-weeklyStatsX.csv",true)));	
		List <Word> anew 		= StatsBank.buildANEW();
		List<String> dictionaryAnew 		= new ArrayList<String>();
		for (Word wi : anew) dictionaryAnew.add(anew.indexOf(wi), wi.getWord()); // create the index

		String channel="us-cnnhd"; // the channel I want to query

		// QUERY ONE YEAR
		for (int i = 0 ; i <  2; i++){

			try{

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar c = Calendar.getInstance();
				c.setTime(sdf.parse(dt));	
				c.add(Calendar.WEEK_OF_MONTH, 1);  // number of weeks to add
				String newdt = sdf.format(c.getTime());  // dt is now the new date

				System.out.println("querying data for channel "+channel+" // dates "+dt.toString()+" to "+newdt.toString());

				// QUERY PARAMETERS FOR API
				String startTime 	= dt.toString()+"T00:00:00";
				String endTime 		= newdt.toString()+"T00:00:00";
				String text 		= APICalls.getFeatures(startTime, endTime,anew, channel); // api call
				double [] values = StatsBank.calcANEWVals(text, anew,dictionaryAnew, StatsBank.readSentimentFiles(true), StatsBank.readSentimentFiles(false));

				// creates a string with the metafeatures
				String valuesText ="";
				for (int w = 0 ; w < 26 ; w++) valuesText+=values[w]+",";
				//writer.println(channel+","+startTime+","+endTime+","+values[5]); // prints the channel, metafeatures and text.
				System.out.println(channel+","+startTime+","+endTime+","+valuesText);
				writer.flush();
				dt = newdt; //update the date

			}
			catch(Exception e){
				System.out.println("something went wrong, and i have no clue what it was");
			}
		}
		writer.close();
	}


	// create the weka files based on the programStats file 
	// to classify programs genres
	public static void createWekaFile() throws IOException {

		// read the source file
		// get the fields 
		// filter by fields : 
		// channel = (fox, fox news, e!, cnn, cartoon network, discovery, msnnbc)
		// number_words >= 600
		// genre : 

		BufferedReader file	= new BufferedReader(new FileReader("files/result-programStats.csv"));
		PrintWriter writer 	= new PrintWriter(new BufferedWriter(new FileWriter("files/result-tvprograms.arff")));	

		writer.println("@relation tvprograms.arff");
		writer.println("@attribute @channel@ {us-discovery,us-msnbc,us-cart,us-cbs,us-cnnhd,us-eentertainment,us-fnchd,us-fox,us-syfy}");
		writer.println("@attribute @@class@@ {[animated],[documentary],[horror],[newscast],[reality]}");
		writer.println("@attribute id numeric");
		for (int n = 0 ; n < 25 ; n++) writer.println("@attribute stat"+n+" numeric");
		writer.println("@attribute text string");
		writer.println(" ");
		writer.println("@data");

		String strLine;
		int  i=0;
		while ((strLine = file.readLine()) != null){

			boolean channelOK 	= false;
			boolean genreOK 	= false;
			boolean nwordsOK 	= false;
			List<String> items 	= Arrays.asList(strLine.split(","));
			String channel 		= items.get(0);
			String genre 		= items.get(1);
			double nwords 		= Double.parseDouble(items.get(3));


			if ((channel.equalsIgnoreCase("us-discovery") || channel.equalsIgnoreCase("us-msnbc") || channel.equalsIgnoreCase("us-cart") || channel.equalsIgnoreCase("us-cbs") || channel.equalsIgnoreCase("us-cnnhd") || channel.equalsIgnoreCase("us-eentertainment") || channel.equalsIgnoreCase("us-fnchd") ||channel.equalsIgnoreCase("us-fnchd") || channel.equalsIgnoreCase("us-fox") || channel.equalsIgnoreCase("us-syfy"))){
				channelOK = true;
			}

			if((genre.equalsIgnoreCase("[animated]") || genre.equalsIgnoreCase("[documentary]") ||genre.equalsIgnoreCase("[horror]") ||genre.equalsIgnoreCase("[newscast]") ||genre.equalsIgnoreCase("[reality]"))){
				genreOK =  true;

			}

			if(nwords > 0){
				nwordsOK=true;
			}

			//System.out.println(channelOK+","+genreOK+","+nwordsOK);
			//System.out.println(channel+","+genre+","+nwords);
			if (genreOK && channelOK && nwordsOK){

				String instance = "";
				for(int x = 0 ; x < items.size()-2; x++) instance+=items.get(x).toString()+",";
				writer.println(instance+"'"+items.get(items.size() -1).toString()+"'");
				i++;
			}
		}
		writer.close();
		file.close();
	}
}


