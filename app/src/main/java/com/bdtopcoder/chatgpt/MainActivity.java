package com.bdtopcoder.chatgpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bdtopcoder.chatgpt.chatmodel.Message;
import com.bdtopcoder.chatgpt.chatmodel.MessageAdapter;

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

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText message_text_text;
    ImageView send_btn;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //====================================
        message_text_text = findViewById(R.id.message_text_text);
        send_btn = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.recyclerView);

        // Create Layout behaves and set it in recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //====================================

        //====================================
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        //====================================

        send_btn.setOnClickListener(view -> {
            String question = message_text_text.getText().toString().trim();
            addToChat(question,Message.SEND_BY_ME);
            message_text_text.setText("");
            callAPI(question);
        });

    } // OnCreate Method End Here ================

    void addToChat (String message, String sendBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sendBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    } // addToChat End Here =====================

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SEND_BY_BOT);
    } // addResponse End Here =======

    void callAPI(String question){
        // okhttp
        messageList.add(new Message("Typing...", Message.SEND_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","text-davinci-003");
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url(API.API_URL)
                .header("Authorization","Bearer "+API.API)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to"+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    addResponse("Failed to load response due to"+response.body().toString());
                }

            }
        });

    } // callAPI End Here =============


} // Public Class End Here =========================