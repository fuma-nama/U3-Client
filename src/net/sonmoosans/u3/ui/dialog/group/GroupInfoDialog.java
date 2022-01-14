package net.sonmoosans.u3.ui.dialog.group;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.ui.component.UserItem;
import net.sonmoosans.u3.ui.popup.InviteOptionsPopup;
import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class GroupInfoDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonInvite;
    private JButton buttonLeave;
    private JLabel nameLabel;
    private JPanel memberContainer;
    private JLabel memberLabel;
    private JLabel detailLabel;
    private JButton buttonEdit;
    private final Group group;

    public GroupInfoDialog(JFrame owner, Group group) {
        super(owner, "Group Info");

        if (group.id == null) throw new NullPointerException("Group ID is null");

        this.group = group;
        nameLabel.setText(group.name);
        detailLabel.setText(group.description);

        CommonUtil.setIconAsync(nameLabel, group.avatar);
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonInvite);

        if (group.creatorID != Memory.getSelfUserID()) {
            buttonInvite.setEnabled(false);
            buttonInvite.setToolTipText("Only the group owner can create invite codes");
            buttonEdit.setVisible(false);
        } else {
            setPopupButton(buttonInvite, ()-> new InviteOptionsPopup(group.id));

            buttonEdit.addActionListener(e-> {
                EditGroupDialog dialog = new EditGroupDialog(this, group);
                dialog.pack();
                dialog.setLocationRelativeTo(buttonEdit);
                dialog.setVisible(true);
            });
        }

        buttonLeave.addActionListener(e -> onLeave());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        memberContainer.setLayout(new BoxLayout(memberContainer, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(400, 600));

        this.updateMembers();
    }

    private void onLeave() {
        int reply = JOptionPane.showConfirmDialog(getParent(),
                "Do you want to leave this group?",
                "Warning", JOptionPane.YES_NO_OPTION);

        if (reply == JOptionPane.YES_OPTION) {
            GroupAPI.leaveGroupAsync(group.id);
            dispose();
        }
    }

    protected void updateMembers() {
        runAsync(()-> GroupAPI.getMembers(group.id), members-> {

            memberContainer.removeAll();
            memberLabel.setText("Members Count: " + members.length);

            for (int memberID : members) {
                UserProfile user = AccountAPI.getUserSafe(memberID);
                memberContainer.add(new UserItem(
                        user, memberID == group.creatorID? "Creator" : null
                ).getPanel());
            }
        });
    }
}
