package net.sonmoosans.u3.ui.dialog.group;

import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.component.GroupItem;
import net.sonmoosans.u3.ui.screen.ChatScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Collections;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class InviteCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JPanel groupContainer;
    private JTextField codeField;
    private JButton copyButton;
    private JButton updateButton;
    private JScrollPane scrollPane;

    public InviteCodeDialog(Window window, int groupID) {
        super(window, "Invite Code");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        runAsync(()-> GroupAPI.getInviteCode(groupID), result-> {
            if (result.isSuccess())
                codeField.setText(result.context());
        });

        buttonOK.addActionListener(e -> onOK());

        copyButton.addActionListener(e-> {
            copyText(codeField.getText());
            copyButton.setText("Copied!");
            waitAsync(1000, ()-> copyButton.setText("Copy"));
        });

        updateButton.addActionListener(e-> {
            runAsync(()-> GroupAPI.updateInviteCode(groupID), result-> {
                if (result.isSuccess())
                    codeField.setText(result.context());
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
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        groupContainer.setLayout(new BoxLayout(groupContainer, BoxLayout.Y_AXIS));

        for (GroupItem joinedGroup : Application.getScreen().getJoinedGroups().values()) {

            groupContainer.add(new GroupShareItem(joinedGroup.group, codeField.getText()).getPanel());

            pack();
        }
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private static class GroupShareItem extends GroupItem {
        public GroupShareItem(Group group, String code) {
            super(group, "Send");
            buttonTag.addActionListener(e-> {
                runAsync(()->GroupAPI.sendMessage(group.id,
                        "Join my group now! " + code,
                        Collections.emptyList(),
                        false
                ), success-> {
                    if (success) {
                        buttonTag.setText("Sent!");
                        buttonTag.setEnabled(false);
                    } else {
                        buttonTag.setText("Failed");
                        buttonTag.setBackground(Color.red);
                    }
                });
            });
        }
    }
}
