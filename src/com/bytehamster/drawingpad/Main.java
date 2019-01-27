package com.bytehamster.drawingpad;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private static final double INPUT_PADDING = 20;
    private FirstLastArrayList<WritableImage> history = new FirstLastArrayList<>();
    private FirstLastArrayList<Point> currentPath = new FirstLastArrayList<>();
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private boolean linesEnabled = true;
    private boolean isUsingRubber = false;
    private static File outputFile;
    private static File inputFile;
    private final KeyCombination KEY_UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    private final KeyCombination KEY_SAVE = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setTitle("DrawingPad");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(keyEvent -> {
            if (KEY_UNDO.match(keyEvent)) {
                undo();
            } else if (KEY_SAVE.match(keyEvent)) {
                Platform.exit();
            }
        });

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10));
        buttons.setSpacing(5);
        root.setTop(buttons);

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(actionEvent -> undo());

        CheckBox linesEnabledCheck = new CheckBox("Line detection");
        linesEnabledCheck.setSelected(true);
        linesEnabledCheck.setOnAction(actionEvent -> linesEnabled = linesEnabledCheck.isSelected());

        buttons.getChildren().addAll(
                undoButton,
                rubberButton(),
                new Separator(Orientation.VERTICAL),
                colorButton("Black", Color.BLACK),
                colorButton("Gray", Color.GRAY),
                colorButton("Red", Color.RED),
                colorButton("Green", Color.GREEN),
                colorButton("Blue", Color.BLUE),
                colorButton("Orange", Color.ORANGE),
                new Separator(Orientation.VERTICAL),
                linesEnabledCheck);

        addCanvas(root);
    }

    private Button rubberButton() {
        Button btn = new Button("Rubber");
        btn.setOnAction(actionEvent -> {
            graphicsContext.setStroke(Color.WHITE);
            graphicsContext.setLineWidth(15);
            isUsingRubber = true;
        });
        return btn;
    }

    private Button colorButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setOnAction(actionEvent -> {
            graphicsContext.setStroke(color);
            graphicsContext.setLineWidth(3);
            isUsingRubber = false;
        });
        return btn;
    }

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Expected the path as command line argument.");
            System.out.println("Using drawingPad.png as a fallback.");
            outputFile = new File("drawingPad.png");
        } else {
            boolean nextIsInput = false;
            for (String arg : args) {
                if (nextIsInput) {
                    inputFile = new File(arg);
                    nextIsInput = false;
                } else if (arg.equals("-i")) {
                    nextIsInput = true;
                } else {
                    outputFile = new File(arg);
                }
            }
        }

        launch(args);
    }

    private void addCanvas(BorderPane root) {
        double width = 1000;
        double height = 1000;
        Image inputImage = null;

        if (inputFile != null) {
            try {
                inputImage = new Image(inputFile.toURI().toString());
                width = inputImage.getWidth() + 2 * INPUT_PADDING;
                height = inputImage.getHeight() + 2 * INPUT_PADDING;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        if (canvas == null) {
            canvas = new Canvas(width, height);
            root.setCenter(canvas);
        }

        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, width, height);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);

        if (inputImage != null) {
            graphicsContext.drawImage(inputImage, INPUT_PADDING, INPUT_PADDING);
        }

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
                        undo();
                        return;
                    }

                    graphicsContext.closePath();
                    checkLine();
                });
    }

    private void undo() {
        if (!history.isEmpty()) {
            graphicsContext.drawImage(history.last(), 0, 0);
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
            image = ImageUtils.trim(image);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}