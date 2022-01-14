package net.sonmoosans.u3.ui.dialog.account;

import net.sonmoosans.u3.api.AccountAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class UpdateEmailDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton verifyButton;
    private JTextField codeField;
    private JLabel errorLabel;

    public UpdateEmailDialog(Window parent) {
        super(parent, "Update Email");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        verifyButton.addActionListener(e-> {
            verifyButton.setEnabled(false);
            verifyButton.setText("Sending Email...");
            pack();

            verifyDataAsync(emailField, email-> isValidEmail(email) && AccountAPI.verifyEmail(email), valid-> {
                if (valid) {
                    codeField.setEnabled(true);
                    buttonOK.setEnabled(true);
                }
                verifyButton.setText("Send Again");
                verifyButton.setEnabled(true);
                pack();
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
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        buttonOK.setEnabled(false);
        verifyDataAsync(codeField,
                code->
                        AccountAPI.isTrueVerifyCode(emailField.getText(), code).context(),
                valid-> {
                    if (valid)
                        runAsync(()-> AccountAPI.updateEmail(emailField.getText(), passwordField.getText(), codeField.getText()), success-> {
                            if (success) {
                                JOptionPane.showMessageDialog(this, "You may need to wait a few minutes to see the changes");
                                dispose();
                            }
                            else {
                                errorLabel.setVisible(true);
                                pack();
                            }
                            buttonOK.setEnabled(true);
                        });
                    else buttonOK.setEnabled(true);
        });
    }

    private void onCancel() {
        dispose();
    }
}
