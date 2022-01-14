package net.sonmoosans.u3.ui.dialog.account;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.model.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.setClickable;

public class LoginDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JLabel forgetLabel;
    private JLabel signUpLabel;
    private String token;

    public LoginDialog(Frame owner) {
        super(owner);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setClickable(forgetLabel, e -> {
                    ResetPasswordDialog dialog = new ResetPasswordDialog(emailField.getText());
                    dialog.pack();
                    dialog.setVisible(true);
        });

        setClickable(signUpLabel, e -> {
            SignUpDialog dialog = new SignUpDialog();
            dialog.pack();
            dialog.setVisible(true);
            token = dialog.getToken();
            dispose();
        });

        buttonOK.addActionListener(e -> onOK());

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

    private void onOK() {
        buttonOK.setEnabled(false);
        Result<String> result = AccountAPI.login(emailField.getText(), passwordField.getText());
        token = result.context();
        if (result.isSuccess())  onCancel();
        else errorLabel.setText("Wrong email or password");
        pack();
        buttonOK.setEnabled(true);
    }

    private void onCancel() {
        dispose();
    }

    public String getToken() {
        return token;
    }
}
