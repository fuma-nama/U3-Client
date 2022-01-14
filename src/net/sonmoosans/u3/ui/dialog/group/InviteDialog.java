package net.sonmoosans.u3.ui.dialog.group;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Friend;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.ui.component.GroupItem;
import net.sonmoosans.u3.ui.component.UserItem;
import net.sonmoosans.u3.ui.screen.ChatScreen;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public class InviteDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonDone;
    private JPanel userContainer;
    private JTextField searchField;
    private JButton searchByIDButton;
    private final int groupID;

    public InviteDialog(Window window, int groupID) {
        super(window, "Invite Friends");
        this.groupID = groupID;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonDone);

        buttonDone.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        userContainer.setLayout(new BoxLayout(userContainer, BoxLayout.Y_AXIS));

        for (Friend friend : Memory.getFriends()) {
            UserProfile user = AccountAPI.getUserSafe(friend.friendID);

            userContainer.add(new InviteItem(user, friend.friendID).getPanel());
            pack();
        }
    }

    private void onCancel() {
        dispose();
    }

    private class InviteItem extends UserItem {
        public InviteItem(UserProfile user, int userID) {
            super(user, "Invite");
            onClickTag(e-> GroupAPI.sendInvite(groupID, userID));
        }
    }
}
