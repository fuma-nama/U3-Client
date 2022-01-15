package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.TextMessage;
import net.sonmoosans.u3.api.model.UserProfile;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.layout.WrapLayout;
import net.sonmoosans.u3.ui.popup.MessageOptionPopup;
import net.sonmoosans.u3.ui.popup.UserDetailPopup;
import net.sonmoosans.u3.ui.util.MessageUtil;
import net.sonmoosans.u3.ui.util.model.LinkInfo;
import org.opengraph.OpenGraph;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Iterator;


import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public abstract class MessageItem extends ItemPane {
    private JPanel Main;
    private JLabel iconHolder;
    private JPanel contextPane;
    private JLabel nameLabel;
    private JLabel dateLabel;
    private JPanel userPane;
    private JPanel bottomBar;
    private JButton moreButton;
    private JButton editButton;
    private JTextArea editArea;
    private JLabel editedLabel;
    private JScrollPane editPane;
    private JPanel replyContainer;
    protected JLabel replyTextLabel;
    private JLabel replyNameLabel;
    private JButton htmlTag;
    private JToolBar optionBar;
    private JLabel newMessageLabel;
    private JEditorPane textPane;
    private final boolean canModifyMessage;
    private final TextMessage message;
    private boolean editing = false;

    public MessageItem(TextMessage message) {
        this.message = message;

        StyleSheet sheet =  ((HTMLEditorKit)textPane.getEditorKit()).getStyleSheet();

        sheet.addRule("p { " +
                "margin: 0 !important;" +
                "}");

        sheet.addRule("a { " +
                "margin: 0 !important;" +
                "}");

        bottomBar.setLayout(new WrapLayout(FlowLayout.LEFT, 8, 8));

        DefaultCaret caret = (DefaultCaret)textPane.getCaret();

        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        setContext(message.context);

        dateLabel.setText(" - " + MessageUtil.getTimeString(message.date));

        editedLabel.setVisible(message.edited);

        htmlTag.setVisible(message.isHTML);

        UserProfile user = AccountAPI.getUserSafe(message.senderID);
        nameLabel.setText(user.name);
        setIconAsync(iconHolder, user.avatar);

        iconHolder.setComponentPopupMenu(new UserDetailPopup(user));

        canModifyMessage = message.senderID == Memory.getSelfUserID();

        if (canModifyMessage) {
            editButton.setVisible(true);
        }

        moreButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                contextPane.getComponentPopupMenu().show(moreButton, e.getX(), e.getY());
            }
        });

        editButton.addActionListener(e-> onEditMessage());

        editArea.registerKeyboardAction(e -> onStopEdit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        editArea.registerKeyboardAction(e -> onFinishEdit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        textPane.addHyperlinkListener(e-> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
                    e.getURL() != null &&
                    confirmDialog(Application.getAppFrame(), "Hold Up!", "Do you sure you want to open this link?")) {
                openWebpage(e.getURL());
            }
        });

        if (message.replyTo != null) {
            replyContainer.setVisible(true);

            replyNameLabel.setText(AccountAPI.getUserSafe(message.replyTo.senderID).name + ": ");

            setReplyText(message.replyTo.context);

            GroupAPI.getSocket().on(GroupAPI.MESSAGE_UPDATED_EVENT, args -> {
                int messageID = (int) args[1];
                String newContext = (String) args[2];

                message.replyTo.context = newContext;
                if (messageID == message.replyTo.ID)
                    setReplyText(newContext);

                setClickable(replyTextLabel, e-> jumpToMessage(message.replyTo.ID), false);
            });


            replyTextLabel.setForeground(Color.GRAY);
        }

        Main.setComponentPopupMenu(new MessageOptionPopupImpl());

        textPane.addMouseListener(this);

        init();

        updateSize();
    }

    protected void updateFiles() {
        for (String file : message.file) {
            try {
                bottomBar.add(new FileItem(file).getPanel());
            } catch (IOException ignored) {

            }
        }
    }

    private void updateBottomItems() {
        bottomBar.removeAll();

        updateFiles();

        HTMLDocument.Iterator iterator = ((HTMLDocument)textPane.getDocument())
                .getIterator(HTML.Tag.A);

        while (iterator.isValid()) {
            String href = (String) iterator.getAttributes().getAttribute(HTML.Attribute.HREF);

                runAsync(()-> {
                    try {
                        OpenGraph info = new OpenGraph(href, true);

                        return new LinkInfo(info.getContent("title"),
                                info.getContent("description"),
                                info.getContent("image"),
                                href);
                    } catch (Exception e) {
                        return null; //Website doesn't update open graph protocol
                    }
                }, link-> {
                    if (link != null && link.title() != null) {
                        bottomBar.add(new ExtendedLinkInfoItem(link).getPanel());
                    }
                });

            iterator.next();
        }

    }

    public void setNewMessageVisible(boolean visible) {
        newMessageLabel.setVisible(visible);
    }

    private void setReplyText(String s) {
        replyTextLabel.setText(s.replace('\n', ' '));
    }

    protected abstract void jumpToMessage(int messageID);

    @Override
    public JPanel getPanel() {
        return Main;
    }

    public abstract void onSetReply();

    public TextMessage getMessage() {
        return message;
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        contextPane.setBackground(color);
        userPane.setBackground(color);
        bottomBar.setBackground(color);
        replyContainer.setBackground(color);
        optionBar.setBackground(color);

        setColorAuto(bottomBar, color.brighter());
    }

    public void onEditMessage() {
        editing = !editing;

        if (editing) {
            editArea.setText(message.context);
            editPane.setVisible(true);
            textPane.setVisible(false);
        } else onStopEdit();
    }

    public void onFinishEdit() {
        editPane.setVisible(false);

        runAsync(()-> GroupAPI.editMessage(message.ID, editArea.getText()), success-> {
            if (success)
                onStopEdit();
            else {
                openErrorDialog(Main, "Failed to edit message");
                editPane.setVisible(true);
            }
        });
    }

    public void onStopEdit() {
        editPane.setVisible(false);
        textPane.setVisible(true);
    }

    public void updateContext(String newContext) {
        message.context = newContext;
        message.edited = true;
        editedLabel.setVisible(true);
        setContext(newContext);

        updateSize();
    }

    @Override
    public void updateSize() {
        repaintContainer(Main);

        Main.setPreferredSize(new Dimension(Main.getMaximumSize().width, Main.getMinimumSize().height + textPane.getPreferredSize().height));

        super.updateSize();
    }

    private void setContext(String text) {
        if (message.isHTML)
            textPane.setText(MessageUtil.addHtmlTag(text));
        else
            textPane.setText(MessageUtil.markdownToHtml(text));

        updateBottomItems();
    }

    private class ExtendedLinkInfoItem extends LinkInfoItem {

        public ExtendedLinkInfoItem(LinkInfo link) {
            super(link);
            descriptionPane.addMouseListener(MessageItem.this);
        }

        @Override
        public void updateSize() {
            super.updateSize();
            MessageItem.this.updateSize();
        }
    }

    private class MessageOptionPopupImpl extends MessageOptionPopup {

        public MessageOptionPopupImpl() {
            super(canModifyMessage);
        }

        @Override
        public void onEdit(ActionEvent e) {
            onEditMessage();
            this.setVisible(false);
        }

        @Override
        public void onDelete(ActionEvent e) {
            deleteButton.setEnabled(false);
            runAsync(()-> GroupAPI.deleteMessage(message.ID), success-> {
                if (success)
                    this.setVisible(false);
            });
        }

        @Override
        public void onCopy(ActionEvent e) {
            copyText(message.context);
            this.setVisible(false);
        }

        @Override
        public void onReply(ActionEvent e) {
            onSetReply();
            this.setVisible(false);
        }
    }
}
