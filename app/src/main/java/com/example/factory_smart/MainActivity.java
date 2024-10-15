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

        // Lắng nghe dữ liệu nhiệt độ từ Firebase
        databaseReference.child("Sensor").child("TEMP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Float temperature = dataSnapshot.getValue(Float.class);
                if (temperature != null) {
                    tvTemperature.setText("Nhiệt độ: " + temperature + "°C");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy dữ liệu nhiệt độ", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe dữ liệu độ ẩm từ Firebase
        databaseReference.child("Sensor").child("HUM").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Float humidity = dataSnapshot.getValue(Float.class);
                if (humidity != null) {
                    tvHumidity.setText("Độ ẩm: " + humidity + "%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy dữ liệu độ ẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Điều khiển bật/tắt quạt
        switchFan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            databaseReference.child("Devices").child("Fan").setValue(isChecked ? 1 : 0);
        });

        // Điều khiển bật/tắt còi
        switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            databaseReference.child("Devices").child("Alarm").setValue(isChecked ? 1 : 0);
        });

        // Điều khiển bật/tắt băng chuyền và lấy giá trị từ nút đảo chiều
        switchConveyorBelt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Bật băng chuyền với trạng thái chiều hiện tại
                databaseReference.child("Devices").child("ConveyorBelt").setValue(isConveyorReversed ? 2 : 1);
            } else {
                // Tắt băng chuyền
                databaseReference.child("Devices").child("ConveyorBelt").setValue(0);
            }
        });

        // Xử lý đảo chiều băng chuyền: luân phiên giữa giá trị 1 và 2
        btnConveyorDirection.setOnClickListener(v -> {
            isConveyorReversed = !isConveyorReversed;
            int newDirection = isConveyorReversed ? 2 : 1;
            databaseReference.child("Devices").child("ConveyorBelt").setValue(newDirection);

            String directionText = (newDirection == 2) ? "đã đảo chiều (2)" : "đã về chiều thuận (1)";
            Toast.makeText(MainActivity.this, "Băng chuyền " + directionText, Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe trạng thái băng chuyền khi khởi động
        databaseReference.child("Devices").child("ConveyorBelt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    switchConveyorBelt.setChecked(status == 1 || status == 2);
                    isConveyorReversed = (status == 2);
                    btnConveyorDirection.setText(isConveyorReversed ? "Đảo chiều băng chuyền (Chiều ngược)" : "Đảo chiều băng chuyền (Chiều thuận)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái băng chuyền", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái quạt
        databaseReference.child("Devices").child("Fan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    switchFan.setChecked(status == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái quạt", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái còi
        databaseReference.child("Devices").child("Alarm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    switchAlarm.setChecked(status == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái còi", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái đèn
        databaseReference.child("Devices").child("Light").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    switchLight.setChecked(status == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy trạng thái đèn", Toast.LENGTH_SHORT).show();
            }
        });

        // Điều khiển bật/tắt đèn
        switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            databaseReference.child("Devices").child("Light").setValue(isChecked ? 1 : 0);
        });
    }
}
