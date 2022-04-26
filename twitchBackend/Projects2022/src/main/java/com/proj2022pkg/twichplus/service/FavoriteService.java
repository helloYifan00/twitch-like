package com.proj2022pkg.twichplus.service;

import com.proj2022pkg.twichplus.dao.FavoriteDao;
import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.entity.db.ItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteDao favoriteDao;

    public void setFavoriteItem(String userId, Item item) { // Item從controller來的
        favoriteDao.setFavoriteItem(userId, item);
    }

    public void unsetFavoriteItem(String userId, String itemId) {
        favoriteDao.unsetFavoriteItem(userId, itemId);
    }

    /*
    user 增加favorite時是隨機的，所以要先去分類favorite_record表裡有哪些item type(VIDEO, STREAM, CLIP)
    所以需要用map，將item type作為key，包成一個value(為一個Lite<Item>)，
    各種item type的List<Item>就成為一個Item map
    * */
    public Map<String, List<Item>> getFavoriteItems(String userId) {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        Set<Item> favorites = favoriteDao.getFavoriteItems(userId);
        for(Item item : favorites) {
            itemMap.get(item.getType().toString()).add(item);
        }
        return itemMap;
    }
}
