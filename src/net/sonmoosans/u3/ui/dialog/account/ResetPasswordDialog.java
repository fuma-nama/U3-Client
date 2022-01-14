package net.sonmoosans.u3.ui.dialog.account;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.ui.panel.ResetPasswordPanel;
import net.sonmoosans.u3.ui.panel.VerifyEmailPanel;
import net.sonmoosans.u3.ui.util.SizeAnimate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public class ResetPasswordDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel panelContainer;
    private boolean isVerifying = true;
    private final VerifyEmailPanel verifyPanel;
    private final ResetPasswordPanel resetPanel = new ResetPasswordPanel();
    private final String email;

    public ResetPasswordDialog(String email) {
        this.email = email;
        verifyPanel = new VerifyEmailPanel(email);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        panelContainer.add(verifyPanel.getPanel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        runAsync(()-> AccountAPI.sendVerifyCode(email));
    }

    private void onOK() {
        if (isVerifying && verifyPanel.tryVerify()) {
            nextPanel(resetPanel.getPanel());
            isVerifying = false;
        } else if (!isVerifying && resetPanel.tryReset(email, verifyPanel.getCode())) {
            onCancel();
        }
    }

    private void nextPanel(JPanel newPanel) {
        if (!isVerifying) return;
        JPanel currentPanel = verifyPanel.getPanel();
        Rectangle from = currentPanel.getBounds();
        Rectangle to = new Rectangle(from.x, from.y, 0, from.height);

        SizeAnimate animate = new SizeAnimate(currentPanel, from, to) {
            public void after() {
                panelContainer.removeAll();
                panelContainer.add(newPanel);
                pack();
                repaint();
            }
        };
        animate.start();
    }

    private void onCancel() {
        dispose();
    }
}
