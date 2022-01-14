package net.sonmoosans.u3.ui.panel;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.util.CommonUtil;
import net.sonmoosans.u3.util.FileUtil;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.File;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class SignUpPanel extends AddablePanel {
    private JPanel MaIn;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel iconHolder;
    private JButton resetButton;
    private VerifyEmailPanel verifyPanel;
    private String token;
    private BufferedImage selectedIcon;
    private String selectedFileName;

    public SignUpPanel() {
        resetButton.addActionListener(e-> {
            setBlankIcon(iconHolder);
            selectedFileName = null;
            selectedIcon = null;
        });

        setIconChooser(iconHolder, MaIn, ((image, file) -> {
            selectedIcon = image;
            selectedFileName = file.getName();
        }));
    }

    public JPanel getVerifyPanel() {
        if (verifyPanel == null)
            verifyPanel = new VerifyEmailPanel(emailField.getText());
        return verifyPanel.getPanel();
    }

    public boolean trySignUp() {
        if (verifyPanel.tryVerify()) {
            Result<String> result;

            if (selectedIcon != null && selectedFileName != null) try {
                File out = FileUtil.writeTempImage(selectedFileName, selectedIcon);

                result = AccountAPI.signUp(emailField.getText(),
                        nameField.getText(),
                        passwordField.getText(),
                        out,
                        verifyPanel.getCode()
                );
            } catch (Exception e) {
                openErrorDialog(MaIn, e, "Failed to upload avatar image");
                return false;
            } else {
                result = AccountAPI.signUp(emailField.getText(),
                        nameField.getText(),
                        passwordField.getText(),
                        verifyPanel.getCode()
                );
            }

            token = result.context();
            return result.isSuccess();
        }
        return false;
    }

    public boolean tryVerifyEmail() {
        return isValidData(nameField, CommonUtil::isValidName) &&
                isValidData(emailField, CommonUtil::isValidEmail) &&
                isValidData(passwordField, CommonUtil::isValidPassword) &&
                isValidData(emailField, AccountAPI::verifyEmail);
    }

    public boolean isVerified() {
        return verifyPanel != null;
    }

    public String getToken() {
        return token;
    }

    @Override
    public JPanel getPanel() {
        return MaIn;
    }
}
