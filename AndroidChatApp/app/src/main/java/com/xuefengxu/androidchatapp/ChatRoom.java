package com.xuefengxu.androidchatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xuefengxu.androidchatapp.MainActivity.contents;

public class ChatRoom extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        WebSocket ws = MainActivity.getWebSocket();
        String[] myDataset = contents.toArray(new String[contents.size()]);
        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(mAdapter);
        ws.addListener(new WebSocketAdapter(){
            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                JSONObject obj = new JSONObject(message);
                String usr = obj.getString("user");
                String msg = obj.getString("message");

                contents.add(usr + ": " + msg);

                Log.d("newmessage", "messages received " + message);
                String[] myDataset = contents.toArray(new String[contents.size()]);
                // specify an adapter (see also next example)
                mAdapter = new MyAdapter(myDataset);
                recyclerView.setAdapter(mAdapter);
            }
        });

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView roomName = findViewById(R.id.roomName);
        roomName.setText(message);
    }

    public void sendMessage(View view) {
        WebSocket ws = MainActivity.getWebSocket();
        EditText editmessage = findViewById(R.id.message);
        EditText editusername = findViewById(R.id.userName);
        String message = editmessage.getText().toString();
        String username = editusername.getText().toString();
        ws.sendText(username + " " + message);

        Log.d("send", username + " " + message);
        editmessage.setText("");
    }
}
