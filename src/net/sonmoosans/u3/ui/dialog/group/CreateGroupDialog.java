package net.sonmoosans.u3.ui.dialog.group;


import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.ui.util.CommonUtil;
import net.sonmoosans.u3.util.FileUtil;

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class CreateGroupDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonCreate;
    private JButton buttonCancel;
    private JButton resetIconButton;
    private JLabel iconView;
    private JTextField nameField;
    private JLabel errorLabel;
    private JTextArea descriptionArea;
    private JTextField codeField;
    private JButton buttonJoin;
    private JLabel wrongCodeLabel;
    private BufferedImage selectedIcon;
    private String selectedFileName;

    public CreateGroupDialog(JFrame owner) {
        super(owner, "Create Group");
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonCreate);

        buttonCreate.addActionListener(e -> onCreate());

        buttonCancel.addActionListener(e -> onCancel());

        resetIconButton.addActionListener(e -> {
            setBlankIcon(iconView);
            selectedIcon = null;
            selectedFileName = null;
        });

        setIconChooser(iconView, this, (image, file) -> {
            selectedIcon = image;
            selectedFileName = file.getName();
        });

        buttonJoin.addActionListener(e-> {
            runAsync(()-> GroupAPI.joinGroup(codeField.getText()), success-> {
                if (success) dispose();
                else {
                    wrongCodeLabel.setVisible(true);
                    pack();
                }
            });
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCreate() {
        if (isValidData(nameField, CommonUtil::isValidName) && isValidData(descriptionArea, CommonUtil::isValidDescription)) {
            buttonCreate.setEnabled(false);

            runAsync(()-> {
                if (selectedIcon != null) try {
                    File out = FileUtil.writeTempImage(selectedFileName, selectedIcon);
                    return GroupAPI.createGroup(nameField.getText(), descriptionArea.getText(), out);
                } catch (Exception e) {
                    return false;
                }
                return GroupAPI.createGroup(nameField.getText(), descriptionArea.getText());
            }, success-> {
                if (success) {
                    dispose();
                } else {
                    errorLabel.setVisible(true);
                    pack();
                }
                buttonCreate.setEnabled(true);
            });
        }
    }

    private void onCancel() {
        dispose();
    }
}
