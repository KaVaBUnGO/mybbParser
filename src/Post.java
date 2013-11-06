
public class Post {
	private int id;
	private String date;
	private String author;
	private String content;

	Post(int id, String date, String author, String contnt) {
		this.setId(id);
		this.setDate(date);
		this.setAuthor(author);
		this.setContent(contnt);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
