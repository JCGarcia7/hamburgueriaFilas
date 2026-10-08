package projetoesdlinear;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedQueue<Type> implements Iterable<Type> {

    public static class Node<Type> {
        public Type value;
        public Node<Type> next;

        public Node(Type value) {
            this.value = value;
            this.next = null;
        }
    }

    public Node<Type> start; 
    public Node<Type> end; 
    private int size;

    public LinkedQueue() {
        this.start = null;
        this.end = null;
        this.size = 0;
    }

    // Operação enfileirar no fim
    public void enqueue(Type value) {
        Node<Type> newNode = new Node<>(value);
        if (isEmpty()) {
            start = newNode;
            end = newNode;
        } else {
            end.next = newNode;
            end = newNode;
        }
        size++;
    }

    // Operação desenfileirar do início
    public Type dequeue() {
        if (isEmpty()) {
            return null;
        }
        Type val = start.value;
        start = start.next;
        size--;
        if (start == null) {
            end = null;
        }
        return val;
    }

    public Type peek() {
        if (isEmpty()) {
            return null;
        }
        return start.value;
    }

    public void clear() {
        start = null;
        end = null;
        size = 0;
    }

    public boolean isEmpty() {
        return start == null;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<Type> iterator() {
        return new Iterator<Type>() {
            private Node<Type> current = start;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Type next() {
                if (!hasNext()) throw new NoSuchElementException();
                Type val = current.value;
                current = current.next;
                return val;
            }
        };
    }
}