package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class Dataset {

	static final Logger logger 			= Logger.getLogger(Dataset.class);

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
		logger.info("starting");

		String query 		= "select b.tid, c.artist_name_msd, c.title_msd, b.lyrics, a.tag, a.mood, a.score  "
				+ "from msd_lastfm a , lyricsFull b  , msd_mxm c where "
				+ "(a.mood = 'g29' or a.mood ='g25' or a.mood='g28' or a.mood='g1' or a.mood='g2' or a.mood='g6' or a.mood='g5' or a.mood='g16' or a.mood='g15' or a.mood='g17' or a.mood='g8' or a.mood='g12')  "
				+ "and (b.tid = a.tid) and (b.tid = c.tid) and (a.tid = c.tid)";

		Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/" + "mmm", "root", "123456");
		Statement statement = conexion.prepareStatement(query);
		ResultSet rs 		= statement.executeQuery(query);
		ArrayList<Song> songList = new ArrayList<Song>();

		while (rs.next()){
			String lyricsFull 	= rs.getString(4);

			// create the lyrics in human readable form
			//List<String> lyricWords = 	Arrays.asList(rs.getString(4).split(","));
			/*for(String wordI : lyricWords){
				List<String> values = Arrays.asList(wordI.split(":"));
				values.get(0);
				double tf = Double.parseDouble(values.get(1));
				for(int x = 0 ; x < tf ;  x++) lyricsFull+=" "+values.get(0);
			}*/

			Song song = new Song(rs.getString(1), rs.getString(3), rs.getString(2), rs.getString(5), rs.getDouble(7),  lyricsFull,  rs.getString(6));
			songList.add(song);
		}


		// QUADRANT GRANULARITY
		for (Song song :songList){
			String mood = song.getMood();
			String group = "";
			if (mood.equalsIgnoreCase("G29") || mood.equalsIgnoreCase("G25") ||mood.equalsIgnoreCase("G28")) group="-V+A";
			if (mood.equalsIgnoreCase("G1") ||mood.equalsIgnoreCase("G2") ||mood.equalsIgnoreCase("G6") ||mood.equalsIgnoreCase("G5")) group="+V+A";
			if (mood.equalsIgnoreCase("G16") ||mood.equalsIgnoreCase("G15") ||mood.equalsIgnoreCase("G17"))  group ="-V-A";
			if (mood.equalsIgnoreCase("G12") ||mood.equalsIgnoreCase("G8") ) group ="+V-A";
			song.setTag(group); // if we want groups
		}

		/*// GROUP GRANULARITY
		for (Song song :songList){
			song.setTag(song.getMood());
		}*/

		// SEE WHICH LABELS WE HAVE (CLASSES)
		List<String> classVal = new ArrayList<String>();
		for (Song song : songList){
			String tag = song.getTag();
			if (!classVal.contains(tag))classVal.add(tag);
		}

		for(String tag : classVal){
			logger.info(tag);
		}

		// DELETE DUPLICATES AND NON ENGLISH LYRICS
		ArrayList<Song> songListFiltered = deleteDuplicates(songList);
		createWekaFormatBinaryClassification("data_quadrants_private.arff", songListFiltered, classVal);


	}


	static void createWekaFormatBinaryClassification (String filename, ArrayList<Song> songs, List<String> classVal) throws Exception{

		int nclasses 				= classVal.size();
		ArrayList<Attribute> atts 	= new ArrayList<Attribute>(27+nclasses);
		List<String> valuesx 		= new ArrayList<String>();
		valuesx.add("1");
		valuesx.add("0");
		//valuesx.add("2");
		//valuesx.add("3");
		for(int x = 0 ; x < nclasses ; x++){
			atts.add(new Attribute("@"+classVal.get(x),valuesx));
		}

		atts.add(new Attribute("lyrics",(ArrayList<String>)null));
		Instances data = new Instances("TestInstances",atts,0);

		// ARFF format : lyrics lyrics tag id
		logger.info("nsongs"+songs.size());
		for (Song song : songs){
			double[] instanceValue1 = new double[data.numAttributes()];
			// add classes for binary classification
			for(int x = 0 ; x < nclasses ; x++){
				if(x != classVal.indexOf(song.getTag())) instanceValue1[x] = 1;
				else instanceValue1[classVal.indexOf(song.getTag())] = 0;
			}
			// ONLY FOR PRIVATE DATASET 
			// COMENT FOR PUBLIC DATASET
			//instanceValue1[0] = classVal.indexOf(song.getTag());
			// add metafeatures
			String lyrics 				= Help.stemmString(song.getLyrics());
			instanceValue1[nclasses] 	= data.attribute(nclasses).addStringValue(lyrics);
			data.add(new DenseInstance(1.0, instanceValue1));
		}

		logger.info("ninstances: "+ data.size());

		// PUT LYRICS FIRST
		// REORDER
		// FIX THIS
		Reorder reorder = new Reorder();
		reorder.setAttributeIndices("2,1,3-last");
		reorder.setInputFormat(data);
		Instances newData = Filter.useFilter(data,reorder);

		// save in ARFF file
		ArffSaver saver = new ArffSaver();
		saver.setInstances(newData);
		saver.setFile(new File(Properties.getDir()+"/"+filename));
		saver.setDestination(new File(Properties.getDir()+"/"+filename));
		saver.writeBatch();
	}


	// DELETE DUPLICATES AND NON ENGLISH WORDS
	public static ArrayList<Song> deleteDuplicates(ArrayList<Song> songList) throws LangDetectException{

		logger.info("number of songs:\t"+songList.size());

		List<String> trackIDS 				= new ArrayList<String>();
		List<String> lyrics 				= new ArrayList<String>();
		ArrayList <Song> songsFilteredI 	= new ArrayList<Song>();
		ArrayList <Song> songsFilteredII 	= new ArrayList<Song>();
		DetectorFactory.loadProfile(Properties.getPath_langdetect());

		// 1: REMOVE NON ENGLISH LYRICS
		// 2: REMOVE LYRICS WITH LESS THAN 140 CHARS (A TWEET)
		for(Song song : songList){
			String thisLyric 	=	song.getLyrics();
			Detector detector 	= DetectorFactory.create();
			detector.append(thisLyric);
			if((detector.getProbabilities().get(0).lang.equals("en")) && (thisLyric.length() > 140)){
				songsFilteredI.add(song);
			}
		}

		logger.info("number of songs after Filter 1:\t"+songsFilteredI.size());


		// 3: REMOVE REPEATED LYRICS (BY TRACKID AND BY LYRICS)
		for(Song song : songsFilteredI){
			String trackid = song.getTid();
			String thisLyric = song.getLyrics();
			if ((!trackIDS.contains(trackid)) && (!lyrics.contains(thisLyric))){
				trackIDS.add(trackid);
				lyrics.add(thisLyric);
				songsFilteredII.add(song);
			}
		}

		logger.info("number of songs after Filter 2:\t"+songsFilteredII.size());
		return songsFilteredII;
	}
}



