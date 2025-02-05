package com.greyblue9.qu1ckr00t;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {

    Button rootButton;
    String pocPath;
    String magiskInstPath;
    String magiskPath;
    TextView textView;
    ScrollView scrollView;
    TextView deviceInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootButton = (Button) findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.textView2);
        deviceInfo = (TextView)findViewById(R.id.deviceInfo);
        scrollView = (ScrollView)findViewById(R.id.scrollView2);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        addLabel(ssb, "Device", String.format("%s (Android %s)", DeviceInfo.getDeviceName(), DeviceInfo.getAndroidVersion()));
        addLabel(ssb, "Kernel", String.format("%s (%s)", DeviceInfo.getKernelVersion(), DeviceInfo.getDeviceArchitecture()));
        addLabel(ssb, "Patch", DeviceInfo.getAndroidPatchLevel());
        addLabel(ssb, "Fingerprint", DeviceInfo.getBuildFingerprint());

        deviceInfo.setText(ssb);

        textView.setMovementMethod(new ScrollingMovementMethod());

        rootButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rootButton.setText("Rooting...");
                addStatus("Starting root process");
                rootButton.setClickable(false);
                rootButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                new POCTask().execute();

            }
        });
    }

    private static void addLabel(SpannableStringBuilder ssb, String label, String text)
    {
        int start = ssb.length();
        ssb.append(label + ": ");
        ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ssb.append(text + "\n");
    }

    private void addStatus(String status)
    {
        textView.append(status + "\n");

        // auto scroll: https://stackoverflow.com/a/34866634/5768099
        int bottom = textView.getBottom() + scrollView.getPaddingBottom();
        int sy = scrollView.getScrollY();
        int sh = scrollView.getHeight();
        int delta = bottom - (sy + sh);

        scrollView.smoothScrollBy(0, delta);
    }

    private class POCTask extends AsyncTask<String, String, Boolean> {
        protected Boolean doInBackground(String... programs) {
          for (String p:  programs) {
            publishProgress(String.format(
              "programs[]: %s", p));
          }
          
            extractPoc();
            extractMagisk();
            
            try {
                String [] args0 = {pocPath, "shell_exec", "/system/bin/id"};
                publishProgress("Trying shell_exec id");
                publishProgress(String.format(
                  "pocPath=%s", pocPath));
                publishProgress(String.format(
                  "pocPath exists? %s", 
                  new File(pocPath).exists()));
                
                
                boolean rs = executeNativeCode(args0);
                publishProgress(String.format(
                  "rs = %s", rs));
                
                
                String [] args = {pocPath, "shell_exec", magiskInstPath + " " + magiskPath};
                publishProgress(String.format(
                  "cmd = %s shell_exec \"%s %s\"",
                  pocPath, magiskInstPath, magiskPath
                ));
                
                
                rs = executeNativeCode(args);
                publishProgress(String.format(
                  "rs = %s", rs));
                
                return true;
            } catch(Throwable ie) {
                publishProgress(String.format(
                  "Exception thrown: %s", ie));
                addStatus(ie.toString());
                ie.printStackTrace();
                return true;
            }
        }

        private void extractPoc()
        {
            InputStream poc = getResources().openRawResource(R.raw.poc);
            File pocDir = getApplicationContext().getFilesDir();
            File pocFile = new File(pocDir, "do_root");
            pocPath = pocFile.getPath();
            publishProgress("Extracting native code from APK...");
            copyFile(poc, pocFile.getPath());
            pocFile.setExecutable(true);
        }

        private void extractMagisk()
        {
            publishProgress("Extracting Magisk...");

            InputStream magisk = getResources().openRawResource(R.raw.magiskinit);
            File fileDir = getApplicationContext().getFilesDir();
            // XXX: hardcoded to 64 bit
            File magiskFile = new File(fileDir, "magiskinit");
            magiskPath = magiskFile.getPath();
            copyFile(magisk, magiskPath);
            magiskFile.setExecutable(true);

            publishProgress("Extracting installer...");

            InputStream magiskInst = getResources().openRawResource(R.raw.magisk_install);
            File magiskInstFile = new File(fileDir, "magisk_install");
            magiskInstPath = magiskInstFile.getPath();
            copyFile(magiskInst, magiskInstPath);
            magiskInstFile.setExecutable(true);
        }



         void inheritIO(final InputStream src) {
             new Thread(new Runnable() {
               public void run() {
                 java.util.Scanner sc = new java.util.Scanner(src);
                 while (sc.hasNextLine()) { 
                  publishProgress(String.format(
                    "[NATIVE] %s", sc.nextLine()
                  ));
                 }
               }
             }).start();
         }

        private boolean executeNativeCode(String [] args) throws IOException, InterruptedException {
            publishProgress("Executing native root binary...");
            Process nativeApp = Runtime.getRuntime().exec(args);
            publishProgress(String.format(
              "[NATIVE]: %s", nativeApp));
            inheritIO(nativeApp.getInputStream());
            inheritIO(nativeApp.getErrorStream());
 
            // Waits for the command to finish.
            publishProgress("[NATIVE]: waiting for process");
            nativeApp.waitFor();
            publishProgress(String.format(
              "[NATIVE EXIT CODE]: %d",
              nativeApp.exitValue()
            ));
            return nativeApp.exitValue() == 0;
        }

        protected void onProgressUpdate(String... updates) {
            addStatus(updates[0]);
        }

        protected void onPostExecute(Boolean result) {
            if (!result) {
                addStatus("Root failed :(\n");

                rootButton.setText("Root");
                rootButton.setClickable(true);
                rootButton.getBackground().setColorFilter(null);
            } else {
                addStatus("Enjoy your rooted device!");
                rootButton.setText("Rooted");
            }
        }
    }

    private static void copyFile(InputStream in, String localPath) {
        try {
            FileOutputStream out = new FileOutputStream(localPath);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}