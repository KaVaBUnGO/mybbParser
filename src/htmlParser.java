import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class htmlParser {

	public static void main(String[] args) {
		Parser parser = new Parser();
		parser.run();
	}
}
