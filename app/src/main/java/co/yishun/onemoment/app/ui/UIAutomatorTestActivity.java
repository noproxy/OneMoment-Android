package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/12/15.
 */
public class UIAutomatorTestActivity extends AppCompatActivity {


    // The name of the extra data sent through an {@link Intent}.
    public final static String KEY_EXTRA_MESSAGE = "co.yishun.onemoment.app.ui.MESSAGE";

    /**
     * Creates an {@link Intent} for {@link UIAutomatorTestActivity} with the message to be
     * displayed.
     *
     * @param context the {@link Context} where the {@link Intent} will be used
     * @param message a {@link String} with text to be displayed
     * @return an {@link Intent} used to start {@link UIAutomatorTestActivity}
     */
    static public Intent newStartIntent(Context context, String message) {
        Intent newIntent = new Intent(context, UIAutomatorTestActivity.class);
        newIntent.putExtra(KEY_EXTRA_MESSAGE, message);
        return newIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_automator_test);


        // Get the message from the Intent.
        Intent intent = getIntent();
        String s = intent.getStringExtra(KEY_EXTRA_MESSAGE);


        // Show message.
        ((TextView) findViewById(R.id.showTextView)).setText(s);
    }
}