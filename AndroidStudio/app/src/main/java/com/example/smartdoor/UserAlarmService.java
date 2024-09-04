package com.example.smartdoor;

import static com.android.volley.VolleyLog.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAlarmService extends Service {
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;

    private static final String PREFS_NAME = "SMSReceiverPrefs";
    private static final String PHONE_NUMBERS_KEY = "PhoneNumbers";

    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper()); // 메인 스레드의 Looper를 사용하여 Handler 생성
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            Log.e("UserAlarmService", "UserAlarmService is Running...");
                            boolean human_detect_flag = human_detect_check();
                            boolean forced_open_flag = forced_open_check();
                            if (human_detect_flag) {
                                human_detect_alarm();
                            }
                            if (forced_open_flag) {
                                forced_open_alarm();
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    int isHumanDetected = 0;
    int isDoorForcedOpened = 0;

    public boolean human_detect_check() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://220.69.240.35/SmartDoor/alarm.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // 응답 처리
                        try {
                            if (response.length() > 0) {
                                // 첫 번째 객체 가져오기
                                JSONObject firstObject = response.getJSONObject(0);
                                // isHumanDetected 속성 확인
                                if (firstObject.has("isHumanDetected")) {
                                    isHumanDetected = firstObject.getInt("isHumanDetected");
                                    Log.d(TAG, "isHumanDetected: " + isHumanDetected);
                                } else {
                                    Log.d(TAG, "isHumanDetected 속성이 없습니다.");
                                }
                            } else {
                                Log.d(TAG, "JSON 배열이 비어있습니다.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Parsing Error!", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

        return isHumanDetected == 1;
    }

    public void human_detect_alarm() {
        // 알림 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myCh")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setColor(0xFFFF0000)
                .setContentTitle("사람 감지 알림")
                .setContentText("문앞에 사람이 감지되었습니다. 녹화영상과 스트리밍을 확인할 수 있습니다.");

        notification = builder.build();

        notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(1, notification);

        // SMS 전송
        String message = "경고: 문 앞에서 사람이 감지되었습니다.";
        sendSMSToAllNumbers(message);
    }

    public boolean forced_open_check() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://220.69.240.35/SmartDoor/alarm.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // 응답 처리
                        try {
                            if (response.length() > 0) {
                                // 첫 번째 객체 가져오기
                                JSONObject firstObject = response.getJSONObject(0);
                                // isDoorForcedOpened 속성 확인
                                if (firstObject.has("isDoorForcedOpened")) {
                                    isDoorForcedOpened = firstObject.getInt("isDoorForcedOpened");
                                    Log.d(TAG, "isDoorForcedOpened: " + isDoorForcedOpened);
                                } else {
                                    Log.d(TAG, "isDoorForcedOpened 속성이 없습니다.");
                                }
                            } else {
                                Log.d(TAG, "JSON 배열이 비어있습니다.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Parsing Error!", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

        return isDoorForcedOpened == 1;
    }

    public void forced_open_alarm() {
        // 알림 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myCh")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setColor(0xFFFF0000)
                .setContentTitle("문 강제 개방 감지")
                .setContentText("문이 강제개방 되었습니다.");

        notification = builder.build();

        notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(2, notification);

        // SMS 전송
        String message = "경고: 문이 강제 개방되었습니다!";
        sendSMSToAllNumbers(message);
    }

    // 모든 전화번호로 SMS를 전송하는 메서드
    private void sendSMSToAllNumbers(String message) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> phoneNumbers = loadPhoneNumbers();

        for (String phoneNumber : phoneNumbers) {
            try {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Log.i(TAG, "SMS 전송 성공: " + phoneNumber);
                showToast("SMS 전송 성공: " + phoneNumber);
            } catch (Exception e) {
                Log.e(TAG, "SMS 전송 실패: " + phoneNumber, e);
                showToast("SMS 전송 실패: " + phoneNumber);
            }
        }
    }

    // UI 스레드에서 Toast를 표시하는 메서드
    private void showToast(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UserAlarmService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SharedPreferences에서 전화번호 리스트를 불러오는 메서드
    public List<String> loadPhoneNumbers() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> phoneNumberSet = sharedPreferences.getStringSet(PHONE_NUMBERS_KEY, new HashSet<String>());
        return new ArrayList<>(phoneNumberSet);
    }
}
