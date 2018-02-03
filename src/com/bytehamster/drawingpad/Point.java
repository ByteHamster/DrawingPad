package com.bytehamster.drawingpad;

public class Point {
    double x, y;
    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
}

