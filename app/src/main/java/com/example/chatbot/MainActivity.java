package com.example.chatbot;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the EditText and Button
        usernameEditText = findViewById(R.id.usernameEditText);
        goButton = findViewById(R.id.goButton);

        // Set an OnClickListener for the button
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the button is clicked, retrieve the username entered by the user
                String username = usernameEditText.getText().toString().trim();

                // Check if username is not empty
                if (!username.isEmpty()) {
                    // Pass the username to the next activity
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                } else {
                    // If username is empty, show a message to the user
                    usernameEditText.setError("Please enter your username");
                }
            }
        });
    }
}
