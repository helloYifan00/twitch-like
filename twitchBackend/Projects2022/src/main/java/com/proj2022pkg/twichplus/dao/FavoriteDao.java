package com.proj2022pkg.twichplus.dao;

import com.proj2022pkg.twichplus.entity.db.Item;
import com.proj2022pkg.twichplus.entity.db.ItemType;
import com.proj2022pkg.twichplus.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.proj2022pkg.twichplus.entity.db.ItemType;


@Repository
public class FavoriteDao {
    @Autowired
    private SessionFactory sessionFactory; // 用來創建session object，以提供增刪查改的API

    // Insert a favorite record to the database
    public void setFavoriteItem(String userId, Item item) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            User user = session.get(User.class, userId);
            // .class意思是找到User這個表中有沒有指定的userId的紀錄，導出這項紀錄(為一個object)
            user.getItemSet().add(item);
            session.beginTransaction();
            session.save(user); // 測試update()是否也可以
            session.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            if (session != null) session.close();
        }
    }

    // Remove a favorite record from the database
    // 因為是N:N的關係，只能刪除針對這個user所對應的itemId，不能刪掉該Item，(因為其他user也可能like這個item)
    public void unsetFavoriteItem(String userId, String itemId) { // 只需要傳itemId就可
        Session session = null;

        try {
            session = sessionFactory.openSession();
            User user = session.get(User.class, userId);
            Item item = session.get(Item.class, itemId);
            user.getItemSet().remove(item); // 只刪了favorite_records表中的這個userId+itemId所對應的的紀錄
            // 因為User class裡有寫@ManyToMany、@JoinTable，
            // 所以會去找到favorite_records表的user_id + item_id兩個field找到對應資料，然後刪除這個favorite_records表中的這項紀錄

            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            if (session != null) session.close();
        }
    }

    public Set<Item> getFavoriteItems(String userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, userId).getItemSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<>();
    }

    /* ------------- Recommendation ------------- */
    // Get favorite item ids for the given user
    public Set<String> getFavoriteItemIds(String userId) { // 順便去重??
        Set<String> itemIds = new HashSet<>();

        try (Session session = sessionFactory.openSession()) {
            Set<Item> items = session.get(User.class, userId).getItemSet();
            for(Item item : items) {
                itemIds.add(item.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemIds;
    }

    // Get favorite items for the given user.
    // The returned map includes three entries like {"Video": [item1, item2, item3], "Stream": [item4, ...], "Clip": [item7, ...]}
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) {
        // initialize
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }

        try (Session session = sessionFactory.openSession()) {
            for(String itemId : favoriteItemIds) {
                Item item = session.get(Item.class, itemId); // 根據itemId找到item
                itemMap.get(item.getType().toString()).add(item.getGameId()); // 再由該item，找到type和gameId
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemMap;
    }

}


