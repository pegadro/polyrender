package polyrender;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Utils {

    public static byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }

    public static byte[] createMultipartBody(byte[] imageData, String boundary, String nSegments, int opacity) throws IOException {
        var byteOS = new ByteArrayOutputStream();
        var writer = new PrintWriter(new OutputStreamWriter(byteOS, StandardCharsets.UTF_8), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.png\"\r\n");
        writer.append("Content-Type: image/png\r\n");
        writer.append("\r\n").flush();
        byteOS.write(imageData);
        writer.append("\r\n").flush();

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"n_segments\"\r\n");
        writer.append("\r\n").flush();
        writer.append(nSegments).append("\r\n").flush();

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"opacity\"\r\n");
        writer.append("\r\n").flush();
        writer.append(String.format("%d", opacity)).append("\r\n").flush();

        writer.append("--").append(boundary).append("--\r\n").flush();

        return byteOS.toByteArray();
    }

    public static double calculateRMSE(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        double sum = 0.0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a1 = (rgb1 >> 24) & 0xff;

                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;

                double diffR = r1 - r2;
                double diffG = g1 - g2;
                double diffB = b1 - b2;
                double diffA = a1 - a2;

                sum += diffR * diffR + diffG * diffG + diffB * diffB * diffA * diffA;
            }
        }

        return Math.sqrt(sum / (width * height * 4)) / 255;
    }

    public static BufferedImage copyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = copy.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    public static Color getMeanColor(BufferedImage image) {
        long sumr = 0, sumg = 0, sumb = 0, suma = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int num = width * height;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color pixel = new Color(image.getRGB(i, j));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
                suma += pixel.getAlpha();
            }
        }

        int meanR = (int) (sumr / num);
        int meanG = (int) (sumg / num);
        int meanB = (int) (sumb / num);
        int meanA = (int) (suma / num);

        return new Color(meanR, meanG, meanB, meanA);
    }

    public static int[] stringToArray(String str) {
        String[] stringArray = str.split(",");
        int[] intArray = new int[stringArray.length];

        for (int i = 0; i < stringArray.length; i++) {
            intArray[i] = Integer.parseInt(stringArray[i].trim());
        }

        return intArray;
    }

    public static void repaintJPanel(JPanel panel, BufferedImage image) {
        JLabel label = new JLabel(new ImageIcon(image));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        panel.removeAll();
        panel.add(label);
        panel.revalidate();
        panel.repaint();
    }

    public static ImageIcon getScaledImage(BufferedImage image, int frameWidth, int frameHeight) {
        float widthRatio = (float) (frameWidth) / image.getWidth();
        float heightRatio = (float) (frameHeight) / image.getHeight();
        float minRatio = Math.min(widthRatio, heightRatio);

        int width = (int) (image.getWidth() * minRatio);
        int height = (int) (image.getHeight() * minRatio);

        Image scaledImage = image.getScaledInstance((int) (width / 1.15), (int) (height / 1.15), Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}
