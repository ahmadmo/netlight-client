package org.netlight.util;

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

    public static boolean notNull(Object o) {
        return !isNull(o);
    }

    public static boolean notNull(String s) {
        return !isNull(s);
    }

    @SuppressWarnings("unchecked")
    public static <T> T castOrDefault(Object o, Class<T> type, T def) {
        return o == null || !type.isInstance(o) ? def : (T) o;
    }

}
