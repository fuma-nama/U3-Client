package net.sonmoosans.u3.ui.panel.chat;

import io.socket.client.Socket;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.model.GroupInvite;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.component.GroupInviteItem;
import net.sonmoosans.u3.ui.util.HashLinkedContainer;

import javax.swing.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.repaintContainer;
import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public class GroupInvitesPanel extends AddablePanel {
    private JPanel Main;
    private JScrollPane scrollPane;
    private JPanel invitesContainer;

    private final HashLinkedContainer<GroupInvite, GroupInviteItem, GroupInvite> groupInvites =
            new HashLinkedContainer<>(
                    invitesContainer,
                    GroupInviteItem::new
            );

    public GroupInvitesPanel() {
        invitesContainer.setLayout(new BoxLayout(invitesContainer, BoxLayout.Y_AXIS));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        initInvites();
    }

    public void initSocket(Socket socket) {
        groupInvites.linkTo(socket,
                GroupAPI.GROUP_INVITE_ADDED_EVENT,
                GroupAPI.GROUP_INVITE_REMOVED_EVENT,
                GroupInvite.class
        );
    }

    protected void initInvites() {
        runAsync(GroupAPI::getInvites, result-> {
            if (result.isSuccess()) {
                for (GroupInvite invite : result.context()) {
                    groupInvites.add(invite);
                }
                repaintContainer(invitesContainer);
            }
        });
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
