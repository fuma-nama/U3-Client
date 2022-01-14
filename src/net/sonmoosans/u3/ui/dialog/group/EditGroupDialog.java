package net.sonmoosans.u3.ui.dialog.group;

import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.ui.util.CommonUtil;
import net.sonmoosans.u3.util.FileUtil;

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class EditGroupDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel iconHolder;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private BufferedImage selectedIcon;
    private String selectedFileName;
    private final Group group;

    public EditGroupDialog(JDialog parent, Group group) {
        super(parent, "Edit Group Profile");
        this.group = group;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        nameField.setText(group.name);
        descriptionArea.setText(group.description);

        setIconChooser(iconHolder, this, (image, file) -> {
            selectedIcon = image;
            selectedFileName = file.getName();
        });

        setIconAsync(iconHolder, group.avatar);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

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
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (isValidData(nameField, CommonUtil::isValidName) && isValidData(descriptionArea, CommonUtil::isValidDescription)) {
            buttonOK.setEnabled(false);

            runAsync(()-> {
                if (selectedIcon != null && selectedFileName != null) {
                    try {
                        File out = FileUtil.writeTempImage(selectedFileName, selectedIcon);
                        return GroupAPI.updateGroup(group.id, nameField.getText(), descriptionArea.getText(), out);
                    } catch (Exception e) {
                        return false;
                    }
                } else
                    return GroupAPI.updateGroup(group.id, nameField.getText(), descriptionArea.getText());
            }, success-> {
                if (success)
                    dispose();
                else
                    openErrorDialog(this, "Failed to update group profile");
            });
        }
    }

    private void onCancel() {
        dispose();
    }
}
