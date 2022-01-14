package net.sonmoosans.u3.ui.popup;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class MessageOptionPopup extends JPopupMenu {
    private JPanel Main;
    protected JButton deleteButton;
    protected JButton editButton;
    protected JButton replyButton;
    protected JButton copyTextButton;

    public MessageOptionPopup(boolean canModify) {
        editButton.setVisible(canModify);
        deleteButton.setVisible(canModify);

        editButton.addActionListener(this::onEdit);
        deleteButton.addActionListener(this::onDelete);
        replyButton.addActionListener(this::onReply);
        copyTextButton.addActionListener(this::onCopy);

        this.add(Main);
    }

    public abstract void onEdit(ActionEvent e);

    public abstract void onDelete(ActionEvent e);

    public abstract void onCopy(ActionEvent e);

    public abstract void onReply(ActionEvent e);
}
