package net.sonmoosans.u3.ui.component;

import java.awt.*;

/**
 * Remove hover effect
 * Background color will be set by MessageItem using {@link ItemPane#setColorAuto(Container, Color)}
 * **/
public abstract class MessageItemPane extends ItemPane {
    @Override
    public void init(boolean isDark) {
        Color mainColor = getPanel().getBackground();

        this.defaultColor = isDark? mainColor.darker() : mainColor.brighter();

        setColor(defaultColor);
    }
}
