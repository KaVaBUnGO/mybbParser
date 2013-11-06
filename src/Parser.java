import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {
	public String url = "http://anime4you.mybb.ru/";
	Document doc;
	private PrintWriter pw;
	
	public void connectUrl(String url) {
		this.url = url;
		connect();
	}

	private void connect() {
		try {
			doc = Jsoup
					.connect(url)
					.userAgent("Mozilla")
					.timeout(3000)
					.cookie("mybb_ru",
							"MTU0MHwzfDIzM2UwYjllNWEzYjhmODkwMWFkNDMzYjVlM2UxYWIzNzAzZmJiMGU%3D")
					.get();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			pw = new PrintWriter("output.txt");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		connect();
		HashMap<Integer, ArrayList<Integer>> sectionDependencies = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Section> sections = new ArrayList<Section>();
		HashSet<Integer> hs = new HashSet<Integer>();
		Topic topics[] = new Topic[100000];
		// Забираем темы верхнего уровня
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			if (checkUrlIsSection(link.attr("href"))) {
				String url = link.attr("href");
				Section section = new Section(Integer.valueOf(url.substring(url
						.indexOf('=') + 1)), link.text(), url.substring(0,
						url.indexOf('=')));
				sections.add(section);
				if (!sectionDependencies.keySet().contains(section.getId()))
					sectionDependencies.put(section.getId(),
							new ArrayList<Integer>());
				// System.out.print(link.attr("href") + "    ");
				// System.out.println(link.text());
			}
		}
		// System.out.println(sectionDependencies.size());
		// Пробегаем по темам верхнего уровня и забираем темы нижнего уровня
		int count = 0;
		for (int id : sectionDependencies.keySet()) {
			// System.out.print(id + ": ");
			int pages = 1;
			String urlPattern = "http://anime4you.mybb.ru/viewforum.php?id=";
			String curUrl = urlPattern.concat(String.valueOf(id));
			connectUrl(curUrl);
			Elements topPaginator = doc.getElementsByClass("linkst");
			for (Element e : topPaginator) {
				Elements hrefs = e.getElementsByTag("a");

				for (Element el : hrefs) {
					try {
						int pageNumber = Integer.parseInt(el.text());
						if (pageNumber > pages)
							pages = pageNumber;
					} catch (NumberFormatException exeption) {
						continue;
					}
				}

			}
			for (int i = 1; i <= pages; i++) {
				String url1 = curUrl.concat("&p=".concat(String.valueOf(i)));
				connectUrl(url1);
				links = doc.select("a[href]");
				for (Element link : links) {
					if (checlUrlIsTopic(link.attr("href"))
							&& !hs.contains(link.attr("href").hashCode())) {
						String url = link.attr("href");
						Topic topic = new Topic(Integer.valueOf(url
								.substring(url.indexOf('=') + 1)), link.text(),
								url.substring(0, url.indexOf('=')));
						topics[topic.getId()] = topic;
						sectionDependencies.get(id).add(topic.getId());
						hs.add(link.attr("href").hashCode());
						// System.out.print(topic.getId() + " ");
					}
				}
			}
			System.out.println();

		}
		pw.println("Всего форумов: " + sectionDependencies.size());
		pw
				.println("______________________________________________________________");
		for (Section s : sections) {
			pw.println(s.getName());
			for (int id : sectionDependencies.get(s.getId())) {
				pw.println("----" + topics[id].getName());
			}
			pw
					.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		
		pw.close();
	}

	public boolean checkUrlIsSection(String url) {
		return Pattern
				.matches(
						"(http:\\/\\/anime4you\\.mybb\\.ru\\/viewforum\\.php\\?id=)(\\d)*",
						url);
	}

	public boolean checlUrlIsTopic(String url) {
		return Pattern
				.matches(
						"(http:\\/\\/anime4you\\.mybb\\.ru\\/viewtopic\\.php\\?id=)(\\d)*",
						url);
	}

}