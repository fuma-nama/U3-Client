package net.sonmoosans.u3.ui.dialog.emoji;

import net.sonmoosans.u3.api.EmojiAPI;
import net.sonmoosans.u3.api.model.Emoji;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.ui.component.EmojiItem;
import net.sonmoosans.u3.ui.layout.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Supplier;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class EmojiLibraryDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField searchField;
    private JButton searchButton;
    private JComboBox<Option> searchOption;
    private JPanel itemContainer;
    private JButton buttonUpload;
    private JScrollPane scrollPane;
    private Supplier<Result<Emoji[]>> fetchMethod;
    private int emojiCount = 0;
    private boolean canFetch = false;

    public EmojiLibraryDialog(JFrame frame) {
        super(frame, "Emoji Library");
        setPreferredSize(new Dimension(800, 600));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        for (Option option : Option.values()) {
            searchOption.addItem(option);
        }
        setDialogButton(buttonUpload, ()-> new UploadEmojiDialog(this));

        buttonOK.addActionListener(e -> onCancel());

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

        searchButton.addActionListener(e-> updateEmojis());

        itemContainer.setLayout(new WrapLayout(FlowLayout.LEFT));

        scrollPane.getVerticalScrollBar().addAdjustmentListener(e-> {
            if(!e.getValueIsAdjusting()) {
                JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
                int extent = scrollBar.getModel().getExtent();
                int maximum = scrollBar.getModel().getMaximum();
                if(extent + e.getValue() == maximum){
                    fetchMoreData();
                }
            }
        });

        updateEmojis();
    }

    protected void updateEmojis() {
        canFetch = false;
        emojiCount = 0;

        String search = searchField.getText();
        Option option = (Option) searchOption.getSelectedItem();

        if (search.isEmpty() || option == null)
            fetchMethod = ()-> EmojiAPI.getEmojis(emojiCount);
        else {
            fetchMethod = ()-> switch (option) {
                case ByName -> EmojiAPI.getEmojis(emojiCount, search);
                case ByCreator -> EmojiAPI.getEmojis(emojiCount, Integer.parseInt(search));
            };
        }

        itemContainer.removeAll();
        fetchEmojis();
    }

    protected void fetchMoreData() {
        if (canFetch && fetchMethod != null) {
            canFetch = false;
            fetchEmojis();
        }
    }

    protected void fetchEmojis() {
        runAsync(fetchMethod, emojis-> {
            if (emojis.isSuccess())
                for (Emoji emoji : emojis.context()) {
                    emojiCount++;
                    itemContainer.add(new EmojiItem(emoji).getPanel());
                }
            repaintContainer(itemContainer);
            canFetch = true;
        });
    }

    private void onCancel() {
        dispose();
    }

    public enum Option {
        ByCreator("Creator ID"), ByName("Emoji Name");

        public final String name;

        Option(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}
