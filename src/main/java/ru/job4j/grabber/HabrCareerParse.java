package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK =
            String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static final int PAGES_QTY = 5;
    private static final HabrCareerDateTimeParser TIME_PARSER = new HabrCareerDateTimeParser();

    public static void main(String[] args) throws IOException {
        for (int p = 5; p <= PAGES_QTY; p++) {
            Connection connection = Jsoup.connect(String.format("%s%s", PAGE_LINK, p));
            System.out.println(String.format("PAGE: %s", p));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTimeElement = dateElement.child(0);
                String dataTime = dateTimeElement.attr("datetime");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s %s%n", vacancyName, link,
                        TIME_PARSER.parse(dataTime),
                        retrieveDescription(link));
            });
        }
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
}
