package com.example.myapplication.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class NavStateHelper {
    private static final String KEY_LAST_NAV_ID = "last_nav_id";

    public static void saveLastNavItem(Context context, int itemId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_LAST_NAV_ID, itemId)
                .apply();
    }

    public static int getLastNavItem(Context context, int defaultId) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_LAST_NAV_ID, defaultId);
    }
}