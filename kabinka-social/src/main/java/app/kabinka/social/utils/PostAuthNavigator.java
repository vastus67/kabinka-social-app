package app.kabinka.social.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Helper class to determine the correct post-authentication target activity.
 * This allows kabinka-frontend to redirect to its own MainActivity after OAuth,
 * while kabinka-social standalone can still use its legacy MainActivity.
 */
public class PostAuthNavigator {
    private static final String TAG = "PostAuthNavigator";
    private static final String META_DATA_KEY = "kabinka.post_auth_activity";
    private static final String DEFAULT_ACTIVITY = "app.kabinka.social.MainActivity";
    
    /**
     * Creates an Intent to the configured post-auth activity.
     * Reads from AndroidManifest meta-data key "kabinka.post_auth_activity".
     * If not found, defaults to app.kabinka.social.MainActivity for legacy compatibility.
     * 
     * @param context Android context
     * @param flags Intent flags to set (e.g. FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)
     * @return Intent to the target activity
     */
    public static Intent createPostAuthIntent(Context context, int flags) {
        String targetActivityClassName = getTargetActivityClassName(context);
        
        try {
            Class<?> targetClass = Class.forName(targetActivityClassName);
            Intent intent = new Intent(context, targetClass);
            if (flags != 0) {
                intent.addFlags(flags);
            }
            Log.d(TAG, "Post-auth navigation target: " + targetActivityClassName);
            return intent;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to find activity class: " + targetActivityClassName + ", falling back to default", e);
            // Fallback to default MainActivity
            try {
                Class<?> defaultClass = Class.forName(DEFAULT_ACTIVITY);
                Intent intent = new Intent(context, defaultClass);
                if (flags != 0) {
                    intent.addFlags(flags);
                }
                return intent;
            } catch (ClassNotFoundException ex) {
                // This should never happen as DEFAULT_ACTIVITY is in kabinka-social
                throw new RuntimeException("Cannot find default MainActivity", ex);
            }
        }
    }
    
    /**
     * Gets the target activity class name from manifest meta-data or returns default.
     */
    private static String getTargetActivityClassName(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            
            Bundle metaData = appInfo.metaData;
            if (metaData != null && metaData.containsKey(META_DATA_KEY)) {
                String targetActivity = metaData.getString(META_DATA_KEY);
                if (targetActivity != null && !targetActivity.isEmpty()) {
                    Log.d(TAG, "Found post-auth target in meta-data: " + targetActivity);
                    return targetActivity;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to read meta-data", e);
        }
        
        Log.d(TAG, "No meta-data found, using default: " + DEFAULT_ACTIVITY);
        return DEFAULT_ACTIVITY;
    }
}
