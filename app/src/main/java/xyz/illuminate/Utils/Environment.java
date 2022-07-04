package xyz.illuminate.Utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.illuminate.app.App;

@SuppressLint("SdCardPath")
public final class Environment {

    public static final Map<String, String> IDE_PROPS = new HashMap<>();
    public static final Map<String, String> ENV_VARS = new HashMap<>();

    public static final String DEFAULT_ROOT = "/data/data/xyz.illuminate.mrcode/files";
    public static final String DEFAULT_HOME = DEFAULT_ROOT + "/home";
    private static final List<String> blacklist = new ArrayList<>();
    public static File ROOT;
    public static File PREFIX;
    public static File HOME;
    public static File TERMINAL_HOME;
    public static File TMP_DIR;
    public static File BIN_DIR;


    public static File SHELL;
    public static File LOGIN_SHELL;


    public static void init() {
        ROOT = App.applicationContext.getDataDir();
        PREFIX = mkdirIfNotExits(new File(App.applicationContext.getDataDir(), "usr"));
        HOME = mkdirIfNotExits(new File(App.applicationContext.getDataDir(), "home"));
        TERMINAL_HOME = mkdirIfNotExits(new File(HOME, ".terminal"));
        TMP_DIR = mkdirIfNotExits(new File(PREFIX, "tmp"));
        BIN_DIR = mkdirIfNotExits(new File(PREFIX, "bin"));


        SHELL = new File(BIN_DIR, "bash");
        LOGIN_SHELL = new File(BIN_DIR, "login");

        setExecutable(SHELL);

        System.setProperty("user.home", HOME.getAbsolutePath());
    }

    public static File mkdirIfNotExits(File in) {
        if (in != null && !in.exists()) {
            FileUtils.createOrExistsDir(in);
        }

        return in;
    }

    public static String readProp(String key, String defaultValue) {
        String value = IDE_PROPS.getOrDefault(key, defaultValue);
        if (value == null) {
            return defaultValue;
        }
        if (value.contains("$HOME")) {
            value = value.replace("$HOME", HOME.getAbsolutePath());
        }
        if (value.contains("$SYSROOT")) {
            value = value.replace("$SYSROOT", PREFIX.getAbsolutePath());
        }
        if (value.contains("$PATH")) {
            value = value.replace("$PATH", createPath());
        }
        return value;
    }

    @NonNull
    private static String createPath() {
        String path = "";
        path += String.format(":%s/bin", PREFIX.getAbsolutePath());
        path += String.format(":%s", System.getenv("PATH"));
        return path;
    }

    public static void setExecutable(@NonNull final File file) {
        if (!file.setExecutable(true)) {
            //  LOG.error("Unable to set executable permissions to file", file);
        }
    }


    public static Map<String, String> getEnvironment() {

        if (!ENV_VARS.isEmpty()) {
            return ENV_VARS;
        }

        ENV_VARS.put("HOME", HOME.getAbsolutePath());
        ENV_VARS.put("ANDROID_USER_HOME", HOME.getAbsolutePath() + "/.android");
        ENV_VARS.put("TMPDIR", TMP_DIR.getAbsolutePath());

        ENV_VARS.put("SYSROOT", PREFIX.getAbsolutePath());

        ENV_VARS.put("SHELL", SHELL.getAbsolutePath());
        ENV_VARS.put("CONFIG_SHELL", SHELL.getAbsolutePath());
        ENV_VARS.put("TERM", "screen");

        // If LD_LIBRARY_PATH is set, append $SYSROOT/lib to it,
        // else set it to $SYSROOT/lib
        String ld = System.getenv("LD_LIBRARY_PATH");
        if (ld == null || ld.trim().length() <= 0) {
            ld = "";
        } else {
            ld += File.pathSeparator;
        }
        ENV_VARS.put("LD_LIBRARY_PATH", ld);

        addToEnvIfPresent(ENV_VARS, "ANDROID_ART_ROOT");
        addToEnvIfPresent(ENV_VARS, "DEX2OATBOOTCLASSPATH");
        addToEnvIfPresent(ENV_VARS, "ANDROID_I18N_ROOT");
        addToEnvIfPresent(ENV_VARS, "ANDROID_RUNTIME_ROOT");
        addToEnvIfPresent(ENV_VARS, "ANDROID_TZDATA_ROOT");
        addToEnvIfPresent(ENV_VARS, "ANDROID_DATA");
        addToEnvIfPresent(ENV_VARS, "ANDROID_ROOT");

        String path = createPath();

        ENV_VARS.put("PATH", path);

        for (String key : IDE_PROPS.keySet()) {
            if (!blacklistedVariables().contains(key.trim())) {
                ENV_VARS.put(key, readProp(key, ""));
            }
        }

        return ENV_VARS;
    }

    public static void addToEnvIfPresent(Map<String, String> environment, String name) {
        String value = System.getenv(name);
        if (value != null) {
            environment.put(name, value);
        }
    }

    private static List<String> blacklistedVariables() {
        if (blacklist.isEmpty()) {
            blacklist.add("HOME");
            blacklist.add("SYSROOT");
            blacklist.add("JLS_HOME");
        }
        return blacklist;
    }
}
