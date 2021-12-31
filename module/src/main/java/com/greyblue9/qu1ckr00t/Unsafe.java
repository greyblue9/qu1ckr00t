package com.greyblue9.qu1ckr00t;

@SuppressWarnings("unchecked")
public final class Unsafe {
    public static <T> T unsafeCast(Object obj) {
        if (obj == null)
            return null;

        return (T) obj;
    }
}
