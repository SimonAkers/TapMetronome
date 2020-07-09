package com.gmail.hayesakers.tapmetronome;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.Timer;

public class MainActivity extends WearableActivity {

    private TextView outputText;
    private ConstraintLayout layout;
    private Vibrator v;
    private long lastTime = 0;
    private long currentTime = 0;
    private long averageTime = 0;
    private long bpm = 0;
    private long[] times = new long[4];
    private int timesCount = 0;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView) findViewById(R.id.text);
        layout = (ConstraintLayout) findViewById(R.id.frameLayout);

        outputText.setText("0 bpm");

        layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                screenClicked();
            }
        });

        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onPause() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    protected void screenClicked() {
        bpm = calculateBpm();
        outputText.setText(String.format("%d bpm", (int) bpm));
    }

    protected long calculateBpm() {
        if (timesCount == times.length) {
            timesCount = 0;
        }

        if (currentTime == 0) {
            currentTime = System.currentTimeMillis();
            lastTime = currentTime;
            return 0;
        }

        currentTime = System.currentTimeMillis();

        if (currentTime - lastTime > 4500) {
            lastTime = currentTime - 4500;
        }

        times[timesCount] = currentTime - lastTime;
        timesCount++;
        lastTime = currentTime;

        long sum = 0;
        long count = 0;
        for (long time : times) {
            sum += time;
            if (time != 0) {
                count++;
            }
        }

        if (count == 2) {
            vibrate();
        }

        averageTime = sum / count;

        return 60000 / averageTime;
    }

    protected void pulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(150, 255));
        } else {
            //deprecated in API 26
            v.vibrate(150);
        }
    }

    protected void vibrate() {
        timer.schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    pulse();
                    vibrate();
                }
            },
            averageTime
        );
    }
}
