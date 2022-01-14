package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.ui.layout.WrapLayout;
import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public class GroupItem extends ItemPane {
    public static final Color selectedColor = new Color(0, 88, 178);

    private JPanel Main;
    private JLabel nameLabel;
    private JLabel iconHolder;
    private JLabel detailLabel;
    private JPanel contextPane;
    protected JButton buttonTag;
    private JLabel unreadLabel;
    private JPanel unreadPane;
    private JPanel headPane;
    private final Consumer<GroupItem> onSelect;
    public Group group;

    public GroupItem(Group group, Consumer<GroupItem> onSelect) {
        this.group = group;
        this.onSelect = onSelect;
        nameLabel.setText(group.name);
        detailLabel.setText(group.description);
        CommonUtil.setIconAsync(iconHolder, group.avatar);

        runAsync(()-> GroupAPI.getUnreadMessageCount(group.id), result-> {
            if (result.isSuccess()) {
                group.setUnreadCount(result.context());
                updateUnread();
            }
        });

        headPane.setLayout(new WrapLayout(FlowLayout.LEFT));

        init();

        updateSize();
    }

    public GroupItem(Group group, String tag) {
        this(group, (Consumer<GroupItem>) null);

        buttonTag.setText(tag);
        buttonTag.setVisible(true);
    }

    public void updateGroup() {
        this.group = GroupAPI.getGroupProfile(group.id);
        nameLabel.setText(group.name);
        detailLabel.setText(group.description);
        CommonUtil.setIconAsync(iconHolder, group.avatar);
    }

    public void addUnreadCount() {
        group.unreadCount++;

        updateUnread();
    }

    public void clearUnreadCount() {
        group.unreadCount = 0;

        updateUnread();
    }

    private void updateUnread() {
        if (group.unreadCount != 0) {
            if (!unreadPane.isVisible())
                unreadPane.setVisible(true);
            unreadLabel.setText(String.valueOf(group.unreadCount));
        }
        else unreadPane.setVisible(false);

        updateSize();
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (onSelect != null)
            onSelect.accept(this);
    }

    public void setColor(Color color) {
        super.setColor(color);
        contextPane.setBackground(color);
        headPane.setBackground(color);
    }
}
