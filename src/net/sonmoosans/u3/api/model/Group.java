package net.sonmoosans.u3.api.model;

import net.sonmoosans.u3.ui.util.Key;

import javax.annotation.Nullable;

public class Group implements Key<Integer> {
    public int creatorID;
    public String avatar, name, description;

    /**Set by client side only, can be null**/
    @Nullable
    public Integer id;

    /**Set by client side only**/
    public int unreadCount = 0;

    /**Set by client side only**/
    public boolean isPrivate = false;

    public Group setID(int ID) {
        this.id = ID;
        return this;
    }

    public void setUnreadCount(int count) {
        unreadCount = count;
    }

    public Group() {

    }

    public Group(int creatorID, String avatar, String name, String description, boolean isPrivate) {
        this.creatorID = creatorID;
        this.avatar = avatar;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    @Override
    public Integer getKey() {
        return id;
    }
}