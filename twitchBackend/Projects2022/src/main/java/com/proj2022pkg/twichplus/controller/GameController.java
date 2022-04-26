package com.proj2022pkg.twichplus.controller;

import com.proj2022pkg.twichplus.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proj2022pkg.twichplus.service.TwitchException;
import javax.servlet.ServletException;

/**
 * GameController 是一个基于注解的控制器, 可以同时处理多个请求动作，并且无须实现任何接口。
 * org.springframework.stereotype.Controller注解用于指示该类是一个控制器
 */
@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @RequestMapping(value = "/game", method = RequestMethod.GET)
    public void getGame(@RequestParam(value = "game_name", required = false) String gameName, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            // Return the dedicated game information if gameName is provided in the request URL, otherwise return the top x games.
            if (gameName != null) {
                // 返回在gameService.searchGame內處理完的數據(type是List<Game>)給瀏覽器
                response.getWriter().print(new ObjectMapper().writeValueAsString(gameService.searchGame(gameName)));
                                          // 調用ObjectMapper把object轉成sting
            } else {
                response.getWriter().print(new ObjectMapper().writeValueAsString(gameService.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }
    }


    // 這個方法更推薦 :
//    private final GameService gameService;
//
//    @Autowired
//    public GameController(GameService gameService) {
//        this.gameService = gameService;
//    }


    // game?game_name=..%*^x....
    // /game
//    @RequestMapping(value = "/game", method = RequestMethod.GET)
//    public void getGame(@RequestParam(value = "game_name", required = false) String gameName, HttpServletResponse response) throws IOException {
////        System.out.println("GET game_name is " + gameName);
//    }
}
