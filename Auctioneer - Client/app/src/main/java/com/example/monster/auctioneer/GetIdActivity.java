package com.example.monster.auctioneer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class GetIdActivity extends AppCompatActivity {

    static String auction_id;

    EditText idInput;

    Button submit, home;

    static boolean error;
    static String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_id);

        //Input field for Auction ID
        idInput = (EditText) findViewById(R.id.idInput);

        //"Go Home" button
        home = (Button) findViewById(R.id.btnIdToHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        //Find auction button
        submit = (Button) findViewById(R.id.btnSubmitId);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetIdActivity.auction_id = idInput.getText().toString();    //Read the input
                if(auction_id.equals("")){
                    Toast.makeText(GetIdActivity.this, "Please fill in the input field.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Communication thread for finding auction item
                Thread t = new Thread() {
                    public void run() {
                        try {
                            //"0" means that we are making a query for auction information
                            MainActivity.outToServer.writeBytes("0\n");

                            //The id of the auction we are looking for
                            MainActivity.outToServer.writeBytes(GetIdActivity.auction_id + "\n");

                            //Get response status. "1" means there is an error"
                            String response = MainActivity.inFromServer.readLine();
                            if (response.equals("1")) {
                                GetIdActivity.errorMessage = MainActivity.inFromServer.readLine();
                                GetIdActivity.error = true;
                            } else {
                                //Read auction information
                                AuctionActivity.auction_id = MainActivity.inFromServer.readLine();
                                AuctionActivity.current_bid = MainActivity.inFromServer.readLine();
                                AuctionActivity.minimum_bid = MainActivity.inFromServer.readLine();
                                AuctionActivity.datetime = MainActivity.inFromServer.readLine();
                                AuctionActivity.auction_owner = MainActivity.inFromServer.readLine();
                                AuctionActivity.bid_owner = MainActivity.inFromServer.readLine();

                                GetIdActivity.error = false;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };

                //Make the communication
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Show error message if there is an error
                //Otherwise, go to the auction page.
                if (GetIdActivity.error) {
                    Toast.makeText(GetIdActivity.this, GetIdActivity.errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(getApplicationContext(), AuctionActivity.class);
                    startActivity(i);
                }


            }
        });
    }
}
