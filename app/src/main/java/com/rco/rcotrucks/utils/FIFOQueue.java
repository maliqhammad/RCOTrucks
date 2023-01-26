package com.rco.rcotrucks.utils;

import java.util.LinkedList;

public class FIFOQueue<E> extends LinkedList<E> {
    private int capacity = 5;

    public FIFOQueue(int capacity){
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        if(size() >= capacity)
            removeFirst();
        return super.add(e);
    }
}
