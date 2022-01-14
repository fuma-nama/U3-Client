package net.sonmoosans.u3.ui.util;

import javax.swing.*;
import java.awt.*;

public abstract class SizeAnimate {

    public static final int RUN_TIME = 500;

    private final JPanel panel;
    private final Rectangle from, to;

    private long startTime;

    public SizeAnimate(JPanel panel, Rectangle from, Rectangle to) {
        this.panel = panel;
        this.from = from;
        this.to = to;
    }

    public void start() {
        Timer timer = new Timer(10, e -> {
            long duration = System.currentTimeMillis() - startTime;
            double progress = (double) duration / (double) RUN_TIME;
            if (progress > 1f) {
                progress = 1f;
                after();
                ((Timer) e.getSource()).stop();
            }
            Rectangle target = calculateProgress(from, to, progress);
            panel.setBounds(target);
        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.setInitialDelay(0);
        startTime = System.currentTimeMillis();
        timer.start();
    }

    public abstract void after();

    private static Rectangle calculateProgress(Rectangle startBounds, Rectangle targetBounds, double progress) {

        Rectangle bounds = new Rectangle();

        if (startBounds != null && targetBounds != null) {

            bounds.setLocation(calculateProgress(startBounds.getLocation(), targetBounds.getLocation(), progress));
            bounds.setSize(calculateProgress(startBounds.getSize(), targetBounds.getSize(), progress));

        }

        return bounds;
    }

    private static Point calculateProgress(Point startPoint, Point targetPoint, double progress) {

        Point point = new Point();

        if (startPoint != null && targetPoint != null) {

            point.x = calculateProgress(startPoint.x, targetPoint.x, progress);
            point.y = calculateProgress(startPoint.y, targetPoint.y, progress);

        }

        return point;
    }

    private static int calculateProgress(int startValue, int endValue, double fraction) {

        int distance = endValue - startValue;
        int value = (int)Math.round((double)distance * fraction);
        value += startValue;

        return value;
    }

    private static Dimension calculateProgress(Dimension startSize, Dimension targetSize, double progress) {

        Dimension size = new Dimension();

        if (startSize != null && targetSize != null) {

            size.width = calculateProgress(startSize.width, targetSize.width, progress);
            size.height = calculateProgress(startSize.height, targetSize.height, progress);

        }

        return size;
    }
}
