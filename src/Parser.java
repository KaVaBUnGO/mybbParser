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
							"не логиньтесь с моими куками ;D")
					.get();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		connect();
		HashMap<Integer, ArrayList<Integer>> sectionDependencies = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Section> sections = new ArrayList<Section>();
		HashSet<Integer> hs = new HashSet<Integer>();
		Topic topics[] = new Topic[100000];
		Post posts[] = new Post[1000000];
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
			if (count++ > 1)
				break;
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
		}

		// Побежим разбирать топики на посты
		for (ArrayList<Integer> topicIds : sectionDependencies.values()) {
			for (int topicId : topicIds) {
				// Получим число страниц в топике
				String curUrl = topics[topicId].getUrl() + "=" + topicId;
				connectUrl(curUrl);
				int pages = 1;
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
				System.out.println(topics[topicId].getUrl() + "=" + topicId
						+ " " + pages);
				// пробежим по страницам и выцепим посты
				for (int i = 1; i <= pages; i++) {
					String url1 = curUrl
							.concat("&p=".concat(String.valueOf(i)));
					connectUrl(url1);
					Elements postsArr = doc.getElementsByClass("post");
					for (Element e : postsArr) {
						String name = e.getElementsByClass("pa-author")
								.select("a").text();
						String dateStr = e.getElementsByClass("permalink")
								.text();
						String id = e.select("div[id]").attr("id").substring(1);
						Elements c = e.getElementsByClass("post-content").removeClass("post-sig");
						String text = c.text();
						System.out.println(name + " " + dateStr + " " + id
								+ " " + text);
						Post post = new Post(Integer.valueOf(id), dateStr,
								name, text);
						posts[post.getId()] = post;
						topics[topicId].addPost(post.getId());
					}
					System.out.println(postsArr.size());
				}
			}
		}
		System.out.println();
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
