package net.sonmoosans.u3.ui.popup;

import javax.swing.*;
import java.awt.*;

public class CJPopupMenu extends JPopupMenu {
    private Frame frame;

    public Frame getFrame() {
        return frame;
    }

    public void show(Component invoker, int x, int y) {
        setInvoker(invoker);
        Frame newFrame = getFrame(invoker);
        if (newFrame != frame) {
            // Use the invoker's frame so that events
            // are propagated properly
            if (newFrame != null) {
                this.frame = newFrame;
            }
        }
        Point invokerOrigin;
        if (invoker != null) {
            invokerOrigin = invoker.getLocationOnScreen();

            // To avoid integer overflow
            long lx, ly;
            lx = ((long) invokerOrigin.x) +
                    ((long) x);
            ly = ((long) invokerOrigin.y) +
                    ((long) y);
            if (lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
            if (lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
            if (ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
            if (ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;

            setLocation((int) lx, (int) ly);
        } else {
            setLocation(x, y);
        }
        setVisible(true);
    }

    private static Frame getFrame(Component c) {
        Component w = c;

        while (!(w instanceof Frame) && (w != null)) {
            w = w.getParent();
        }
        return (Frame) w;
    }
}
