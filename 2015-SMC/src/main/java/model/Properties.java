package model;

public class Properties {


	final static String dir 			= System.getProperty("user.dir");
	private static String path_ANEW 			= dir+"/resources/ANEW2010All.txt";
	private static String Path_WN 		= "/usr/local/WordNet-3.0/dict"; 
	private static String stopWordsPath = dir+"/resources/stop.txt";
	private static String stopWordsPathANEW = dir+"/resources/stopANEW.txt";
	private static String wnhome 		= System.getenv("WNHOME");
	private static String path_langdetect = "/Users/hcorona/Google Drive/software/langdetect-09-13-2011/profiles";


	

	public static String getStopWordsPathANEW() {
		return stopWordsPathANEW;
	}

	public static String getPath_langdetect() {
		return path_langdetect;
	}

	public static String getDir() {
		return dir;
	}

	public static String getPath_ANEW() {
		return path_ANEW;
	}
	public static String getPath_WN() {
		return Path_WN;
	}
	public static String getStopWordsPath() {
		return stopWordsPath;
	}
	public static String getWnhome() {
		return wnhome;
	}




}
