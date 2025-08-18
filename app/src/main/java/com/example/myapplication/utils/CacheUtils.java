package com.example.myapplication.utils;

import android.content.Context;

import java.io.File;
import java.util.Locale;

public class CacheUtils {

    /**
     * 获取应用总缓存大小（单位：字节）
     */
    public static long getTotalCacheSize(Context context) {
        long size = 0;
        size += calculateSize(context.getCacheDir());          // 内部缓存
        size += calculateSize(context.getExternalCacheDir()); // 外部缓存
        size += calculateSize(new File(context.getFilesDir(), "custom_cache")); // 自定义缓存目录
        size += getSharedPrefsSize(context);                   // SharedPreferences
        size += getDatabaseSize(context);                      // 数据库
        return size;
    }

    /**
     * 获取可读的缓存大小字符串（自动转换单位）
     */
    public static String getReadableCacheSize(Context context) {
        long sizeBytes = getTotalCacheSize(context);
        return formatSize(sizeBytes);
    }

    // 转换字节数为友好格式（KB/MB/GB）
    private static String formatSize(long sizeBytes) {
        if (sizeBytes <= 0) return "0 KB";

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = sizeBytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex]);
    }

    // 计算 SharedPreferences 大小
    private static long getSharedPrefsSize(Context context) {
        long size = 0;
        File sharedPrefsDir = new File(context.getFilesDir().getParent() + "/shared_prefs");
        if (sharedPrefsDir.exists()) {
            size += calculateSize(sharedPrefsDir);
        }
        return size;
    }

    // 计算数据库大小
    private static long getDatabaseSize(Context context) {
        long size = 0;
        for (String dbName : context.databaseList()) {
            size += calculateSize(context.getDatabasePath(dbName));
        }
        return size;
    }
    public static long calculateSize(File file) {
        if (file == null || !file.exists()) {
            return 0L; // 文件不存在时返回0
        }

        if (file.isDirectory()) {
            long size = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    size += calculateSize(child); // 递归统计子文件
                }
            }
            return size;
        } else {
            return file.length(); // 直接返回文件大小
        }
    }

    /**
     * 清除所有应用缓存（内部+外部+自定义目录）
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());          // 内部缓存
        deleteDir(context.getExternalCacheDir());  // 外部缓存
        deleteDir(new File(context.getFilesDir(), "custom_cache")); // 自定义目录
        clearSharedPrefs(context);                 // SharedPreferences（可选）
        clearDatabases(context);                   // 数据库（可选）
    }

    /**
     * 递归删除目录/文件
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    /**
     * 清除SharedPreferences（谨慎使用！会清空所有存储数据）
     */
    private static void clearSharedPrefs(Context context) {
        File sharedPrefsDir = new File(context.getFilesDir().getParent() + "/shared_prefs");
        deleteDir(sharedPrefsDir);
    }

    /**
     * 清除数据库（谨慎使用！会删除所有本地数据库）
     */
    private static void clearDatabases(Context context) {
        for (String dbName : context.databaseList()) {
            context.deleteDatabase(dbName);
        }
    }
}