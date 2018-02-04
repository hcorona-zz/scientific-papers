package model;

import java.util.ArrayList;
import java.util.Arrays;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

public class Song {

	private String artist;
	private String title;
	private String tid;
	private double score;
	private String lyrics;
	private String tag;
	private String mood;
	private String lfIds;


	public String getLfIds() {
		return lfIds;
	}




	public void setLfIds(String lfIds) {
		this.lfIds = lfIds;
	}


	@Override
	public String toString() {
		return "Song [artist=" + artist + ", title=" + title + ", tid=" + tid
				+ ", score=" + score + ", lyrics=" + lyrics + ", tag=" + tag
				+ ", mood=" + mood + "]";
	}




	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getMood() {
		return mood;
	}
	public void setMood(String mood) {
		this.mood = mood;
	}
	public String getLyrics() {
		return lyrics;
	}
	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}



	public boolean getLanguage () throws LangDetectException{
		// REMOVE NON ENGLISH LYRICS
		DetectorFactory.loadProfile("/Users/hcorona/Dropbox/langdetect-09-13-2011/profiles");
		boolean language = false;
		Detector detector 	= DetectorFactory.create();
		detector.append(this.lyrics);
		ArrayList<Language> prob = detector.getProbabilities();
		if (prob.get(0).lang.equals("en")) language= true;
		return language;

	}

	public Song(String tid, String title, String artist, String tag,
			double score, String lyrics, String mood) {
		super();
		this.artist = artist;
		this.title = title;
		this.tid = tid;
		this.score = score;
		this.lyrics = lyrics;
		this.tag = tag;
		this.mood = mood;
	}


}
