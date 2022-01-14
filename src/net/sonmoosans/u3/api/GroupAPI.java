package net.sonmoosans.u3.api;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.sonmoosans.u3.api.annotation.RequireToken;
import net.sonmoosans.u3.api.annotation.SocketIOFunction;
import net.sonmoosans.u3.api.core.APICaller;
import net.sonmoosans.u3.api.core.Parameter;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.api.model.GroupInvite;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.api.model.TextMessage;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

public class GroupAPI {
    public static final String URL = "http://localhost:9092?token=",
            MESSAGE_EVENT = "message",
            MESSAGE_UPDATED_EVENT = "message_update",
            MESSAGE_DELETE_EVENT = "message_delete",
            JOIN_GROUP_EVENT = "join_group",
            LEAVE_GROUP_EVENT = "leave_group",
            USER_PROFILE_UPDATE = "user_update",
            GROUP_PROFILE_UPDATE = "group_update",
            EMOJI_SAVED_EVENT = "emoji_saved",
            EMOJI_REMOVED_EVENT = "emoji_removed",
            UPDATE_READING_GROUP = "update_reading_group",
            REMOVE_READING_GROUP = "remove_reading_group",
            GROUP_INVITE_ADDED_EVENT = "group_invite_add",
            GROUP_INVITE_REMOVED_EVENT = "group_invite_remove",
            FRIEND_INVITE_ADDED_EVENT = "friend_invite_add",
            FRIEND_INVITE_REMOVED_EVENT = "friend_invite_remove",
            FRIEND_ADDED_EVENT = "friend_add",
            FRIEND_REMOVED_EVENT = "friend_remove",
            VOICE_EVENT = "voice",
            JOIN_VOICE_EVENT = "join_voice",
            LEAVE_VOICE_EVENT = "leave_voice";

    private static Socket socket;

    public static void connectToServerAsync(String token, Consumer<Boolean> handler) {
        URI uri = URI.create(URL + token);

        if (socket != null) socket.disconnect();
        socket = IO.socket(uri);
        socket.connect();

        socket.once(Socket.EVENT_CONNECT, arg -> handler.accept(true));
        socket.once(Socket.EVENT_CONNECT_ERROR, arg -> handler.accept(false));
    }

    /**Get Joined groups
     * @return A array of joined groups ID
     * **/
    public static Result<int[]> getJoinedGroups() {
        return APICaller.callGET(int[].class, "group/join");
    }

    /**Create a new group
     * @return ID of the new group
     * **/
    @RequireToken
    public static boolean createGroup(String name, String description) {
        return APICaller.callPOST("group",
                new Parameter("name", name),
                new Parameter("detail", description)
        );
    }

    /**Create a new group
     * @return ID of the new group
     * **/
    @RequireToken
    public static boolean createGroup(String name, String description, File icon) {
        return APICaller.callPOSTFile(icon, "group",
                new Parameter("name", name),
                new Parameter("detail", description)
        );
    }

    @RequireToken
    public static boolean updateGroup(int groupID, String name, String description, File icon) {
        return APICaller.callPUTFile(icon, "group",
                Parameter.intValue("id", groupID),
                new Parameter("name", name),
                new Parameter("detail", description)
        );
    }

    @RequireToken
    public static boolean updateGroup(int groupID, String name, String description) {
        return APICaller.callPUT("group",
                Parameter.intValue("id", groupID),
                new Parameter("name", name),
                new Parameter("detail", description)
        );
    }

    /**
     * Get Group info
     * If group ID already exists in memory, get from memory
     * @return Group info, Null if group ID doesn't exist
     * @see Memory#getGroup
     * **/
    @Nullable
    public static Group getGroupProfile(int groupID) {
        Group group = Memory.getGroup(groupID);
        if (group == null) {
            group = APICaller.callGET(Group.class, "group",
                    Parameter.intValue("id", groupID)
            ).context();
            Memory.putGroup(groupID, group);
        }
        return group.setID(groupID);
    }

    /**@return A list of user IDs, null if failed to get**/
    @RequireToken
    public static int[] getMembers(int groupID) {
        return APICaller.callGET(int[].class, "group/member",
                Parameter.intValue("id", groupID)).context();
    }

    @RequireToken
    public static TextMessage[] getMessages(int groupID, int offset) {
        return APICaller.callGET(TextMessage[].class, "group/messages",
                Parameter.intValue("id", groupID),
                Parameter.intValue("offset", offset)
        ).context();
    }

    @RequireToken
    public static boolean joinGroup(String inviteCode) {
        return APICaller.callPOST("group/join",
                new Parameter("code", inviteCode)
        );
    }

    /**@apiNote Must be invited by inviter before called**/
    @RequireToken
    public static boolean joinGroupFromInvite(int groupID, int inviterID) {
        return APICaller.callPOST("group/join",
                Parameter.intValue("id", groupID),
                Parameter.intValue("sender", inviterID)
        );
    }

    @SocketIOFunction
    public static void leaveGroupAsync(int groupID) {
        socket.emit(LEAVE_GROUP_EVENT, groupID);
    }

    @RequireToken
    public static boolean sendMessage(int groupID, String context, Iterable<File> files, boolean useHTML) {
        return APICaller.callPOSTFiles(files, "group/messages",
                Parameter.intValue("id", groupID),
                new Parameter("context", context),
                Parameter.boolValue("html", useHTML)
        );
    }

    @RequireToken
    public static boolean sendMessage(int groupID, int replyTo, String context, Iterable<File> files, boolean useHTML) {
        return APICaller.callPOSTFiles(files, "group/messages",
                Parameter.intValue("id", groupID),
                Parameter.intValue("reply", replyTo),
                new Parameter("context", context),
                Parameter.boolValue("html", useHTML)
        );
    }

    /**
     * Delete message
     * <br>This action can't be redone
     * <br>Only the sender or the group creator can delete message
     * **/
    @RequireToken
    public static boolean deleteMessage(int messageID) {
        return APICaller.callDELETE("group/messages",
                Parameter.intValue("id", messageID)
        );
    }

    /**
     * Modify message context
     * <br>This action can't be redone
     * <br>Only the sender or the group creator can delete message
     * **/
    @RequireToken
    public static boolean editMessage(int messageID, String newContext) {
        return APICaller.callPUT("group/messages",
                Parameter.intValue("id", messageID),
                new Parameter("context", newContext)
        );
    }

    /**@return New invite code**/
    @RequireToken
    public static Result<String> getInviteCode(int groupID) {
        return APICaller.callGET(String.class, "group/invite/code",
                Parameter.intValue("id", groupID)
        );
    }

    /**@return New invite code**/
    @RequireToken
    public static Result<String> updateInviteCode(int groupID) {
        return APICaller.callPOST(String.class, "group/invite/code",
                Parameter.intValue("id", groupID)
        );
    }

    public static boolean sendInvite(int groupID, int targetID) {
        return APICaller.callPOST("group/invite",
                Parameter.intValue("id", groupID),
                Parameter.intValue("target", targetID)
        );
    }

    public static Result<int[]> getVoiceCallMembers(int groupID) {
        return APICaller.callGET(int[].class, "/group/voice",
                Parameter.intValue("id", groupID)
        );
    }

    public static void joinVoiceRoom(int groupID) {
        socket.emit(JOIN_VOICE_EVENT, groupID);
    }

    public static void leaveVoiceRoom(int groupID) {
        socket.emit(LEAVE_VOICE_EVENT, groupID);
    }

    public static void sendAudio(int groupID, byte[] data, int bufferSize) {
        socket.emit(VOICE_EVENT, groupID, data, bufferSize);
    }

    @RequireToken
    public static boolean removeInvite(int groupID, int inviterID) {
        return APICaller.callDELETE("group/invite",
                Parameter.intValue("id", groupID),
                Parameter.intValue("sender", inviterID)
        );
    }

    public static Result<GroupInvite[]> getInvites() {
        return APICaller.callGET(GroupInvite[].class, "group/invite");
    }

    @RequireToken
    public static Result<Integer> getUnreadMessageCount(int groupID) {
        return APICaller.callGET(Integer.class, "group/messages/unread",
                Parameter.intValue("id", groupID)
        );
    }

    @SocketIOFunction
    public static void updateReadingGroup(int groupID) {
        socket.emit(UPDATE_READING_GROUP, groupID);
    }

    @SocketIOFunction
    public static void removeReadingGroup() {
        socket.emit(REMOVE_READING_GROUP);
    }

    public static Socket getSocket() {
        return socket;
    }
}
