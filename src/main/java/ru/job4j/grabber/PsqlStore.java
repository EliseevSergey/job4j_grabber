package ru.job4j.grabber;

import java.sql.*;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cn;

    public PsqlStore(Properties cfg) {

    }
    @Override
    public void save(Post post) {

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
    }
}
