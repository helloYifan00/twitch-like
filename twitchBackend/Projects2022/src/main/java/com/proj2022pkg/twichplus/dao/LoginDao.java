package com.proj2022pkg.twichplus.dao;

import com.proj2022pkg.twichplus.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LoginDao {

    @Autowired
    private SessionFactory sessionFactory; // 連接到數據庫

    // Verify if the given user Id and password are correct. Returns the user name when it passes
    public String verifyLogin(String userId, String password) {
        String name = "";

        try (Session session = sessionFactory.openSession()) {
            // get操作其實是需要transaction(因為你在讀的時候可能別人在寫)，不過這裡的情況較簡單只會是單線程
            User user = session.get(User.class, userId); // User.class就是要找User這個表
            if(user != null && user.getPassword().equals((password))) {
                name = user.getFirstName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return name;
    }
}
