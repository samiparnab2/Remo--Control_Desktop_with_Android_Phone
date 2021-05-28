package com.example.remo.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.remo.Connection;
import com.example.remo.MainActivity;
import com.example.remo.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class stream_fragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //my Params
    private ImageView display, mousepad;
    private Button l_btn, r_btn;
    private Bitmap bimg;
    Connection connection;
    Thread displayThread, mouseThread;
    float startX, startY, endX, endY, dx, dy;
    private String mParam1;
    private String mParam2;
    private boolean paused,showingImage;
    public boolean connected;
    MainActivity mainActivity;
    String operation;

    public stream_fragment() {

    }

    public static stream_fragment newInstance(String param1, String param2) {
        stream_fragment fragment = new stream_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onPause() {


        paused = true;
        Log.d("stream fragment PAUSE","PAUSED:"+Boolean.toString(paused));
        super.onPause();
    }

    @Override
    public void onResume() {

        paused=false;
        Log.d("stream fragment RESUME","PAUSED:"+Boolean.toString(paused));
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stream_fragment, container, false);
        connection = Connection.getInstance();
        display = view.findViewById(R.id.display);
        mousepad = view.findViewById(R.id.imageView2);
        l_btn = view.findViewById(R.id.left_btn);
        r_btn = view.findViewById(R.id.right_btn);
        paused=false;
        showingImage=false;
        mainActivity = (MainActivity) getActivity();
        dx = 0;
        dy = 0;
        connected = true;
        operation = "z";
        l_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operation = "l";
            }
        });

        r_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operation = "r";
            }
        });

        mousepad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        endX = event.getRawX();
                        endY = event.getRawY();
                        dx = endX - startX;
                        dy = endY - startY;
                        operation = "m";
                        startX = endX;
                        startY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        operation = "z";
                        dx = 0;
                        dy = 0;
                        break;
                }
                return true;
            }
        });

        displayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (connected) {
                    try {
                            bimg = connection.getImage();
                            Log.d("NETWORK","IMAGE RECEIVED");
                    } catch (Exception e) {
                        fragmentCrashed();
                    }
//
                    display.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!paused) {
                                display.setImageBitmap(bimg);
                                Log.d("UI","DISPLAY UPDATED");
                            }
                        }
                    });
                }
                connection.disconnectDisplay();
            }
        });

        mouseThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (connected) {
                    switch (operation) {
                        case "m":
                            try {
                                connection.sendMouseMove((int) dx, (int) dy);
                            } catch (Exception e) {
                                fragmentCrashed();
                            }
                            break;
                        case "l":
                            try {
                                connection.sendMouseLeftClick();
                                operation = "z";
                            } catch (Exception e) {
                                fragmentCrashed();
                            }
                            break;
                        case "r":
                            try {
                                connection.sendMouseRightClick();
                                operation = "z";
                            } catch (Exception e) {
                                fragmentCrashed();
                            }
                            break;
                        default:
                            break;
                    }
                }
                connection.disconnectMouse();
            }
        });
        displayThread.start();
        mouseThread.start();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connected = false;
    }

    void fragmentCrashed() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(getContext(), "Unexpected Crash due to network issues", Toast.LENGTH_LONG);
            }
        });
        mainActivity.changeFragment();
    }

}

