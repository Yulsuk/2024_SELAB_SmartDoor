package com.example.smartdoor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditSMSReceiverActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1; // 권한 요청 코드
    private static final String PREFS_NAME = "SMSReceiverPrefs";
    private static final String PHONE_NUMBERS_KEY = "PhoneNumbers";
    private static final String PHONE_NAMES_KEY = "PhoneNames";

    private EditText editTextPhoneNumber;
    private ListView listViewPhoneNumbers;
    private ArrayAdapter<String> adapter;
    private List<String> displayList; // 이름과 전화번호를 함께 표시하는 리스트
    private Map<String, String> phoneNumberNameMap; // 전화번호와 이름을 저장하는 맵

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

        // 전화번호와 이름을 저장하는 맵 초기화
        phoneNumberNameMap = loadPhoneNumberNameMap();

        // 전화번호와 이름을 표시할 리스트 초기화
        displayList = generateDisplayList();

        // 어댑터 설정
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewPhoneNumbers.setAdapter(adapter);

        // 전화번호 추가 버튼 클릭 시
        buttonAddPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                if (!phoneNumber.isEmpty() && !phoneNumberNameMap.containsKey(phoneNumber)) {
                    phoneNumberNameMap.put(phoneNumber, "이름 없음");
                    displayList.add(phoneNumber + " (이름 없음)");
                    adapter.notifyDataSetChanged();
                    savePhoneNumberNameMap(); // 전화번호가 추가될 때마다 저장
                    editTextPhoneNumber.setText("");
                } else {
                    Toast.makeText(EditSMSReceiverActivity.this, "유효하지 않거나 중복된 전화번호입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 전화번호 리스트 항목 길게 누를 시 이름 설정 기능 제공
        listViewPhoneNumbers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneNumber = getPhoneNumberFromDisplay(displayList.get(position));

                // 이름 입력 대화상자 표시
                final EditText input = new EditText(EditSMSReceiverActivity.this);
                new AlertDialog.Builder(EditSMSReceiverActivity.this)
                        .setTitle("이름 설정")
                        .setMessage(phoneNumber + " 번호의 이름을 입력하세요.")
                        .setView(input)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = input.getText().toString().trim();
                                if (!name.isEmpty()) {
                                    phoneNumberNameMap.put(phoneNumber, name);
                                    updateDisplayList();
                                    adapter.notifyDataSetChanged();
                                    savePhoneNumberNameMap(); // 이름이 설정될 때마다 저장
                                    Toast.makeText(EditSMSReceiverActivity.this, "이름이 설정되었습니다: " + name, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EditSMSReceiverActivity.this, "이름이 비어 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });

        // 모든 전화번호로 SMS 전송 버튼 클릭 시
        buttonRemovePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneNumberNameMap.isEmpty()) {
                    String message = "알림 수신 번호로 등록되었습니다."; // 전송할 메시지 내용
                    sendSMSToAllNumbers(message);
                } else {
                    Toast.makeText(EditSMSReceiverActivity.this, "전송할 전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 전화번호 리스트를 SharedPreferences에 저장하는 메서드
    private void savePhoneNumberNameMap() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> phoneNumbers = phoneNumberNameMap.keySet();
        editor.putStringSet(PHONE_NUMBERS_KEY, phoneNumbers);
        for (String phoneNumber : phoneNumbers) {
            editor.putString(phoneNumber, phoneNumberNameMap.get(phoneNumber));
        }
        editor.apply(); // 변경 사항을 즉시 저장
    }

    // SharedPreferences에서 전화번호 리스트와 이름을 불러오는 메서드
    private Map<String, String> loadPhoneNumberNameMap() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> phoneNumbers = sharedPreferences.getStringSet(PHONE_NUMBERS_KEY, new HashSet<>());
        Map<String, String> phoneNumberNameMap = new HashMap<>();
        for (String phoneNumber : phoneNumbers) {
            String name = sharedPreferences.getString(phoneNumber, "이름 없음");
            phoneNumberNameMap.put(phoneNumber, name);
        }
        return phoneNumberNameMap;
    }

    // 화면에 표시할 리스트 생성
    private List<String> generateDisplayList() {
        List<String> displayList = new ArrayList<>();
        for (Map.Entry<String, String> entry : phoneNumberNameMap.entrySet()) {
            displayList.add(entry.getKey() + " (" + entry.getValue() + ")");
        }
        return displayList;
    }

    // 전화번호를 화면에 표시할 리스트로 변환
    private void updateDisplayList() {
        displayList.clear();
        displayList.addAll(generateDisplayList());
    }

    // 화면 표시용 리스트에서 전화번호 추출
    private String getPhoneNumberFromDisplay(String display) {
        return display.split(" ")[0]; // 전화번호와 이름은 공백으로 구분됨
    }

    // 모든 전화번호로 SMS를 전송하는 메서드
    private void sendSMSToAllNumbers(String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            for (String phoneNumber : phoneNumberNameMap.keySet()) {
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