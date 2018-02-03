package com.bytehamster.drawingpad;

import java.util.ArrayList;

public class FirstLastArrayList<T> extends ArrayList<T> {
    T first() {
        return get(0);
    }
    T last() {
        return get(size() - 1);
    }
    void removeLast() {
        if (isEmpty()) return;
        remove(size() - 1);
    }
}
