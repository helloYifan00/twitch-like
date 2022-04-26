package com.proj2022pkg.twichplus.controller;

import com.proj2022pkg.twichplus.entity.db.User;
import com.proj2022pkg.twichplus.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller // HTTPservlet藉由這個annotation來發請求
public class RegisterController {

    @Autowired
    private RegisterService registerService;
    //RequestMapping : 把前端發送的URL mapping到這個method來處理
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@RequestBody User user, HttpServletResponse response) throws IOException {
                          // @RequestBody : 將JSON string格式 轉成 userbody格式
        if (!registerService.register(user)) { // 檢查是否註冊成功
            response.setStatus(HttpServletResponse.SC_CONFLICT);
//            response.getWriter().print("Fail to register"); // 可以自己加錯誤原因
        }
    }
}

