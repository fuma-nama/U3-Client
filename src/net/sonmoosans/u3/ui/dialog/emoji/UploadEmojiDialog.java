package net.sonmoosans.u3.ui.dialog.emoji;

import net.sonmoosans.u3.api.EmojiAPI;
import net.sonmoosans.u3.ui.util.CommonUtil;
import net.sonmoosans.u3.util.FileUtil;

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class UploadEmojiDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel iconHolder;
    private JTextField nameField;
    private BufferedImage selectedIcon;
    private String selectedFileName;

    public UploadEmojiDialog(JDialog parent) {
        super(parent, "Upload Emoji");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        CommonUtil.setIconChooser(iconHolder, this, (image, file) -> {
            selectedIcon = image;
            selectedFileName = file.getName();
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void onOK() {
        if (isValidData(nameField, CommonUtil::isValidName)) {
            try {
                File out = FileUtil.writeTempImage(selectedFileName, selectedIcon);
                runAsync(()-> EmojiAPI.addEmoji(nameField.getText(), out), success -> {
                    if (success) {
                        dispose();
                    }
                });
            } catch (IOException e) {
                openErrorDialog(this, "Failed to upload emoji");
            }
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
