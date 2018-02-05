package com.bytehamster.drawingpad;

import java.awt.image.BufferedImage;

public class ImageUtils {
    private static final int MIN_PADDING = 20;

    private ImageUtils() {
        // Utility class
    }

    static BufferedImage trim(BufferedImage input) {
        int topPadding = 0;
        while (topPadding < input.getHeight() && isRowEmpty(input, topPadding)) {
            topPadding++;
        }
        int bottomPadding = 0;
        while (input.getHeight() - 1 - bottomPadding > topPadding
                && isRowEmpty(input, input.getHeight() - 1 - bottomPadding)) {
            bottomPadding++;
        }
        int leftPadding = 0;
        while (leftPadding < input.getWidth() && isColumnEmpty(input, leftPadding)) {
            leftPadding++;
        }
        int rightPadding = 0;
        while (input.getWidth() - 1 - rightPadding > leftPadding
                && isColumnEmpty(input, input.getWidth() - 1 - rightPadding)) {
            rightPadding++;
        }

        // input.setRGB(0, topPadding, 0xffff0000);
        // input.setRGB(0, input.getHeight() - 1 - bottomPadding, 0xffff0000);
        // input.setRGB(leftPadding, 0, 0xffff0000);
        // input.setRGB(input.getWidth() - 1 - rightPadding, 0, 0xffff0000);

        int fromX = Math.max(0, leftPadding - MIN_PADDING);
        int fromY = Math.max(0, topPadding - MIN_PADDING);
        int toX = Math.min(input.getWidth(), input.getWidth() - rightPadding + MIN_PADDING);
        int toY = Math.min(input.getHeight(), input.getHeight() - bottomPadding + MIN_PADDING);

        int width = toX - fromX;
        int height = toY - fromY;

        return input.getSubimage(fromX, fromY, width, height);
    }

    private static boolean isRowEmpty(BufferedImage image, int row) {
        for (int i = 0; i < image.getWidth(); i++) {
            if (image.getRGB(i, row) != 0xffffffff) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColumnEmpty(BufferedImage image, int column) {
        for (int i = 0; i < image.getHeight(); i++) {
            if (image.getRGB(column, i) != 0xffffffff) {
                return false;
            }
        }
        return true;
    }
}
