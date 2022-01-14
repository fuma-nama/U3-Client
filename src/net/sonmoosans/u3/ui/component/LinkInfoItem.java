package net.sonmoosans.u3.ui.component;

import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.util.model.LinkInfo;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

import java.awt.*;
import java.net.URL;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;
import static net.sonmoosans.u3.ui.util.MessageUtil.markdownToHtml;

public class LinkInfoItem extends MessageItemPane {
    private JPanel Main;
    private JLabel titleLabel;
    private JLabel imageHolder;
    private JPanel contextPane;
    protected JEditorPane descriptionPane;

    public LinkInfoItem(LinkInfo link) {
        titleLabel.setText(link.title());

        if (link.description() != null) {
            descriptionPane.setText(markdownToHtml(link.description()));

            descriptionPane.addHyperlinkListener(e-> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
                        e.getURL() != null &&
                        confirmDialog(Application.getAppFrame(), "Hold Up!", "Do you sure you want to open this link?")) {
                    openWebpage(e.getURL());
                }
            });
        }
        else
            descriptionPane.setVisible(false);

        if (link.imageUrl() != null) {
            runAsync(()-> getImage(link.imageUrl()), image -> {
                if (image != null)
                    setIcon(imageHolder, image, getScaledDimension(image, 300, 300, imageHolder));
                else
                    imageHolder.setVisible(false);

                updateSize();
            });
        } else
            imageHolder.setVisible(false);

        if (link.url() != null) {
            setClickable(titleLabel, e-> {
                try {
                    openWebpage(new URL(link.url()));
                } catch (Exception ex) {
                    openErrorDialog(titleLabel, ex, "Failed to open link");
                }
            }, false);
        }

        updateSize();
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        contextPane.setBackground(color);
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }
}
