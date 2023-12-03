package polyrender;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class Gi {
    private static JPanel imagePanel;
    private static BufferedImage image, targetImage;
    private static ImageIcon scaledImage;
    private static int opacity;
    private static String segments;
    private static ImageProcessor imageProcessor;
    private static JLabel accuracyLabel, numberPolygonsLabel, opacityLabel;
    private static JButton selectImageButton, startButton, stopButton, imageToggle, saveButton, configButton;
    private static boolean running, paintCurrentImage;

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("PolyRender");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        mainFrame.setLayout(new BorderLayout());

        JPanel leftPanel = createOptionsPanel();

        imagePanel = new JPanel();
        imagePanel.setLayout(new GridBagLayout());
        imagePanel.setBackground(Color.WHITE);

        mainFrame.add(leftPanel, BorderLayout.NORTH);
        mainFrame.add(imagePanel, BorderLayout.CENTER);

        mainFrame.setVisible(true);
    }

    private static JPanel createOptionsPanel() {
        opacity = 128;
        segments = "50,250,500,1000,1500,2000";
        paintCurrentImage = false;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        InputStream openIconResource, playIconResource, stopIconResource, saveIconResource, changeIconResource, configIconResource;

        selectImageButton = new JButton();
        startButton = new JButton();
        stopButton = new JButton();
        imageToggle = new JButton();
        saveButton = new JButton();
        configButton = new JButton();

        try {
            openIconResource = Gi.class.getClassLoader().getResourceAsStream("open.png");
            playIconResource = Gi.class.getClassLoader().getResourceAsStream("play.png");
            stopIconResource = Gi.class.getClassLoader().getResourceAsStream("stop.png");
            changeIconResource = Gi.class.getClassLoader().getResourceAsStream("change.png");
            saveIconResource = Gi.class.getClassLoader().getResourceAsStream("save.png");
            configIconResource = Gi.class.getClassLoader().getResourceAsStream("config.png");
            if (openIconResource == null || playIconResource == null || stopIconResource == null || changeIconResource == null || saveIconResource == null || configIconResource == null) {
                throw new IOException("El recurso no se pudo cargar.");
            }

            int width = 30;
            int height = 30;
            ImageIcon openIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(openIconResource)), width, height);
            ImageIcon playIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(playIconResource)), width, height);
            ImageIcon stopIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(stopIconResource)), width, height);
            ImageIcon changeIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(changeIconResource)), width, height);
            ImageIcon saveIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(saveIconResource)), width, height);
            ImageIcon configIcon = Utils.resizeIcon(new ImageIcon(ImageIO.read(configIconResource)), width, height);

            selectImageButton.setIcon(openIcon);
            startButton.setIcon(playIcon);
            stopButton.setIcon(stopIcon);
            imageToggle.setIcon(changeIcon);
            saveButton.setIcon(saveIcon);
            configButton.setIcon(configIcon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        selectImageButton.setBorderPainted(false);
        selectImageButton.setBorder(null);
        selectImageButton.setFocusPainted(false);
        selectImageButton.setContentAreaFilled(false);

        startButton.setBorderPainted(false);
        startButton.setBorder(null);
        startButton.setFocusPainted(false);
        startButton.setContentAreaFilled(false);

        stopButton.setBorderPainted(false);
        stopButton.setBorder(null);
        stopButton.setFocusPainted(false);
        stopButton.setContentAreaFilled(false);

        imageToggle.setBorderPainted(false);
        imageToggle.setBorder(null);
        imageToggle.setFocusPainted(false);
        imageToggle.setContentAreaFilled(false);

        saveButton.setBorderPainted(false);
        saveButton.setBorder(null);
        saveButton.setFocusPainted(false);
        saveButton.setContentAreaFilled(false);

        configButton.setBorderPainted(false);
        configButton.setBorder(null);
        configButton.setFocusPainted(false);
        configButton.setContentAreaFilled(false);

        optionsPanel.add(selectImageButton);
        optionsPanel.add(startButton);
        optionsPanel.add(stopButton);
        optionsPanel.add(imageToggle);
        optionsPanel.add(saveButton);
        optionsPanel.add(configButton);

        accuracyLabel = new JLabel("Precisión: 0.0");
        accuracyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        numberPolygonsLabel = new JLabel("Poligonos: 0");
        numberPolygonsLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        optionsPanel.add(accuracyLabel);
        optionsPanel.add(numberPolygonsLabel);
        defaultOptions();

        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de Imagen", "jpg", "png", "gif", "jpeg", "bmp");
                fileChooser.setFileFilter(filter);

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    try {
                        image = ImageIO.read(selectedFile);
                        scaledImage = Utils.getScaledImage(image, imagePanel.getWidth(), imagePanel.getHeight());
                        JLabel label = new JLabel(scaledImage);

                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.weightx = 1;
                        gbc.weighty = 1;
                        gbc.fill = GridBagConstraints.NONE;  // No estirar el componente
                        gbc.anchor = GridBagConstraints.CENTER;

                        imagePanel.removeAll();
                        imagePanel.add(label);
                        imagePanel.revalidate();
                        imagePanel.repaint();

                        targetImage = new BufferedImage(scaledImage.getIconWidth(), scaledImage.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                        targetImage.getGraphics().drawImage(scaledImage.getImage(), 0, 0, null);

                        imageSelectedOptions();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopOptions();
                if (imageProcessor != null) imageProcessor.cancelFuture();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //imageProcessor.saveCurrentImage();
                saveRecreation();
            }
        });

        imageToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paintCurrentImage = !paintCurrentImage;
                if (imageProcessor != null && running) imageProcessor.setPaintCurrentImage(paintCurrentImage);

                if (imageProcessor != null && !running) {
                    if (paintCurrentImage) {
                        Utils.repaintJPanel(imagePanel, imageProcessor.getCurrentImage());
                    } else {
                        Utils.repaintJPanel(imagePanel, targetImage);
                    }
                }
            }
        });

        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConfigFrame();
            }
        });

        return optionsPanel;
    }

    public static void execute() {
        runningOptions();
        paintCurrentImage = true;
        int [] segments_array = Utils.stringToArray(segments);
        imageProcessor = new ImageProcessor(targetImage, segments_array, opacity);
        imageProcessor.run(imagePanel, accuracyLabel, numberPolygonsLabel);
    }

    public static void showConfigFrame() {
        JFrame jFrame = new JFrame("Configuraciones");
        jFrame.setLayout(new BorderLayout());
        jFrame.setSize(400, 400);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel opacityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        opacityLabel = new JLabel(String.format("Opacidad (%d):", opacity));
        opacityLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JSlider opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 255, opacity);
        opacitySlider.setMajorTickSpacing(10);
        opacitySlider.setMinorTickSpacing(1);

        opacityPanel.add(opacityLabel);
        opacityPanel.add(opacitySlider);

        JPanel segmentsPanel = new JPanel(new BorderLayout());

        JLabel segmentsLabel = new JLabel("Segmentos");
        segmentsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextArea segmentsTextArea = new JTextArea();
        segmentsTextArea.setText(segments);
        JScrollPane scrollPane = new JScrollPane(segmentsTextArea);
        scrollPane.setPreferredSize(new Dimension(200, 20));

        segmentsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        segmentsPanel.add(segmentsLabel, BorderLayout.NORTH);
        segmentsPanel.add(segmentsTextArea, BorderLayout.CENTER);

        opacityPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        segmentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        jFrame.add(opacityPanel, BorderLayout.NORTH);
        jFrame.add(segmentsPanel, BorderLayout.CENTER);

        jFrame.setVisible(true);

        opacitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                opacity = source.getValue();
                if (imageProcessor != null) imageProcessor.setOpacity(opacity);
                opacityLabel.setText(String.format("Opacidad (%d):", opacity));
            }
        });

        segmentsTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChanged();
            }

            private void documentChanged() {
                segments = segmentsTextArea.getText();
            }
        });
    }

    public static void saveRecreation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione una Ruta");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            Random random = new Random();
            String filename = "recreation" + random.nextInt(Integer.MAX_VALUE) + ".png";
            File output = new File(directory.getAbsolutePath(), filename);

            try {
                System.out.println("Archivo guardado en: " + output.getAbsolutePath());
                ImageIO.write(imageProcessor.getCurrentImage(), "png", output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No se seleccionó ningún directorio.");
        }
    }

    public static void defaultOptions() {
        selectImageButton.setEnabled(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        imageToggle.setEnabled(false);
        saveButton.setEnabled(false);
    }

    public static void imageSelectedOptions() {
        defaultOptions();
        startButton.setEnabled(true);
    }

    public static void runningOptions() {
        running = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        imageToggle.setEnabled(true);
    }

    public static void stopOptions() {
        running = false;
        defaultOptions();
        imageToggle.setEnabled(true);
        saveButton.setEnabled(true);
    }
}
