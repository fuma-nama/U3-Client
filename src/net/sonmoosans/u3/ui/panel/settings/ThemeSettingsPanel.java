package net.sonmoosans.u3.ui.panel.settings;

import com.formdev.flatlaf.IntelliJTheme;
import net.sonmoosans.u3.manager.Application;
import org.reflections.Reflections;

import javax.swing.*;

import java.util.Objects;

import static net.sonmoosans.u3.manager.Application.DEFAULT_THEME;
import static net.sonmoosans.u3.manager.Application.THEME;

public class ThemeSettingsPanel extends SettingsPanel {
    private JPanel Main;
    private JList<ThemeClass> themeList;
    private JButton resetButton;
    private final String currentTheme = Application.getPrefs().get(THEME, DEFAULT_THEME);

    public ThemeSettingsPanel() {
        Reflections reflections = new Reflections("com.formdev.flatlaf");

        DefaultListModel<ThemeClass> model = new DefaultListModel<>();

        model.addAll(
                reflections.
                        getSubTypesOf(IntelliJTheme.ThemeLaf.class)
                        .stream()
                        .map(ThemeClass::new)
                        .toList()
        );

        themeList.setModel(model);

        try {
            themeList.setSelectedValue(new ThemeClass(Class.forName(currentTheme)), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        resetButton.addActionListener(e-> themeList.clearSelection());
    }

    private void updateTheme(String name) {
        int reply = JOptionPane.showConfirmDialog(Main, "Could you want to restart the application?");

        if (reply == JOptionPane.YES_OPTION) {
            if (name == null)
                Application.getPrefs().remove(THEME);
            else
                Application.getPrefs().put(THEME, name);

            Application.restart();
        }
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public boolean onSave() {
        ThemeClass selected = themeList.getSelectedValue();

        String name = null;

        if (selected != null)
            name = selected.themeClass.getName();

        if (Objects.equals(name, currentTheme)) return true;

        updateTheme(name);

        return false;
    }

    public static record ThemeClass(Class<?> themeClass) {
        public String toString() {
            return themeClass.getSimpleName();
        }
    }
}
