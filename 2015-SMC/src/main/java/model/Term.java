package model;

import java.util.ArrayList;

public class Term {

	private int id;
	private String name;
	private int idfcount;
	private int tfcount;
	private ArrayList<String> classes;
	private int[] classcount;
	
	

	public int[] getClasscount() {
		return classcount;
	}
	public void setClasscount(int[] classcount) {
		this.classcount = classcount;	
	}
	
	public void addClasscount(double d){
		this.classcount[(int) d] = this.classcount[(int) d]+1;
	}
	
	public ArrayList<String> getClasses() {
		return classes;
	}
	public void setClasses(ArrayList<String> classes) {
		this.classes = classes;
	}
	
	//extra method to add classes
	public void addClasses(String classX){
		//if it is new add otherwise forget
		if (this.classes.indexOf(classX) == -1){
			this.classes.add(classX);
		}
	}

	// extra method to count classes
	public int countClasses(){
		return this.classes.size();
	}

	public int getTfcount() {
		return tfcount;
	}
	public void setTfcount(int tfcount) {
		this.tfcount = tfcount;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIdfcount() {
		return idfcount;
	}
	public void setIdfcount(int idfcount) {
		this.idfcount = idfcount;
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
			Term other = (Term) obj;
			
			if(!name.equalsIgnoreCase(other.name))
				return false;
			return true;
		}
	
}
