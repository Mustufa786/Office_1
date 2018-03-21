package com.example.ttc.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ttc.myapplication.permssions.Permssions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private ImageView mImageView;
    private Button mScreenShotButton;
    float scalediff;

    public static final int REQUEST_CODE = 1;

    private static final String TAG = "MainActivity";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mScreenShotButton = findViewById(R.id.screenshot);
        mScreenShotButton.setOnClickListener(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(250,250);
        layoutParams.leftMargin = 50;
        layoutParams.topMargin = 50;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        mImageView.setLayoutParams(layoutParams);

        mImageView.setOnTouchListener(new View.OnTouchListener() {

            RelativeLayout.LayoutParams parms;
            int startwidth;
            int startheight;
            float dx = 0, dy = 0, x = 0, y = 0;
            float angle = 0;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final ImageView ivView = (ImageView) view;
                ((BitmapDrawable) ivView.getDrawable()).setAntiAlias(true);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        parms = (RelativeLayout.LayoutParams) ivView.getLayoutParams();
                        startheight = parms.height;
                        startwidth = parms.width;
                        dx = event.getRawX() - parms.leftMargin;
                        dy = event.getRawY() - parms.topMargin;
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:

                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            mode = ZOOM;
                        }

                        d = rotation(event);

                        break;

                    case MotionEvent.ACTION_UP:

                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {

                            x = event.getRawX();
                            y = event.getRawY();

                            parms.leftMargin = (int) (x - dx);
                            parms.topMargin = (int) (y - dy);

                            parms.rightMargin = 0;
                            parms.bottomMargin = 0;
                            parms.rightMargin = parms.leftMargin + (5 * parms.width);
                            parms.bottomMargin = parms.topMargin + (10 * parms.height);

                            view.setLayoutParams(parms);

                        } else if (mode == ZOOM) {

                            if (event.getPointerCount() == 2) {

                                newRot = rotation(event);
                                float r = newRot - d;
                                angle = r;

                                x = event.getRawX();
                                y = event.getRawY();

                                float newDist = spacing(event);
                                if (newDist > 10f) {
                                    float scale = newDist / oldDist * view.getScaleX();
                                    if (scale > 0.6) {
                                        scalediff = scale;
                                        view.setScaleX(scale);
                                        view.setScaleY(scale);

                                    }
                                }

                                view.animate().rotationBy(angle).setDuration(0)
                                        .setInterpolator(new LinearInterpolator()).start();

                                x = event.getRawX();
                                y = event.getRawY();

                                parms.leftMargin = (int) ((x - dx) + scalediff);
                                parms.topMargin = (int) ((y - dy) + scalediff);

                                parms.rightMargin = 0;
                                parms.bottomMargin = 0;
                                parms.rightMargin = parms.leftMargin + (5 * parms.width);
                                parms.bottomMargin = parms.topMargin + (10 * parms.height);

                                view.setLayoutParams(parms);

                            }
                        }
                        break;
                }
                return true;
            }
        });

    }


//    private View.OnTouchListener onTouchListener() {
//
//        return new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                final int x = (int) motionEvent.getRawX();
//                final int y = (int) motionEvent.getRawY();
//
//                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
//
//                    case MotionEvent.ACTION_DOWN:
//
//                        RelativeLayout.LayoutParams params1 =
//                                (RelativeLayout.LayoutParams) view.getLayoutParams();
//
//                        xDelta = x - params1.leftMargin;
//                        yDelta = y - params1.topMargin;
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                    RelativeLayout.LayoutParams params2 =
//                            (RelativeLayout.LayoutParams) view.getLayoutParams();
//                    params2.leftMargin = x - xDelta;
//                    params2.topMargin = y - yDelta;
//                    params2.rightMargin = 0;
//                    params2.bottomMargin = 0;
//                    view.setLayoutParams(params2);
//                    break;
//
//                }
//                mainLayout.invalidate();
//                return true;
//
//            }
//
//        };
//    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    public void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                MainActivity.this,
                permissions,
                REQUEST_CODE
        );

    }

    public boolean checkPermssions(String[] permissions) {

        int permssionsRequest = ActivityCompat.checkSelfPermission(
                MainActivity.this,
                permissions[0]
        );

        if(permssionsRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermssions: permission was not granted by the user " + permissions[0]);
            return false;
        }else{
            Log.d(TAG, "checkPermssions: perssmision for " + permissions[0]);
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: requesting for permission " + permissions[0]);

        switch (requestCode) {

            case REQUEST_CODE:
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: user has permssion to access " + permissions[i]);

                    }else {

                        break;
                    }
                }

                break;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.screenshot:
                Log.d(TAG, "onClick: taking screen shot");
                for(int i=0; i< Permssions.permssions.length; i++) {
                    String[] perm = {Permssions.permssions[i]};
                    if(checkPermssions(perm)) {
                        takeScreenshot();

                    }else {

                        verifyPermissions(Permssions.permssions);
                    }
                }
                break;
        }
    }
}
