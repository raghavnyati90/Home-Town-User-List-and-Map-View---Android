package edu.sdu.rnyati.assignment4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onShowUsers(View v){
        Intent addUser = new Intent(this, ShowUserActivity.class);
        startActivity(addUser);
    }

    public void onCreateUser(View v){
        Intent showUser = new Intent(this, AddUserActivity.class);
        startActivity(showUser);
    }
}
