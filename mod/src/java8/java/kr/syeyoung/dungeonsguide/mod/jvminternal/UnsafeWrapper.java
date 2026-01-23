package kr.syeyoung.dungeonsguide.mod.jvminternal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeWrapper {
    private static Unsafe unsafe;

    static {
        try {

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object allocateInstance(Class<?> clazz) throws InstantiationException {
        return unsafe.allocateInstance(clazz);
    }
}
