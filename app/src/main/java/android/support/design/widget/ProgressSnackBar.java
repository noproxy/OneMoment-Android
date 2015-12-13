package android.support.design.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 12/13/15.
 */
public class ProgressSnackBar {

    private ProgressSnackBar() {

    }

    public static Snackbar with(Snackbar snackbar) {
        Snackbar.SnackbarLayout snackbarLayout = ((Snackbar.SnackbarLayout) snackbar.getView());
        Button actionView = snackbarLayout.getActionView();

        snackbarLayout.removeView(actionView);

        Context context = snackbarLayout.getContext();
        LayoutInflater.from(context).inflate(R.layout.layout_progress_snack_bar, snackbarLayout, true);
        return snackbar;
    }
}

