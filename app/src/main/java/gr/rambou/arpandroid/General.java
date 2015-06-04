package gr.rambou.arpandroid;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class General {

    /*
    * Send command to be executed trough Shell
    * with the help of RootTools Lib
    */
    public String exec(String command) {
        final StringBuffer myOutput = new StringBuffer();
        //Get our command in a Command Object, to send it to Shell
        Command cmd = new Command(0, command) {
            @Override
            public void output(int id, String line) {
                myOutput.append(line);
            }
        };

        try {
            RootTools.getShell(RootTools.useRoot);
            RootTools.getShell(RootTools.useRoot).add(cmd).waitForFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myOutput.toString();
    }

    /**
     * These Gets and return the mac address into string
     * stolen...cough cough, i mean taken :p
     * from www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
     */
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                //Log.v("WifiStaticArp", "LINE:" + line);
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
