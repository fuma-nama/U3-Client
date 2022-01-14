package net.sonmoosans.u3.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.util.function.Consumer;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class FileItem extends MessageItemPane {
    private JPanel Main;
    private JLabel iconHolder;
    private JLabel nameLabel;
    private JLabel byteLabel;
    private JPanel contextPane;
    private JButton sideButton;

    public FileItem(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        Path path = Path.of(url.getPath());
        runAsync(()-> {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");

                return conn.getContentLength();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, size-> byteLabel.setText(size + " bytes"));

        setIcon(iconHolder, Toolkit.getDefaultToolkit().getImage("file.png"));
        if (isImage(path)) {
            runAsync(()-> getImage(fileUrl), image ->
                    setIcon(iconHolder, image, getScaledDimension(image, 70, 70, iconHolder))
            );
        }

        nameLabel.setText(getFileName(path));

        init(false);

        setSideButton("Open", e-> openWebpage(url));
    }

    public FileItem(File localFile) throws IOException {
        nameLabel.setText(localFile.getName());
        byteLabel.setText(localFile.length() + " bytes");
        setIcon(iconHolder, Toolkit.getDefaultToolkit().getImage("file.png"));

        if (isImage(localFile.toPath())) {
            runAsync(()-> getImage(localFile), image ->
                    setIcon(iconHolder, image, getScaledDimension(image, 70, 70, iconHolder))
            );
        }
        init();
    }

    public FileItem setSideButton(String text, Consumer<FileItem> onClick) {
        sideButton.setText(text);
        sideButton.addActionListener(e-> onClick.accept(this));
        sideButton.setVisible(true);
        return this;
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        contextPane.setBackground(color);
    }
}
