package net.sonmoosans.u3.api.model;

import net.sonmoosans.u3.ui.util.Key;

import java.time.LocalDateTime;

public class FriendInvite implements Key<Integer> {
    public int senderID;
    public LocalDateTime inviteTime;

    @Override
    public Integer getKey() {
        return senderID;
    }
}
