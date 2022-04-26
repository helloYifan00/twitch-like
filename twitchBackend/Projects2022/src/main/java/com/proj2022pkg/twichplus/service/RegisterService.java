package com.proj2022pkg.twichplus.service;

import com.proj2022pkg.twichplus.dao.RegisterDao;
import com.proj2022pkg.twichplus.entity.db.User;
import com.proj2022pkg.twichplus.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service 
public class RegisterService {

    @Autowired
    private RegisterDao registerDao;

    public boolean register(User user) throws IOException {
        user.setPassword(Util.encryptPassword(user.getUserId(), user.getPassword()));
        return registerDao.register(user);
    }
}

