package net.sonmoosans.u3.ui.popup;

import net.sonmoosans.u3.ui.dialog.group.InviteCodeDialog;
import net.sonmoosans.u3.ui.dialog.group.InviteDialog;
import net.sonmoosans.u3.ui.screen.ChatScreen;

import javax.swing.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.setDialogButton;

public class InviteOptionsPopup extends CJPopupMenu {
    private JPanel Main;
    private JButton friendButton;
    private JButton codeButton;

    public InviteOptionsPopup(int groupID) {
        setDialogButton(friendButton, ()-> new InviteDialog(getFrame(), groupID));
        setDialogButton(codeButton, ()-> new InviteCodeDialog(getFrame(), groupID));

        super.add(Main);
    }
}
