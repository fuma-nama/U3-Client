package net.sonmoosans.u3.ui.screen;

import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.EmojiAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Result;
import net.sonmoosans.u3.manager.Application;
import net.sonmoosans.u3.ui.dialog.account.LoginDialog;

import javax.swing.*;


import java.util.function.Consumer;

import static net.sonmoosans.u3.manager.Application.*;
import static net.sonmoosans.u3.ui.util.CommonUtil.runAsync;

public class LoadingScreen {
    private JPanel Main;
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private final JFrame frame;

    /**
     * Set up all client data
     * <br>For example: Token, Messages
     * <br> Then connect to socket server
     * **/
    public static void loadClient(Consumer<String> afterConnect) {
        JFrame frame = new JFrame("Loading");
        LoadingScreen screen = new LoadingScreen(frame);
        frame.setContentPane(screen.Main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        screen.start(afterConnect);
    }

    public LoadingScreen(JFrame frame) {
        this.frame = frame;
    }

    public void start(Consumer<String> afterConnect) {
        progressBar.setValue(0);

        loadingLabel.setText("Loading token");
        String token = Application.getPrefs().get(TOKEN, null);

        loadingLabel.setText("Loading Account");
        progressBar.setValue(25);

        if (token == null) {
            token = requireToken(frame);
            if (token == null) frame.dispose();
        }

        Application.getPrefs().put(TOKEN, token);

        progressBar.setValue(50);
        loadingLabel.setText("Connecting to server");

        connect(token, afterConnect);
    }

    private void connect(String token, Consumer<String> after) {
        GroupAPI.connectToServerAsync(token, success-> {
            if (success) {
                Application.getPrefs().put(TOKEN, token);

                int userID = AccountAPI.getUserID(token);
                Memory.storeSelfData(token, userID);

                progressBar.setValue(75);
                loadingLabel.setText("Loading client data");

                Result<int[]> result = EmojiAPI.getSavedEmojis();
                if (result.isSuccess())
                    Memory.storeSavedEmojis(result.context());

                SwingUtilities.invokeLater(()->{
                    frame.dispose();
                    after.accept(token);
                });
            } else {
                String newToken = requireToken(frame);
                if (newToken == null) frame.dispose();
                else connect(newToken, after);
            }
        });
    }

    private String requireToken(JFrame owner) {
        LoginDialog dialog = new LoginDialog(owner);
        dialog.pack();
        dialog.setLocationRelativeTo(Main);
        dialog.setVisible(true);
        return dialog.getToken();
    }
}
