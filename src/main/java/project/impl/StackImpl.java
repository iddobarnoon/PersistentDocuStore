package project.impl;

import project.Stack;

public class StackImpl<T> implements Stack<T> {
    private Node head;
    private class Node<T>{
        private T value;
        private Node next;
        public Node(T value){
            this.value = value;
            this.next = null;
        }
    }

    public StackImpl(){
        this.head = null;
    }

    @Override
    public void push(T element) {
        Node<T> newNode = new Node<>(element);
        if(this.head != null){
            newNode.next = this.head;
        }
        this.head = newNode;

    }

    @Override
    public T pop() {
        T target;
        if(this.head == null){return null;}
        target = (T) this.head.value;
        this.head = this.head.next;
        return target;
    }

    @Override
    public T peek() {
        return this.head != null ? (T) this.head.value : null;
    }

    @Override
    public int size() {
        Node current = this.head;
        int count = 0;
        while(current != null){
            count++;
            current = current.next;
        } return count;
    }
}
