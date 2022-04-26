package com.proj2022pkg.twichplus.controller;

import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.entity.request.FavoriteRequestBody;
import com.proj2022pkg.twichplus.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

// 反序列化: string ->
@Controller // controller 用來定義REST API，處理分配從前端收到的請求
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // value為url
    @RequestMapping(value = "/favorite", method = RequestMethod.POST)
    public void setFavoriteItem(@RequestBody FavoriteRequestBody requestBody, HttpServletRequest request, HttpServletResponse response) {
                             // 利用@RequestBody將JSON string格式的request轉成requestBody
        // 第1步要找到user id，而user id存在session裡，所以要先得到當前session
        // session可以看成是一個hashmap
        HttpSession session = request.getSession(false); // false意思是如果沒有找到session就將session設為null
        if (session == null) { // 表示還沒登入，所以沒有session，要返回一個403 forbidden response，提醒用戶登入
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ;
        }
        // 如果有session，就會去得到先前登入時設定的user_id (看LoginController)
        String userId = (String) session.getAttribute("user_id");
        favoriteService.setFavoriteItem(userId, requestBody.getFavoriteItem());
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.DELETE)
    public void unsetFavoriteItem(@RequestBody FavoriteRequestBody requestBody, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = (String) session.getAttribute("user_id");
        favoriteService.unsetFavoriteItem(userId, requestBody.getFavoriteItem().getId());
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Item>> getFavoriteItem(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new HashMap<>();
        }
        String userId = (String) session.getAttribute("user_id");
        return favoriteService.getFavoriteItems(userId);
    }
}
