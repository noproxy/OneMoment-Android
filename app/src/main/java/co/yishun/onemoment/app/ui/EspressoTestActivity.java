package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import co.yishun.onemoment.app.R;

public class EspressoTestActivity extends AppCompatActivity {
    public static final String TEXT_CHANGE_TO = "Text changed!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_espresso_test);

    }

    public void onChangeText(View view) {
        ((EditText) findViewById(R.id.nameEditText)).setText(TEXT_CHANGE_TO);
    }
}
