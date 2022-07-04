/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
package xyz.illuminate.terminal.Utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.illuminate.terminal.app.App;

@SuppressLint("SdCardPath")
public final class Environment {

    public static final Map<String, String> IDE_PROPS = new HashMap<>();
    public static final Map<String, String> ENV_VARS = new HashMap<>();

    public static final String DEFAULT_ROOT = "/data/data/com.itsaky.androidide/files";
    public static final String DEFAULT_HOME = DEFAULT_ROOT + "/home";
    private static final String DEFAULT_ANDROID_HOME = DEFAULT_HOME + "/android-sdk";
    public static final String DEFAULT_PREFIX = DEFAULT_ROOT + "/usr";
    private static final String DEFAULT_JAVA_HOME = DEFAULT_PREFIX + "/opt/openjdk";
    private static final List<String> blacklist = new ArrayList<>();
    public static File ROOT;
    public static File PREFIX;
    public static File HOME;
    public static File ANDROIDIDE_HOME;
    public static File JAVA_HOME;
    public static File ANDROID_HOME;
    public static File TMP_DIR;
    public static File BIN_DIR;
    public static File LIB_DIR;

    public static File IDE_PROPS_FILE;
    public static File LIB_HOOK;
    /**
     * JDK modules used by the java language server for completions. This version of JDK modules
     * contains only the classes included in android.jar
     */
    public static File COMPILER_MODULE;

    public static File TOOLING_API_JAR;

    public static File INIT_SCRIPT;
    public static File PROJECT_DATA_FILE;
    public static File GRADLE_PROPS;
    public static File GRADLE_USER_HOME;
    public static File AAPT2;
    public static File JAVA;
    public static File BUSYBOX;
    public static File SHELL;
    public static File LOGIN_SHELL;
    public static File BOOTCLASSPATH;

    public static void init() {
        ROOT = App.applicationContext.getDataDir();
        PREFIX = mkdirIfNotExits(new File(App.applicationContext.getDataDir(), "usr"));
        HOME = mkdirIfNotExits(new File(App.applicationContext.getDataDir(), "home"));
        ANDROIDIDE_HOME = mkdirIfNotExits(new File(HOME, ".terminal"));
        TMP_DIR = mkdirIfNotExits(new File(PREFIX, "tmp"));
        BIN_DIR = mkdirIfNotExits(new File(PREFIX, "bin"));
        LIB_DIR = mkdirIfNotExits(new File(PREFIX, "lib"));

        // If JDK 17 is not installed, check for any JDK in home directory
        if (!JAVA_HOME.exists()) {
            final var java_home = new File(HOME, "jdk");
            if (java_home.exists()) {
                JAVA_HOME = java_home;
            }
        }

        JAVA = new File(JAVA_HOME, "bin/java");
        BUSYBOX = new File(BIN_DIR, "busybox");
        SHELL = new File(BIN_DIR, "bash");
        LOGIN_SHELL = new File(BIN_DIR, "login");

        setExecutable(JAVA);
        setExecutable(BUSYBOX);
        setExecutable(SHELL);

        System.setProperty("user.home", HOME.getAbsolutePath());
        System.setProperty("java.home", JAVA_HOME.getAbsolutePath());
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
        path += String.format("%s/bin", JAVA_HOME.getAbsolutePath());
        path += String.format(":%s/cmdline-tools/latest/bin", ANDROID_HOME.getAbsolutePath());
        path += String.format(":%s/bin", PREFIX.getAbsolutePath());
        path += String.format(":%s", System.getenv("PATH"));
        return path;
    }

    public static void setExecutable(@NonNull final File file) {
        if (!file.setExecutable(true)) {
            //  LOG.error("Unable to set executable permissions to file", file);
        }
    }

    public static void setBootClasspath(@NonNull File file) {
        BOOTCLASSPATH = new File(file.getAbsolutePath());
    }

    public static Map<String, String> getEnvironment() {

        if (!ENV_VARS.isEmpty()) {
            return ENV_VARS;
        }

        ENV_VARS.put("HOME", HOME.getAbsolutePath());
        ENV_VARS.put("ANDROID_HOME", ANDROID_HOME.getAbsolutePath());
        ENV_VARS.put("ANDROID_SDK_ROOT", ANDROID_HOME.getAbsolutePath());
        ENV_VARS.put("ANDROID_USER_HOME", HOME.getAbsolutePath() + "/.android");
        ENV_VARS.put("JAVA_HOME", JAVA_HOME.getAbsolutePath());
        ENV_VARS.put("GRADLE_USER_HOME", GRADLE_USER_HOME.getAbsolutePath());
        ENV_VARS.put("TMPDIR", TMP_DIR.getAbsolutePath());

        ENV_VARS.put("SYSROOT", PREFIX.getAbsolutePath());

        ENV_VARS.put("BUSYBOX", BUSYBOX.getAbsolutePath());
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
        ld += LIB_DIR.getAbsolutePath();
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
