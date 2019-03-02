package in.slanglabs;

import java.util.HashMap;
import java.util.Locale;

import in.slanglabs.platform.SlangLocale;

public class RNSlangLocaleMap {
    public static final String LOCALE_ENGLISH_IN = "LOCALE_ENGLISH_IN";
    public static final String LOCALE_HINDI_IN = "LOCALE_HINDI_IN";
    public static final String LOCALE_ENGLISH_US = "LOCALE_ENGLISH_US";

    private static final HashMap<String, Locale> sLocaleMap;

    static {
        sLocaleMap = new HashMap<>();
        sLocaleMap.put(LOCALE_ENGLISH_IN, SlangLocale.LOCALE_ENGLISH_IN);
        sLocaleMap.put(LOCALE_HINDI_IN, SlangLocale.LOCALE_HINDI_IN);
        sLocaleMap.put(LOCALE_ENGLISH_US, SlangLocale.LOCALE_ENGLISH_US);
    }

    public static Locale getSlangLocale(String locale) {
        if (locale == null || sLocaleMap.containsKey(locale)) {
            return SlangLocale.LOCALE_ENGLISH_IN; // Default
        }
        return sLocaleMap.get(locale);
    }
}
