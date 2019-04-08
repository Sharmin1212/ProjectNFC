package com.example.marcnebot.projectnfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private boolean mResumed = false;
    boolean canRead = true;
    boolean canWrite = true;
    NfcAdapter nfcAdapter;

    PendingIntent mNfcPendingIntent;
    IntentFilter[] mNdefExchangeFilters;

    int intData;

    EditText editText;

    TextView textViewSendData;
    TextView textViewReceiveData;
    TextView textViewReceivedData;

    Button buttonEnableSend;
    Button buttonEnableReceive;
    Button buttonDisableSend;
    Button buttonDisableReceive;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(mTextWatcher);


        textViewSendData = findViewById(R.id.textViewSendData);
        textViewReceiveData = findViewById(R.id.textViewReceiveData);
        textViewReceivedData = findViewById(R.id.textViewReceivedData);

        buttonEnableSend = findViewById(R.id.buttonEnableSend);
        buttonEnableReceive = findViewById(R.id.buttonEnableReceive);
        buttonDisableSend = findViewById(R.id.buttonDisableSend);
        buttonDisableReceive = findViewById(R.id.buttonDisableReceive);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        buttonEnableSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Send enabled", Toast.LENGTH_SHORT).show();
                enableNdefExchangeMode();
                textViewSendData.setText(getString(R.string.sendDataEnabled));
            }
        });

        buttonEnableReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Receive enabled", Toast.LENGTH_SHORT).show();
                canRead = true;
                textViewReceiveData.setText(getString(R.string.receiveDataEnabled));
            }
        });

        buttonDisableSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Send disabled", Toast.LENGTH_SHORT).show();
                disableNdefExchangeMode();
                textViewSendData.setText(getString(R.string.sendDataDisabled));
            }
        });

        buttonDisableReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Receive disabled", Toast.LENGTH_SHORT).show();
                canRead = false;
                textViewReceiveData.setText(getString(R.string.receiveDataDisabled));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.notNFC), Toast.LENGTH_SHORT).show();
            finish();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Toast.makeText(this, getString(R.string.notAndroidBeam), Toast.LENGTH_SHORT).show();
            finish();
        }


        if (!nfcAdapter.isEnabled() || !nfcAdapter.isNdefPushEnabled()) {
            createAlertDialog();
        }


        mResumed = true;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            NdefMessage[] messages = getNdefMessages(getIntent());
            byte[] payload = messages[0].getRecords()[0].getPayload();
            setNoteBody(new String(payload));
            setIntent(new Intent()); // Consume this intent.
        }
            enableNdefExchangeMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
        nfcAdapter.setNdefPushMessage(null, MainActivity.this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) && canRead) {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (mResumed) {
                nfcAdapter.setNdefPushMessage(getNoteAsNdef(), MainActivity.this);
            }
        }
    };

    private void promptForContent(final NdefMessage msg) {
        new AlertDialog.Builder(this).setTitle("Replace current content?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String body = new String(msg.getRecords()[0].getPayload());
                        setNoteBody(body);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
    }

    private void setNoteBody(String body) {
        textViewReceivedData.setText(body);

        if (isInteger(body)) {
            intData = Integer.parseInt(body);
        }

    }

    private NdefMessage getNoteAsNdef() {
        byte[] textBytes = editText.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[]{}, textBytes);
        return new NdefMessage(new NdefRecord[]{
                textRecord
        });
    }

    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                msgs = new NdefMessage[]{
                        msg
                };
            }
        } else {
            finish();
        }
        return msgs;
    }

    private void enableNdefExchangeMode() {
        nfcAdapter.setNdefPushMessage(getNoteAsNdef(),MainActivity.this);
        nfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode() {
       nfcAdapter.setNdefPushMessage(null, MainActivity.this);
       nfcAdapter.disableForegroundDispatch(this);
    }


    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
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

    public void createAlertDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Set Custom Title
        TextView title = new TextView(this);
        // Title Properties
        title.setText(getString(R.string.attention));
        title.setPadding(16, 16, 16, 16);   // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);

        TextView msg = new TextView(this);
        msg.setText(getString(R.string.requirements));
        msg.setPadding(16, 16, 16, 16);
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextColor(Color.BLACK);
        alertDialog.setView(msg);

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        new Dialog(getApplicationContext());
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
}



/* App Simple
- Permisos
- Comprobar NFC, si no tiene avisar
- Funciones para escribir NFC
- Funciones leer NFC
 */

// pasar string --> si es solo un int: comprobar como int