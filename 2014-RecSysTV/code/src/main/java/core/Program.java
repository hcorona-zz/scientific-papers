package core;

public class Program {

	private String channel;
	private String genre;
	private int id;
	private String text;
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public void addText(String text){
		this.text+=text;
	}

	public Program(String channel, String genre, int id, String text) {
		super();
		this.channel = channel;
		this.genre = genre;
		this.id = id;
		this.text = text;
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
		Program other = (Program) obj;

		if(id!=other.getId())
			return false;
		return true;
	}

}
