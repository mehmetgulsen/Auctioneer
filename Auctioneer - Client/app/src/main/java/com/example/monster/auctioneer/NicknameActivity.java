package com.example.monster.auctioneer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NicknameActivity extends AppCompatActivity {

    String name;

    EditText nameInput;

    Button submit, home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        //Input field for typing the nickname
        nameInput = (EditText) findViewById(R.id.nameInput);
        nameInput.setText(name);    //show the current nickname in the input field


        //Setting the nickname
        submit = (Button) findViewById(R.id.btnSubmitName);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameInput.getText().toString();  //reading user input
                MainActivity.nickname = name;
                Toast.makeText(NicknameActivity.this, "Nickname is " + name, Toast.LENGTH_SHORT).show();
            }
        });

        //"Go home" button
        home = (Button) findViewById(R.id.btnNameToHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });
    }
}
