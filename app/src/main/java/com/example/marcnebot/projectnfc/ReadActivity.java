package com.example.marcnebot.projectnfc;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends AppCompatActivity {
    boolean canRead = true;

    TextView textViewReceiveData;
    TextView textViewReceivedData;
    Button buttonEnableReceive;
    Button buttonDisableReceive;
    Button buttonWrite;

    PendingIntent mNfcPendingIntent;
    IntentFilter[] mNdefExchangeFilters;

    int intData;

    Utils u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        textViewReceiveData = findViewById(R.id.textViewReceiveData);
        textViewReceivedData = findViewById(R.id.textViewReceivedData);
        buttonEnableReceive = findViewById(R.id.buttonEnableReceive);
        buttonDisableReceive = findViewById(R.id.buttonDisableReceive);
        buttonWrite = findViewById(R.id.buttonWrite);

        u = new Utils(this);

        u.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        buttonEnableReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), getString(R.string.receiveEnabled), Toast.LENGTH_SHORT).show();
                canRead = true;
                textViewReceiveData.setText(getString(R.string.receiveDataEnabled));
            }
        });

        buttonDisableReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), getString(R.string.receiveDisabled), Toast.LENGTH_SHORT).show();
                canRead = false;
                textViewReceiveData.setText(getString(R.string.receiveDataDisabled));
            }
        });

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        u.checkNFC();

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
        u.nfcAdapter.setNdefPushMessage(null, ReadActivity.this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) && canRead) {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }
    }

    private void setNoteBody(String body) {
        textViewReceivedData.setText(body);
        if (Utils.isInteger(body)) {
            Toast.makeText(getApplicationContext(), getString(R.string.intReceived), Toast.LENGTH_SHORT).show();
            intData = Integer.parseInt(body);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.stringReceived), Toast.LENGTH_SHORT).show();
        }
    }

    private void promptForContent(final NdefMessage msg) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.replaceContent))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String body = new String(msg.getRecords()[0].getPayload());
                        setNoteBody(body);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();
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
        u.nfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }
}