package ru.job4j.grabber;

import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String url = cfg.getProperty("url");
        String login = cfg.getProperty("username");
        String password = cfg.getProperty("password");
        cnn = DriverManager.getConnection(url, login, password);
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement(
                /*"insert into post (name, text, link, created) values (?, ?, ?, ?)" +
                        "on conflict on constraint post_link_key" +
                        "do" +
                        "update post set text = (?) where link = (?)",*/
                "insert into post (name, text, link, created) values (?, ?, ?, ?)" +
                        "on conflict on constraint post_link_key" +
                        " do nothing;",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            //ps.setString(5, post.getDescription());
            //ps.setString(6, post.getLink());
            ps.execute();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        return null;
    }

    @Override
    public Post findById(int id) {
        return null;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        Properties cfg = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("app.properties")) {
            cfg.load(in);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        PsqlStore store = new PsqlStore(cfg);
        HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
        String SOURCE_LINK = "https://career.habr.com";
        String PAGE_LINK =
                String.format("%s/vacancies/java_developer?page=1", SOURCE_LINK);
        List<Post> download = parser.list(PAGE_LINK);
        store.save(download.get(1));
        store.save(download.get(2));
        store.save(download.get(3));
        store.save(download.get(4));
    }
}
