package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // Get the username from the Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // Add a welcome message with the username
        addToChat("Welcome, " + username + "!", Message.SENT_BY_BOT);

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENT_BY_ME);
                messageEditText.setText("");
                callAPI(question);
            }
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response) {
        runOnUiThread(() -> {
            messageList.remove(messageList.size() - 1); // Ensure this is done on the UI thread
            addToChat(response, Message.SENT_BY_BOT);
        });
    }

    void callAPI(String question) {
        //okhttp
        runOnUiThread(() -> messageList.add(new Message("Typing... ", Message.SENT_BY_BOT))); // Ensure this is done on the UI thread

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant."));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", question));
            jsonBody.put("messages", messages);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer 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")  // Replace with your actual API key
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> addResponse("Failed to load response due to " + e.getMessage())); // Ensure this is done on the UI thread
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        String result = choices.getJSONObject(0).getJSONObject("message").getString("content"); // Get the content from the message object
                        runOnUiThread(() -> addResponse(result.trim())); // Ensure this is done on the UI thread
                    } catch (JSONException e) {
                        runOnUiThread(() -> addResponse("Failed to parse response: " + e.getMessage())); // Handle JSON parsing errors
                        e.printStackTrace();
                    }
                } else {
                    // Read the response body and display the message
                    runOnUiThread(() -> {
                        try {
                            String errorMessage = response.body().string();
                            addResponse("Failed to load response due to " + errorMessage); // Ensure this is done on the UI thread
                        } catch (IOException e) {
                            addResponse("Failed to load response and to read error message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
























