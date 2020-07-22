package site.kason.kalang.sdk.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * @author KasonYang
 */
public class CacheHolder<K,V> {

    private LinkedHashMap<K,V> cacheMap = new LinkedHashMap<>();

    private final int maxSize;

    public CacheHolder(int maxSize) {
        this.maxSize = maxSize;
    }

    public V get(K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        V value = cacheMap.remove(key);
        cacheMap.put(key, value);
        return value;
    }

    public V computeIfAbsent(K key, Function<K,V> computer) {
        if (cacheMap.containsKey(key)) {
            return cacheMap.get(key);
        }
        V value = computer.apply(key);
        put(key, value);
        return value;
    }

    public void put(K key, V value) {
        cacheMap.remove(key);
        cacheMap.put(key, value);
        this.pack();
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    private void pack() {
        int deleteCount = cacheMap.size() - maxSize;
        if (deleteCount > 0) {
            ArrayList<K> keys = new ArrayList<>(cacheMap.keySet());
            for (int i = 0; i < deleteCount; i++) {
                cacheMap.remove(keys.get(i));
            }
        }
    }

}
