package com.example.monster.auctioneer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class AuctionActivity extends AppCompatActivity {

    public static String auction_id;
    public static String current_bid;
    public static String minimum_bid;
    public static String datetime;
    public static String auction_owner;
    public static String bid_owner;
    static String bid;

    EditText input;

    static boolean error;
    static String errorMessage;

    Button home, submit, refresh;
    TextView auctionIdField, auctionOwnerField,
            minimumField,deadlineField,currentBidField,
            currentBidOwnerField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction);

        //Writing the auction information on the screen
        auctionIdField = (TextView) findViewById(R.id.fieldAuctionId);
        auctionIdField.setText(auction_id);

        auctionOwnerField = (TextView) findViewById(R.id.fieldAuctionOwner);
        auctionOwnerField.setText(auction_owner);

        minimumField = (TextView) findViewById(R.id.fieldMinimum);
        minimumField.setText(minimum_bid);

        deadlineField = (TextView) findViewById(R.id.fieldDeadline);
        deadlineField.setText(datetime);

        currentBidField = (TextView) findViewById(R.id.fieldCurrentBid);
        currentBidField.setText(current_bid);

        currentBidOwnerField = (TextView) findViewById(R.id.fieldBidOwner);
        currentBidOwnerField.setText(bid_owner);

        //User input field for next bid.
        input = (EditText) findViewById(R.id.inputBid);

        //"Go home" button
        home = (Button) findViewById(R.id.btnAuctionToHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        //Make a new bid button
        submit = (Button) findViewById(R.id.btnBidSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //User has to have a nickname to make a bid
                if (MainActivity.nickname == null || MainActivity.nickname.equals("")) {
                    Toast.makeText(AuctionActivity.this, "You need to select a nickname", Toast.LENGTH_SHORT).show();
                    return;
                }

                bid = input.getText().toString();   //reading user input
                if(bid.equals("")){
                    Toast.makeText(AuctionActivity.this, "Please fill in the input field.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Communication thread for making a bid
                Thread t = new Thread() {
                    public void run() {
                        try {
                            //Request type "1" is for making a bid.
                            MainActivity.outToServer.writeBytes("1" + "\n");
                            //Send the necessary auction info and the new bid info
                            MainActivity.outToServer.writeBytes(AuctionActivity.auction_id + "\n");
                            MainActivity.outToServer.writeBytes(AuctionActivity.current_bid + "\n");
                            MainActivity.outToServer.writeBytes(AuctionActivity.bid + "\n");
                            MainActivity.outToServer.writeBytes(MainActivity.nickname + "\n");

                            String status = MainActivity.inFromServer.readLine();

                            //response type "1" means there is an error
                            if (status.equals("1")) {
                                //Set the error flag.
                                //Make a query to reread auction information

                                AuctionActivity.error = true;
                                AuctionActivity.errorMessage = MainActivity.inFromServer.readLine();

                                MainActivity.outToServer.writeBytes("0\n");
                                MainActivity.outToServer.writeBytes(auction_id + "\n");
                                status = MainActivity.inFromServer.readLine();
                            } else {
                                AuctionActivity.error = false;
                            }

                            //Read the updated auction info
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

                //Print success/error message
                if (error) {
                    Toast.makeText(AuctionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AuctionActivity.this, "Bid was successful", Toast.LENGTH_SHORT).show();
                }

                //Refresh the page
                auctionIdField.setText(AuctionActivity.auction_id);
                currentBidField.setText(AuctionActivity.current_bid);
                minimumField.setText(AuctionActivity.minimum_bid);
                deadlineField.setText(AuctionActivity.datetime);
                auctionOwnerField.setText(AuctionActivity.auction_owner);
                currentBidOwnerField.setText(AuctionActivity.bid_owner);

            }
        });

        //Button for refreshing the page
        refresh = (Button) findViewById(R.id.btnAuctionRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //communication thread to read auction information
                Thread t = new Thread() {
                    public void run() {
                        try {
                            //request type "0" is for reading auction information
                            MainActivity.outToServer.writeBytes("0" + "\n");
                            MainActivity.outToServer.writeBytes(AuctionActivity.auction_id + "\n");
                            String status = MainActivity.inFromServer.readLine();

                            //response status "1" means there is an error
                            if (status.equals("1")) {
                                AuctionActivity.error = true;
                                AuctionActivity.errorMessage = MainActivity.inFromServer.readLine();
                            } else {
                                //read auction information
                                AuctionActivity.error = false;
                                AuctionActivity.auction_id = MainActivity.inFromServer.readLine();
                                AuctionActivity.current_bid = MainActivity.inFromServer.readLine();
                                AuctionActivity.minimum_bid = MainActivity.inFromServer.readLine();
                                AuctionActivity.datetime = MainActivity.inFromServer.readLine();
                                AuctionActivity.auction_owner = MainActivity.inFromServer.readLine();
                                AuctionActivity.bid_owner = MainActivity.inFromServer.readLine();
                            }


                        } catch (Exception e) {

                        }
                    }
                };

                //make the communication
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //print the message if there is an error.
                //otherwise, refresh the page
                if (AuctionActivity.error) {
                    Toast.makeText(AuctionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    auctionIdField.setText(AuctionActivity.auction_id);
                    currentBidField.setText(AuctionActivity.current_bid);
                    minimumField.setText(AuctionActivity.minimum_bid);
                    deadlineField.setText(AuctionActivity.datetime);
                    auctionOwnerField.setText(AuctionActivity.auction_owner);
                    currentBidOwnerField.setText(AuctionActivity.bid_owner);

                    Toast.makeText(AuctionActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}
