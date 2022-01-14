package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.FriendAPI;
import net.sonmoosans.u3.api.model.FriendInvite;
import net.sonmoosans.u3.api.model.UserProfile;

import javax.swing.*;

import java.util.function.Supplier;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class FriendInviteItem extends ItemPane {
    private JPanel Main;
    private JLabel avatarHolder;
    private JButton agreeButton;
    private JButton ignoreButton;
    private JLabel nameLabel;

    public FriendInviteItem(FriendInvite invite) {
        UserProfile user = AccountAPI.getUserSafe(invite.senderID);

        setIconAsync(avatarHolder, user.avatar);
        nameLabel.setText(user.name);

        agreeButton.addActionListener(e-> {
            agreeButton.setEnabled(false);
            runAsync(()-> FriendAPI.agreeInvite(invite.senderID), success-> {
                if (success)
                    getPanel().setVisible(false);
                else {
                    openErrorDialog(Main, "Failed to agree friend invite");
                    agreeButton.setEnabled(true);
                }
            });
        });

        ignoreButton.addActionListener(e-> {
            ignoreButton.setEnabled(false);
            runAsync(()-> FriendAPI.removeInvite(invite.senderID), success-> {
                if (success)
                    getPanel().setVisible(false);
                else {
                    openErrorDialog(Main, "Failed to remove friend invite");
                    ignoreButton.setEnabled(true);
                }
            });
        });

        updateSize();

        init();
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
