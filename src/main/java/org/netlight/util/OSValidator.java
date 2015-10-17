package org.netlight.util;

/**
 * @author ahmad
 */
public final class OSValidator {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final boolean UNIX;
    private static final boolean SUN_OS;
    private static final boolean MAC;
    private static final boolean WIN;

    static {
        if (UNIX = OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            SUN_OS = false;
            MAC = false;
            WIN = false;
        } else if (SUN_OS = OS.contains("sunos")) {
            MAC = false;
            WIN = false;
        } else {
            WIN = !(MAC = OS.contains("mac")) && OS.contains("win");
        }
    }

    private OSValidator() {
    }

    public static boolean isUnix() {
        return UNIX;
    }

    public static boolean isSolaris() {
        return SUN_OS;
    }

    public static boolean isMac() {
        return MAC;
    }

    public static boolean isWindows() {
        return WIN;
    }

}
