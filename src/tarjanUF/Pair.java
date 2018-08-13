package tarjanUF;

public class Pair<K, V> {
    // Cannot use javafx's implementation as it is being discontinued in java 10.
    private final K key;
    private final V value;

    // Constructor.
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    // Get the first value.
    public K getKey() {
        return this.key;
    }

    // Get the second value.
    public V getValue() {
        return this.value;
    }
}
