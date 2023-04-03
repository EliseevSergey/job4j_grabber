package ru.job4j.demo;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class DescrExtraction {
    public static void main(String[] args) throws IOException {
        String url = "https://career.habr.com/vacancies/1000098293";
        Connection connection = Jsoup.connect(url);
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
        });
        out.println(sb);
    }
}