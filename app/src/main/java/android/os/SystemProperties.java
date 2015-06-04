package android.os;

public class SystemProperties
{
    private static native String native_get(String key, String def);
    public static String get(String key, String def) {
        return native_get(key, def);
    }
}
