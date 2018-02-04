package model;

import java.util.ArrayList;

public class MyResult {

	private String className;
	private ArrayList<Double> resultArray = new ArrayList<Double>();

	// THEY ARE EQUAL WHEN THEY HAVE THE SAME NAME
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyResult other = (MyResult) obj;

		if(!className.equalsIgnoreCase(other.className))
			return false;
		return true;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public ArrayList<Double> getResultArray() {
		return resultArray;
	}

	public void setResultArray(ArrayList<Double> resultArray) {
		this.resultArray = resultArray;
	}

	@Override
	public String toString() {

		String arr = "";
		for(Double d:this.getResultArray()) {
			arr+= String.format( "%.3f", d );
			arr+= " & ";
			// prints [Tommy, tiger]
		}



		return className.substring(1, className.length()) + " & "
		+ arr.substring(0, arr.length()-2) + "\\\\ \\"+"hline";
	}






}
