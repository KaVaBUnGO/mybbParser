import java.util.ArrayList;

public class Topic extends Section {
	ArrayList<Integer> posts;

	Topic(int id, String name, String url) {
		super(id, name, url);
		posts = new ArrayList<Integer>();
	}

	public void addPost(int id) {
		posts.add(id);
	}

	public ArrayList<Integer> getPosts() {
		return posts;
	}

}
