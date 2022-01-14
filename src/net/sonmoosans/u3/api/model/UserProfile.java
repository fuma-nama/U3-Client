package net.sonmoosans.u3.api.model;

import net.sonmoosans.u3.ui.util.Key;

public class UserProfile implements Key<Integer> {
    public static final UserProfile DELETED_USER = new UserProfile("Deleted User", null);
    public String name, avatar;
    /**Set by client side**/
    public int userID = -1;

    //For Json
    public UserProfile() {

    }

    public UserProfile(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public Integer getKey() {
        return userID;
    }
}
