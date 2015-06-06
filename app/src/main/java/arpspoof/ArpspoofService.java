
package arpspoof;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

import gr.rambou.arpandroid.R;

public class ArpspoofService extends IntentService {

    private static final String TAG = "ArpspoofService";
    private static final int SHOW_SPOOFING = 1;
    protected static volatile boolean isSpoofing = false;
    private static volatile WifiManager.WifiLock wifiLock;
    private static volatile PowerManager.WakeLock wakeLock;
    private volatile Thread myThread;

    public ArpspoofService() {
        super("ArpspoofService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        String localBin = bundle.getString("localBin");
        String gateway = bundle.getString("gateway");
        String wifiInterface = bundle.getString("interface");
        String target = bundle.getString("target");
        if (target != null)
            target = " -t " + target;
        else
            target = "";
        final String command = localBin + " -i " + wifiInterface + target + " " + gateway;
        Notification notification = new Notification(R.drawable.icon, "now spoofing: " + gateway, System.currentTimeMillis());
        Intent launchActivity = new Intent(this, SpoofingActivity.class);
        launchActivity.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchActivity, 0);
        notification.setLatestEventInfo(this, "spoofing: " + gateway,
                "tap to open Arpspoof", pendingIntent);
        startForeground(SHOW_SPOOFING, notification);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
        wifiLock.acquire();
        wakeLock.acquire();
        try {
            myThread = new ExecuteCommand(command);
        } catch (IOException e) {
            Log.e(TAG, "error initializing arpspoof command", e);
        }
        myThread.setDaemon(true);
        isSpoofing = true;
        myThread.start();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "Spoofing was interrupted", e);
        }
        if (myThread != null)
            myThread = null;
    }

    @Override
    public void onDestroy() {
        Thread killAll;
        try {
            killAll = new ExecuteCommand("killall arpspoof");
            killAll.setDaemon(true);
            killAll.start();
            killAll.join();
        } catch (IOException e) {
            Log.w(TAG, "error initializing killall arpspoof command", e);
        } catch (InterruptedException e) {
            // don't care
        }

        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
        wifiLock.release();
        wakeLock.release();
        stopForeground(true);
        isSpoofing = false;
    }
}