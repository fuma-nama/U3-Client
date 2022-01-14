package net.sonmoosans.u3.api;

import net.sonmoosans.u3.api.annotation.RequireToken;
import net.sonmoosans.u3.api.core.APICaller;
import net.sonmoosans.u3.api.core.Parameter;
import net.sonmoosans.u3.api.model.Friend;
import net.sonmoosans.u3.api.model.FriendInvite;
import net.sonmoosans.u3.api.model.Result;

public class FriendAPI {
    @RequireToken
    public static Result<Friend[]> getFriends() {
        return APICaller.callGET(Friend[].class, "friend");
    }

    @RequireToken
    public static boolean removeFriend(int friendID) {
        return APICaller.callDELETE("friend",
                Parameter.intValue("id", friendID)
        );
    }

    @RequireToken
    public static Result<FriendInvite[]> getInvites() {
        return APICaller.callGET(FriendInvite[].class, "friend/invite");
    }

    @RequireToken
    public static boolean sendInvite(int targetID) {
        return APICaller.callPOST("friend/invite",
                Parameter.intValue("target", targetID)
        );
    }

    @RequireToken
    public static boolean agreeInvite(int senderID) {
        return APICaller.callPOST("friend/invite/agree",
                Parameter.intValue("sender", senderID)
        );
    }

    @RequireToken
    public static boolean removeInvite(int senderID) {
        return APICaller.callDELETE("friend/invite",
                Parameter.intValue("sender", senderID)
        );
    }
}
