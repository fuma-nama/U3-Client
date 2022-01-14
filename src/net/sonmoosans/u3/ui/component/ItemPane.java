package net.sonmoosans.u3.ui.component;

import com.formdev.flatlaf.FlatLaf;
import net.sonmoosans.u3.ui.AddablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public abstract class ItemPane extends AddablePanel implements MouseListener {
    public Color defaultColor, hoveredColor;

    /**
     * @throws NullPointerException If method called before init
     * **/
    public void init() {
        init(UIManager.getLookAndFeel() instanceof FlatLaf flatLaf && flatLaf.isDark());

    }

    public void updateSize() {
        JPanel panel = getPanel();

        panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, panel.getPreferredSize().height));
    }

    public void init(boolean isDark) {
        Color mainColor = getPanel().getBackground();

        if (isDark) {
            this.defaultColor = mainColor.darker();
            hoveredColor = defaultColor.brighter();
        } else {
            this.defaultColor = mainColor.brighter();
            hoveredColor = defaultColor.darker();
        }

        getPanel().addMouseListener(this);
        setColor(defaultColor);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (getPanel().getBackground() == defaultColor)
            setColor(hoveredColor);
    }

    public void mouseExited(MouseEvent e) {
        if (getPanel().getBackground() == hoveredColor)
            setColor(defaultColor);
    }

    public void setColor(Color color) {
        getPanel().setBackground(color);
    }

    protected void setColorAuto(Container container, Color color) {
        for (Component component : container.getComponents()) {

            if (component instanceof Container childContainer) {
                component.setBackground(color.brighter());

                setColorAuto(childContainer, color);
            }
        }
    }
}
