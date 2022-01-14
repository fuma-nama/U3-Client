package net.sonmoosans.u3.ui.panel.chat;

import com.formdev.flatlaf.ui.FlatBorder;
import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.component.CFTextArea;
import net.sonmoosans.u3.ui.component.FileItem;
import net.sonmoosans.u3.ui.component.MessageItem;
import net.sonmoosans.u3.ui.popup.EmojiPopup;
import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;
import static net.sonmoosans.u3.ui.util.CommonUtil.repaintContainer;

public abstract class MessageBar extends AddablePanel {
    private JPanel Main;
    private JPanel fileContainer;
    private JButton stopReplyButton;
    private JLabel replyNameLabel;
    private JCheckBox htmlCheckBox;
    private JPanel replyPane;
    private JButton buttonAddFile;
    private JButton sendButton;
    private JButton emojibutton;
    private CFTextArea messageArea;

    private final List<File> messageFiles = new ArrayList<>();
    private MessageItem replyMessage = null;

    public MessageBar() {
        messageArea.setBorder(new FlatBorder());

        htmlCheckBox.addItemListener(e->
                emojibutton.setVisible(!htmlCheckBox.isSelected())
        );

        sendButton.addActionListener(e-> {
            Integer currentGroupID = getCurrentGroupID();

            if (currentGroupID != null && isValidData(messageArea, CommonUtil::isValidContext)) {
                String text = messageArea.getText();
                runAsync(()-> {
                    if (replyMessage != null) {
                        int replyTo = replyMessage.getMessage().ID;
                        return GroupAPI.sendMessage(currentGroupID, replyTo, text, messageFiles, htmlCheckBox.isSelected());
                    } else return GroupAPI.sendMessage(currentGroupID, text, messageFiles, htmlCheckBox.isSelected());
                }, success-> {
                    if (success) {
                        messageFiles.clear();
                        fileContainer.removeAll();
                        messageArea.setText(null);
                        closeReply();
                        repaintContainer(fileContainer);
                    } else
                        System.out.println("Failed to send message");
                });
            }
        });

        buttonAddFile.addActionListener(e -> {
            File[] files = chooseFiles(Main, CommonUtil.FileType.ALL);
            if (files == null) return;

            for (File file : files) {
                try {
                    fileContainer.add(
                            new FileItem(file)
                                    .setSideButton("x", item-> {
                                        fileContainer.remove(item.getPanel());
                                        messageFiles.remove(file);
                                        repaintContainer(fileContainer);
                                    })
                                    .getPanel()
                    );
                    messageFiles.add(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            repaintContainer(fileContainer);
        });

        emojibutton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (htmlCheckBox.isSelected()) return;

                EmojiPopup popup = new EmojiPopup(messageArea);

                Dimension size = new Dimension(500, 500);
                popup.setPreferredSize(size);

                popup.show(emojibutton, e.getX() - size.width, e.getY() - size.height);
            }
        });

        stopReplyButton.addActionListener(e-> closeReply());

        setClickable(replyNameLabel, e-> jumpToMessage(replyMessage));
    }

    public void setReplyTo(MessageItem message) {
        replyPane.setVisible(true);
        replyMessage = message;
        replyNameLabel.setText(AccountAPI.getUserSafe(message.getMessage().senderID).name);
    }

    private void closeReply() {
        replyMessage = null;
        replyPane.setVisible(false);
    }

    protected abstract void jumpToMessage(MessageItem message);

    @Nullable
    protected abstract Integer getCurrentGroupID();

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
