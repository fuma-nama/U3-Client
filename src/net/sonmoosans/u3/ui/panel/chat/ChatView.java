package net.sonmoosans.u3.ui.panel.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.socket.client.Socket;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.api.model.TextMessage;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.component.GroupItem;
import net.sonmoosans.u3.ui.component.MessageItem;
import net.sonmoosans.u3.ui.dialog.group.GroupInfoDialog;
import net.sonmoosans.u3.ui.util.GUIMessageCache;

import javax.annotation.Nullable;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Objects;

import static net.sonmoosans.u3.api.core.APICaller.mapper;
import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public abstract class ChatView extends AddablePanel {
    private JPanel Main;
    private JScrollPane messageScrollPane;
    private JPanel messageContainer;
    private JLabel nameLabel;
    private JButton moreButton;
    private JPanel messagePane;
    private JPanel actionBar;

    private final GUIMessageCache messageCaches = new GUIMessageCache();

    private final MessageBar messageBar = new MessageBarImpl();
    private final GroupAudioPanel audioPanel = new GroupAudioPanel();

    private boolean autoLoad = false, isBottom = false;
    private Integer currentGroupID = null;

    public ChatView() {
        messageBar.addTo(messagePane);

        actionBar.add(audioPanel.getPanel(), 0);

        moreButton.addActionListener(e-> {
            Group group = Memory.getGroup(currentGroupID);

            if (!group.isPrivate) {
                GroupInfoDialog dialog = new GroupInfoDialog(Application.getAppFrame(), group);
                dialog.pack();
                dialog.setLocationRelativeTo(moreButton);
                dialog.setVisible(true);
            }
        });

        messageScrollPane.getVerticalScrollBar().addAdjustmentListener(e ->{
            loadNewMessages();
        });

        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));

        messageContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onViewSizeChanged();
            }
        });

        messageScrollPane.getVerticalScrollBar().setUnitIncrement(20);
    }

    public void initSocket(Socket socket) {
        audioPanel.initSocket(socket);

        socket.on(GroupAPI.MESSAGE_EVENT, args -> {
            try {
                TextMessage message =  mapper.readValue((String) args[0], TextMessage.class);

                addMessage(message, Objects.equals(message.groupID, currentGroupID), true);
            } catch (JsonProcessingException e) {
                //Invalid message format
            }
        });

        socket.on(GroupAPI.MESSAGE_UPDATED_EVENT, args -> {
            int groupID = (int) args[0],
                    messageID = (int) args[1];
            String newContext = (String) args[2];

            messageCaches.updateMessage(messageID, newContext);

            if (Objects.equals(currentGroupID, groupID))
                repaintContainer(messageContainer);
        });

        socket.on(GroupAPI.MESSAGE_DELETE_EVENT, args -> {
            int groupID = (int) args[0],
                    messageID = (int) args[1];

            MessageItem item = messageCaches.removeMessage(groupID, messageID);

            if (item != null && Objects.equals(currentGroupID, groupID)) {
                messageContainer.remove(item.getPanel());
                repaintContainer(messageContainer);
            }
        });
    }

    public void setReadingGroup(GroupItem item) {
        currentGroupID = item.group.id;

        nameLabel.setText(item.group.name);

        audioPanel.setGroup(item.group);

        loadMessages(item);
    }

    private MessageItem addMessage(TextMessage message, boolean addToContainer, boolean toBottom) {
        MessageItem item = new MessageItemImpl(message);

        if (addToContainer) {
            if (toBottom)
                messageContainer.add(getPanel(item));
            else
                messageContainer.add(getPanel(item), 0);
        } else if (message.senderID != Memory.getSelfUserID()) {
            addUnread(message);
        }

        messageCaches.addMessage(item, toBottom);

        return item;
    }

    private void loadNewMessages() {

        JScrollBar scrollBar = messageScrollPane.getVerticalScrollBar();

        int value = scrollBar.getValue();

        if (!autoLoad || currentGroupID == null || messageCaches.isEnded(currentGroupID)) return;

        if (value == scrollBar.getModel().getMinimum()) {
            autoLoad = false;

            List<Integer> cacheMessages = messageCaches.getMessages(currentGroupID);

            if (cacheMessages != null)
                runAsync(()-> GroupAPI.getMessages(currentGroupID, cacheMessages.size()), messages-> {
                    if (messages.length == 0) {
                        messageCaches.setEnded(currentGroupID);
                        return;
                    }

                    int height = 0;
                    for (TextMessage message : messages) {
                        height += addMessage(message, true, false).getPanel().getPreferredSize().height;
                    }
                    JScrollBar scroll = messageScrollPane.getVerticalScrollBar();
                    scroll.setValue(scroll.getValue() + height);
                    repaintContainer(messageContainer);

                    autoLoad = true;
                });
        }
    }

    private void loadMessages(GroupItem groupItem) throws NullPointerException {
        autoLoad = false;

        messageContainer.removeAll();

        if (groupItem.group.id == null) throw new NullPointerException("Group ID can't be null");

        int groupID = groupItem.group.id;

        List<Integer> cacheMessages = messageCaches.getMessages(groupID);

        if (cacheMessages != null) {
            int newMessageLineIndex = groupItem.group.unreadCount - 1;

            for (int i = 0;i < cacheMessages.size();i++) {
                MessageItem item = Objects.requireNonNull(messageCaches.getMessageItem(cacheMessages.get(i)));
                addMessage(item);

                item.setNewMessageVisible(i == newMessageLineIndex);
            }
            groupItem.clearUnreadCount();
            scrollToBottom();
            autoLoad = true;
        } else
            runAsync(()-> GroupAPI.getMessages(groupID, 0), messages-> {

                int newMessageLineIndex = groupItem.group.unreadCount - 1;

                for (int i = 0;i < messages.length;i++) {
                    MessageItem item = addMessage(messages[i], true, false);

                    item.setNewMessageVisible(i == newMessageLineIndex);
                }
                groupItem.clearUnreadCount();
                scrollToBottom();
                autoLoad = true;
            });
    }

    private void scrollToElement(JComponent component) {
        messageScrollPane.getVerticalScrollBar().setValue(component.getLocation().y);
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    private void scrollToBottom() {
        messageContainer.validate();
        messageScrollPane.validate();

        JScrollBar sb = messageScrollPane.getVerticalScrollBar();

        sb.setValue(sb.getMaximum());
    }

    private void onViewSizeChanged() {
        if (isBottom)
            scrollToBottom();

        JScrollBar scrollBar = messageScrollPane.getVerticalScrollBar();

        int value = scrollBar.getValue(), max = scrollBar.getMaximum();

        isBottom = value >= max - 930;
    }

    private void addMessage(MessageItem item) {
        messageContainer.add(getPanel(item));
    }

    private JPanel getPanel(MessageItem item) {
        JPanel panel = item.getPanel();
        panel.setPreferredSize(new Dimension(messageContainer.getWidth(), panel.getPreferredSize().height));
        return panel;
    }

    private void jumpToMessage(MessageItem item) {
        scrollToElement(item.getPanel());

        item.setColor(item.defaultColor.brighter().brighter());

        waitAsync(1000, ()-> item.setColor(item.defaultColor));
    }

    protected abstract void addUnread(TextMessage message);

    class MessageItemImpl extends MessageItem {
        public MessageItemImpl(TextMessage message) {
            super(message);
        }

        @Override
        protected void jumpToMessage(int messageID) {
            MessageItem item = messageCaches.getMessageItem(messageID);
            if (item != null)
                ChatView.this.jumpToMessage(item);
        }

        @Override
        public void onSetReply() {
            messageBar.setReplyTo(this);
        }
    }

    class MessageBarImpl extends MessageBar {
        @Override
        protected void jumpToMessage(MessageItem message) {
            ChatView.this.jumpToMessage(message);
        }

        @Nullable
        @Override
        protected Integer getCurrentGroupID() {
            return currentGroupID;
        }
    }
}