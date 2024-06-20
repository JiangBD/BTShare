/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.btshare;

import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;



public class PBarUpdater {
    int currentPerc;
    int received, totalSize;
    ProgressBar pBar;
    Handler handler;
    public PBarUpdater(int fileSize) {
        currentPerc = 0; received = 0; totalSize = fileSize;
        pBar = MyApplication.getMainActivity().getPBar();
        handler = new Handler(Looper.getMainLooper());
    }

    synchronized public void onDataChunkTransferred(int newChunk) {
        if (pBar != null) {
            received += newChunk;
            if (received >= totalSize) handler.post(() -> pBar.setProgress(100) );
            else {
                int tempPerc = received / totalSize * 100;
                if (tempPerc - currentPerc >= 3) {
                    currentPerc = tempPerc;
                    handler.post(() -> pBar.setProgress(tempPerc));
                }
            }
        }
    }


}
