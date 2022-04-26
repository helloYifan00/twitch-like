package com.proj2022pkg.twichplus.dao;

import com.proj2022pkg.twichplus.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;

@Repository // 也是一個component，表示這個class跟data base 儲存相關
public class RegisterDao {

    @Autowired
    private SessionFactory sessionFactory; // 看ApplicationConfig

    public boolean register(User user) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            session.beginTransaction(); // mySQL才有Transaction的概念 : 多個對數據庫的操作，要嘛都成功不然都失敗
            session.save(user);
            session.getTransaction().commit();
        } catch (PersistenceException | IllegalStateException ex) { // commit之後可能發生的異常，看EntityTransaction.java
            // if hibernate throws this exception, it means the user already be register
            ex.printStackTrace();
            session.getTransaction().rollback(); // rollback到register前的狀態(刪除之前的操作)
            return false;
        } finally {
            if (session != null) session.close();
        }
        return true;
    }
}


