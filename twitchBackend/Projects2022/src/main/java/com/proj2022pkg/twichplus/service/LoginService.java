package com.proj2022pkg.twichplus.service;

import com.proj2022pkg.twichplus.dao.LoginDao;
import com.proj2022pkg.twichplus.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class LoginService {

    @Autowired
    private LoginDao loginDao;

    public String verifyLogin(String userId, String password) throws IOException {
        password = Util.encryptPassword(userId, password);  // 加密
        return loginDao.verifyLogin(userId, password);
    }
}
