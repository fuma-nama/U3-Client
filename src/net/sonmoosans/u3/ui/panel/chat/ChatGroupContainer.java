package net.sonmoosans.u3.ui.panel.chat;

import io.socket.client.Socket;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.component.GroupItem;

public abstract class ChatGroupContainer extends AddablePanel {
    public abstract void initSocket(Socket socket);

    public abstract void onSelected(GroupItem item);

    public abstract GroupItem getSelectedItem();
}
