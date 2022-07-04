package xyz.illuminate.Utils;

public class ConstantsBridge {

  public static final String VIRTUAL_KEYS =
      "["
          + "\n  ["
          + "\n    \"ESC\","
          + "\n    {"
          + "\n      \"key\": \"/\","
          + "\n      \"popup\": \"\\\\\""
          + "\n    },"
          + "\n    {"
          + "\n      \"key\": \"-\","
          + "\n      \"popup\": \"|\""
          + "\n    },"
          + "\n    \"HOME\","
          + "\n    \"UP\","
          + "\n    \"END\","
          + "\n    \"PGUP\""
          + "\n  ],"
          + "\n  ["
          + "\n    \"TAB\","
          + "\n    \"CTRL\","
          + "\n    \"ALT\","
          + "\n    \"LEFT\","
          + "\n    \"DOWN\","
          + "\n    \"RIGHT\","
          + "\n    \"PGDN\""
          + "\n  ]"
          + "\n]";
  public static boolean EDITOR_PREF_SIZE_CHANGED = false;
  public static boolean EDITOR_PREF_LIGATURES_CHANGED = false;
  public static boolean EDITOR_PREF_FLAGS_CHANGED = false;
  public static boolean EDITOR_PREF_DRAW_HEX_CHANGED = false;
  public static boolean EDITOR_PREF_VISIBLE_PASSWORD_CHANGED = false;
  public static boolean EDITOR_PREF_WORD_WRAP_CHANGED = false;
  public static boolean EDITOR_PREF_USE_MAGNIFIER_CHANGED = false;

  public static boolean CLASS_LOAD_SUCCESS = true;
  public static boolean SPLASH_TO_MAIN = false;
}
