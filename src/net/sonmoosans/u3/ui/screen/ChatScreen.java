package net.sonmoosans.u3.ui.screen;

import com.formdev.flatlaf.FlatDarkLaf;
import io.socket.client.Socket;
import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.TextMessage;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.component.*;
import net.sonmoosans.u3.ui.dialog.group.CreateGroupDialog;
import net.sonmoosans.u3.ui.dialog.emoji.EmojiLibraryDialog;
import net.sonmoosans.u3.ui.dialog.SettingsDialog;
import net.sonmoosans.u3.ui.panel.chat.ChatView;
import net.sonmoosans.u3.ui.panel.chat.FriendPanel;
import net.sonmoosans.u3.ui.panel.chat.GroupInvitesPanel;
import net.sonmoosans.u3.ui.panel.chat.GroupsPanel;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import static net.sonmoosans.u3.manager.Application.DEFAULT_THEME;
import static net.sonmoosans.u3.manager.Application.THEME;
import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class ChatScreen {
    //TODO: Improve Voice Chat
    public JPanel Main;
    private JButton buttonCreateGroup;
    private JPanel selfInfoContainer;
    private JPanel messagePane;
    private JButton emojiMarketButton;
    private JTabbedPane tabbedPane;

    private final FriendPanel friendPanel = new FriendPanel() {
        @Override
        public void onSelected(GroupItem item) {
            super.onSelected(item);
            ChatScreen.this.onSelected(item);
        }
    };

    private final GroupsPanel groupsPanel = new GroupsPanel() {
        @Override
        public void onSelected(GroupItem item) {
            super.onSelected(item);
            ChatScreen.this.onSelected(item);
        }
    };

    private final ChatView chatView = new ChatView() {
        @Override
        protected void addUnread(TextMessage message) {
            ChatScreen.this.addUnread(message);
        }
    };

    private final GroupInvitesPanel invitesPanel = new GroupInvitesPanel();

    public static void main(String[] args) {
        String className = Application.getPrefs().get(THEME, DEFAULT_THEME);
        System.out.println(className);
        try {
            UIManager.setLookAndFeel((LookAndFeel) Class.forName(className).getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            FlatDarkLaf.install();
            e.printStackTrace();
        }

        LoadingScreen.loadClient(token -> {
            JFrame frame = new JFrame("Untitled 3 V1");
            Application.setAppFrame(frame);
            ChatScreen screen = new ChatScreen(frame);
            Application.setChatScreen(screen);
            frame.setContentPane(screen.Main);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    screen.onCloseApplication();
                }
            });
            frame.setVisible(true);
        });
    }

    public ChatScreen(JFrame frame) {
        messagePane.setVisible(false);
        Main.setBackground(Main.getBackground().darker());

        setDialogButton(emojiMarketButton,()-> new EmojiLibraryDialog(frame));

        buttonCreateGroup.addActionListener(e -> {
            CreateGroupDialog dialog = new CreateGroupDialog(frame);
            dialog.pack();
            dialog.setLocationRelativeTo(buttonCreateGroup);
            dialog.setVisible(true);
        });

        chatView.addTo(messagePane);
        groupsPanel.addTo("Groups", tabbedPane);
        friendPanel.addTo("Friends", tabbedPane);
        invitesPanel.addTo("Invites", tabbedPane);

        initSocket(GroupAPI.getSocket());
        updateSelfUser();
    }

    public void initSocket(Socket socket) {
        friendPanel.initSocket(socket);
        invitesPanel.initSocket(socket);
        groupsPanel.initSocket(socket);
        chatView.initSocket(socket);

        socket.on(GroupAPI.USER_PROFILE_UPDATE, args -> {
            Memory.updateUser(Memory.getSelfUserID(), user-> {
                user.name = (String) args[0];

                if (args[1] != null)
                    user.avatar = (String) args[1];
            });
            updateSelfUser();
        });

        socket.on(GroupAPI.EMOJI_SAVED_EVENT, args-> Memory.getSavedEmojis().add((int) args[0]));

        socket.on(GroupAPI.EMOJI_REMOVED_EVENT, args-> Memory.getSavedEmojis().remove((int) args[0]));
    }

    public void onCloseApplication() {
        GroupAPI.removeReadingGroup();
    }

    protected void updateSelfUser() {
        runAsync(()-> AccountAPI.getUser(Memory.getSelfUserID()), selfUser-> {
            selfInfoContainer.removeAll();

            selfInfoContainer.add(new UserItem(selfUser, "Settings")
                            .onClickTag(e-> {
                                SettingsDialog dialog = new SettingsDialog(Application.getAppFrame());
                                dialog.setMinimumSize(new Dimension(600, 700));
                                dialog.setLocationRelativeTo(null);
                                dialog.setVisible(true);
                            })
                    .getPanel(), BorderLayout.CENTER);

            repaintContainer(selfInfoContainer);
        });
    }

    protected void onSelected(@Nullable GroupItem item) {
        if (item == null || item.group.id == null) {
            messagePane.setVisible(false);
            GroupAPI.removeReadingGroup();
            return;
        }

        if (!messagePane.isVisible())
            messagePane.setVisible(true);

        chatView.setReadingGroup(item);
        GroupAPI.updateReadingGroup(item.group.id);
    }

    private void addUnread(TextMessage message) {
        //Get Group Item
        GroupItem item = groupsPanel.getGroups().get(message.groupID);

        if (item != null) {
            item.addUnreadCount();
        } else {
            //Get Friend Item
            friendPanel.getFriends().getIfPresent(message.senderID, FriendItem::addUnreadCount);
        }
    }

    public HashMap<Integer, GroupItem> getJoinedGroups() {
        return groupsPanel.getGroups().getMap();
    }
}
