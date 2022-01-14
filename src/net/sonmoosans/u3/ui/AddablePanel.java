package net.sonmoosans.u3.ui;

import net.sonmoosans.u3.ui.util.CommonUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public abstract class AddablePanel extends AddableComponent {
    public abstract JPanel getPanel();

    protected boolean confirmDialog(String title, String message) {
        int reply = JOptionPane.showConfirmDialog(
                getPanel(),
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
         return reply == JOptionPane.YES_OPTION;
    }

    public static boolean confirmDialog(Component parent, String title, String message) {
        return CommonUtil.confirmDialog(parent, title, message);
    }

    public void addTo(Container container) {
        if (!(container.getLayout() instanceof BorderLayout)) {
            container.setLayout(new BorderLayout());
        }
        if (container.getComponentCount() > 0)
            container.removeAll();

        container.add(getPanel(), BorderLayout.CENTER);
    }

    public void addTo(String title, JTabbedPane pane) {
        pane.add(title, getPanel());
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }
}
