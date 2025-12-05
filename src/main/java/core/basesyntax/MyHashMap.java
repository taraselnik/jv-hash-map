package core.basesyntax;

import java.util.Objects;

@SuppressWarnings("unchecked")
public class MyHashMap<K, V> implements MyMap<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private int size;
    private int threshold;
    private int currentCapacity;
    private Node<K,V>[] table;

    @Override
    public void put(K key, V value) {
        checkTable();

        Node<K, V> newNode = new Node<>(key, value, null);
        int bucketIndex = key != null ? getBucketByHash(key) : 0;
        Node<K, V> nodeFromBucket = findNodeOrLastInBucket(bucketIndex, key);

        if (nodeFromBucket == null) {
            table[bucketIndex] = newNode;
            size++;
            return;
        }

        if (!nodeFromBucket.equals(newNode)) {
            nodeFromBucket.next = newNode;
            size++;
            return;
        }

        nodeFromBucket.value = value;
    }

    @Override
    public V getValue(K key) {
        if (table == null || table.length == 0) {
            return null;
        }

        int index = key != null ? getBucketByHash(key) : 0;
        Node<K, V> nodeFromBucket = findNodeOrLastInBucket(index, key);
        if (nodeFromBucket == null || !Objects.equals(nodeFromBucket.key, key)) {
            return null;
        }

        return nodeFromBucket.value;
    }

    @Override
    public int getSize() {
        return size;
    }

    private void checkTable() {
        if (table == null) {
            table = (Node<K, V>[]) new Node[DEFAULT_INITIAL_CAPACITY];
            this.currentCapacity = DEFAULT_INITIAL_CAPACITY;
            this.threshold = (int) (this.currentCapacity * DEFAULT_LOAD_FACTOR);
            return;
        }

        if (size > threshold) {
            resizeTable();
        }
    }

    private void resizeTable() {
        this.currentCapacity = this.currentCapacity << 1;
        this.threshold = (int) (this.currentCapacity * DEFAULT_LOAD_FACTOR);
        table = transferTable(table, this.currentCapacity);
    }

    private Node<K, V>[] transferTable(Node<K, V>[] oldTable, int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("newCapacity must be > 0");
        }

        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];
        Node<K, V>[] tails = (Node<K, V>[]) new Node[newCapacity];

        for (Node<K, V> kvNode : oldTable) {
            Node<K, V> node = kvNode;

            while (node != null) {
                Node<K, V> next = node.next;
                node.next = null;
                int index = getBucketByHash(node.key);

                if (newTable[index] == null) {
                    newTable[index] = node;
                } else {
                    tails[index].next = node;
                }

                tails[index] = node;
                node = next;
            }
        }

        return newTable;
    }

    private int getBucketByHash(K key) {
        return key.hashCode() & (currentCapacity - 1);
    }

    private Node<K, V> findNodeOrLastInBucket(int bucketIndex, K key) {
        if (bucketIndex < 0 || bucketIndex >= table.length) {
            throw new IndexOutOfBoundsException("bucketIndex out of range: " + bucketIndex);
        }

        Node<K, V> node = table[bucketIndex];
        // iterate through the chain and compare keys safely
        while (node != null) {
            if (Objects.equals(node.key, key) || node.next == null) {
                return node; // return existing node which is first equal or the last one
            }
            node = node.next;
        }
        return null; // not found or empty bucket
    }

    private static class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> next;

        public Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(key, node.key);
        }
    }
}
