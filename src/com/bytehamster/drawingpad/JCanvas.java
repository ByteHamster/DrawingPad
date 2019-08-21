package com.bytehamster.drawingpad;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class JCanvas extends JPanel {
    private final BufferedImage img;
    private final Graphics2D graphics;

    public JCanvas(int width, int height) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = img.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        setLayout(null);
        setPreferredSize(new Dimension(width, height));
    }

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public BufferedImage snapshot() {
        return copyImage(img);
    }
}
