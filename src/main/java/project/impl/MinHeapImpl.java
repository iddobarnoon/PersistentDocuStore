package project.impl;

import project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    private int size;
    public MinHeapImpl(){
        this.size = 20;
        this.elements = (E[]) new Comparable[this.size];
    }

    @Override
    public void reHeapify(E element) {
        //should it move up?
        //should it move down?
        //stay where it is?

        // approach one, just call upHeap, downHeap, on the element
        int index = getArrayIndex(element);
        if(index == -1){return;}
        upHeap(index);
        downHeap(index);
    }

    @Override
    protected int getArrayIndex(E element) {
        //could use bin search for o(logn) but optimal time not needed.
        for(int i = 1; i < elements.length; i++){
            if(elements[i] != null)if(elements[i].equals(element)){return i;}
        } return -1;
    }

    @Override
    protected void doubleArraySize() {
        E[] temp = (E[]) new Comparable[this.size*2];
        System.arraycopy(this.elements, 1, temp, 1, this.size);
        this.size = this.size*2;
        this.elements = temp;
    }
}
