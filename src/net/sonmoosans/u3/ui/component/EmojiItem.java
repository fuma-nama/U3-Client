package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.EmojiAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Emoji;

import javax.swing.*;

import java.awt.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;
import static net.sonmoosans.u3.ui.util.EmojiUtil.getEmojiUrl;

public class EmojiItem extends ItemPane {
    private JPanel Main;
    private JLabel iconHolder;
    private JLabel nameLabel;
    private JButton buttonSave;
    private JPanel userContainer;
    private JPanel bottomBar;
    private JPanel detailPane;
    private JButton deleteButton;
    private JLabel avatarHolder;
    private JLabel creatorLabel;
    private boolean saved = false;

    public EmojiItem(Emoji emoji) {
        nameLabel.setText(emoji.name);
        setIconAsync(iconHolder, getEmojiUrl(emoji.ID));

        if (Memory.getSavedEmojis().contains(emoji.ID)) {
            setItemSaved();
        }

        buttonSave.addActionListener(e-> {
            buttonSave.setEnabled(false);

            if (saved) {
                runAsync(()-> EmojiAPI.unSaveEmoji(emoji.ID), success-> {
                    if (success)
                        setItemUnSaved();
                    else {
                        openErrorDialog(Main, "Failed to remove emoji");
                    }
                    buttonSave.setEnabled(true);
                });
            } else {
                runAsync(()-> EmojiAPI.saveEmoji(emoji.ID), success-> {
                    if (success)
                        setItemSaved();
                    else
                        openErrorDialog(Main, "Failed to save emoji");
                    buttonSave.setEnabled(true);
                });
            }
        });

        if (emoji.creatorID == Memory.getSelfUserID()) {
            deleteButton.setVisible(true);

            deleteButton.addActionListener(e-> {
                deleteButton.setEnabled(false);
                runAsync(()-> EmojiAPI.deleteEmoji(emoji.ID), success-> {
                    if (success)
                        Main.setVisible(false);
                    else {
                        openErrorDialog(Main, "Failed to delete emoji");
                        deleteButton.setEnabled(true);
                    }
                });
            });
            creatorLabel.setForeground(Color.YELLOW);
            creatorLabel.setText("You");
        } else {
            runAsync(()-> AccountAPI.getUserSafe(emoji.creatorID), user -> {
                setIconAsync(avatarHolder, user.avatar, 30);
                creatorLabel.setText(user.name);
            });
        }

        init();
    }

    private void setItemUnSaved() {
        saved = false;
        buttonSave.setText("Save");
    }

    private void setItemSaved() {
        saved = true;
        buttonSave.setText("Remove from Saves");
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        detailPane.setBackground(color);
        bottomBar.setBackground(color);
        userContainer.setBackground(color);
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
