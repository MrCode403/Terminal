package com.illuminate.Utils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.illuminate.app.App;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ToolsManager {

    public static final int LOG_SENDER_VERSION = 2;
    public static final String KEY_LOG_SENDER_VERSION = "tools_logsenderVersion";
    public static String ARCH_SPECIFIC_ASSET_DATA_DIR = "data/" + App.getArch();
    public static String COMMON_ASSET_DATA_DIR = "data/common";

    public static void init(@NonNull App app, Runnable onFinish) {

        if (App.getArch() == null) {
            //LOG.error("Device not supported");
            return;
        }

        CompletableFuture.runAsync(
                        () -> {
                            copyBusyboxIfNeeded();
                            extractLogsenderIfNeeded();
                            extractAapt2();
                            //              extractLibHooks();
                            extractGradlePlugin();
                            extractToolingApi();
                            extractIdeEnv();
                            writeInitScript();
                        })
                .whenComplete(
                        (__, error) -> {
                            if (error != null) {
                                //LOG.error("Error extracting tools", error);
                            }

                            if (onFinish != null) {
                                onFinish.run();
                            }
                        });
    }

    private static void extractIdeEnv() {
        final var file = new File(Environment.BIN_DIR, "ideenv");
        if (file.exists()) {
            file.delete();
        }

        var contents = ResourceUtils.readAssets2String(getCommonAsset("ideenv"));
        contents = contents.replace("@PREFIX@", Environment.PREFIX.getAbsolutePath());
        FileIOUtils.writeFileFromString(file, contents);

        if (!file.canExecute()) {
            file.setExecutable(true);
        }
    }

    private static void copyBusyboxIfNeeded() {
        File exec = Environment.BUSYBOX;
        if (exec.exists()) return;
        Environment.mkdirIfNotExits(exec.getParentFile());
        ResourceUtils.copyFileFromAssets(getArchSpecificAsset("busybox"), exec.getAbsolutePath());
        if (!exec.canExecute()) {
            if (!exec.setExecutable(true)) {
                // LOG.error("Cannot set busybox executable permissions.");
            }
        }
    }

    @NonNull
    @Contract(pure = true)
    public static String getArchSpecificAsset(String name) {
        return ARCH_SPECIFIC_ASSET_DATA_DIR + "/" + name;
    }

    private static void extractLogsenderIfNeeded() {
    }

    @NonNull
    @Contract(pure = true)
    public static String getCommonAsset(String name) {
        return COMMON_ASSET_DATA_DIR + "/" + name;
    }

    private static void extractAapt2() {
        if (!Environment.AAPT2.exists()) {
            ResourceUtils.copyFileFromAssets(
                    getArchSpecificAsset("aapt2"), Environment.AAPT2.getAbsolutePath());
        }

        if (!Environment.AAPT2.canExecute() && !Environment.AAPT2.setExecutable(true)) {
            // LOG.error("Cannot set executable permissions to AAPT2 binary");
        }
    }

    private static void extractGradlePlugin() {
        final var repoDir = new File(Environment.ANDROIDIDE_HOME, "repo");
        FileUtils.createOrExistsDir(repoDir);

        final var zip = new File(Environment.TMP_DIR, "gradle-plugin.zip");
        if (zip.exists()) {
            FileUtils.delete(zip);
        }

        ResourceUtils.copyFileFromAssets(getCommonAsset("gradle-plugin.zip"), zip.getAbsolutePath());
        try {
            ZipUtils.unzipFile(zip, repoDir);
        } catch (Throwable e) {
            //LOG.error("Unable to extract gradle plugin zip file");
        }
    }

    private static void extractToolingApi() {
        if (Environment.TOOLING_API_JAR.exists()) {
            FileUtils.delete(Environment.TOOLING_API_JAR);
        }

        ResourceUtils.copyFileFromAssets(
                getCommonAsset("tooling-api-all.jar"), Environment.TOOLING_API_JAR.getAbsolutePath());
    }

    private static void writeInitScript() {
        if (Environment.INIT_SCRIPT.exists()) {
            FileUtils.delete(Environment.INIT_SCRIPT);
        }
        FileIOUtils.writeFileFromString(Environment.INIT_SCRIPT, readInitScript());
    }

    @NonNull
    private static String readInitScript() {
        return ResourceUtils.readAssets2String(getCommonAsset("androidide.init.gradle"));
    }

    public static void extractLibHooks() {
        if (!Environment.LIB_HOOK.exists()) {
            ResourceUtils.copyFileFromAssets(
                    getArchSpecificAsset("libhook.so"), Environment.LIB_HOOK.getAbsolutePath());
        }
    }
}
