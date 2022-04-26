package com.proj2022pkg.twichplus.controller;

import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.service.RecommendationException;
import com.proj2022pkg.twichplus.service.RecommendationService;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @RequestMapping(value = "/recommendation", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Item>> recommendation(HttpServletRequest request) throws ServletException {
                                  // Rest API    // HttpServletRequest 用來創session，根據session是否為null，來看要怎麼推薦(by default 或by user)
        HttpSession session = request.getSession(false); // 記得設false，若是沒登入過的user才會將session設為null
        Map<String, List<Item>> itemMap;
        try {
            if (session == null) { // session沒有存在過(i.e. user沒登入過)
                itemMap = recommendationService.recommendItemsByDefault();
            } else {
                String userId = (String) request.getSession().getAttribute("user_id");
                itemMap = recommendationService.recommendItemsByUser(userId);
            }
        } catch (RecommendationException e) {
            throw new ServletException(e);
        }

        return itemMap; // type : item list
    }
}

