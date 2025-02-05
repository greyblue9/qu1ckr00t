package com.greyblue9.qu1ckr00t;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Binder;
import android.util.Log;

import com.greyblue9.qu1ckr00t.shared.Constants;
import com.greyblue9.qu1ckr00t.shared.IQu1ckr00t;

import $android.app.ActivityManager;
import $android.os.UserManager;

import static com.greyblue9.qu1ckr00t.shared.Constants.TAG;

public class WhitelistService extends IQu1ckr00t.Stub {
    @Override
    public int version() {
        return Constants.SERVICE_VERSION;
    }

    @Override
    public String[] queryPackages() {
        return DataStore.instance.queryPackages().toArray(new String[0]);
    }

    @Override
    public void addPackage(String packageName) {
        DataStore.instance.addPackage(packageName);

        final long token = Binder.clearCallingIdentity();

        try {
            forceStopPackage(packageName);
        } catch (Throwable throwable) {
            Log.w(TAG, "Force stop package " + packageName + ": " + throwable, throwable);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void removePackage(String packageName) {
        DataStore.instance.removePackage(packageName);

        final long token = Binder.clearCallingIdentity();

        try {
            forceStopPackage(packageName);
        } catch (Throwable throwable) {
            Log.w(TAG, "Force stop package " + packageName + ": " + throwable, throwable);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void forceStopPackage(String packageName) {
        final ActivityThread thread = ActivityThread.currentActivityThread();
        if (thread == null)
            throw new IllegalStateException("System unavailable");

        final Context context = thread.getSystemContext();
        if (context == null)
            throw new IllegalStateException("System unavailable");

        final UserManager userManager = context.getSystemService(UserManager.class);
        if (userManager == null)
            throw new IllegalStateException("UserManager unavailable");

        final ActivityManager activityManager = context.getSystemService(ActivityManager.class);
        if (activityManager == null)
            throw new IllegalStateException("ActivityManager unavailable");

        userManager.getUsers().forEach((u) -> activityManager.forceStopPackageAsUser(packageName, u.id));
    }
}
