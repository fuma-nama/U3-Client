package net.sonmoosans.u3.ui.panel;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.ui.AddablePanel;

import javax.swing.*;
import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class VerifyEmailPanel extends AddablePanel {
    protected final String email;
    private JPanel Main;
    protected JTextField codeField;

    /**@param email The email to verify, the code should be sent to email before verify**/
    public VerifyEmailPanel(String email) {
        this.email = email;
    }

    public boolean tryVerify() {
        Result<Boolean> result = AccountAPI.isTrueVerifyCode(email, codeField.getText());

        return isValidData(codeField, code-> result != null && result.context());
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    public String getCode() {
        return codeField.getText();
    }
}
