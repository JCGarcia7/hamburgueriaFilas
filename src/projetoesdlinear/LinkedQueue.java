package projetoesdlinear;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TAD Fila (Queue / FIFO) com estrutura encadeada simples.
 * Extraído para separar a lógica de Estruturas de Dados do Jogo.
 */
public class LinkedQueue<Type> implements Iterable<Type> {

    public static class Node<Type> {
        public Type value;
        public Node<Type> next;

        public Node(Type value) {
            this.value = value;
            this.next = null;
        }
    }

    public Node<Type> start; // Início / Cabeça da Fila
    public Node<Type> end;   // Fim / Cauda da Fila
    private int size;

    public LinkedQueue() {
        this.start = null;
        this.end = null;
        this.size = 0;
    }

    // Operação enqueue (enfileirar no fim)
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

    // Operação dequeue (desenfileirar do início)
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

    // Operação peek (consultar o início)
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

    // Permite varrer a fila em loops for-each (usado na hora de desenhar a esteira)
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