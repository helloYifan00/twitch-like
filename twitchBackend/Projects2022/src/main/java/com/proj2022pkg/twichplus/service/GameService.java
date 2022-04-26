package com.proj2022pkg.twichplus.service;

import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.entity.db.ItemType;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proj2022pkg.twichplus.entity.response.Game;

import java.util.*;


@Service
public class GameService {
    //OAuth or App Access Token required : need TOKEN, CLIENT_ID
    private static final String TOKEN = "Bearer 9ln5c4vxpjf81d1i3vrc10tl8vdfyq";
    private static final String CLIENT_ID = "hzj2vwbfuk7kvg2hqe4lg01iiy6tzq";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    // name是根據browser的輸入
    private static final int DEFAULT_GAME_LIMIT = 20; // 前20個數據

    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    // first : Maximum number of objects to return
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    // 專門創建發送請求的URL的method
    // Build the request URL which will be used when calling Twitch APIs,
    // e.g. https://api.twitch.tv/helix/games/top when trying to get top games.
    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) { // 為空表示是要搜尋TOP_GAME_URL，那我就需要
            return String.format(url, limit);
        } else { // gameName不為空 表示是要找特定game
            try {
                // Encode special characters in URL, e.g. Rick Sun -> Rick%20Sun
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    // Similar to buildGameURL(), build Search URL that will be used when calling Twitch API.
    // e.g. https://api.twitch.tv/helix/clips?game_id=12924.
    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8"); // encode一些字符e.g %s -> ??
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }

    // Send HTTP request to Twitch Backend based on the given URL, and returns the body of the HTTP response returned from Twitch backend.
    // 這個String指的是 twitch發送出來的，還沒處理過
    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Define the response handler to parse and return HTTP response body returned from Twitch
        // 對從twitch(第三方)發出的response進行處理
        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) { // 確認發出的請求是否被twitch成功處理，200表示成功
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            HttpEntity entity = response.getEntity(); // 獲得數據，例 data : box_art_url:..., id:..., name
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            // 因為要返回的型態是String
            return obj.getJSONArray("data").toString(); // 將得到的JSON格式數據轉成string，且，只要key為"data的部分"
        };

        try {
            // Define the HTTP request, TOKEN and CLIENT_ID are used for user authentication on Twitch backend
            HttpGet request = new HttpGet(url); // url可能是top 或game search
            // 根據Twitch的要求，需要兩個Auth的資料
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(request, responseHandler); // 返回的就是twitch給的且處理過的string
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally { // 關掉HTTPclient，以免佔用資源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Convert JSON format data returned from Twitch to an Arraylist of Game objects
    // twitch的結果返回給自己的browser
    // 用這個method就會返回指定型態，List<Game>，因為有可能twitch的資料還有其他field是我們不需要的，
    // 藉由這個method 濾掉不需要的
    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 調用這個方法返回給controller
            // 將一串  "data": [
            //        {
            //            "id": "512953",
            //            "name": "Elden Ring",
            //            "box_art_url": "https://static-cdn.jtvnw.net/ttv-boxart/512953_IGDB-{width}x{height}.jpg"
            //        },
            //        ...
            // ... 這樣的數據，轉換成List<Game>的型態
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    // Integrate search() and getGameList() together, returns the top x popular games from Twitch.
    // @requestMapping??
    // 不需要gameName，只需要返回前limit個數據
    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        // 1. 創建top_game ULR //2. call searchTwitch就會返回處理過的data // 3. return
        String url = buildGameURL(TOP_GAME_URL, "", limit);
        String data = searchTwitch(url);
        return getGameList(data);
//        return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));
    }

    // Integrate search() and getGameList() together, returns the dedicated game based on the game name.
    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }

    // Similar to getGameList, convert the json data returned from Twitch to a list of Item objects.
    // 將twitch返回給我的JSON array 轉成 List<Item> object (濾掉其中不需要的數據)
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        // objectmapper根据Item class的field 变量名从JSON里爬数据
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    // Returns the top x streams based on game ID.
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        String searchUrl = buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(searchUrl);
        List<Item> streams = getItemList(data);
//        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName()); // 拼接成完整url
        }
        return streams;
    }

    // Returns the top x clips based on game ID.
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
            // 因為這個返回的數據已經有clip的url，所以這裡不用拼接其他數據成新的url
        }
        return clips;
    }

    // Returns the top x videos based on game ID.
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    /* Finally, add the public searchByType and searchItems
       to return items for a specific type, or items for all types.
     */
    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>(); // key : value = ItemType : data[...]
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        // 就找到所有typ(STREAM, VIDEO, CLIP)e的data
        return itemMap;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break; // 要記得break，以防同一個call內執行到其他type
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId); // 有些type(e.g. VIDEO)的數據沒有game_id，所以這裡統一再set一下
        }
        return items;
    }

}
