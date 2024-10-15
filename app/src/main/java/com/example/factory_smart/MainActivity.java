package com.example.factory_smart;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvTemperature, tvHumidity, tvWarning;
    private Switch switchFan, switchAlarm, switchConveyorBelt, switchLight;
    private Button btnConveyorDirection;
    private DatabaseReference databaseReference;
    private boolean isConveyorReversed = false; // Trạng thái đảo chiều băng chuyền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần UI
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWarning = findViewById(R.id.tvWarning);
        switchFan = findViewById(R.id.switchFan);
        switchAlarm = findViewById(R.id.switchAlarm);
        switchConveyorBelt = findViewById(R.id.switchConveyorBelt);
        switchLight = findViewById(R.id.switchLight);
        btnConveyorDirection = findViewById(R.id.btnConveyorDirection);

        // Kết nối đến Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Điều khiển bật/tắt băng chuyền
        switchConveyorBelt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            databaseReference.child("Devices").child("ConveyorBelt").setValue(isChecked ? 1 : 0);
        });

        // Xử lý đảo chiều băng chuyền: luân phiên giữa giá trị 1 và 2
        btnConveyorDirection.setOnClickListener(v -> {
            // Kiểm tra giá trị hiện tại, sau đó đảo chiều
            isConveyorReversed = !isConveyorReversed;
            int newDirection = isConveyorReversed ? 2 : 1;
            databaseReference.child("Devices").child("ConveyorBeltDirection").setValue(newDirection);

            String directionText = (newDirection == 2) ? "đã đảo chiều (2)" : "đã về chiều thuận (1)";
            Toast.makeText(MainActivity.this, "Băng chuyền " + directionText, Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe trạng thái băng chuyền
        databaseReference.child("Devices").child("ConveyorBelt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    switchConveyorBelt.setChecked(status == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái băng chuyền", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái chiều băng chuyền
        databaseReference.child("Devices").child("ConveyorBeltDirection").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer direction = dataSnapshot.getValue(Integer.class);
                if (direction != null) {
                    isConveyorReversed = (direction == 2);
                    String directionText = isConveyorReversed ? "Chiều ngược (2)" : "Chiều thuận (1)";
                    btnConveyorDirection.setText("Đảo chiều băng chuyền (" + directionText + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái chiều băng chuyền", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm hiển thị cảnh báo
    private void showWarning(String message) {
        tvWarning.setText(message);
        tvWarning.setVisibility(View.VISIBLE); // Hiển thị TextView cảnh báo
    }

    // Hàm ẩn cảnh báo
    private void hideWarning() {
        tvWarning.setVisibility(View.GONE); // Ẩn TextView cảnh báo
    }
}
