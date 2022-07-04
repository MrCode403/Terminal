package xyz.illuminate.mrcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.KeyboardUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import xyz.illuminate.Utils.BootstrapInstaller;
import xyz.illuminate.Utils.ConstantsBridge;
import xyz.illuminate.Utils.Environment;
import xyz.illuminate.mrcode.R;
import xyz.illuminate.terminal.TerminalEmulator;
import xyz.illuminate.terminal.TerminalSession;
import xyz.illuminate.terminal.TerminalSessionClient;
import xyz.illuminate.terminal.TextStyle;
import xyz.illuminate.terminal.view.TerminalView;
import xyz.illuminate.terminal.view.TerminalViewClient;
import xyz.illuminate.terminal.view.virtualkeys.SpecialButton;
import xyz.illuminate.terminal.view.virtualkeys.VirtualKeyButton;
import xyz.illuminate.terminal.view.virtualkeys.VirtualKeysConstants;
import xyz.illuminate.terminal.view.virtualkeys.VirtualKeysInfo;
import xyz.illuminate.terminal.view.virtualkeys.VirtualKeysView;

public class MainActivity extends AppCompatActivity
        implements TerminalViewClient, TerminalSessionClient {

    public static final String KEY_WORKING_DIRECTORY = "terminal_workingDirectory";
    private static final byte[] SOURCES_LIST_CONTENT =
            "deb https://androidide.com/packages/apt/termux-main/ stable main".getBytes();
    private static final String KEY_FONT_SIZE = "terminal_fontSize";

    private TerminalView terminal;
    private TerminalSession session;
    private boolean isVisible = false;
    private KeyListener listener;
    private int MIN_FONT_SIZE;
    private int MAX_FONT_SIZE;
    private int DEFAULT_FONT_SIZE;

    LinearLayout root;
    VirtualKeysView virtualKeyTable;

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent e) {
        return false;
    }

    @Override
    public void onTextChanged(TerminalSession changedSession) {
        terminal.onScreenUpdated();
    }

    @Override
    public void onTitleChanged(TerminalSession changedSession) {
    }

    @Override
    public void onSessionFinished(TerminalSession finishedSession) {
        finish();
    }

    @Override
    public void onCopyTextToClipboard(TerminalSession session, String text) {
        ClipboardUtils.copyText("AndroidIDE Terminal", text);
    }

    @Override
    public void onPasteTextFromClipboard(TerminalSession session) {
        String clip = ClipboardUtils.getText().toString();
        if (clip.trim().length() > 0 && terminal != null && terminal.mEmulator != null) {
            terminal.mEmulator.paste(clip);
        }
    }

    @Override
    public void onBell(TerminalSession session) {
    }

    @Override
    public void onColorsChanged(TerminalSession session) {
    }

    @Override
    public void onTerminalCursorStateChange(boolean state) {
    }

    @Override
    public Integer getTerminalCursorStyle() {
        return TerminalEmulator.DEFAULT_TERMINAL_CURSOR_STYLE;
    }

    @Override
    public float onScale(float scale) {
        if (scale < 0.9f || scale > 1.1f) {
            boolean increase = scale > 1.f;
            changeFontSize(increase);
            return 1.0f;
        }
        return scale;
    }

    private void changeFontSize(final boolean increase) {
        int fontSize = getFontSize();
        fontSize += (increase ? 1 : -1) * 2;
        fontSize = Math.max(MIN_FONT_SIZE, Math.min(fontSize, MAX_FONT_SIZE));
        setFontSize(fontSize, true);
    }

    @Override
    public void onSingleTapUp(MotionEvent e) {
        KeyboardUtils.showSoftInput(terminal);
    }

    @Override
    public boolean shouldBackButtonBeMappedToEscape() {
        return false;
    }

    @Override
    public boolean shouldEnforceCharBasedInput() {
        return true;
    }

    @Override
    public boolean shouldUseCtrlSpaceWorkaround() {
        return false;
    }

    @Override
    public boolean isTerminalViewSelected() {
        return true;
    }

    @Override
    public void copyModeChanged(boolean copyMode) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && !session.isRunning()) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongPress(MotionEvent event) {
        return false;
    }

    @Override
    public boolean readControlKey() {
        Boolean state = virtualKeyTable.readSpecialButton(SpecialButton.CTRL, true);
        return state != null && state;
    }

    @Override
    public boolean readAltKey() {
        Boolean state = virtualKeyTable.readSpecialButton(SpecialButton.ALT, true);
        return state != null && state;
    }

    @Override
    public boolean readShiftKey() {
        return false;
    }

    @Override
    public boolean readFnKey() {
        return false;
    }

    @Override
    public boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session) {
        return false;
    }

    @Override
    public void onEmulatorSet() {
        setTerminalCursorBlinkingState(true);

        if (session != null) {
            root.setBackgroundColor(session.getEmulator().mColors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]);
        }
    }

    @Override
    public void logError(String tag, String message) {
    }

    @Override
    public void logWarn(String tag, String message) {
    }

    @Override
    public void logInfo(String tag, String message) {
    }

    @Override
    public void logDebug(String tag, String message) {
    }

    @Override
    public void logVerbose(String tag, String message) {
    }

    @Override
    public void logStackTraceWithMessage(String tag, String message, Exception e) {
    }

    @Override
    public void logStackTrace(String tag, Exception e) {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        virtualKeyTable = findViewById(R.id.virtual_key_table);
        Environment.init();
        final File bash = new File(Environment.BIN_DIR, "bash");
        final boolean useSystemShell = getSharedPreferences("config", Activity.MODE_PRIVATE).getBoolean("KEY_TERMINAL_USE_SYSTEM_SHELL", false);
        if ((Environment.PREFIX.exists()
                && Environment.PREFIX.isDirectory()
                && bash.exists()
                && bash.isFile()
                && bash.canExecute())
                || useSystemShell) {
            setupTerminalView();
        } else {
            // LOG.info("Bootstrap is not installed.");

            // Show the progress sheet
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Please wait!");
            progress.setCancelable(false);
            progress.show();

            //LOG.debug("Starting installation...");
            // Install bootstrap asynchronously
            final var future =
                    BootstrapInstaller.doInstall(
                            this, message -> runOnUiThread(() -> progress.setMessage(message)));

            future.whenComplete(
                    (voidResult, throwable) -> {
                        // LOG.debug("Completable future has been complete.", throwable);

                        runOnUiThread(
                                () -> {
                                    progress.dismiss();

                                    if (future.isCompletedExceptionally() || throwable != null) {
                                        // LOG.error("Future has been completed exceptionally.");
                                        showInstallationError(throwable);
                                        Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    setupTerminalView();
                                });
                    });
        }
    }

    private void showInstallationError(Throwable throwable) {
        virtualKeyTable.setVisibility(View.GONE);
    }

    private void setupTerminalView() {
        setFontVariables();
        terminal = new TerminalView(this, null);
        terminal.setTerminalViewClient(this);
        terminal.attachSession(createSession(getWorkingDirectory()));
        terminal.setKeepScreenOn(true);
        terminal.setTextSize(getFontSize());
        terminal.setTypeface(Typeface.createFromAsset(getAssets(), "font/jetbrains-mono.ttf"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, 0);
        params.weight = 1f;

        root.addView(terminal, 0, params);
        try {
            virtualKeyTable.setVirtualKeysViewClient(getKeyListener());
            virtualKeyTable.reload(
                    new VirtualKeysInfo(
                            ConstantsBridge.VIRTUAL_KEYS, "", VirtualKeysConstants.CONTROL_CHARS_ALIASES));
        } catch (JSONException e) {
            // LOG.error("Unable to parse terminal virtual keys json data", e);
        }
    }

    @NonNull
    private String getWorkingDirectory() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(KEY_WORKING_DIRECTORY)) {
            String directory = extras.getString(KEY_WORKING_DIRECTORY, null);
            if (directory == null || directory.trim().length() <= 0) {
                directory = Environment.HOME.getAbsolutePath();
            }
            return directory;
        }
        return Environment.HOME.getAbsolutePath();
    }

    private KeyListener getKeyListener() {
        return listener == null ? listener = new KeyListener(terminal) : listener;
    }

    private TerminalSession createSession(final String workingDirectory) {
        final Map<String, String> environment = Environment.getEnvironment();
        final String[] env = new String[environment.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            env[i] = entry.getKey() + "=" + entry.getValue();
            i++;
        }

        session =
                new TerminalSession(
                        getShellPath(), // Shell command
                        workingDirectory, // Working directory
                        new String[]{}, // Arguments
                        env, // Environment variables
                        TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS, // Transcript rows
                        this // TerminalSessionClient
                );

        try {
            final File file = new File(Environment.PREFIX, "etc/apt/sources.list");
            final FileOutputStream out = new FileOutputStream(file);
            out.write(SOURCES_LIST_CONTENT);
            out.flush();
            out.close();
        } catch (Throwable th) {
            // LOG.error("Unable to update sources.list", th);
        }

        return session;
    }

    @NonNull
    private String getShellPath() {
        final boolean useSystemShell = getSharedPreferences("config", Activity.MODE_PRIVATE).getBoolean("KEY_TERMINAL_USE_SYSTEM_SHELL", false);
        if (!useSystemShell && Environment.LOGIN_SHELL.exists() && Environment.LOGIN_SHELL.isFile()) {
            return Environment.LOGIN_SHELL.getAbsolutePath();
        }

        if (!useSystemShell) {
            // LOG.error("Default shell does not exist. Falling back to '/system/bin/sh'.", "This should not happen in normal circumstances.");
        }

        return "/system/bin/sh";
    }

    public void setFontVariables() {
        int[] sizes = getDefaultFontSizes();

        DEFAULT_FONT_SIZE = sizes[0];
        MIN_FONT_SIZE = sizes[1];
        MAX_FONT_SIZE = sizes[2];
    }

    // https://github.com/termux/termux-app/blob/82b15803126138eef8899e0c7b582713f872cd09/termux-shared/src/main/java/com/termux/shared/termux/settings/preferences/TermuxAppSharedPreferences.java
    private int[] getDefaultFontSizes() {
        float dipInPixels =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        int[] sizes = new int[3];

        // This is a bit arbitrary and sub-optimal. We want to give a sensible default for minimum font
        // size
        // to prevent invisible text due to zoom be mistake:
        sizes[1] = (int) (4f * dipInPixels); // min

        // http://www.google.com/design/spec/style/typography.html#typography-line-height
        int defaultFontSize = Math.round(9 * dipInPixels);
        // Make it divisible by 2 since that is the minimal adjustment step:
        if (defaultFontSize % 2 == 1) defaultFontSize--;

        sizes[0] = defaultFontSize; // default

        sizes[2] = 256; // max

        if (getSharedPreferences("config", Activity.MODE_PRIVATE).getString(KEY_FONT_SIZE, "<not_available>").equals("<not_available>")) {
            setFontSize(defaultFontSize, false);
        }

        return sizes;
    }

    public void setFontSize(int value, boolean apply) {
        getSharedPreferences("config", Activity.MODE_PRIVATE).edit().putString(KEY_FONT_SIZE, String.valueOf(value)).apply();

        if (apply) {
            terminal.setTextSize(getFontSize());
        }
    }

    public int getFontSize() {
        int fontSize;
        try {
            fontSize =
                    Integer.parseInt(
                            getSharedPreferences("config", Activity.MODE_PRIVATE).getString(KEY_FONT_SIZE, String.valueOf(DEFAULT_FONT_SIZE)));
        } catch (NumberFormatException | ClassCastException e) {
            fontSize = DEFAULT_FONT_SIZE;
        }

        return Math.min(Math.max(fontSize, MIN_FONT_SIZE), MAX_FONT_SIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTerminalCursorBlinkingState(true);
    }

    private void setTerminalCursorBlinkingState(boolean start) {
        if (terminal != null && terminal.mEmulator != null) {
            terminal.setTerminalCursorBlinkerState(start, true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
        setTerminalCursorBlinkingState(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static final class KeyListener implements VirtualKeysView.IVirtualKeysView {

        private final TerminalView terminal;

        public KeyListener(TerminalView terminal) {
            this.terminal = terminal;
        }

        @Override
        public void onVirtualKeyButtonClick(View view, VirtualKeyButton buttonInfo, Button button) {
            if (terminal == null) {
                return;
            }
            if (buttonInfo.isMacro()) {
                String[] keys = buttonInfo.getKey().split(" ");
                boolean ctrlDown = false;
                boolean altDown = false;
                boolean shiftDown = false;
                boolean fnDown = false;
                for (String key : keys) {
                    if (SpecialButton.CTRL.getKey().equals(key)) {
                        ctrlDown = true;
                    } else if (SpecialButton.ALT.getKey().equals(key)) {
                        altDown = true;
                    } else if (SpecialButton.SHIFT.getKey().equals(key)) {
                        shiftDown = true;
                    } else if (SpecialButton.FN.getKey().equals(key)) {
                        fnDown = true;
                    } else {
                        onTerminalExtraKeyButtonClick(key, ctrlDown, altDown, shiftDown, fnDown);
                        ctrlDown = false;
                        altDown = false;
                        shiftDown = false;
                        fnDown = false;
                    }
                }
            } else {
                onTerminalExtraKeyButtonClick(buttonInfo.getKey(), false, false, false, false);
            }
        }

        private void onTerminalExtraKeyButtonClick(
                String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
            if (VirtualKeysConstants.PRIMARY_KEY_CODES_FOR_STRINGS.containsKey(key)) {
                Integer keyCode = VirtualKeysConstants.PRIMARY_KEY_CODES_FOR_STRINGS.get(key);
                if (keyCode == null) {
                    return;
                }
                int metaState = 0;
                if (ctrlDown) {
                    metaState |= KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
                }
                if (altDown) {
                    metaState |= KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
                }
                if (shiftDown) {
                    metaState |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
                }
                if (fnDown) {
                    metaState |= KeyEvent.META_FUNCTION_ON;
                }

                KeyEvent keyEvent = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState);
                terminal.onKeyDown(keyCode, keyEvent);
            } else {
                // not a control char
                for (int off = 0; off < key.length(); ) {
                    int codePoint = key.codePointAt(off);
                    terminal.inputCodePoint(codePoint, ctrlDown, altDown);
                    off += Character.charCount(codePoint);
                }
            }
        }

        @Override
        public boolean performVirtualKeyButtonHapticFeedback(
                View view, VirtualKeyButton buttonInfo, Button button) {
            // No need to handle this
            // VirtualKeysView will take care of performing haptic feedback
            return false;
        }
    }
}
