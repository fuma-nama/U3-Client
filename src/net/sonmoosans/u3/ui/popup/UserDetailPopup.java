package net.sonmoosans.u3.ui.popup;

import net.sonmoosans.u3.api.FriendAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.UserProfile;

import javax.swing.*;

import java.awt.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;
import static net.sonmoosans.u3.ui.util.CommonUtil.setIconAsync;

public class UserDetailPopup extends JPopupMenu {
    private JPanel Main;
    private JLabel avatarHolder;
    private JLabel nameLabel;
    private JPanel actionBar;
    private JButton addFriendButton;
    private JLabel idLabel;

    public UserDetailPopup(UserProfile user) {
        int userID = user.userID;

        setIconAsync(avatarHolder, user.avatar);
        nameLabel.setText(user.name);
        idLabel.setText("ID: " + userID);

        if (userID == Memory.getSelfUserID() || Memory.hasFriend(userID)) {
            addFriendButton.setVisible(false);
        } else {
            addFriendButton.addActionListener(e-> {
                addFriendButton.setEnabled(false);
                runAsync(()-> FriendAPI.sendInvite(userID), success-> {
                    if (success) {
                        setVisible(false);
                    }

                    addFriendButton.setEnabled(true);
                });
            });
        }

        super.add(Main);
    }
}
