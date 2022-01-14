package net.sonmoosans.u3.ui.popup;

import main.Backend;
import main.Emoji;
import main.EmojiDataLoader;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.ui.layout.WrapLayout;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.util.List;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;
import static net.sonmoosans.u3.ui.util.EmojiUtil.getEmojiUrl;

public class EmojiPopup extends JPopupMenu {
    private static Backend backend;
    private JPanel Main;
    private JTabbedPane tabbedPane;
    private final JTextComponent field;

    public EmojiPopup(JTextComponent field) {
        this.field = field;

        setFont(getFont().deriveFont(22f));

        JPanel container = addTab("Saved Emojis");

        for (int emojiID : Memory.getSavedEmojis()) {
            runAsync(()-> getImage(getEmojiUrl(emojiID)), image-> {
                JButton button = new JButton(new ImageIcon(image));

                button.addActionListener(e-> {
                    field.setText(field.getText() + "{:" + emojiID + ":}");
                });

                container.add(button);

                repaintContainer(container);
            });
        }

        loadDefaultEmojis();

        this.add(Main);
    }

    private void loadDefaultEmojis() {
        runAsync(()-> {
            if (backend == null) {
                backend = new Backend();
                EmojiDataLoader dataLoader = new EmojiDataLoader();

                try {
                    dataLoader.loadEmojiData(backend);
                } catch (Exception e) {
                    //Ignored
                }
            }

            return backend;
        }, backend-> {
            for (String group : backend.getEmojiGroupIDs()) {

                List<Emoji> emojis = backend.getEmojiGroup(group);
                if (!emojis.isEmpty()) {
                    JPanel container = addTab(group);

                    for (Emoji emoji : emojis) {
                        if (getFont().canDisplayUpTo(emoji.qualifiedSequence) < 0) {
                            JButton button = new JButton(emoji.qualifiedSequence);
                            button.setFont(getFont());
                            button.addActionListener(e-> {
                                field.setText(field.getText() + emoji.qualifiedSequence);
                            });
                            container.add(button);
                        }
                    }
                }
            }
        });
    }

    private JPanel addTab(String title) {
        JPanel container = new JPanel(new WrapLayout(FlowLayout.LEFT));

        JScrollPane scrollPane = new JScrollPane(container);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        tabbedPane.add(title, scrollPane);

        return container;
    }
}
