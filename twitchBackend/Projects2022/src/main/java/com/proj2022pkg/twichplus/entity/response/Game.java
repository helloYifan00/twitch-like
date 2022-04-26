package com.proj2022pkg.twichplus.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

// 反序列化 序列化??? 再聽一次 把不需要的數據discard
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL) //值是"非空"才返回
@JsonDeserialize(builder = Game.Builder.class) // 用這個class來創建???
public class Game { //作為response返回給browser
    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    // @JsonProperty將這裡的參數和twitch的field做了mapping
    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    private Game(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }

    // 透過Builder把對象轉化為JSON : String -> JSON
    // 類的內部有很多成員變量，有些默認有些要另設值
    // 所以Builder可以做統一處理 PS. search builder pattern
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("box_art_url")
        private String boxArtUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder boxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }
}

