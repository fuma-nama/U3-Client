package net.sonmoosans.u3.ui.dialog;

import net.sonmoosans.u3.ui.panel.settings.AccountSettingsPanel;
import net.sonmoosans.u3.ui.panel.settings.SettingsPanel;
import net.sonmoosans.u3.ui.panel.settings.ThemeSettingsPanel;

import javax.swing.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTabbedPane tabbedPane;
    private JButton buttonClose;
    private final SettingsPanel[] panels = {new AccountSettingsPanel(), new ThemeSettingsPanel()};

    public SettingsDialog(JFrame owner) {
        super(owner);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonClose.addActionListener(e-> onCancel());

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

        tabbedPane.add("Account", get(panels[0]));
        tabbedPane.add("Theme", get(panels[1]));
    }

    private JPanel get(SettingsPanel panel) {
        panel.parent = this;
        return panel.getPanel();
    }

    private void onOK() {
        if (panels[tabbedPane.getSelectedIndex()].onSave())
            dispose();
    }

    private void onCancel() {
        dispose();
    }
}
