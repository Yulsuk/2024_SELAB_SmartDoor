package com.example.smartdoor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, UserAlarmService.class);
        startService(serviceIntent);

        Button btnWatchRecordedVideo = (Button)findViewById(R.id.buttonWatchRecordedVideo);
        btnWatchRecordedVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://220.69.240.35/smartdoor/watchRecordedVideos.php";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);
            }
        });

        Button btnWatchStreaming = (Button)findViewById(R.id.buttonWatchStreaming);
        btnWatchStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://220.69.240.35/smartdoor/watchStreaming.php";//수정필요

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);
            }
        });

        Button btnWatchEntryTimeGraph = (Button)findViewById(R.id.buttonWatchRecordEntryGraph);
        btnWatchEntryTimeGraph.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), WatchEntryTimeGraph.class);
                startActivity(intent);
            }
        });

        Button btnEditSmsReceiver = (Button)findViewById(R.id.buttonEditSmsReceiver);
        btnEditSmsReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditSMSReceiverActivity.class);
                startActivity(intent);
            }
        });
    }


}