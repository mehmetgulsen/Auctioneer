package com.example.monster.auctioneer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CreateActivity extends AppCompatActivity {

    static EditText inputStart, inputMinimum, inputDatetime;

    static String starting_bid, minimum_bid, datetime;

    Button home, submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        inputStart = (EditText) findViewById(R.id.createStartBid);
        inputMinimum = (EditText) findViewById(R.id.createMinimumBid);
        inputDatetime = (EditText) findViewById(R.id.createDatetime);


        //"Go Home" button
        home = (Button) findViewById(R.id.btnCreateToHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        //Auction creation button
        submit = (Button) findViewById(R.id.btnCreateAuction);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //User has to have a nickname to create an auction item
                if (MainActivity.nickname == null || MainActivity.nickname.equals("")) {
                    Toast.makeText(CreateActivity.this, "You need to select a nickname", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Read user input.
                CreateActivity.starting_bid = inputStart.getText().toString();
                CreateActivity.minimum_bid = inputMinimum.getText().toString();
                CreateActivity.datetime = inputDatetime.getText().toString();
                if(datetime.equals("") || starting_bid.equals("") || minimum_bid.equals("")){
                    Toast.makeText(CreateActivity.this, "Please fill in all of the input fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Communication thread for auction item creation
                Thread t = new Thread() {
                    public void run() {
                        try {
                            //Request type "2" is for auction item creation.
                            MainActivity.outToServer.writeBytes("2" + "\n");

                            //Send auction item specifications
                            MainActivity.outToServer.writeBytes(CreateActivity.starting_bid + "\n");
                            MainActivity.outToServer.writeBytes(CreateActivity.minimum_bid + "\n");
                            MainActivity.outToServer.writeBytes(CreateActivity.datetime + "\n");
                            MainActivity.outToServer.writeBytes(MainActivity.nickname + "\n");

                            //Read the info of the item created.
                            //This is especially for id.
                            String status = MainActivity.inFromServer.readLine();
                            AuctionActivity.auction_id = MainActivity.inFromServer.readLine();
                            AuctionActivity.current_bid = MainActivity.inFromServer.readLine();
                            AuctionActivity.minimum_bid = MainActivity.inFromServer.readLine();
                            AuctionActivity.datetime = MainActivity.inFromServer.readLine();
                            AuctionActivity.auction_owner = MainActivity.inFromServer.readLine();
                            AuctionActivity.bid_owner = MainActivity.inFromServer.readLine();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };

                //Make the communication
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Go to the auction page for the new item.
                Intent i = new Intent(getApplicationContext(), AuctionActivity.class);
                startActivity(i);


            }
        });
    }
}
