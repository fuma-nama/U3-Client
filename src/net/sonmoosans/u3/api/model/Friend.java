package net.sonmoosans.u3.api.model;

import net.sonmoosans.u3.ui.util.Key;
public class Friend implements Key<Integer> {
    public int friendID, privateGroupID;

    public Friend() {

    }

    public Friend(int privateGroupID) {
        this.privateGroupID = privateGroupID;
    }

    @Override
    public Integer getKey() {
        return friendID;
    }
}
