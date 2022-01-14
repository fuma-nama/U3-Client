package net.sonmoosans.u3.ui.panel;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.ui.AddablePanel;

import javax.swing.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.isValidPassword;
import static net.sonmoosans.u3.ui.util.CommonUtil.isValidData;

public class ResetPasswordPanel extends AddablePanel {
    private JPanel Main;
    private JPasswordField passwordField;
    private JPasswordField reEnterField;

    public boolean tryReset(String email, String code) {

        if (isValidData(reEnterField,
                password -> password.equals(passwordField.getText()) && isValidPassword(password))
        ) {
            return AccountAPI.resetPassword(email, code, passwordField.getText());
        } else return false;
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
