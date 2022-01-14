package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.model.GroupInvite;

import javax.swing.*;

import java.awt.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class GroupInviteItem extends ItemPane {
    private JPanel Main;
    private JLabel senderLabel;
    private JPanel groupInfoPane;
    private JLabel iconHolder;
    private JLabel groupLabel;
    private JButton joinButton;
    private JButton ignoreButton;
    private JPanel actionBar;
    private JPanel headPane;

    public GroupInviteItem(GroupInvite invite) {
        runAsync(()-> AccountAPI.getUserSafe(invite.senderID), sender-> {
            senderLabel.setText(sender.name);
            setIconAsync(senderLabel, sender.avatar);
            updateSize();
        });

        runAsync(()-> GroupAPI.getGroupProfile(invite.groupID), group-> {
            if (group == null) getPanel().setVisible(false);
            else {
                setIconAsync(iconHolder, group.avatar);
                groupLabel.setText(group.name);
                updateSize();
            }
        });

        joinButton.addActionListener(e-> {
             runAsync(()-> GroupAPI.joinGroupFromInvite(invite.groupID, invite.senderID), success -> {
                 if (!success)
                     openErrorDialog(Main, "Failed to join group");
             });
        });

        ignoreButton.addActionListener(e-> {
            runAsync(()-> GroupAPI.removeInvite(invite.groupID, invite.senderID), success -> {
                if (!success)
                    openErrorDialog(Main, "Failed to remove group invite");
            });
        });

        init();
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        headPane.setBackground(color);
        groupInfoPane.setBackground(color.brighter());
        actionBar.setBackground(color);
    }
}
