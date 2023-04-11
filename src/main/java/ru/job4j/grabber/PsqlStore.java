package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
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
                "insert into post (name, text, link, created) values (?, ?, ?, ?)"
                        + "on conflict on constraint post_link_key"
                        + " do "
                        + "update set name = (?) , text = (?), created  = (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.setString(5, post.getTitle());
            ps.setString(6, post.getDescription());
            ps.setTimestamp(7, Timestamp.valueOf(post.getCreated()));
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
       List<Post> all = new ArrayList<>();
       try (Statement st = cnn.createStatement()) {
           String select = "select * from post";
           try (ResultSet resultSet = st.executeQuery(select)) {
               while (resultSet.next()) {
                   Post post = new Post();
                   post.setId(resultSet.getInt(1));
                   post.setTitle(resultSet.getString(2));
                   post.setDescription(resultSet.getString(3));
                   post.setLink(resultSet.getString(4));
                   post.setCreated(resultSet.getTimestamp(5).toLocalDateTime());
                   all.add(post);
               }
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
       return all;
    }

    @Override
    public Post findById(int id) {
        Post rsl = new Post();
        try (PreparedStatement ps = cnn.prepareStatement(
                "select * from post where id = ?")) {
            ps.setInt(1, id);
            try (ResultSet generatedKeys = ps.executeQuery()) {
                if (generatedKeys.next()) {
                    rsl.setId(generatedKeys.getInt(1));
                    rsl.setTitle(generatedKeys.getString(2));
                    rsl.setDescription(generatedKeys.getString(3));
                    rsl.setLink(generatedKeys.getString(4));
                    rsl.setCreated(generatedKeys.getTimestamp(5).toLocalDateTime().withNano(0));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        Properties cfg = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        try (PsqlStore store = new PsqlStore(cfg)) {
            HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
            String sourceLink = "https://career.habr.com";
            String pageLink =
                    String.format("%s/vacancies/java_developer?page=1", sourceLink);
            List<Post> download = parser.list(pageLink);
            store.save(download.get(1));
            store.save(download.get(2));
            List<Post> all = store.getAll();
            Post rsl = store.findById(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
