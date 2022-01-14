package net.sonmoosans.u3.ui.panel.settings;

import net.sonmoosans.u3.ui.AddablePanel;

import java.awt.*;

public abstract class SettingsPanel extends AddablePanel {
    public Window parent;
    /**@return Can close settings**/
    public abstract boolean onSave();
}