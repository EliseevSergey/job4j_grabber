package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.TimeZone;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private static Properties loadCfg() {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    private static Connection getConnection(Properties properties) {
        Properties cfg = loadCfg();
        Connection connection = null;
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return connection;
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            int interval = Integer.parseInt(loadCfg().getProperty("interval"));
            data.put("interval", interval);
            data.put("connection", getConnection(loadCfg()));
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .withRepeatCount(10);
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(30000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            try (PreparedStatement ps = ((Connection) context
                    .getJobDetail()
                    .getJobDataMap()
                            .get("connection")).
                    prepareStatement(
                    "insert into rabbit (created_date) values (?)")) {
                    LocalDateTime jobTime = LocalDateTime
                            .ofInstant(Instant.ofEpochMilli(context.getFireTime().getTime()),
                        TimeZone.getDefault().toZoneId());
                    ps.setTimestamp(1, Timestamp.valueOf(jobTime));
                System.out.printf("Rabbit runs here with frequency: %s seconds",
                        context.getJobDetail()
                                .getJobDataMap()
                                        .get("interval"));
                ps.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}