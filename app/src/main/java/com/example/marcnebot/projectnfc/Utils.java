package com.example.marcnebot.projectnfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

class Utils {
    private Context c;
    NfcAdapter nfcAdapter;


    Utils(Context c) {
        this.c = c;
    }

    void checkNFC() {
        if (nfcAdapter == null) {
            Toast.makeText(c.getApplicationContext(), c.getString(R.string.notNFC), Toast.LENGTH_SHORT).show();
            System.exit(0);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Toast.makeText(c.getApplicationContext(), c.getString(R.string.notAndroidBeam), Toast.LENGTH_SHORT).show();
            System.exit(0);
        }


        if (!nfcAdapter.isEnabled() || !nfcAdapter.isNdefPushEnabled()) {
            createAlertDialog();
        }
    }

    private void createAlertDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(c).create();

        // Set Custom Title
        TextView title = new TextView(c);
        // Title Properties
        title.setText(c.getString(R.string.attention));
        title.setPadding(16, 16, 16, 16);   // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);

        TextView msg = new TextView(c);
        msg.setText(c.getString(R.string.requirements));
        msg.setPadding(16, 16, 16, 16);
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextColor(Color.BLACK);
        alertDialog.setView(msg);

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, c.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                c.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });

        new Dialog(c.getApplicationContext());
        alertDialog.show();

        final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        okBT.setPadding(50, 10, 10, 10);   // Set Position
        okBT.setLayoutParams(neutralBtnLP);

        final Button cancelBT = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams negBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        negBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        cancelBT.setLayoutParams(negBtnLP);
    }

    static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    private static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }
}