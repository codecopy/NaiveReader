package sixue.naviereader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class ReadActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final String TAG = "ReadActivity";
    private GestureDetector detector;
    private ReaderView readerView;
    private BroadcastReceiver batteryReceiver;
    private ImageView mask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        Utils.verifyStoragePermissions(this);
        String text = Utils.readText("test.txt");
        if (text == null) {
            text = "Can't open file.";
        }

        readerView = (ReaderView) findViewById(R.id.textArea);
        mask = (ImageView) findViewById(R.id.mask);

        readerView.importText(text);
        readerView.setOnTouchListener(this);

        detector = new GestureDetector(this, this);
        detector.setIsLongpressEnabled(true);

        final TextView progress = (TextView) findViewById(R.id.progress);
        readerView.setOnPageChangeListener(new ReaderView.OnPageChangeListener() {
            @Override
            public void onPageChanged(ReaderView v) {
                int textLength = readerView.getTextLength();
                int startChart = readerView.getStartChar();
                if (textLength <= 0) {
                    progress.setText("0");
                    return;
                }

                progress.setText(String.format(Locale.CHINA, "%d/%d", startChart + 1, textLength));
            }
        });

        final TextView battery = (TextView) findViewById(R.id.battery);
        battery.setText("?");
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra("level", 0);
                    int scale = intent.getIntExtra("scale", 100);
                    battery.setText((level * 100 / scale) + "%");
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.i(TAG, "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        Log.i(TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.i(TAG, "onSingleTapUp");

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        Log.d(TAG, "Display:width=" + widthPixels + ",height=" + heightPixels + "; Touch:x=" + x + ",y=" + y);

        if ((x < (widthPixels / 2)) && (y < (heightPixels / 2))) {
            turnPage(-1);
        } else {
            turnPage(1);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float vX, float vY) {
        Log.i(TAG, "onScroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.i(TAG, "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float vX, float vY) {
        Log.i(TAG, "onFling");
        Log.d(TAG, "Fling:vX=" + vX + ",vY=" + vY);
        if (vX > 0) {
            turnPage(-1);
        } else {
            turnPage(1);
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.i(TAG, "onTouch");
        return detector.onTouchEvent(motionEvent);
    }

    private void turnPage(final int step) {
        final Animation anim = new TranslateAnimation(readerView.getRight() * step, 0, 0, 0);
        anim.setDuration(1000);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mask.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                readerView.turnPage(step);
                mask.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mask.setImageBitmap(readerView.generateMask(step));
        mask.startAnimation(anim);
    }
}