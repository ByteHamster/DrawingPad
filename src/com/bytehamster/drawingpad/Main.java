package com.bytehamster.drawingpad;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private FirstLastArrayList<WritableImage> history = new FirstLastArrayList<>();
    private FirstLastArrayList<Point> currentPath = new FirstLastArrayList<>();
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private static File outputFile;

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setTitle("DrawingPad");
        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> addCanvas(root));
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Expected the path as command line argument.");
            System.out.println("Using drawingPad.png as a fallback.");
            outputFile = new File("drawingPad.png");
        } else {
            outputFile = new File(args[0]);
        }

        launch(args);
    }

    private void addCanvas(StackPane root) {
        canvas = new Canvas(root.getWidth(), root.getHeight());
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    currentPath.clear();
                    currentPath.add(new Point(event.getX(), event.getY()));
                    if (event.getButton() == MouseButton.MIDDLE || event.getButton() == MouseButton.SECONDARY) {
                        return;
                    }

                    history.add(canvas.snapshot(null, null));

                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    if (event.getButton() == MouseButton.MIDDLE || event.getButton() == MouseButton.SECONDARY) {
                        return;
                    }

                    graphicsContext.lineTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                    currentPath.add(new Point(event.getX(), event.getY()));
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    currentPath.add(new Point(event.getX(), event.getY()));

                    if (event.getButton() == MouseButton.MIDDLE) {
                        return;
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        //if (!currentPath.first().equals(currentPath.last())) {
                        //    return;
                        //}
                        graphicsContext.drawImage(history.last(), 0, 0);
                        history.removeLast();
                        return;
                    }

                    graphicsContext.closePath();
                    checkLine();
                });
        root.getChildren().add(canvas);
    }

    private void checkLine() {
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
                //graphicsContext.fillOval(p.x, perfectLine, 8, 8);
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
                //graphicsContext.fillOval(perfectLine, p.y, 8, 8);
                if (Math.abs(p.x - perfectLine) > maxDifference) {
                    return;
                }
            }
        }

        graphicsContext.drawImage(history.last(), 0, 0);

        graphicsContext.save();
        graphicsContext.setLineWidth(3.5);
        graphicsContext.beginPath();
        graphicsContext.moveTo(currentPath.first().x, currentPath.first().y);
        graphicsContext.lineTo(currentPath.last().x, currentPath.last().y);
        graphicsContext.stroke();
        graphicsContext.closePath();
        graphicsContext.restore();
    }

    @Override
    public void stop() {
        try {
            BufferedImage image = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}