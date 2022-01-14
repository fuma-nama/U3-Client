package net.sonmoosans.u3.manager;

import net.sonmoosans.u3.ui.screen.ChatScreen;

import javax.swing.*;
import java.util.prefs.Preferences;

public class Application {
    public static final String DEFAULT_NODE = "U3/data",
            TOKEN = "token",
            THEME = "theme",
            DEFAULT_THEME = "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlIJTheme";
    private static final Preferences fPrefs = Preferences.userRoot().node(DEFAULT_NODE);
    private static JFrame frame;
    private static ChatScreen screen;

    public static void setAppFrame(JFrame frame) {
        Application.frame = frame;
    }

    public static void setChatScreen(ChatScreen screen) {
        Application.screen = screen;
    }

    public static ChatScreen getScreen() {
        return screen;
    }

    public static JFrame getAppFrame() {
        return frame;
    }

    public static Preferences getPrefs() {
        return fPrefs;
    }

    public static void restart() {
        getAppFrame().dispose();

        ChatScreen.main(null);
    }
}
