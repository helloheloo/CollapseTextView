package com.helloheloo.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnRecyclerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        float density = getResources().getDisplayMetrics().density;
        int densityDpi = getResources().getDisplayMetrics().densityDpi;

        Log.e("xxx","density : " + density);
        Log.e("xxx","densityDpi : " + densityDpi);

        btnRecyclerview = (Button) findViewById(R.id.btn_to_recyclerview);
        btnRecyclerview.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_to_recyclerview:
                Intent i = new Intent(this,RecyclerActivity.class);
                startActivity(i);
                break;
        }
    }
}
