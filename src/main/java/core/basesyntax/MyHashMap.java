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

        int bucketIndex = getBucketByHash(key, currentCapacity);
        SearchResult<K, V> res = searchBucket(bucketIndex, key);

        if (res.node == null) {
            // empty bucket -> insert as head
            table[bucketIndex] = new Node<>(key, value, null);
            size++;
            return;
        }

        if (res.found) {
            // update existing
            res.node.value = value;
            return;
        }

        // not found -> append after last node (res.node is last)
        res.node.next = new Node<>(key, value, null);
        size++;
    }

    @Override
    public V getValue(K key) {
        if (table == null || table.length == 0) {
            return null;
        }

        int bucketIndex = getBucketByHash(key, currentCapacity);
        Node<K, V> node = table[bucketIndex];

        while (node != null) {
            if (Objects.equals(node.key, key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
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

        if (size >= threshold) {
            resizeTable();
        }
    }

    private void resizeTable() {
        int newCapacity = this.currentCapacity << 1;
        this.currentCapacity = newCapacity;
        this.threshold = (int) (this.currentCapacity * DEFAULT_LOAD_FACTOR);
        table = transferTable(table, newCapacity);
    }

    private Node<K, V>[] transferTable(Node<K, V>[] oldTable, int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("newCapacity must be > 0");
        }

        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];

        if (oldTable == null) {
            return newTable;
        }

        Node<K, V>[] tails = (Node<K, V>[]) new Node[newCapacity];

        for (Node<K, V> kvNode : oldTable) {
            Node<K, V> node = kvNode;

            while (node != null) {
                Node<K, V> next = node.next;
                node.next = null;
                int index = getBucketByHash(node.key, newCapacity);

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

    private int getBucketByHash(K key, int capacity) {
        int h = (key == null) ? 0 : key.hashCode();
        return h & (capacity - 1);
    }

    private SearchResult<K, V> searchBucket(int bucketIndex, K key) {
        if (table == null) {
            return new SearchResult<>(null, false);
        }

        if (bucketIndex < 0 || bucketIndex >= table.length) {
            throw new IndexOutOfBoundsException("bucketIndex out of range: " + bucketIndex);
        }

        Node<K, V> node = table[bucketIndex];
        if (node == null) {
            return new SearchResult<>(null, false); // empty bucket
        }

        while (true) {
            if (Objects.equals(node.key, key)) {
                return new SearchResult<>(node, true); // found matching node
            }
            if (node.next == null) {
                return new SearchResult<>(node, false); // reached last node, not found
            }
            node = node.next;
        }
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

    private static final class SearchResult<K, V> {
        private final Node<K, V> node; // either the matching node or the last node in bucket
        // (or null if bucket empty)
        private final boolean found; // true if node.key equals searched key

        SearchResult(Node<K, V> node, boolean found) {
            this.node = node;
            this.found = found;
        }
    }

}
