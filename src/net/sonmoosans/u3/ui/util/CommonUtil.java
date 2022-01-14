package net.sonmoosans.u3.ui.util;

import com.formdev.flatlaf.ui.FlatBorder;
import com.github.rjeschke.txtmark.Processor;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.manager.Application;
import org.apache.commons.validator.routines.EmailValidator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class CommonUtil {
    private static final Image blankIcon = new BufferedImage(50, 50, TYPE_INT_RGB);

    public static boolean isValidData(JTextComponent field, Function<String, Boolean> verifyFunction) {
        if (verifyFunction.apply(field.getText())) {
            field.setBorder(new FlatBorder());
            return true;
        } else {
            field.setBorder(new RedFlatBorder());
            return false;
        }
    }

    public static void verifyDataAsync(JTextComponent field, Function<String, Boolean> verifyFunction, Consumer<Boolean> after) {
        runAsync(()-> verifyFunction.apply(field.getText()), valid-> {
            if (valid) {
                field.setBorder(new FlatBorder());
            } else {
                field.setBorder(new RedFlatBorder());
            }
            after.accept(valid);
        });

    }

    public static void runAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static <T> void runAsync(Supplier<T> supplier, Consumer<T> consumer) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return supplier.get();
            }

            @Override
            protected void done() {
                try {
                    consumer.accept(get());
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public static void waitAsync(long milli, Runnable runnable) {
        runAsync(()-> {
            try {
                Thread.sleep(milli);
            } catch (Exception ex) {
                return 1;
            }
            return 0;
        }, result-> {
            if (result == 0) runnable.run();
        });
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static void setClickable(JLabel label, Consumer<MouseEvent> listener) {
        setClickable(label, listener, true);
    }
    public static void setClickable(JLabel label, Consumer<MouseEvent> listener, boolean updateFont) {
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (updateFont) {
            label.setForeground(Color.CYAN);
            Font font = label.getFont();
            Map attributes = font.getAttributes();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            label.setFont(font.deriveFont(attributes));
        }

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listener.accept(e);
            }
        });
    }

    public static void setIconAsync(JLabel iconHolder, String url) {
        setBlankIcon(iconHolder);
        runAsync(()-> getImage(url), image ->
                setIcon(iconHolder, image)
        );
    }

    public static void setIconAsync(JLabel iconHolder, String url, int length) {
        setBlankIcon(iconHolder, new Dimension(length, length));
        runAsync(()-> getImage(url), image ->
                setIcon(iconHolder, image, length)
        );
    }

    public static Image getImage(String url) {
        Image image = null;
        try {
            if (url != null && !url.isEmpty()) {
                image = Memory.getImage(url);
                if (image == null)
                    image = Memory.saveImage(url, ImageIO.read(new URL(url)));
            }
        } catch (Exception ignored) {}
        return image;
    }

    public static BufferedImage getImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (Exception ignored) {}
        return image;
    }

    public static BufferedImage setIcon(JLabel iconHolder, BufferedImage image) {
        if (image != null) {
            image = resize(image, 50 ,50);
            iconHolder.setIcon(new ImageIcon(image));
        } else setBlankIcon(iconHolder);

        return image;
    }


    public static Image setIcon(JLabel iconHolder, Image image) {
        return setIcon(iconHolder, image, 50);
    }

    public static Image setIcon(JLabel iconHolder, Image image, int length) {
        return setIcon(iconHolder, image, new Dimension(length, length));
    }

    public static Image setIcon(JLabel iconHolder, Image image, Dimension dimension) {
        if (image != null){
            image = image.getScaledInstance(dimension.width, dimension.height, Image.SCALE_DEFAULT);
            iconHolder.setIcon(new ImageIcon(image));
        } else setBlankIcon(iconHolder, dimension);

        return image;
    }

    public static void setBlankIcon(JLabel iconHolder) {
        iconHolder.setIcon(new ImageIcon(blankIcon));
    }

    public static void setBlankIcon(JLabel iconHolder, Dimension dimension) {
        Image image = blankIcon;
        image = image.getScaledInstance(dimension.width, dimension.height, Image.SCALE_DEFAULT);
        iconHolder.setIcon(new ImageIcon(image));
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static File[] chooseFiles(Container parent, FileType type) {
        JFileChooser chooser = getChooser(type);

        chooser.setMultiSelectionEnabled(true);

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFiles();
        } else return null;
    }

    public static File chooseFile(Container parent, FileType type) {
        JFileChooser chooser = getChooser(type);

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else return null;
    }

    private static JFileChooser getChooser(FileType type) {
        JFileChooser chooser = new JFileChooser();

        if (type == FileType.IMAGE) {
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Only", ImageIO.getReaderFileSuffixes()));
            chooser.setAcceptAllFileFilterUsed(false);
        }

        return chooser;
    }

    public static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    public static boolean isImage(Path path) throws IOException {
        String mimetype= Files.probeContentType(path);
        String type = mimetype.split("/")[0];
        return type.equals("image");
    }

    public static void openWebpage(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
                openErrorDialog(Application.getAppFrame(), "Unable to open URL");
            }
        }
    }

    public static Dimension getScaledDimension(Image image, int boundW, int boundH, ImageObserver observer) {
        int new_width = image.getWidth(observer), imageW = new_width;
        int new_height = image.getHeight(observer), imageH = new_height;

        // first check if we need to scale width
        if (imageW > boundW) {
            //scale width to fit
            new_width = boundW;
            //scale height to maintain aspect ratio
            new_height = (new_width * imageH) / imageW;
        }

        // then check if we need to scale even with the new height
        if (new_height > boundH) {
            //scale height to fit instead
            new_height = boundH;
            //scale width to maintain aspect ratio
            new_width = (new_height * imageW) / imageH;
        }

        return new Dimension(new_width, new_height);
    }

    public static boolean confirmDialog(Component parent, String title, String message) {
        int reply = JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );
        return reply == JOptionPane.YES_OPTION;
    }

    public static void openErrorDialog(Component parent, String message) {
        openErrorDialog(parent, message, "Error");
    }

    public static void openErrorDialog(Component parent, Exception e, String title) {
        openErrorDialog(parent, e.getMessage(), title);
    }

    public static void openErrorDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void copyText(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    public static void setIconChooser(JLabel iconHolder, Container parent, ChooseListener listener) {
        iconHolder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File file = chooseFile(parent, FileType.IMAGE);
                if (file != null) {
                    runAsync(()-> getImage(file), image ->
                            listener.accept(setIcon(iconHolder, image), file));
                }
            }
        });
        setBlankIcon(iconHolder);
    }

    public static void setDialogButton(JButton button, Supplier<JDialog> supplier) {
        button.addActionListener(e-> {
            JDialog dialog = supplier.get();
            dialog.pack();
            dialog.setLocationRelativeTo(button);
            dialog.setVisible(true);
        });
    }

    public static void setDialogButton(JButton button, Supplier<Boolean> listener, Supplier<JDialog> supplier) {
        button.addActionListener(e-> {
            if (listener.get()) {
                JDialog dialog = supplier.get();
                dialog.pack();
                dialog.setLocationRelativeTo(button);
                dialog.setVisible(true);
            }
        });
    }

    public static void setPopupButton(JButton button, Supplier<JPopupMenu> supplier) {
        button.addActionListener(e-> {
            JPopupMenu popup = supplier.get();
            popup.show(button, 0, 0);
        });
    }

    public static void repaintContainer(Container container) {
        container.revalidate();
        container.repaint();
    }

    /**Also will be checked on server-side**/
    public static boolean isValidName(String name) {
        return !name.isBlank() && name.length() <= 50;
    }

    /**Also will be checked on server-side**/
    public static boolean isValidContext(String message) {
        return !message.isBlank() && message.length() <= 2000;
    }

    /**Also will be checked on server-side**/
    public static boolean isValidDescription(String description) {
        return description.length() <= 2000;
    }

    /**Also will be checked on server-side**/
    public static boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() <= 50;
    }

    public interface ChooseListener {
        void accept(BufferedImage image, File file);
    }

    public enum FileType {
        IMAGE, ALL
    }

    private static class RedFlatBorder extends FlatBorder {
        protected Paint getBorderColor(Component c) {
            return Color.red;
        }
    }
}
