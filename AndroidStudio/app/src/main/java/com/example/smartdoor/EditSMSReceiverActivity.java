package com.example.smartdoor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditSMSReceiverActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1; // 권한 요청 코드
    private static final String PREFS_NAME = "SMSReceiverPrefs";
    private static final String PHONE_NUMBERS_KEY = "PhoneNumbers";

    private EditText editTextPhoneNumber;
    private ListView listViewPhoneNumbers;
    private ArrayAdapter<String> adapter;
    private List<String> phoneNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms_receiver);

        // SMS 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        }

        // UI 요소들 연결
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        listViewPhoneNumbers = findViewById(R.id.listViewPhoneNumbers);
        Button buttonAddPhoneNumber = findViewById(R.id.buttonAddPhoneNumber);
        Button buttonRemovePhoneNumber = findViewById(R.id.buttonRemovePhoneNumber);

        // 전화번호 리스트 초기화 및 어댑터 설정
        phoneNumbers = loadPhoneNumbers(); // 저장된 전화번호 리스트 불러오기
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phoneNumbers);
        listViewPhoneNumbers.setAdapter(adapter);

        // 전화번호 추가 버튼 클릭 시
        buttonAddPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                if (!phoneNumber.isEmpty() && !phoneNumbers.contains(phoneNumber)) {
                    phoneNumbers.add(phoneNumber);
                    adapter.notifyDataSetChanged();
                    savePhoneNumbers(); // 전화번호가 추가될 때마다 저장
                    editTextPhoneNumber.setText("");
                } else {
                    Toast.makeText(EditSMSReceiverActivity.this, "유효하지 않거나 중복된 전화번호입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 전화번호 리스트 항목 클릭 시 삭제 옵션 제공
        listViewPhoneNumbers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneNumber = phoneNumbers.get(position);

                // 삭제 확인 대화상자 표시
                new AlertDialog.Builder(EditSMSReceiverActivity.this)
                        .setTitle("전화번호 삭제")
                        .setMessage(phoneNumber + " 번호를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            phoneNumbers.remove(phoneNumber);
                            adapter.notifyDataSetChanged();
                            savePhoneNumbers(); // 전화번호가 제거될 때마다 저장
                            Toast.makeText(EditSMSReceiverActivity.this, "전화번호가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // 모든 전화번호로 SMS 전송 버튼 클릭 시
        buttonRemovePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneNumbers.isEmpty()) {
                    String message = "안전 알림: 현재 상황을 확인하세요."; // 전송할 메시지 내용
                    sendSMSToAllNumbers(message);
                } else {
                    Toast.makeText(EditSMSReceiverActivity.this, "전송할 전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 전화번호 리스트를 SharedPreferences에 저장하는 메서드
    private void savePhoneNumbers() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> phoneNumberSet = new HashSet<>(phoneNumbers);
        editor.putStringSet(PHONE_NUMBERS_KEY, phoneNumberSet);
        editor.apply(); // 변경 사항을 즉시 저장
    }

    // SharedPreferences에서 전화번호 리스트를 불러오는 메서드
    public List<String> loadPhoneNumbers() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> phoneNumberSet = sharedPreferences.getStringSet(PHONE_NUMBERS_KEY, new HashSet<String>());
        return new ArrayList<>(phoneNumberSet);
    }

    // 모든 전화번호로 SMS를 전송하는 메서드
    private void sendSMSToAllNumbers(String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            for (String phoneNumber : phoneNumbers) {
                try {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(this, "SMS 전송 성공: " + phoneNumber, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "SMS 전송 실패: " + phoneNumber, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "SMS 전송 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS 전송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS 전송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
