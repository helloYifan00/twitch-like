package com.proj2022pkg.twichplus;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableWebMvc
public class ApplicationConfig { // sessionfaocty是singleton
    @Bean(name = "sessionFactory") // 這裡的session和authentication的session不同!!
    public LocalSessionFactoryBean sessionFactory() { // 通過sessionFactory來和數據庫交互(增刪查改)
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        // make sure you add your own package name if your class is not under com.laioffer.jupiter.entity.db
        sessionFactory.setPackagesToScan("com.proj2022pkg.twichplus.entity.db");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory; // 得到這個數據庫session
    }

    @Bean(name = "dataSource") // 指向AWS的數據庫
    public DataSource dataSource() {
        String RDS_ENDPOINT = "twitch.XXXXXXXXXX.rds.amazonaws.com";
        String USERNAME = "XXX";
        String PASSWORD = "XXXX";
        //需要修改红色部分, 保留其他内容,  YOUR_RDS_INSTANCE_ADDRESS,USERNAME,  PASSWORD are information created last lesson
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + RDS_ENDPOINT + ":3306/twitch?createDatabaseIfNotExist=true&serverTimezone=UTC");
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        return dataSource;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        hibernateProperties.setProperty("hibernate.show_sql", "true");
        return hibernateProperties;
    }
}

