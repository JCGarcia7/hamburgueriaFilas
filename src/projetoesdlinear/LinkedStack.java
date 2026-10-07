package projetoesdlinear;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TAD Pilha (Stack / LIFO) encadeada para montagem do hambúrguer.
 * Baseada na especificação da Aula 02 do Prof. Dr. David Buzatto.
 */
public class LinkedStack<Type> implements Iterable<Type> {

    public static class Node<Type> {
        public Type value;
        public Node<Type> previous; // Aponta para o nó de baixo

        public Node(Type value, Node<Type> previous) {
            this.value = value;
            this.previous = previous;
        }
    }

    public Node<Type> top; // Topo da pilha
    private int size;

    public LinkedStack() {
        this.top = null;
        this.size = 0;
    }

    // push: empilha no topo
    public void push(Type value) {
        top = new Node<>(value, top);
        size++;
    }

    // pop: desempilha o elemento do topo
    public Type pop() {
        if (isEmpty()) {
            return null;
        }
        Type val = top.value;
        top = top.previous;
        size--;
        return val;
    }

    // peek: consulta o elemento do topo sem remover
    public Type peek() {
        if (isEmpty()) {
            return null;
        }
        return top.value;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int getSize() {
        return size;
    }

    // Itera a partir do topo até a base
    @Override
    public Iterator<Type> iterator() {
        return new Iterator<Type>() {
            private Node<Type> current = top;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Type next() {
                if (!hasNext()) throw new NoSuchElementException();
                Type val = current.value;
                current = current.previous;
                return val;
            }
        };
    }
}