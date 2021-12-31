package com.greyblue9.qu1ckr00t;

import android.content.Context;
import android.content.IClipboard;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import static com.greyblue9.qu1ckr00t.shared.Constants.TAG;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class Injector extends ServiceProxy {
    public static void inject(String argument) {
        Log.i(TAG, String.format("Uid = %d Pid = %d", Process.myUid(), Process.myPid()));

        Injector injector = new Injector();

        try {
            injector.install();

            Log.i(TAG, "Inject successfully");
        } catch (Exception e) {
            Log.e(TAG, "Inject failure", e);
        }
    }

    @Override
    protected IBinder onAddService(String name, IBinder service) {
        if (Context.CLIPBOARD_SERVICE.equals(name)) {
            try {
                return ProxyFactory.instance(service, new ClipboardProxy(IClipboard.Stub.asInterface(service)));
            } catch (Exception e) {
                Log.e(TAG, "Proxy ClipboardManager failure", e);
            }
        }

        return super.onAddService(name, service);
    }
}
