package net.sonmoosans.u3.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {
    public static File writeTempImage(String name, BufferedImage image) throws IOException {
        int dot = name.lastIndexOf('.');
        if (dot == -1) throw new IOException("Invalid file type");

        String suffix = name.substring(dot), prefix = ".png";
        if (suffix.length() < 2) throw new IOException();
        // create a temporary file
        File tempFile = Files.createTempFile(prefix, suffix).toFile();

        ImageIO.write(image, "png", tempFile);
        return tempFile;
    }
}
