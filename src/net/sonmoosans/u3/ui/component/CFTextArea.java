package net.sonmoosans.u3.ui.component;

import net.sonmoosans.cfte.tool.action.GetSimilarWord;
import net.sonmoosans.cfte.tool.action.SearchWord;
import net.sonmoosans.cfte.tool.checker.SpellChecker;
import net.sonmoosans.cfte.ui.dialog.SearchReplaceDialog;
import net.sonmoosans.cfte.ui.dialog.TranslateDialog;
import net.sonmoosans.cfte.ui.pane.WordActionTextArea;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.screen.ChatScreen;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CFTextArea extends WordActionTextArea {

    public CFTextArea() {
        addSuggester("Dictionary", new GetSimilarWord(), new SearchWord());
        setTextChecker(new SpellChecker());
    }

    @Override
    protected void buildMenu(JPopupMenu menu) {
        super.buildMenu(menu);
        CFTextArea editor = this;

        menu.add(new AbstractAction("Search Word") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getSelectedText() == null) selectCurrentWord();
                SearchReplaceDialog dialog = new SearchReplaceDialog(Application.getAppFrame(), editor);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        menu.add(new AbstractAction("Translate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getSelectedText() == null) selectCurrentWord();
                TranslateDialog dialog = new TranslateDialog(Application.getAppFrame(), editor);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }
}
