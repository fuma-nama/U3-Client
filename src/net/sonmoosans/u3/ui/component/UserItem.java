package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.ui.popup.UserDetailPopup;
import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class UserItem extends ItemPane {
    private JPanel Main;
    private JLabel iconHolder;
    private JLabel nameLabel;
    private JButton tagButton;
    private JPanel contextPane;
    private JLabel idLabel;

    public UserItem(UserProfile user, @Nullable String tag) {

        nameLabel.setText(user.name);

        if (user.userID != -1)
            idLabel.setText("ID: " + user.userID);
        else
            idLabel.setVisible(false);

        CommonUtil.setIconAsync(iconHolder, user.avatar);
        if (tag != null) {
            tagButton.setText(tag);
            tagButton.setVisible(true);
        }

        if (user.userID != Memory.getSelfUserID())
            Main.setComponentPopupMenu(new UserDetailPopup(user));

        init();
    }

    public UserItem onClickTag(ActionListener listener) {
        tagButton.addActionListener(listener);
        return this;
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        contextPane.setBackground(color);
    }
}
