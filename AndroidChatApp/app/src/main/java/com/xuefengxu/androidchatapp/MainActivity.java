package com.xuefengxu.androidchatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.ChatRoom.MESSAGE";
    private static WebSocket ws;
    public static List<String> contents = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            System.out.println("Trying to connect...");
            ws = new WebSocketFactory().createSocket("ws://10.0.2.2:8080");
            ws.connectAsynchronously();
        } catch (IOException e){
            e.printStackTrace();
        }
        ws.addListener(new WebSocketAdapter(){
            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                JSONObject obj = new JSONObject(message);
                String usr = obj.getString("user");
                String msg = obj.getString("message");

                contents.add(usr + ": " + msg);

                Log.d("newmessage", "messages received " + message);
            }
        });
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, ChatRoom.class);
        EditText editText = (EditText) findViewById(R.id.joinRoom);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        ws.sendText("join " + message);
        startActivity(intent);
    }

    public static WebSocket getWebSocket(){return ws;}
}