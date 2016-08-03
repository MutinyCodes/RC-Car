package co.lujun.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCbt = (Button) findViewById(R.id.btn_cbt_activity);
        Button btnBle = (Button) findViewById(R.id.btn_ble_activity);
        Button btnBle2 = (Button) findViewById(R.id.btn_ble2_activity);

        btnCbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ClassicBluetoothActivity.class));
            }
        });

        btnBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BleActivity.class));
            }
        });

        btnBle2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MutinyCarControllerActivity.class));
            }
        });
    }
}
