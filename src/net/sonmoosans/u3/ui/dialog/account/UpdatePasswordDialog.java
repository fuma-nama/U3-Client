package net.sonmoosans.u3.ui.dialog.account;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class UpdatePasswordDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField oldField;
    private JPasswordField newField;

    public UpdatePasswordDialog(Window frame) {
        super(frame, "Update Password");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        if (isValidData(newField, CommonUtil::isValidPassword)) {
            verifyDataAsync(oldField, old->
                    AccountAPI.updatePassword(old, newField.getText()),
                    success-> {
                        if (success)
                            dispose();
                        else
                            openErrorDialog(this, "Failed to update password");
            });
        }
    }

    private void onCancel() {
        dispose();
    }
}
