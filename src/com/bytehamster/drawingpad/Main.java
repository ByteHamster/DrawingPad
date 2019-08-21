package com.bytehamster.drawingpad;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final int INPUT_PADDING = 20;
    private FirstLastArrayList<Image> history = new FirstLastArrayList<>();
    private FirstLastArrayList<Point> currentPath = new FirstLastArrayList<>();
    private Graphics2D graphicsContext;
    private JCanvas canvas;
    private boolean linesEnabled = true;
    private boolean isUsingRubber = false;
    private File outputFile;
    private File inputFile;

    public static void main(String[] args) {
        Main main = new Main();
        if (args.length != 1 && args.length != 3) {
            System.out.println("Expected the path as command line argument.");
            System.out.println("Using drawingPad.png as a fallback.");
            main.outputFile = new File("drawingPad.png");
        } else {
            boolean nextIsInput = false;
            for (String arg : args) {
                if (nextIsInput) {
                    main.inputFile = new File(arg);
                    nextIsInput = false;
                } else if (arg.equals("-i")) {
                    nextIsInput = true;
                } else {
                    main.outputFile = new File(arg);
                }
            }
        }

        main.launch();
    }

    private void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setTitle("DrawingPad");
        frame.setSize(800, 800);
        frame.setResizable(true);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(keyEvent -> {
            if ((keyEvent.getModifiers() & ActionEvent.CTRL_MASK) != 0 && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_Z:
                        undo();
                        break;
                    case KeyEvent.VK_S:
                        exit();
                        break;
                }
            }
            return true;
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        Box layout = Box.createVerticalBox();

        JButton undoButton = new JButton("Undo");
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(actionEvent -> undo());

        JCheckBox linesEnabledCheck = new JCheckBox("Line detection");
        linesEnabledCheck.setFocusPainted(false);
        linesEnabledCheck.setSelected(true);
        linesEnabledCheck.addItemListener(actionEvent -> linesEnabled = linesEnabledCheck.isSelected());

        Box buttons = Box.createHorizontalBox();
        buttons.add(bigPadding());
        buttons.add(undoButton);
        buttons.add(smallPadding());
        buttons.add(rubberButton());
        buttons.add(bigPadding());
        buttons.add(bigPadding());
        buttons.add(colorButton("Black", Color.BLACK));
        buttons.add(smallPadding());
        buttons.add(colorButton("Gray", Color.GRAY));
        buttons.add(smallPadding());
        buttons.add(colorButton("Red", Color.RED));
        buttons.add(smallPadding());
        buttons.add(colorButton("Green", Color.GREEN));
        buttons.add(smallPadding());
        buttons.add(colorButton("Blue", Color.BLUE));
        buttons.add(smallPadding());
        buttons.add(colorButton("Orange", Color.ORANGE));
        buttons.add(bigPadding());
        buttons.add(bigPadding());
        buttons.add(Box.createHorizontalGlue());
        buttons.add(linesEnabledCheck);
        buttons.add(bigPadding());

        layout.add(bigPadding());
        layout.add(buttons);
        layout.add(bigPadding());
        layout.add(new JScrollPane(createCanvas()));

        frame.add(layout);

        frame.setVisible(true);
    }

    private Component smallPadding() {
        return Box.createRigidArea(new Dimension(5, 5));
    }

    private Component bigPadding() {
        return Box.createRigidArea(new Dimension(10, 10));
    }

    private JButton rubberButton() {
        JButton btn = new JButton("Rubber");
        btn.setFocusPainted(false);
        btn.addActionListener(actionEvent -> {
            graphicsContext.setColor(Color.WHITE);
            graphicsContext.setStroke(new BasicStroke(15));
            isUsingRubber = true;
        });
        return btn;
    }

    private JButton colorButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.addActionListener(actionEvent -> {
            graphicsContext.setColor(color);
            graphicsContext.setStroke(new BasicStroke(3));
            isUsingRubber = false;
        });
        return btn;
    }

    private JCanvas createCanvas() {
        int width = 1000;
        int height = 1000;
        BufferedImage inputImage = null;

        canvas = new JCanvas(width, height);
        graphicsContext = canvas.getGraphics();

        if (inputFile != null) {
            try {
                inputImage = ImageIO.read(inputFile);
                width = inputImage.getWidth() + 2 * INPUT_PADDING;
                height = inputImage.getHeight() + 2 * INPUT_PADDING;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        graphicsContext.setColor(Color.WHITE);
        graphicsContext.fillRect(0, 0, width, height);
        graphicsContext.setColor(Color.BLACK);
        graphicsContext.setStroke(new BasicStroke(3));

        if (inputImage != null) {
            graphicsContext.drawImage(inputImage, INPUT_PADDING, INPUT_PADDING, null);
        }

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                currentPath.clear();
                currentPath.add(new Point(event.getX(), event.getY()));
                history.add(canvas.snapshot());
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                currentPath.add(new Point(event.getX(), event.getY()));
                checkLine();
            }
        });
        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                Point previous = currentPath.last();
                currentPath.add(new Point(event.getX(), event.getY()));
                graphicsContext.drawLine(previous.x, previous.y, event.getX(), event.getY());
                canvas.getParent().repaint();
            }
        });

        return canvas;
    }

    private void undo() {
        if (!history.isEmpty()) {
            graphicsContext.drawImage(history.last(), 0, 0, null);
            canvas.getParent().repaint();
            history.removeLast();
        }
    }

    private void checkLine() {
        if (!linesEnabled || isUsingRubber) {
            return;
        }

        double dx = currentPath.last().x - currentPath.first().x;
        double dy = currentPath.last().y - currentPath.first().y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length < 80) {
            return;
        }

        final double maxDifference = length / 15;
        if (Math.abs(dx) > Math.abs(dy)) {
            double m = dy / dx;
            double c = currentPath.first().y - m * currentPath.first().x;
            for (Point p : currentPath) {
                double perfectLine = m * p.x + c;
                if (Math.abs(p.y - perfectLine) > maxDifference) {
                    return;
                }
            }
        } else {
            // Switch x and y to prevent lines with m=infinity
            double m = dx / dy;
            double c = currentPath.first().x - m * currentPath.first().y;
            for (Point p : currentPath) {
                double perfectLine = m * p.y + c;
                if (Math.abs(p.x - perfectLine) > maxDifference) {
                    return;
                }
            }
        }

        graphicsContext.drawImage(history.last(), 0, 0, null);
        graphicsContext.drawLine(currentPath.first().x, currentPath.first().y, currentPath.last().x, currentPath.last().y);
        canvas.getParent().repaint();
    }

    private void exit() {
        try {
            BufferedImage image = ImageUtils.trim(canvas.snapshot());
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}