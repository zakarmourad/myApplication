package com.zakar.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CountryCodePicker picker;
    private EditText searchTxt;
    private RadioButton number, name;
    private Button search;
    private Button add;
    private PhoneNumberUtil phoneNumberUtil;
    RequestQueue requestQueue;
    String insertUrl = "http://192.168.137.160/Numberbook/insert.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
        picker = findViewById(R.id.picker);
        searchTxt = findViewById(R.id.searchTxt);
        number = findViewById(R.id.number);
        name = findViewById(R.id.name);
        search = findViewById(R.id.search);
        add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!number.isChecked() && !name.isChecked()) {
                    Toast.makeText(MainActivity.this, "Veuillez choisir une option d'Ajout", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(number.isChecked()) {
                        Phonenumber.PhoneNumber pn1 = null;
                        try {
                            pn1 = phoneNumberUtil.parse(searchTxt.getText().toString(), picker.getSelectedCountryNameCode());
                        } catch (NumberParseException e) {
                            e.printStackTrace();
                        }
                        if(!phoneNumberUtil.isValidNumber(pn1)) {
                            Toast.makeText(MainActivity.this, "Numéro de telephone invalide!", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent(Intent.ACTION_INSERT);
                            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            intent.putExtra(ContactsContract.Intents.Insert.PHONE, searchTxt.getText().toString());
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    }


                    else{
                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, searchTxt.getText().toString());
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!number.isChecked() && !name.isChecked()) {
                    Toast.makeText(MainActivity.this, "Veuillez choisir une option de recherche", Toast.LENGTH_SHORT).show();
                }else {
                    if(number.isChecked()) {
                        String[] display = loadContactByNumber(getContentResolver(), searchTxt.getText().toString(), picker.getSelectedCountryNameCode());
                        if(display != null) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                            alertDialogBuilder.setMessage(display[0] + " " + display[1]);


                            alertDialogBuilder.setPositiveButton("CALL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+display[1]));
                                    startActivity(call);
                                }
                            });
                            alertDialogBuilder.setNegativeButton("SMS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent msg = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+display[1]));
                                    startActivity(msg);
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }else {
                        String[] display = loadContactByName(getContentResolver(), searchTxt.getText().toString(), picker.getSelectedCountryNameCode());
                        if(display != null) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                            alertDialogBuilder.setMessage(display[0] + " " + display[1]);


                            alertDialogBuilder.setPositiveButton("CALL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+display[1]));
                                    startActivity(call);
                                }
                            });
                            alertDialogBuilder.setNegativeButton("SMS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent msg = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+display[1]));
                                    startActivity(msg);
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                }
            }
        });

    }

    @SuppressLint("Range")
    public String[] loadContactByNumber (ContentResolver cr, String number, String region) {
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            try {
                Phonenumber.PhoneNumber pn1 = phoneNumberUtil.parse(number, region);
                Phonenumber.PhoneNumber pn2 = phoneNumberUtil.parse(phoneNumber, region);
                if(!phoneNumberUtil.isValidNumber(pn1)) {
                    Toast.makeText(MainActivity.this, "Numéro de telephone invalide!", Toast.LENGTH_SHORT).show();
                }else if(pn1.getNationalNumber() == pn2.getNationalNumber()) {
                    return new String[]{phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)), phoneNumber};
                }else {
                    Toast.makeText(MainActivity.this, "Numéro de telephone introuvable!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
        }
        phones.close();
        return null;
    }

    @SuppressLint("Range")
    public String[] loadContactByName (ContentResolver cr, String name, String region) {
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String nameR = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            if(name.equals(nameR)) {
                try {
                    Phonenumber.PhoneNumber pn = phoneNumberUtil.parse(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), region);
                    if(phoneNumberUtil.isValidNumber(pn)) {
                        return new String[]{nameR, phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))};
                    }
                } catch (NumberParseException e) {
                    e.printStackTrace();
                }
            }
        }
        phones.close();
        Toast.makeText(MainActivity.this, "Nom introuvable!", Toast.LENGTH_SHORT).show();
        return null;
    }

}