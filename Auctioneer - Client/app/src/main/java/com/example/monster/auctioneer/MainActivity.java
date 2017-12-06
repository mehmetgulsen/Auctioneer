package com.example.monster.auctioneer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    public static final String hostname = "192.168.43.126";
    public static final int port = 9090;

    public static String nickname = "";

    static Socket clientSocket;
    static DataOutputStream outToServer;
    static BufferedReader inFromServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button nameButton = (Button) findViewById(R.id.btnName);
        Button findButton = (Button) findViewById(R.id.btnFind);
        Button createButton = (Button) findViewById(R.id.btnCreate);

        //This thread is used for establishing the connection
        Thread connectionThread = new Thread() {
            public void run() {
                try {
                    MainActivity.clientSocket = new Socket(MainActivity.hostname, MainActivity.port);
                    MainActivity.outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    MainActivity.inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };


        //Create connection each time the user goes to the home page
        MainActivity.clientSocket = null;
        MainActivity.outToServer = null;
        MainActivity.inFromServer = null;

        connectionThread.start();
        try {
            connectionThread.join();
            Log.i("Main", "Connection success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        nameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), NicknameActivity.class);
                startActivity(i);
            }
        });

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), GetIdActivity.class);
                startActivity(i);

            }

        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(i);

            }
        });

    }

}
