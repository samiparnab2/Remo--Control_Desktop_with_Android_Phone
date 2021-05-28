package com.example.remo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.remo.fragments.start_fragment;
import com.example.remo.fragments.stream_fragment;

public class MainActivity extends AppCompatActivity {

    int fragindex;
    String[] strt_btn_string = new String[2];
    Button start_btn;
    start_fragment startFrag;
    stream_fragment streamFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow(). addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        start_btn = (Button) findViewById(R.id.start_btn);
        strt_btn_string[0] = "start as Remote";
        strt_btn_string[1] = "Stop";
        fragindex = 0;
        startFrag = new start_fragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.canvas_layout, startFrag, "first frag");
        transaction.commit();
        start_btn.setText(strt_btn_string[fragindex]);


        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeFragment();
                    }
                }).start();
            }
        });

    }

    public void changeFragment() {


        fragindex = (fragindex + 1) % 2;
        if (fragindex == 0) {
            startFrag = new start_fragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.canvas_layout, startFrag);
            transaction.commit();

        } else {
            Connection c = Connection.getInstance();
            c.setIP(startFrag.getIP());
            if (!c.connect()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Something wrong with IP", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            streamFrag = new stream_fragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.canvas_layout, streamFrag);
            transaction.commit();

        }

        start_btn.post(new Runnable() {
            @Override
            public void run() {
                start_btn.setText(strt_btn_string[fragindex]);
            }
        });

    }
}