package net.sonmoosans.u3.ui.dialog.account;

import net.sonmoosans.u3.ui.panel.SignUpPanel;
import net.sonmoosans.u3.ui.util.SizeAnimate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SignUpDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonNext;
    private JButton buttonCancel;
    private JPanel panelContainer;
    private JPanel currentPanel;
    private final SignUpPanel signUpPanel = new SignUpPanel();
    private String token;

    public SignUpDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonNext);

        buttonNext.addActionListener(e -> next());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        currentPanel = signUpPanel.getPanel();
        panelContainer.add(currentPanel);
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void next() {
        boolean verified = signUpPanel.isVerified();

        buttonNext.setEnabled(false);
        if (!verified && signUpPanel.tryVerifyEmail()) {
            switchPanel(signUpPanel.getVerifyPanel());
        } else if (verified && signUpPanel.trySignUp()) {
            onCancel();
        }
        buttonNext.setEnabled(true);
    }

    private void switchPanel(JPanel newPanel) {
        Rectangle from = currentPanel.getBounds();
        Rectangle to = new Rectangle(from.x, from.y, 0, from.height);

        SizeAnimate animate = new SizeAnimate(currentPanel, from, to) {
            public void after() {
                panelContainer.removeAll();
                panelContainer.add(newPanel);
                currentPanel = newPanel;
                pack();
                repaint();
            }
        };
        animate.start();
    }

    private void onCancel() {
        token = signUpPanel.getToken();
        dispose();
    }

    public String getToken() {
        return token;
    }
}
