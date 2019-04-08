package com.example.marcnebot.projectnfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WriteActivity extends AppCompatActivity {
    private boolean mResumed = false;

    NfcAdapter nfcAdapter;

    PendingIntent mNfcPendingIntent;
    IntentFilter[] mNdefExchangeFilters;


    EditText editText;

    TextView textViewSendData;
    Button buttonEnableSend;
    Button buttonDisableSend;
    Button buttonRead;

    Utils u = new Utils(this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(mTextWatcher);


        textViewSendData = findViewById(R.id.textViewSendData);
        buttonEnableSend = findViewById(R.id.buttonEnableSend);
        buttonDisableSend = findViewById(R.id.buttonDisableSend);
        buttonRead = findViewById(R.id.buttonRead);


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


        buttonDisableSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Send disabled", Toast.LENGTH_SHORT).show();
                disableNdefExchangeMode();
                textViewSendData.setText(getString(R.string.sendDataDisabled));
            }
        });

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReadActivity.class);
                startActivity(intent);
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
            u.createAlertDialog();
        }


        mResumed = true;
        enableNdefExchangeMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
        nfcAdapter.setNdefPushMessage(null, WriteActivity.this);
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
                nfcAdapter.setNdefPushMessage(getNoteAsNdef(), WriteActivity.this);
            }
        }
    };


    private NdefMessage getNoteAsNdef() {
        byte[] textBytes = editText.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[]{}, textBytes);
        return new NdefMessage(new NdefRecord[]{
                textRecord
        });
    }


    private void enableNdefExchangeMode() {
        nfcAdapter.setNdefPushMessage(getNoteAsNdef(), WriteActivity.this);
        nfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode() {
        nfcAdapter.setNdefPushMessage(null, WriteActivity.this);
        nfcAdapter.disableForegroundDispatch(this);
    }


}



/* App Simple
- Permisos
- Comprobar NFC, si no tiene avisar
- Funciones para escribir NFC
- Funciones leer NFC
 */

// pasar string --> si es solo un int: comprobar como int