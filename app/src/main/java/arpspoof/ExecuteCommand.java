package arpspoof;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class ExecuteCommand extends Thread {
    private static final String TAG = "ExecuteCommand";
    private static final int NUM_ITEMS = 5;
    private final String command;
    private Process process = null;
    private BufferedReader reader = null;
    private DataOutputStream os = null;
    private ListView outputLV = null;
    private ArrayAdapter<String> outputAdapter = null;


    public ExecuteCommand(String cmd) throws IOException {
        command = cmd;
        ProcessBuilder pb = new ProcessBuilder().command("su");
        pb.redirectErrorStream(true);
        process = pb.start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        os = new DataOutputStream(process.getOutputStream());
    }

    public ExecuteCommand(String cmd, ListView lv, ArrayAdapter<String> aa) throws IOException {
        this(cmd);
        outputLV = lv;
        outputAdapter = aa;
    }

    public void run() {

        class StreamGobbler extends Thread {
            /*"gobblers" seem to be the recommended way to ensure the streams don't cause issues */

            public BufferedReader buffReader = null;

            public StreamGobbler(BufferedReader br) {
                buffReader = br;

            }

            public void run() {
                try {
                    String line = null;
                    if (outputLV == null) {
                        char[] buffer = new char[4096];
                        while (buffReader.read(buffer) > 0) {
                        }
                    } else {
                        while ((line = buffReader.readLine()) != null) {
                            if (outputLV != null) {
                                final String tmpLine = new String(line);
                                outputLV.post(new Runnable() {
                                    public void run() {
                                        outputAdapter.add(tmpLine);
                                        if (outputAdapter.getCount() > NUM_ITEMS)
                                            outputAdapter.remove(outputAdapter.getItem(0));
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "StreamGobbler couldn't read stream", e);
                } finally {
                    try {
                        if (buffReader != null) {
                            buffReader.close();
                            buffReader = null;
                        }
                    } catch (IOException e) {
                        //swallow error
                    }
                }
            }
        }

        try {
            os.writeBytes(command + '\n');
            os.flush();
            StreamGobbler stdOutGobbler = new StreamGobbler(reader);
            stdOutGobbler.setDaemon(true);
            stdOutGobbler.start();
            os.writeBytes("exit\n");
            os.flush();
            //The following catastrophe of code seems to be the best way to ensure this thread can be interrupted
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    process.exitValue();
                    Thread.currentThread().interrupt();
                } catch (IllegalThreadStateException e) {
                    //the process hasn't terminated yet so sleep some, then check again
                    Thread.sleep(250);//.25 seconds seems reasonable
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "error running commands", e);
        } catch (InterruptedException e) {
            try {
                if (os != null) {
                    os.close();//key to killing arpspoof executable and process
                    os = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException ex) {
                // swallow error
            } finally {
                if (process != null) {
                    process.destroy();
                    process = null;
                }
            }
        } finally {
            if (process != null) {
                process.destroy();
                process = null;
            }
        }
    }

}