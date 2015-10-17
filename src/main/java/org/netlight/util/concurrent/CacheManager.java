package org.netlight.util.concurrent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.netlight.util.TimeProperty;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author ahmad
 */
public final class CacheManager<K, V> {

    private final Map<K, V> cache;

    public CacheManager(TimeProperty duration) {
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(duration.getTime(), duration.getUnit())
                .concurrencyLevel(16)
                .<K, V>build().asMap();
    }

    public CacheManager(TimeProperty duration, RemovalListener<K, V> listener) {
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(duration.getTime(), duration.getUnit())
                .concurrencyLevel(16)
                .removalListener(listener)
                .<K, V>build().asMap();
    }

    public V cache(K k, V v) {
        return cache.put(k, v);
    }

    public V cacheIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    public V retrieve(K k) {
        return cache.get(k);
    }

    public V expire(K k) {
        return cache.remove(k);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        cache.forEach(action);
    }

    public void forEachValue(Consumer<? super V> action) {
        cache.values().forEach(action);
    }

    public void clear() {
        cache.clear();
    }

}
