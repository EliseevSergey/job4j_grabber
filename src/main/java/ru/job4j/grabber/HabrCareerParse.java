package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK =
            String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static final int PAGES_QTY = 1;

    public static void main(String[] args) throws IOException {
        for (int p = 1; p <= PAGES_QTY; p++) {
            Connection connection = Jsoup.connect(String.format(PAGE_LINK + p));
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
                var timeParser = new HabrCareerDateTimeParser();
                LocalDateTime localDateTime = timeParser.parse(dataTime);
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = null;
                try {
                    description = retrieveDescription(link);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.printf("%s %s %s %s%n", vacancyName, link, localDateTime, description);
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document page = connection.get();
        Elements rows = page.select(".style-ugc");
        Scanner sc = new Scanner(rows.get(0).toString());
        Pattern delimiter = Pattern.compile("</?\\w+>");
        sc.useDelimiter(delimiter);
        List<String> list = new ArrayList<>();
        while (sc.hasNext()) {
            String str = sc.next();
            if (!str.isBlank()) {
                list.add(str);
            }
        }
        list.remove(0);
        StringBuilder sb = new StringBuilder();
        list.forEach(s -> {
            sb.append(s);
            sb.append(System.lineSeparator());
            }
        );
        return sb.toString();
    }
}
