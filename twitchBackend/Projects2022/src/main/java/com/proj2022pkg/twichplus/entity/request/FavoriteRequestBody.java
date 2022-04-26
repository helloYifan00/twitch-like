package com.proj2022pkg.twichplus.entity.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.proj2022pkg.twichplus.entity.db.Item;

/* to represents the payload that frontend sends to server */
/*
controller會去設定這個FavoriteRequestBody class，
Spring framework就能由定義好的FavoriteRequestBody class，
將收到JSON格式的request內的key去一一對應Item class內的field，拿到這個key的值，再賦值給Item 的field，
以此方式一一對應賦值，最後就將整個JSON string轉成Item type的object
* */
public class FavoriteRequestBody {
    private final Item favoriteItem;

    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
                                    // key : favorite, type: Item
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
