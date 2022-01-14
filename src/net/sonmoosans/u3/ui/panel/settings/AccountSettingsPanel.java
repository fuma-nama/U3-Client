package net.sonmoosans.u3.ui.panel.settings;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.LoginEntry;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.dialog.account.UpdateEmailDialog;
import net.sonmoosans.u3.ui.dialog.account.UpdatePasswordDialog;
import net.sonmoosans.u3.ui.util.CommonUtil;
import net.sonmoosans.u3.util.FileUtil;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.File;

import static net.sonmoosans.u3.manager.Application.TOKEN;
import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class AccountSettingsPanel extends SettingsPanel {
    private JPanel Main;
    private JButton logOutButton;
    private JButton deleteButton;
    private JLabel avatarHolder;
    private JTextField nameField;
    private JButton resetAvatarButton;
    private JTextField emailField;
    private JButton editEmailButton;
    private JButton editPasswordButton;
    private JTextField passwordField;
    private BufferedImage selectedIcon;
    private String selectedFileName;
    private final UserProfile user = AccountAPI.getUser(Memory.getSelfUserID());

    public AccountSettingsPanel() {
        if (user == null) throw new NullPointerException("No user profile found");
        runAsync(AccountAPI::getLoginEntry, result -> {
            if (result.isSuccess()) {
                LoginEntry entry = result.context();
                emailField.setText(entry.email);

                passwordField.setText("*".repeat(entry.passwordLength));

                setDialogButton(editPasswordButton, ()->
                                CommonUtil.confirmDialog(editPasswordButton,
                                        "Warning",
                                        "Do you sure you want to change your password?"
                                ),
                        ()-> new UpdatePasswordDialog(parent)
                );
            } else {
                openErrorDialog(Main, "Failed to get user secret data");
            }
        });

        setDialogButton(editEmailButton, ()->
                        CommonUtil.confirmDialog(editEmailButton,
                                "Warning",
                                "Do you sure you want to change your email?"
                        ),
                ()-> new UpdateEmailDialog(parent)
        );

        setIconChooser(avatarHolder, Main, (image, file) -> {
            selectedIcon = image;
            selectedFileName = file.getName();
        });

        setIconAsync(avatarHolder, user.avatar);

        resetAvatarButton.addActionListener(e-> {
            setIconAsync(avatarHolder, user.avatar);
            selectedIcon = null;
            selectedFileName = null;
        });

        nameField.setText(user.name);

        logOutButton.addActionListener(e-> {
            if (confirmDialog("Hold Up", "Are you sure you want to log out?"))
                logOut();
        });

        deleteButton.addActionListener(e-> {
            if (confirmDialog("Last Waring", "Do you sure?\nAll data will be deleted\nThis can't be undone"))
                runAsync(AccountAPI::deleteAccount, success-> {
                    if (success) {
                        logOut();
                    } else {
                        JOptionPane.showMessageDialog(Main, "Failed to delete account", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
        });
    }

    private static void logOut() {
        Application.getPrefs().remove(TOKEN);
        Application.restart();
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public boolean onSave() {
        if (user != null && (selectedIcon != null || !nameField.getText().equals(user.name))) {

            if (confirmDialog("Wait a second", "Do you want to save changes?")) {
                    runAsync(()-> {
                        try {
                            if (selectedIcon != null && selectedFileName != null) {
                                File out = FileUtil.writeTempImage(selectedFileName, selectedIcon);
                                return AccountAPI.updateUser(nameField.getText(), out);
                            } else {
                                return AccountAPI.updateUser(nameField.getText());
                            }
                        } catch (Exception e) {
                            openErrorDialog(Main, e, "Failed to upload avatar");
                        }

                        return false;
                    }, success -> {
                        if (!success)
                            openErrorDialog(Main, "Failed to update profile");
                    });
            }
        }
        return true;
    }
}
