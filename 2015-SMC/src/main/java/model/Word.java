package model;


public class Word {

	private int id;
	private String word;
	private String wordStemmed;

	private float arousalMean;
	private float arousalStd;
	private float arousalMedian;

	private float valenceMean;
	private float valenceStd;
	private float valenceMedian;

	private float dominanceMean;
	private float dominanceMedian;
	private float dominanceStd;

	private double idf;
	private double tf;
	private double icf_i;
	private int[] classcount;

	public int[] getClasscount() {
		return classcount;
	}
	public void setClasscount(int[] classcount) {
		this.classcount = classcount;	
	}




	public double getIcf_i() {
		return icf_i;
	}


	public void setIcf_i(double icf_i) {
		this.icf_i = icf_i;
	}


	public double getIdf() {
		return idf;
	}


	public void setIdf(double d) {
		this.idf = d;
	}


	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf;
	}





	public int getId() {
		return id;
	}





	public void setId(int id) {
		this.id = id;
	}





	public String getWord() {
		return word;
	}





	public void setWord(String word) {
		this.word = word;
	}





	public String getWordStemmed() {
		return wordStemmed;
	}





	public void setWordStemmed(String wordStemmed) {
		this.wordStemmed = wordStemmed;
	}





	public float getArousalMean() {
		return arousalMean;
	}





	public void setArousalMean(float arousalMean) {
		this.arousalMean = arousalMean;
	}





	public float getArousalStd() {
		return arousalStd;
	}





	public void setArousalStd(float arousalStd) {
		this.arousalStd = arousalStd;
	}





	public float getArousalMedian() {
		return arousalMedian;
	}





	public void setArousalMedian(float arousalMedian) {
		this.arousalMedian = arousalMedian;
	}





	public float getValenceMean() {
		return valenceMean;
	}





	public void setValenceMean(float valenceMean) {
		this.valenceMean = valenceMean;
	}





	public float getValenceStd() {
		return valenceStd;
	}





	public void setValenceStd(float valenceStd) {
		this.valenceStd = valenceStd;
	}





	public float getValenceMedian() {
		return valenceMedian;
	}





	public void setValenceMedian(float valenceMedian) {
		this.valenceMedian = valenceMedian;
	}






	public float getDominanceMean() {
		return dominanceMean;
	}





	public void setDominanceMean(float dominanceMean) {
		this.dominanceMean = dominanceMean;
	}





	public float getDominanceMedian() {
		return dominanceMedian;
	}





	public void setDominanceMedian(float dominanceMedian) {
		this.dominanceMedian = dominanceMedian;
	}





	public float getDominanceStd() {
		return dominanceStd;
	}





	public void setDominanceStd(float dominanceStd) {
		this.dominanceStd = dominanceStd;
	}





	// THEY ARE EQUAL WHEN THEY HAVE THE SAME NAME
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;

		if(!word.equalsIgnoreCase(other.word))
			return false;
		return true;
	}

	public Word(int id, String word, String wordStemmed,
			float arousalMean, float arousalStd, float arousalMedian,
			float valenceMean, float valenceStd, float valenceMedian,
			float dominanceMean, float dominanceMedian, float dominanceStd) {
		super();

		this.id = id;
		this.word = word;
		this.wordStemmed = wordStemmed;
		this.arousalMean = arousalMean;
		this.arousalStd = arousalStd;
		this.arousalMedian = arousalMedian;
		this.valenceMean = valenceMean;
		this.valenceStd = valenceStd;
		this.valenceMedian = valenceMedian;
		this.dominanceMean = dominanceMean;
		this.dominanceMedian = dominanceMedian;
		this.dominanceStd = dominanceStd;
	}

	public Word() {
		super();
	}
	@Override
	public String toString() {
		// Word	Wdnum	ValMn	ValSD	AroMn	AroSD	DomMn	DomSD
		return id + "\t" + word + "\t" + valenceMean + "\t" + valenceStd + "\t" + arousalMean 
				+ "\t" + arousalStd + "\t" + dominanceMean + "\t" + dominanceStd;

	}




}
