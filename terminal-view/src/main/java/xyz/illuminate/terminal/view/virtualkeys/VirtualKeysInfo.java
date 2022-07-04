package xyz.illuminate.terminal.view.virtualkeys;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.illuminate.terminal.view.virtualkeys.VirtualKeysConstants.EXTRA_KEY_DISPLAY_MAPS;

public class VirtualKeysInfo {

    /**
     * Matrix of buttons to be displayed in {@link VirtualKeysView}.
     */
    private final VirtualKeyButton[][] mButtons;

    /**
     * Initialize {@link VirtualKeysInfo}.
     *
     * @param propertiesInfo   The {@link String} containing the info to create the {@link
     *                         VirtualKeysInfo}. Check the class javadoc for details.
     * @param style            The style to pass to {@link #getCharDisplayMapForStyle(String)} to get the {@link
     *                         VirtualKeysConstants.VirtualKeyDisplayMap} that defines the display text mapping for the
     *                         keys if a custom value is not defined by {@link VirtualKeyButton#KEY_DISPLAY_NAME} for a
     *                         key.
     * @param extraKeyAliasMap The {@link VirtualKeysConstants.VirtualKeyDisplayMap} that defines the
     *                         aliases for the actual key names. You can create your own or optionally pass {@link
     *                         VirtualKeysConstants#CONTROL_CHARS_ALIASES}.
     */
    public VirtualKeysInfo(
            @NonNull String propertiesInfo,
            String style,
            @NonNull VirtualKeysConstants.VirtualKeyDisplayMap extraKeyAliasMap)
            throws JSONException {
        mButtons =
                initVirtualKeysInfo(propertiesInfo, getCharDisplayMapForStyle(style), extraKeyAliasMap);
    }

    private VirtualKeyButton[][] initVirtualKeysInfo(
            @NonNull String propertiesInfo,
            @NonNull VirtualKeysConstants.VirtualKeyDisplayMap extraKeyDisplayMap,
            @NonNull VirtualKeysConstants.VirtualKeyDisplayMap extraKeyAliasMap)
            throws JSONException {
        // Convert String propertiesInfo to Array of Arrays
        JSONArray arr = new JSONArray(propertiesInfo);
        Object[][] matrix = new Object[arr.length()][];
        for (int i = 0; i < arr.length(); i++) {
            JSONArray line = arr.getJSONArray(i);
            matrix[i] = new Object[line.length()];
            for (int j = 0; j < line.length(); j++) {
                matrix[i][j] = line.get(j);
            }
        }

        // convert matrix to buttons
        VirtualKeyButton[][] buttons = new VirtualKeyButton[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            buttons[i] = new VirtualKeyButton[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                Object key = matrix[i][j];

                JSONObject jobject = normalizeKeyConfig(key);

                VirtualKeyButton button;

                if (!jobject.has(VirtualKeyButton.KEY_POPUP)) {
                    // no popup
                    button = new VirtualKeyButton(jobject, extraKeyDisplayMap, extraKeyAliasMap);
                } else {
                    // a popup
                    JSONObject popupJobject = normalizeKeyConfig(jobject.get(VirtualKeyButton.KEY_POPUP));
                    VirtualKeyButton popup =
                            new VirtualKeyButton(popupJobject, extraKeyDisplayMap, extraKeyAliasMap);
                    button = new VirtualKeyButton(jobject, popup, extraKeyDisplayMap, extraKeyAliasMap);
                }

                buttons[i][j] = button;
            }
        }

        return buttons;
    }

    /**
     * Convert "value" -> {"key": "value"}. Required by {@link
     * VirtualKeyButton#VirtualKeyButton(JSONObject, VirtualKeyButton,
     * VirtualKeysConstants.VirtualKeyDisplayMap, VirtualKeysConstants.VirtualKeyDisplayMap)}.
     */
    private static JSONObject normalizeKeyConfig(Object key) throws JSONException {
        JSONObject jobject;
        if (key instanceof String) {
            jobject = new JSONObject();
            jobject.put(VirtualKeyButton.KEY_KEY_NAME, key);
        } else if (key instanceof JSONObject) {
            jobject = (JSONObject) key;
        } else {
            throw new JSONException("An key in the extra-key matrix must be a string or an object");
        }
        return jobject;
    }

    @NonNull
    public static VirtualKeysConstants.VirtualKeyDisplayMap getCharDisplayMapForStyle(String style) {
        switch (style) {
            case "arrows-only":
                return EXTRA_KEY_DISPLAY_MAPS.ARROWS_ONLY_CHAR_DISPLAY;
            case "arrows-all":
                return EXTRA_KEY_DISPLAY_MAPS.LOTS_OF_ARROWS_CHAR_DISPLAY;
            case "all":
                return EXTRA_KEY_DISPLAY_MAPS.FULL_ISO_CHAR_DISPLAY;
            case "none":
                return new VirtualKeysConstants.VirtualKeyDisplayMap();
            default:
                return EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY;
        }
    }

    /**
     * Initialize {@link VirtualKeysInfo}.
     *
     * @param propertiesInfo     The {@link String} containing the info to create the {@link
     *                           VirtualKeysInfo}. Check the class javadoc for details.
     * @param extraKeyDisplayMap The {@link VirtualKeysConstants.VirtualKeyDisplayMap} that defines
     *                           the display text mapping for the keys if a custom value is not defined by {@link
     *                           VirtualKeyButton#KEY_DISPLAY_NAME} for a key. You can create your own or optionally pass
     *                           one of the values defined in {@link #getCharDisplayMapForStyle(String)}.
     * @param extraKeyAliasMap   The {@link VirtualKeysConstants.VirtualKeyDisplayMap} that defines the
     *                           aliases for the actual key names. You can create your own or optionally pass {@link
     *                           VirtualKeysConstants#CONTROL_CHARS_ALIASES}.
     */
    public VirtualKeysInfo(
            @NonNull String propertiesInfo,
            @NonNull VirtualKeysConstants.VirtualKeyDisplayMap extraKeyDisplayMap,
            @NonNull VirtualKeysConstants.VirtualKeyDisplayMap extraKeyAliasMap)
            throws JSONException {
        mButtons = initVirtualKeysInfo(propertiesInfo, extraKeyDisplayMap, extraKeyAliasMap);
    }

    public VirtualKeyButton[][] getMatrix() {
        return mButtons;
    }
}
