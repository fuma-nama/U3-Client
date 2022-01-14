package net.sonmoosans.u3.api;

import net.sonmoosans.u3.api.model.Friend;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.api.model.UserProfile;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * After getting info from server
 * Some kind of info (ex: user profile) will be stored in memory
 * Therefore we can have a better performance
 * **/
public class Memory {
    private static final HashSet<Integer> emojis = new HashSet<>();
    private static final HashMap<Integer, Group> groupCache = new HashMap<>();
    private static final HashMap<Integer, UserProfile> userCache = new HashMap<>();
    private static final HashMap<String, Image> imageCache = new HashMap<>();
    private static final HashMap<Integer, Friend> friendPool = new HashMap<>();
    private static String selfToken;
    private static int selfUserID = -1;

    public static Group getGroup(int groupID) {
        return groupCache.getOrDefault(groupID, null);
    }

    public static void putGroup(int groupID, Group group) {
        if (group == null) return;
        groupCache.put(groupID, group);
    }

    public static UserProfile getUser(int userID) {
        return userCache.getOrDefault(userID, null);
    }

    public static void putUser(int userID, UserProfile user) {
        if (user == null) return;
        userCache.put(userID, user);
    }

    public static void updateUser(int userID, Consumer<UserProfile> updateFunction) {
        userCache.computeIfPresent(userID, (k, v)-> {
            updateFunction.accept(v);
            return v;
        });
    }

    public static void updateGroup(int groupID, Consumer<Group> updateFunction) {
        groupCache.computeIfPresent(groupID, (k, v)-> {
            updateFunction.accept(v);
            return v;
        });
    }

    public static void storeSelfData(String token, int userID) {
        selfToken = token;
        selfUserID = userID;
    }

    public static void storeSavedEmojis(int[] emojis) {
        for (int emoji : emojis)
            Memory.emojis.add(emoji);
    }

    public static HashMap<Integer, Friend> getFriendPool() {
        return friendPool;
    }

    public static Collection<Friend> getFriends() {
        return friendPool.values();
    }

    public static boolean hasFriend(int userID) {
        return friendPool.containsKey(userID);
    }

    public static HashSet<Integer> getSavedEmojis() {
        return Memory.emojis;
    }

    public static String getSelfToken() {
        return selfToken;
    }

    public static int getSelfUserID() {
        return selfUserID;
    }

    public static Image saveImage(String url, Image image) {
        imageCache.put(url, image);
        if (imageCache.size() > 20) {
            Optional<String> firstKey = imageCache.keySet().stream().findFirst();
            imageCache.remove(firstKey.get());
        }
        return image;
    }

    public static Image getImage(String url) {
        return imageCache.getOrDefault(url, null);
    }
}
