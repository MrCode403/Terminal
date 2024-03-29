package xyz.illuminate.app;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.util.Arrays;

public class App extends Application {

    public static volatile Context applicationContext;

    public static String getArch() {
        if (isAarch64()) {
            return "arm64-v8a";
        } else if (isArmv7a()) {
            return "armeabi-v7a";
        }
        return null;
    }

    public static boolean isAbiSupported() {
        return isAarch64() || isArmv7a();
    }

    private static boolean isArmv7a() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains("armeabi-v7a");
    }

    private static boolean isAarch64() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains("arm64-v8a");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            applicationContext = getApplicationContext();
        } catch (Exception ignore) {
        }

        if (applicationContext == null) {
            applicationContext = getApplicationContext();
        }
    }
}
