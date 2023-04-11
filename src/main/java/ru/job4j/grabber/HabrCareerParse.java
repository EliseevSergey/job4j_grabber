package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK =
            String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static final int PAGES_QTY = 1;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> download = parser.list(PAGE_LINK);
        download.forEach(System.out::println);
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document page = null;
        try {
            page = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements rows = page.select(".vacancy-description__text");
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(rows.get(0).toString())) {
            Pattern delimiter = Pattern.compile("</?\\w+>");
            sc.useDelimiter(delimiter);
            while (sc.hasNext()) {
                String str = sc.next();
                if (!str.isBlank()) {
                    sb.append((str
                            .replace("&nbsp;", " "))
                                    .replaceAll("<.*>", "").trim())
                            .append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> rsl = new ArrayList<>();
        for (int p = 1; p <= PAGES_QTY; p++) {
            Connection connection = Jsoup.connect(String.format("%s%s", link, p));
            System.out.printf("PAGE: %s%n", p);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTimeElement = dateElement.child(0);
                String dataTime = dateTimeElement.attr("datetime");
                String vacancyUrl = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = retrieveDescription(vacancyUrl);
                Post post = new Post();
                post.setTitle(vacancyName);
                post.setLink(vacancyUrl);
                post.setDescription(description);
                post.setCreated(dateTimeParser.parse(dataTime));
                rsl.add(post);
            });
        }
        return rsl;
    }
}
