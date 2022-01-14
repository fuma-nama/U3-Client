package net.sonmoosans.u3.ui.util;

import net.sonmoosans.u3.api.model.TextMessage;
import net.sonmoosans.u3.ui.component.MessageItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIMessageCache {
    //MessageID - MessageItem
    private static final HashMap<Integer, MessageItem> messageItems = new HashMap<>();
    //GroupID - MessageIDs
    private static final GroupMessageCacheMap messageCaches = new GroupMessageCacheMap();

    public void updateMessage(int messageID, String newContext) {
        messageItems.get(messageID).updateContext(newContext);
    }

    public void addMessage(MessageItem item, boolean bottom) {
        TextMessage message = item.getMessage();

        messageItems.putIfAbsent(message.ID, item);
        messageCaches.addMessage(message.groupID, message.ID, bottom);
    }

    public boolean isEnded(int groupID) {
        GroupMessageCache cache = messageCaches.getOrDefault(groupID, null);
        return cache != null && cache.isEnd;
    }

    public void setEnded(int groupID) {
        GroupMessageCache cache = messageCaches.get(groupID);
        cache.isEnd = true;
    }

    /**
     * @return Removed Item
     **/
    public MessageItem removeMessage(int groupID, int messageID) {
        MessageItem item = messageItems.remove(messageID);

        if (item != null)
            messageCaches.removeMessage(groupID, messageID);

        return item;
    }

    @Nullable
    public MessageItem getMessageItem(int messageID) {
        return messageItems.getOrDefault(messageID, null);
    }

    public List<Integer> getMessages(int groupID) {
        return messageCaches.getMessages(groupID);
    }

    private static class GroupMessageCacheMap extends HashMap<Integer, GroupMessageCache> {
        public void addMessage(int groupID, int messageID, boolean bottom) {
            compute(groupID, (k, v) -> {
                if (v == null) v = GroupMessageCache.getDefault();

                if (bottom)
                    v.messages.add(messageID);
                else
                    v.messages.add(0, messageID);

                return v;
            });
        }

        @Nullable
        public List<Integer> getMessages(int groupID) {
            GroupMessageCache cache = getOrDefault(groupID, null);
            return cache == null ? null : cache.messages;
        }

        public void removeMessage(int groupID, int messageID) {
            computeIfPresent(groupID, (k, v) -> {
                v.messages.remove((Integer) messageID);
                return v;
            });
        }
    }

    private static class GroupMessageCache {
        public final List<Integer> messages;
        public boolean isEnd;

        public GroupMessageCache(List<Integer> messages, boolean isEnd) {
            this.messages = messages;
            this.isEnd = isEnd;
        }

        public static GroupMessageCache getDefault() {
            return new GroupMessageCache(new ArrayList<>(), false);
        }
    }
}
