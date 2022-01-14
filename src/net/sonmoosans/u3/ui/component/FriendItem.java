package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.FriendAPI;
import net.sonmoosans.u3.api.model.Friend;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.api.model.UserProfile;

import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class FriendItem extends GroupItem {
    private final Consumer<FriendItem> onSelect;
    public final Friend friend;

    public FriendItem(UserProfile user, Friend friend, Consumer<FriendItem> onSelect) {
        super(new Group(-1, user.avatar, user.name, "", true).setID(friend.privateGroupID), (Consumer<GroupItem>)null);
        this.friend = friend;
        this.onSelect = onSelect;
        buttonTag.setText("Remove");
        buttonTag.addActionListener(e-> FriendAPI.removeFriend(friend.friendID));
        buttonTag.setVisible(true);
    }

    public FriendItem(Friend friend, Consumer<FriendItem> onSelect) {
        this(AccountAPI.getUserSafe(friend.friendID), friend, onSelect);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (onSelect != null)
            onSelect.accept(this);
    }
}
