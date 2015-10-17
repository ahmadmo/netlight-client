package org.netlight.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author ahmad
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isNull(Object o) {
        return o instanceof String ? isNull((String) o) : o != null;
    }

    public static boolean isNull(String s) {
        return s == null || (s = s.trim()).isEmpty() || s.equalsIgnoreCase("null");
    }

    public static boolean isNull(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNull(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean notNull(Object o) {
        return !isNull(o);
    }

    public static boolean notNull(String s) {
        return !isNull(s);
    }

    public static boolean notNull(Collection collection) {
        return !isNull(collection);
    }

    public static boolean notNull(Map map) {
        return !isNull(map);
    }

    public static <T> T getOrDefault(T t, T def) {
        return t == null ? def : t;
    }

    @SuppressWarnings("unchecked")
    public static <T> T castOrDefault(Object o, Class<T> type, T def) {
        return o == null || !type.isInstance(o) ? def : (T) o;
    }

}
