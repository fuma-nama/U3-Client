package net.sonmoosans.u3.ui.panel.chat;

import io.socket.client.Socket;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.ui.component.GroupItem;
import net.sonmoosans.u3.ui.util.HashLinkedContainer;

import javax.swing.*;
import java.util.HashMap;

import static net.sonmoosans.u3.ui.util.CommonUtil.repaintContainer;
import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public abstract class GroupsPanel extends ChatGroupContainer {
    private JPanel Main;
    private JScrollPane scrollPane;
    private JPanel groupContainer;
    protected GroupItem selected;

    private final HashLinkedContainer<Group, GroupItem, Integer> groups = new HashLinkedContainer<>(
            groupContainer,
            key-> new GroupItem(key, this::onSelected)
    );

    public GroupsPanel() {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        groupContainer.setLayout(new BoxLayout(groupContainer, BoxLayout.Y_AXIS));

        updateGroups();
    }

    protected void updateGroups() {
        runAsync(GroupAPI::getJoinedGroups, result-> {
            if (result.isSuccess()) {
                for (int groupID : result.context()) {
                    Group group = GroupAPI.getGroupProfile(groupID);
                    if (group != null) {
                        groups.add(group);
                    }
                }

                updateSelection();
                repaintContainer(groupContainer);
            }
        });
    }

    protected void updateSelection() {
        HashMap<Integer, GroupItem> map = groups.getMap();

        if (selected == null)  {
            if (!map.isEmpty())
                onSelected(map.values().toArray(GroupItem[]::new)[0]);
        } else {
            if (!groups.containsKey(selected.group)) {
                onSelected(null);
            }
        }
    }

    public void initSocket(Socket socket) {
        socket.on(GroupAPI.JOIN_GROUP_EVENT, args -> {
            int groupID = (int) args[0];
            runAsync(()-> GroupAPI.getGroupProfile(groupID), group -> {
                if (group != null) {
                    groups.add(group);
                    updateSelection();
                    repaintContainer(groupContainer);
                }
            });
        });

        socket.on(GroupAPI.LEAVE_GROUP_EVENT, args -> {
            int groupID = (int) args[0];
            GroupItem item =  groups.remove(groupID);

            if (item != null) {
                updateSelection();
                repaintContainer(groupContainer);
            }
        });

        socket.on(GroupAPI.GROUP_PROFILE_UPDATE, args -> {
            int groupID = (int) args[0];

            String name = (String) args[1],
                    description = (String) args[2],
                    iconUrl = (String) args[3];

            Memory.updateGroup(groupID, group-> {
                group.name = name;
                group.description = description;

                if (iconUrl != null)
                    group.avatar = iconUrl;
            });
            groups.getIfPresent(groupID, GroupItem::updateGroup);

            repaintContainer(groupContainer);
        });
    }

    public void onSelected(GroupItem item) {
        if (selected != null)
            selected.setColor(selected.defaultColor);

        if (item != null) {
            item.setColor(GroupItem.selectedColor);
        }

        selected = item;
    }

    public HashLinkedContainer<Group, GroupItem, Integer> getGroups() {
        return groups;
    }

    @Override
    public GroupItem getSelectedItem() {
        return selected;
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
