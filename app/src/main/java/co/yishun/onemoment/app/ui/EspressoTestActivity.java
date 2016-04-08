package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import co.yishun.onemoment.app.R;

public class EspressoTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_espresso_test);

    }

    public void onChangeText(View view) {
        Editable e = ((EditText) findViewById(R.id.editText)).getText();
        ((TextView) findViewById(R.id.showTextView)).setText(e);
    }
}
