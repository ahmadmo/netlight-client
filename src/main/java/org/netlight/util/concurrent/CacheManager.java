package org.netlight.util.concurrent;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import org.netlight.util.TimeProperty;

import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author ahmad
 */
public final class CacheManager<K, V> {

    private final ConcurrentMap<K, V> cache;

    private CacheManager(ConcurrentMap<K, V> cache) {
        this.cache = cache;
    }

    public static <K, V> CacheManagerBuilder<K, V> newBuilder() {
        return new CacheManagerBuilder<>();
    }

    public V cache(K k, V v) {
        return cache.put(k, v);
    }

    public V cacheIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    public boolean containsKey(K k) {
        return cache.containsKey(k);
    }

    public boolean containsValue(V v) {
        return cache.containsValue(v);
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

    @Override
    public String toString() {
        return cache.toString();
    }

    public static final class CacheManagerBuilder<K, V> {

        private CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        public CacheManagerBuilder<K, V> initialCapacity(int initialCapacity) {
            cacheBuilder.initialCapacity(initialCapacity);
            return this;
        }

        public CacheManagerBuilder<K, V> concurrencyLevel(int concurrencyLevel) {
            cacheBuilder.concurrencyLevel(concurrencyLevel);
            return this;
        }

        public CacheManagerBuilder<K, V> expireAfterAccess(TimeProperty duration) {
            cacheBuilder.expireAfterAccess(duration.getTime(), duration.getUnit());
            return this;
        }

        public CacheManagerBuilder<K, V> expireAfterWrite(TimeProperty duration) {
            cacheBuilder.expireAfterWrite(duration.getTime(), duration.getUnit());
            return this;
        }

        public CacheManagerBuilder<K, V> maximumSize(long size) {
            cacheBuilder.maximumSize(size);
            return this;
        }

        public CacheManagerBuilder<K, V> maximumWeight(long size) {
            cacheBuilder.maximumWeight(size);
            return this;
        }

        public CacheManagerBuilder<K, V> recordStats() {
            cacheBuilder.recordStats();
            return this;
        }

        public CacheManagerBuilder<K, V> refreshAfterWrite(TimeProperty duration) {
            cacheBuilder.refreshAfterWrite(duration.getTime(), duration.getUnit());
            return this;
        }

        public CacheManagerBuilder<K, V> removalListener(RemovalListener<K, V> listener) {
            cacheBuilder.removalListener(listener);
            return this;
        }

        public CacheManagerBuilder<K, V> softValues() {
            cacheBuilder.softValues();
            return this;
        }

        public CacheManagerBuilder<K, V> softValues(Ticker ticker) {
            cacheBuilder.ticker(ticker);
            return this;
        }

        public CacheManagerBuilder<K, V> weakKeys() {
            cacheBuilder.weakKeys();
            return this;
        }

        public CacheManagerBuilder<K, V> weakValues() {
            cacheBuilder.weakValues();
            return this;
        }

        public CacheManagerBuilder<K, V> softValues(Weigher<K, V> weigher) {
            cacheBuilder.weigher(weigher);
            return this;
        }

        @Override
        public String toString() {
            return cacheBuilder.toString();
        }

        public <K1 extends K, V1 extends V> CacheManager<K1, V1> build() {
            return new CacheManager<>(cacheBuilder.<K1, V1>build().asMap());
        }

    }

}