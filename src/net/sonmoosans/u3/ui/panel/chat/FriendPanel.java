package net.sonmoosans.u3.ui.panel.chat;

import io.socket.client.Socket;
import net.sonmoosans.cfte.api.model.ChangeListener;
import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.FriendAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Friend;
import net.sonmoosans.u3.api.model.FriendInvite;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.ui.component.FriendInviteItem;
import net.sonmoosans.u3.ui.component.FriendItem;
import net.sonmoosans.u3.ui.component.GroupItem;
import net.sonmoosans.u3.ui.component.UserItem;
import net.sonmoosans.u3.ui.util.HashLinkedContainer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public abstract class FriendPanel extends ChatGroupContainer {
    private JPanel Main;
    private JTextField userIdField;
    private JButton addButton;
    private JPanel userContainer;
    private JPanel inviteContainer;
    private JPanel friendContainer;
    private JScrollPane scrollPane;
    private FriendItem selected;

    private final HashLinkedContainer<FriendInvite, FriendInviteItem, Integer> friendInvites =
            new HashLinkedContainer<>(
                    inviteContainer,
                    FriendInviteItem::new
            );

    private final HashLinkedContainer<Friend, FriendItem, Integer> friends = new HashLinkedContainer<>(
            friendContainer,
            key-> new FriendItem(key, this::onSelected)) {

        @Override
        protected void onSocketAdd(Friend key) {
            super.onSocketAdd(key);
            updateSelection();
        }

        @Override
        protected void onSocketRemove(Integer key) {
            super.onSocketRemove(key);
            updateSelection();
        }
    };

    private int userID = -1;

    public FriendPanel() {

        userIdField.getDocument().addDocumentListener(new ChangeListener(e-> {
            if (!userIdField.getText().isBlank()) try {
                userID = Integer.parseInt(userIdField.getText());
                UserProfile user = AccountAPI.getUser(userID);

                if (user != null) {
                    userContainer.add(new UserItem(user, null).getPanel(), BorderLayout.CENTER);
                    repaintContainer(Main);
                    return;
                }

            } catch (NumberFormatException ex) {
                userID = -1;
            }

            userContainer.removeAll();
            repaintContainer(Main);
        }));

        addButton.addActionListener(e-> addFriend());

        friendContainer.setLayout(new BoxLayout(friendContainer, BoxLayout.Y_AXIS));
        inviteContainer.setLayout(new BoxLayout(inviteContainer, BoxLayout.Y_AXIS));

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        updateFriends();
    }

    public void onSelected(GroupItem item) {
        if (selected != null)
            selected.setColor(selected.defaultColor);

        if (item != null) {
            item.setColor(GroupItem.selectedColor);

            if (item instanceof FriendItem friendItem) {
                selected = friendItem;
            }
        } else
            selected = null;
    }

    protected void updateSelection() {
        HashMap<Integer, FriendItem> map = friends.getMap();

        if (selected == null)  {
            if (!map.isEmpty())
                onSelected(map.values().toArray(GroupItem[]::new)[0]);
        } else {
            if (!friends.containsKey(selected.friend)) {
                onSelected(null);
            }
        }
    }

    private void addFriend() {

        if (userID == -1) {
            openErrorDialog(Main, "Invalid User ID");
        } else if (Memory.hasFriend(userID)) {
            openErrorDialog(Main, "Already added this friend");
        } else if (userID == Memory.getSelfUserID()) {
            openErrorDialog(Main, "You can't add yourself");
        } else {
            addButton.setEnabled(false);
            addButton.setText("Sending...");

            runAsync(()-> FriendAPI.sendInvite(userID), success -> {
                if (success) {
                    userIdField.setText(null);
                    addButton.setText("Sent Invite");
                    waitAsync(1000, this::resetAddButton);
                } else {
                    openErrorDialog(Main, "Failed to send friend invite");
                    resetAddButton();
                }
            });
        }
    }

    protected void updateFriends() {

        runAsync(FriendAPI::getInvites, result-> {
            if (result.isSuccess()) {
                for (FriendInvite invite : result.context()) {
                    friendInvites.add(invite);
                }
                repaintContainer(inviteContainer);
            }
        });

        runAsync(FriendAPI::getFriends, result-> {
            if (result.isSuccess()) {
                for (Friend friend : result.context()) {
                    friends.add(friend);
                }
            }
            repaintContainer(friendContainer);
        });
    }

    public HashLinkedContainer<Friend, FriendItem, Integer> getFriends() {
        return friends;
    }

    public void initSocket(Socket socket) {
        friends.linkMemory(Memory.getFriendPool());

        friendInvites.linkTo(socket,
                GroupAPI.FRIEND_INVITE_ADDED_EVENT,
                GroupAPI.FRIEND_INVITE_REMOVED_EVENT,
                Integer.class
        );

        friends.linkTo(socket, GroupAPI.FRIEND_ADDED_EVENT, GroupAPI.FRIEND_REMOVED_EVENT, Friend.class, Integer.class);
    }

    @Override
    public GroupItem getSelectedItem() {
        return selected;
    }

    private void resetAddButton() {
        addButton.setText("Add Friend");
        addButton.setEnabled(true);
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
