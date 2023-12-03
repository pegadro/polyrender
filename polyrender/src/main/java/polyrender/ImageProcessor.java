package polyrender;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ImageProcessor {
    private final BufferedImage targetImage;
    private BufferedImage currentImage;
    private int numberPolygons;
    private double scoreCurrentImage;
    private int opacity;
    private int[] segments;
    private boolean paintCurrentImage;
    private final String SERVER_URL = "http://localhost:8000/process_image/";
    private CompletableFuture<Void> future;

    public ImageProcessor(BufferedImage targetImage, int [] segments, int opacity) {
        this.targetImage = targetImage;
        this.segments = segments;
        this.opacity = opacity;
        this.numberPolygons = 0;
        initializeCurrentImage();
        paintCurrentImage = true;
        this.scoreCurrentImage = Utils.calculateRMSE(targetImage, this.currentImage);
    }

    public void run(JPanel visualizationPanel, JLabel accuracyLabel, JLabel numberPolygonsLabel) {

        Thread thread = new Thread(() -> {
            String segmentsString = Arrays.toString(this.segments);

            try {
                byte[] imageData = Utils.imageToByteArray(this.targetImage);
                HttpClient client = HttpClient.newHttpClient();

                String boundary = new BigInteger(256, new Random()).toString();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(this.SERVER_URL))
                        .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(Utils.createMultipartBody(imageData, boundary, segmentsString, this.opacity)))
                        .build();

                this.future = client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenApply(HttpResponse::body)
                        .thenAccept(stream -> {
                            stream.forEach(line -> {
                                SwingUtilities.invokeLater(() -> {
                                    if (this.paintCurrentImage) {
                                        Utils.repaintJPanel(visualizationPanel, this.currentImage);
                                    } else {
                                        Utils.repaintJPanel(visualizationPanel, this.targetImage);
                                    }
                                    accuracyLabel.setText(String.format("Precisión: %f", 1 - this.scoreCurrentImage));
                                    numberPolygonsLabel.setText(String.format("Poligonos: %d", this.numberPolygons));
                                });

                                Gson gson = new Gson();
                                PolygonInfo polygonInfo = gson.fromJson(line, PolygonInfo.class);

                                BufferedImage newCurrentImage = Utils.copyImage(this.currentImage);
                                Graphics2D g2d = newCurrentImage.createGraphics();

                                Color color = new Color(polygonInfo.getColor().get(0),polygonInfo.getColor().get(1),polygonInfo.getColor().get(2),polygonInfo.getColor().get(3));

                                g2d.setColor(color);
                                Polygon polygon = new Polygon();

                                for (List<Integer> polygon_point : polygonInfo.getPolygonPoints()) {
                                    polygon.addPoint(polygon_point.get(0), polygon_point.get(1));
                                }

                                g2d.fillPolygon(polygon);

                                double newScoreCurrentImage = Utils.calculateRMSE(this.targetImage, newCurrentImage);

                                this.currentImage = newCurrentImage;
                                this.scoreCurrentImage = newScoreCurrentImage;

                                this.numberPolygons++;
                            });
                        });

                this.future.join();

                if (this.future.isDone()) {
                    Gi.stopOptions();
                    System.out.println("Representación terminada");
                } else if (this.future.isCancelled()){
                    System.out.println("Representación cancelada");
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
    }

    public void initializeCurrentImage() {
        int width = this.targetImage.getWidth();
        int height = this.targetImage.getHeight();

        JPanel jPanel = new JPanel();
        jPanel.setBackground(Utils.getMeanColor(this.targetImage));
        jPanel.setSize(width, height);

        this.currentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        jPanel.paint(this.currentImage.getGraphics());
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setSegments(int[] segments) {
        this.segments = segments;
    }

    public void setPaintCurrentImage(boolean paintCurrentImage) {
        this.paintCurrentImage = paintCurrentImage;
    }

    public void cancelFuture() {
        if (this.future != null) {
            this.future.cancel(true);
        }
    }
}
