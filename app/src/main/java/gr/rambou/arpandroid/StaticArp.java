package gr.rambou.arpandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StaticArp extends BroadcastReceiver {

    private static final String TAG = "StaticArp";
    private final Handler handler; // Handler used to execute code on the UI thread
    private View rootView;
    private boolean rooted;

    public StaticArp(Handler handler, boolean rooted) {
        this.handler = handler;
        this.rooted = rooted;
    }

    /*
    * This is called when Wifi state is changed
    */
    @Override
    public void onReceive(Context context, Intent intent) {

        final Context mContext = context;
        rootView = ((MainActivity) context).getWindow().getDecorView().findViewById(android.R.id.content);

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        Log.d(TAG, (new StringBuilder("onReceive() intent: ")).append(intent).toString());

        /*
        * Class that is
        */
        class SetStaticArp extends AsyncTask<Void, Void, Boolean> {

            private static final String TAG = "SetTask";
            private General Utilities = new General();
            private String interfaceName;
            private String gatewayIP;
            private String gatewayMAC;

            protected Boolean doInBackground(Void... params) {

                interfaceName = android.os.SystemProperties.get("wifi.interface", "unknown");
                if (interfaceName.equals("unknown")) {
                    Log.v(TAG, "Aborting: can't get wifi interface name");
                    return false;
                }

                gatewayIP = android.os.SystemProperties.get("dhcp." + interfaceName + ".gateway", "unknown");
                if (gatewayIP.equals("unknown")) {
                    Log.v(TAG, "Aborting: can't get gateway IP address");
                    return false;
                }

                // make sure the gateway mac is in the arp cache
                try {
                    InetAddress.getByName(gatewayIP).isReachable(4000);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                gatewayMAC = Utilities.getMacFromArpCache(gatewayIP);
                if (gatewayMAC == null || gatewayMAC.equals("00:00:00:00:00:00")) {
                    // retry getting the gateway mac address
                    Log.v(TAG, "RETRYING: " + interfaceName + " " + gatewayIP + " " + gatewayMAC);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Utilities.exec("ping -c 2 -w 2 " + gatewayIP);
                    gatewayMAC = Utilities.getMacFromArpCache(gatewayIP);
                }

                if (gatewayMAC == null || gatewayMAC.equals("00:00:00:00:00:00")) {
                    return false;
                }

                return true;
            }

            protected void onPostExecute(Boolean result) {

                int out = 0;
                if (result == true) {
                    //RootTools.useRoot = rooted;
                    if (RootTools.isAccessGiven()) {
                        /*
                        *   All the magic is happening here.
                        *   Setting the Static Mac on ARP Table
                         */
                        Utilities.exec("ip neighbor add " + gatewayIP + " lladdr " + gatewayMAC + " nud permanent dev " + interfaceName);
                        Utilities.exec("ip neighbor change " + gatewayIP + " lladdr " + gatewayMAC + " nud permanent dev " + interfaceName);
                        String output = "MAC address " + gatewayMAC + " static for " + gatewayIP + " on " + interfaceName;
                        Log.v(TAG, output);
                        Toast.makeText(mContext, output, Toast.LENGTH_LONG).show();
                        setColor(true);
                        return;
                    } else {
                        out = R.string.Failed_Root;
                    }

                } else {
                    out = R.string.Failed;
                }
                Log.v(TAG, mContext.getResources().getString(out));
                Toast.makeText(mContext, mContext.getResources().getString(out), Toast.LENGTH_LONG).show();
                setColor(false);
            }
        }

        //Check if wifi is enabled and setStatic Arp
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                Log.v(TAG, "WIFI_STATE_ENABLED");
                //if (rooted)
                new SetStaticArp().execute();
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                Log.v(TAG, "WIFI_STATE_DISABLED");
                setColor(false);
                break;
        }

    }

    public void setRooted(boolean status) {
        this.rooted = status;
    }

    private void setColor(final boolean Success) {
        // Post the UI updating code to our Handler

        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) rootView.findViewById(R.id.header);
                if (Success) {
                    tv.setBackgroundColor(Color.GREEN);
                    tv.setText(R.string.WifiStaticArp_ON);
                } else {
                    tv.setBackgroundColor(Color.RED);
                    tv.setText(R.string.WifiStaticArp_OFF);
                }
            }
        });
    }
}
