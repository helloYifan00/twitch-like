package com.proj2022pkg.twichplus.service;

import com.proj2022pkg.twichplus.dao.FavoriteDao;
import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.entity.db.ItemType;
import com.proj2022pkg.twichplus.entity.response.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;


@Service
public class RecommendationService {
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;
    @Autowired
    private GameService gameService;

    @Autowired
    private FavoriteDao favoriteDao;

    // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip]. Add items are related to the top games provided in the argument

    /*  to handle recommendation when the user is not logged in.
        The recommendation is purely based-on top games returned by Twitch. */
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
        List<Item> recommendedItems = new ArrayList<>();

        for (Game game : topGames) {
            List<Item> items;
            try { // 根據given type去找到這個gameId的前10個最熱門的
                items = gameService.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    return recommendedItems;
                }
                recommendedItems.add(item);
            }
        }
        return recommendedItems;
    }

    /* Return a list of Item objects for the given type.
       Types are one of [Stream, Video, Clip].
       All items are related to the items previously set favorite by the user.
       E.g., if a user favorited some videos about game "Just Chatting", then it will return some other videos about the same game. */
    private List<Item> recommendByFavoriteHistory(
            Set<String> favoritedItemIds, List<String> favoriteGameIds, ItemType type) throws RecommendationException {
        // Count the favorite game IDs from the database for the given user. E.g. if the favorited game ID list is ["1234", "2345", "2345", "3456"], the returned Map is {"1234": 1, "2345": 2, "3456": 1}
        //    Map<String, Long> favoriteGameIdByCount = new HashMap<>();
        //    for(String gameId : favoritedGameId) {
        //      favoriteGameIdByCount.put(gameId, favoriteGameIdByCount.getOrDefault(gameId, 0L) + 1);
        //    }

        /* 1. 統計 */
        // 下面這行功能等同上面寫法
        Map<String, Long> favoriteGameIdByCount = favoriteGameIds.parallelStream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        /* 2. 排序 : Sort the game Id by count.  */
        // 同一個item可能同個 gameid
        // E.g. if the input is {"1234": 1, "2345": 2, "3456": 1}, the returned Map is {"2345": 2, "1234": 1, "3456": 1}
        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>( favoriteGameIdByCount.entrySet());
        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) -> Long
                .compare(e2.getValue(), e1.getValue())); // 次數大的(較喜歡)排前面，所以e2放前面
        // See also (怎麼sort): https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values

        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
        }

        List<Item> recommendedItems = new ArrayList<>();

        /* 3. Search Twitch based on the favorite game IDs returned in the last step. & de-dup*/
        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {
            List<Item> items;
            try {
                items = gameService.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            // 去重
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    return recommendedItems;
                }
                if (!favoritedItemIds.contains(item.getId())) { // 去掉like過的
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    /* recommendItemsByUser, recommendItemsByDefault are public method for user to get recommendtaions */
    // Return a map of Item objects as the recommendation result.
    // Keys of the may are [Stream, Video, Clip]. Each key is corresponding to a list of Items objects,
    // each item object is a recommended item based on the previous favorite records by the user.
    /* content-based recommendation : conten is gameId here */
    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
//        Set<String> favoriteItemIds;
//        Map<String, List<String>> favoriteGameIds;
        // 1. 先找用戶like過的itemId
        Set<String> favoriteItemIds = favoriteDao.getFavoriteItemIds(userId);
        // 2.
        Map<String, List<String>> favoriteGameIds = favoriteDao.getFavoriteGameIds(favoriteItemIds);
        // favoriteItemIds -> 根據type分類轉成 map<type: gameId list>，
        // How : 根據like過的gameId找出相同的gameId，並去掉like過的item，去做推薦
        // Ps. like過的gameId中，like次數高的先找出有相同gameId的item
        // Ps. 一個gameId找10個item，每個type最多找20個item
        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            if (entry.getValue().size() == 0) { // 沒有like過任何game，就by top game去推薦
                List<Game> topGames;
                try {
                    topGames = gameService.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else {
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
                                                                                  // 用來去重,       , 用來排序,         itemType
            }
        }
        return recommendedItemMap;
    }

    // Return a map of Item objects as the recommendation result.
    // Keys of the may are [Stream, Video, Clip]. Each key is corresponding to a list of Items objects,
    // each item object is a recommended item based on the top games currently on Twitch.
    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        List<Game> topGames;
        // 先找到top games, e.g. gameA, gameB, gameC
        try {
            topGames = gameService.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        // 將topGames通過recommendByTopGames()，得到list of item，再根據不同type為key分類在recommendedItemMap裡
        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        return recommendedItemMap;
    }


    // summary :  根據每個type，如果user在這個type沒有like過的game，就by top game 推薦，如果有就by user favorited 推薦
    // 一個gameId最多找出10個item，每個type最多返回20個item
}
