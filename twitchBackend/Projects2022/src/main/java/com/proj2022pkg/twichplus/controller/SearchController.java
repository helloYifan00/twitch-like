package com.proj2022pkg.twichplus.controller;

import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

@Controller // 定義了一個API要處理這個請求，就要加上@Controller這個annotation (可再聽一次)
public class SearchController {

    @Autowired
    private GameService gameService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody // 自動把指定的object(這裡是一個Map)轉成String格式
    public Map<String, List<Item>> search(@RequestParam(value = "game_id") String gameId) {
        return gameService.searchItems(gameId);
    }

}
