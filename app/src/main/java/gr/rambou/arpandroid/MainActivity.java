package gr.rambou.arpandroid;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

public class MainActivity extends AppCompatActivity {

    private StaticArp receiver;
    private boolean rooted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the receiver
        receiver = new StaticArp(new Handler(), false);
    }

    public void CheckBox_onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            rooted = true;
        } else {
            rooted = false;
        }

        RootTools.useRoot = rooted;
        //Check is the phone has Root access
        if (RootTools.isAccessGiven()) {
            rooted = true;
            Toast.makeText(getApplicationContext(), R.string.CheckBox_ON, Toast.LENGTH_LONG).show();
        } else {
            rooted = false;
            Toast.makeText(getApplicationContext(), R.string.CheckBox_OFF, Toast.LENGTH_LONG).show();
        }

        //Now set the CheckBox
        checkBox.setChecked(rooted);
    }

    public void StartSpoofButton_clicked(View v) {
        //Check if root is enabled
        if (rooted) {
            //Set Reciever RootStatus
            receiver.setRooted(true);

            // Register receiver, Trigger this Broadcast Reciever when device is connected to Wifi
            registerReceiver(receiver, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
        } else {
            Toast.makeText(getApplicationContext(), R.string.CheckBox_OFF, Toast.LENGTH_LONG).show();
            if (receiver.getAbortBroadcast())
                receiver.abortBroadcast();
        }
    }

    public void StartProtectButton_clicked(View v) {
        //Check if root is enabled
        if (rooted) {
            //Start activity ArpSpoof
            Intent i = new Intent(MainActivity.this, arpspoof.Arpspoof.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), R.string.CheckBox_OFF, Toast.LENGTH_LONG).show();
        }
    }

    public void Image_clicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Rambou"));
        startActivity(i);
    }
}
