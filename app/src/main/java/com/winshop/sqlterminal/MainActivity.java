package com.winshop.sqlterminal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.ConfigurationCompat;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    String un, pass, db, ip, phone, email;
    String query = null;
    String server, database, username, password;
    Button connectButton;
    Object[][] resultSet;
    String error_code;
    String z = "";
    Boolean isSuccess = false;
    Integer rowCount = 0;
    ResultSet rs = null;
    String toast_message;
    Boolean knownConnection = false;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SERVER = "server";
    public static final String DATABASE = "database";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PHONE = "phone" ;
    public static final String EMAIL = "email" ;
    public static final String GUID = "guid";
    public static final String LOGO = "logo";
    public static final String ID = "id";
    public static final String SKLAD = "sklad";
    public static final String SKLAD_NAZEV = "sklad_nazev";
    public static final String LOGOSTRING = "logostring";
    public static final String BARCODE_NEW = "barcode_new";
    public static final String BARCODE_IS_NEW = "barcode_is_new";
    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_TO_SEND = "language_to_send";
    public static final String PRINTER_IP = "printer_ip";
    public static final String USERPASSWORD = "userpassword";
    public static final String TERMINAL_ID = "terminal_id";

    // Typeface font;
    String logoStringShort;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String androidId;
    String query_st;
    Boolean allowLogin = false;
    String logoString;
    byte[] logoData;
    Bitmap logo;
    TextView statusTextView;
    String userpassword;
    public static Context contextOfApplication;
    Integer counter, sklad;
    String nazev_skladu, terminal_id;
    Boolean sklad_present = true;
    Button hiddenButton;
    Boolean correct = false;
    String language, deviceLanguage;
    String langString, jazyk, langMessage, printerIPaddress;



    String guid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);
        contextOfApplication = getApplicationContext();
        final Spinner dropdown = new Spinner(MainActivity.this);
        //localization



        TextInputLayout usernameTextObj = (TextInputLayout)    findViewById(R.id.input_username_id);
        //  font = Typeface.createFromAsset(getAssets(), "fonts/ir.ttf");
        //  usernameTextObj .setTypeface(font);





        final ConnectionClass connectionClass = new ConnectionClass();
        Button hiddenButton = findViewById(R.id.hiddenButton);
        String settings = getResources().getString(R.string.connection_settings);
        String settings_notification = getResources().getString(R.string.connection_settings_notification);
        final String str_server = getResources().getString(R.string.Server);
        final String str_database = getResources().getString(R.string.Database);
        final String str_set = getResources().getString(R.string.string_set);
        //String settings = getResources().getString(R.string.connection_settings);
        final String enter_server_msg = getResources().getString(R.string.enter_server_msg);
        final String enter_db_msg = getResources().getString(R.string.enter_db_msg);
        final String con_error_msg = getResources().getString(R.string.connection_error_msg);
        final String login_allowed = getResources().getString(R.string.login_allowed);
        final String login_denied = getResources().getString(R.string.login_denied);
        final String login_success = getResources().getString(R.string.login_success);
        final String error = getResources().getString(R.string.error);
        final String close = getResources().getString(R.string.close);
        final String app_settings_title = getResources().getString(R.string.app_settings_title);
        final String app_settings_notification = getResources().getString(R.string.app_settings_notification);
        final String str_password = getResources().getString(R.string.password);
        final String registration_title = getResources().getString(R.string.registration_title);
        String amd_pw = getResources().getString(R.string.adm_pw);
        final String device_not_found = getResources().getString(R.string.device_not_found);
        final String pw_wrong = getResources().getString(R.string.pw_wrong);
        final String wh_alert_title = getResources().getString(R.string.wh_alert_title);
        final String wh_alert_text = getResources().getString(R.string.wh_alert_text);
        final String registration_text = getResources().getString(R.string.registration_text);
        final String enter_pw_msg = getResources().getString(R.string.enter_pw_msg);
        final String cancel = getResources().getString(R.string.cancel);
        final String ok = getResources().getString(R.string.ok);
        final String languageTitle = getResources().getString(R.string.languageTitle);
        final String languageMessage = getResources().getString(R.string.languageMessage);
        final String save = getResources().getString(R.string.save);
        final String changeToEn = getResources().getString(R.string.change_to_en);
        final String changeToCz = getResources().getString(R.string.change_to_cz);

        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor  = preferences.edit();

        server = preferences.getString(SERVER, "");
        database = preferences.getString(DATABASE, "");

        if(server.matches("")|| database.matches("")){
            knownConnection = false;
        }else{
            knownConnection = true;
        }


        username = preferences.getString(USERNAME, "");
        password = preferences.getString(PASSWORD, "");
        phone = preferences.getString(PHONE, "");
        email = preferences.getString(EMAIL, "");
        userpassword = preferences.getString(USERPASSWORD, "");
        guid = preferences.getString(GUID, "");
        logoString = preferences.getString(LOGO,"");
        logoStringShort = "";
        sklad = preferences.getInt(SKLAD, 0);
        language = preferences.getString(LANGUAGE, "");

        editor.putString(BARCODE_NEW, "");
        editor.putBoolean(BARCODE_IS_NEW, false);
        editor.apply();

        statusTextView = findViewById(R.id.statusTextView);

        final EditText loginPwEditText = findViewById(R.id.loginPwEditText);

        ImageButton languageButton = findViewById(R.id.languageButton);


        Locale locale = new Locale("cs");
        Configuration config = getBaseContext().getResources().getConfiguration();

        deviceLanguage = Locale.getDefault().getLanguage();

        deviceLanguage = deviceLanguage;

        if(language == ""){
            language = deviceLanguage;
            //editor.putString(LANGUAGE, language);
        }
        Locale current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        String currentLocale = new String();
        currentLocale = current.getLanguage().toString().toLowerCase();

        switch (language) {
            case "cs":
                languageButton.setImageResource(R.drawable.cz_flag);
                locale = new Locale("cs");
                config = getBaseContext().getResources().getConfiguration();
                if (Build.VERSION.SDK_INT >= 17) {
                    config.setLocale(locale);
                } else {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                break;
            case "en":
                languageButton.setImageResource(R.drawable.english_flag);
                locale = new Locale("en");
                config = getBaseContext().getResources().getConfiguration();
                if (Build.VERSION.SDK_INT >= 17) {
                    config.setLocale(locale);
                } else {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                break;
            default:
                languageButton.setVisibility(View.INVISIBLE);
                locale = new Locale("en");
                config = getBaseContext().getResources().getConfiguration();
                if (Build.VERSION.SDK_INT >= 17) {
                    config.setLocale(locale);
                } else {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                break;
        }
        if(language.toLowerCase().matches(currentLocale)) {

        }else{
            finishAffinity();
            Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(restartIntent);

        }

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] jazyky = new String[2];
                jazyky[0] = "Čeština";
                jazyky[1] = "English";
                final String[] jazyky_code = new String[2];
                jazyky_code[0] = "cs";
                jazyky_code[1] = "en";

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, jazyky);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dropdown.setAdapter(adapter);
                dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parentView,
                                               View selectedItemView, int position, long id) {
                        // Object item = parentView.getItemAtPosition(position);
                        jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                        jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                    }

                });
                AlertDialog.Builder alert_lang = new AlertDialog.Builder(MainActivity.this);

                alert_lang.setTitle(languageTitle);
                alert_lang.setMessage(languageMessage);

                LinearLayout ll=new LinearLayout(MainActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(dropdown);

                alert_lang.setView(ll);

                alert_lang.setCancelable(false);
                ImageButton languageButton = findViewById(R.id.languageButton);
                Locale locale = new Locale("cs");
                Configuration config = getBaseContext().getResources().getConfiguration();

                alert_lang.setPositiveButton(save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        language = jazyk;
                        editor.putString(LANGUAGE, language);
                        editor.apply();
                        ImageButton languageButton = findViewById(R.id.languageButton);
                        Locale locale = new Locale("en");
                        Configuration config = getBaseContext().getResources().getConfiguration();
                        switch (language){
                            case "cs":
                                locale = new Locale("cs");
                                config = getBaseContext().getResources().getConfiguration();
                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                langMessage = changeToCz;
                                break;
                            case "en":
                                locale = new Locale("en");
                                config = getBaseContext().getResources().getConfiguration();
                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                langMessage = changeToEn;
                                break;
                            default:
                                locale = new Locale("en");
                                config = getBaseContext().getResources().getConfiguration();
                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                break;
                        }

                        finishAffinity();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

                alert_lang.show();            }
        });



        androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        editor.putString(ID, androidId);
        editor.apply();

        if (knownConnection == false){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(settings);
            alert.setMessage(settings_notification);

// Set an EditText view to get user input
            final EditText inputServer = new EditText(this);
            final EditText inputDB = new EditText(this);
            final TextView inServer = new TextView(this);
            final TextView inDB = new TextView(this);
            inServer.setText(str_server);
            inDB.setText(str_database);
            inputServer.setText(server);
            inputDB.setText(database);
            LinearLayout ll=new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(inServer);
            ll.addView(inputServer);
            ll.addView(inDB);
            ll.addView(inputDB);

            alert.setView(ll);

            alert.setCancelable(false);


            alert.setPositiveButton(str_set, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                    String edServer = inputServer.getText().toString();
                    String edDB = inputDB.getText().toString();


                    if (edServer == "") {
                        Toast.makeText(MainActivity.this, str_server, Toast.LENGTH_SHORT);

                    } else if (edDB == "") {
                        Toast.makeText(MainActivity.this, str_database, Toast.LENGTH_SHORT);


                    } else {
                        server = edServer;
                        editor.putString(SERVER,server);
                        database = edDB;
                        editor.putString(DATABASE,database);
                        knownConnection = true;
                        editor.apply();

                        try {
                            z = "trying to connect";
                            Connection con = connectionClass.CONN();
                            z = "yeet";

                            if (con == null) {
                                z = "Error in connection with SQL server";
                                statusTextView.setText(con_error_msg);

                            } else {
                                z = "wow, such else";
                                query_st = "exec MOBILNI_LOGIN 2,'" + androidId +"' , '"+ userpassword +"' , '"+ username +"' , '"+ "" +"' , '"+ phone +"' , '"+ email +"','" + sklad + "'";
                                Statement cs = null;
                                cs = con.createStatement();
                                rs = cs.executeQuery("exec MOBILNI_LOGIN 2,'" + androidId +"'");


                                if (rs.next()){

                                    switch(rs.getInt(1)){

                                        case 0:
                                            //zarezeni neexistuje, zobrazit registracni formular
                                            toast_message = "device not found";
                                            logoString = rs.getString(2);


                                            if (logoString.substring(0, 2).toLowerCase()=="0x"){
                                                logoStringShort = logoString.substring(2);
                                            }else{

                                                logoStringShort = logoString;
                                            }

                                            if(logoString == null || logoString == ""){

                                            }else{
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);
                                            }

                                            editor.putString(LOGO, logoString);
                                            editor.apply();

                                            final String[] jazyky = new String[2];
                                            jazyky[0] = "Čeština";
                                            jazyky[1] = "English";
                                            final String[] jazyky_code = new String[2];
                                            jazyky_code[0] = "cs";
                                            jazyky_code[1] = "en";

                                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_dropdown_item_1line, jazyky);

                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            dropdown.setAdapter(adapter);
                                            dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                                public void onItemSelected(AdapterView<?> parentView,
                                                                           View selectedItemView, int position, long id) {
                                                    // Object item = parentView.getItemAtPosition(position);
                                                    jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                                }

                                                public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                                                    jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                                }

                                            });
                                            AlertDialog.Builder alert_lang = new AlertDialog.Builder(MainActivity.this);

                                            alert_lang.setTitle(languageTitle);
                                            alert_lang.setMessage(languageMessage);

                                            LinearLayout ll=new LinearLayout(MainActivity.this);
                                            ll.setOrientation(LinearLayout.VERTICAL);
                                            ll.addView(dropdown);

                                            alert_lang.setView(ll);

                                            alert_lang.setCancelable(false);
                                            ImageButton languageButton = findViewById(R.id.languageButton);
                                            Locale locale = new Locale("cs");
                                            Configuration config = getBaseContext().getResources().getConfiguration();

                                            alert_lang.setPositiveButton(save, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    language = jazyk;
                                                    editor.putString(LANGUAGE_TO_SEND, language);
                                                    editor.putString(LANGUAGE, language);
                                                    editor.apply();
                                                    ImageButton languageButton = findViewById(R.id.languageButton);
                                                    Locale locale = new Locale("en");
                                                    Configuration config = getBaseContext().getResources().getConfiguration();
                                                    switch (language){
                                                        case "cs":
                                                            languageButton.setImageResource(R.drawable.cz_flag);
                                                            locale = new Locale("cs");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                        case "en":
                                                            languageButton.setImageResource(R.drawable.english_flag);
                                                            locale = new Locale("en");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                        default:
                                                            languageButton.setVisibility(View.INVISIBLE);
                                                            locale = new Locale("en");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                    }
                                                    Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                                                    i.putExtra("logostring", logoString);
                                                    startActivity(i);
                                                    // Do something with value!
                                                }
                                            });

                                            alert_lang.show();





                                            break;

                                        case 1:
                                            //zarizeni nalezeno. povoleno prihlaseni
                                            toast_message = "device found";
                                            statusTextView.setText(login_allowed);
                                            logoString = rs.getString(2);
                                            if(logoString == null || logoString == ""){

                                            }else{
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);
                                            }
                                            languageButton = findViewById(R.id.languageButton);
                                            locale = new Locale("cs");
                                            config = getBaseContext().getResources().getConfiguration();
                                            langString = rs.getString(3);
                                            switch (langString){
                                                case "cs":
                                                    languageButton.setImageResource(R.drawable.cz_flag);
                                                    locale = new Locale("cs");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                case "en":
                                                    languageButton.setImageResource(R.drawable.english_flag);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                default:
                                                    languageButton.setVisibility(View.INVISIBLE);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                            }


                                            editor.putString(LOGO, logoString);
                                            editor.putString(LANGUAGE, langString);
                                            editor.apply();
                                            break;

                                        case 2:
                                            //zarizeni nalezeno, ale nepovoleno
                                            statusTextView.setText(login_denied);
                                            logoString = rs.getString(2);
                                            if(logoString == null || logoString == ""){

                                            }else{
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);
                                            }
                                            languageButton = findViewById(R.id.languageButton);
                                            locale = new Locale("cs");
                                            config = getBaseContext().getResources().getConfiguration();
                                            langString = rs.getString(3);
                                            switch (langString){
                                                case "cs":
                                                    languageButton.setImageResource(R.drawable.cz_flag);
                                                    locale = new Locale("cs");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                case "en":
                                                    languageButton.setImageResource(R.drawable.english_flag);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                default:
                                                    languageButton.setVisibility(View.INVISIBLE);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                            }


                                            editor.putString(LOGO, logoString);
                                            editor.putString(LANGUAGE, langString);
                                            editor.apply();
                                            break;

                                        default:
                                            error_code = "Chyba při připojování, zkontrolujte připojení k internetu nebo se obraťe na technickou podporu WinShop";
                                            statusTextView.setText(con_error_msg);
                                            //nemelo by se dit, just-in-case reseni
                                            break;

                                    }

                                } else {
                                    error_code = "no resultSet";
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            isSuccess = false;
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                            alert.setTitle(error);
                            alert.setMessage(ex.toString());

                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });
                            alert.show();

                        }
                    }
                    // Do something with value!
                }
            });

            alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    exit(0);
                }
            });
            alert.show();
            knownConnection = true;

        }else{


            try {
                z = "trying to connect";
                Connection con = connectionClass.CONN();
                z = "yeet";

                if (con == null) {
                    z = "Error in connection with SQL server";
                    statusTextView.setText(con_error_msg);

                } else {
                    z = "wow, such else";
                    Statement cs = null;
                    cs = con.createStatement();
                    rs = cs.executeQuery("exec MOBILNI_LOGIN 2,'" + androidId +"'");


                    if (rs.next()){

                        switch(rs.getInt(1)){

                            case 0:
                                //zarezeni neexistuje, zobrazit registracni formular
                                toast_message = "device not found";
                                logoString = rs.getString(2);
                                if(logoString == null || logoString == ""){

                                }else{
                                    logoData = hexStringToByteArray(logoString);
                                    logo = BitmapFactory.decodeByteArray(logoData, 0,
                                            logoData.length);
                                }
                                editor.putString(LOGO, logoString);
                                editor.apply();
                                final String[] jazyky = new String[2];
                                jazyky[0] = "Čeština";
                                jazyky[1] = "English";
                                final String[] jazyky_code = new String[2];
                                jazyky_code[0] = "cs";
                                jazyky_code[1] = "en";

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_dropdown_item_1line, jazyky);

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dropdown.setAdapter(adapter);
                                dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                    public void onItemSelected(AdapterView<?> parentView,
                                                               View selectedItemView, int position, long id) {
                                        // Object item = parentView.getItemAtPosition(position);
                                        jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                    }

                                    public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                                        jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                    }

                                });
                                AlertDialog.Builder alert_lang = new AlertDialog.Builder(MainActivity.this);

                                alert_lang.setTitle(languageTitle);
                                alert_lang.setMessage(languageMessage);

                                LinearLayout ll=new LinearLayout(MainActivity.this);
                                ll.setOrientation(LinearLayout.VERTICAL);
                                ll.addView(dropdown);

                                alert_lang.setView(ll);

                                alert_lang.setCancelable(false);
                                languageButton = findViewById(R.id.languageButton);
                                locale = new Locale("en");
                                config = getBaseContext().getResources().getConfiguration();

                                alert_lang.setPositiveButton(save, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        language = jazyk;
                                        editor.putString(LANGUAGE_TO_SEND, language);
                                        editor.putString(LANGUAGE, language);
                                        editor.apply();
                                        ImageButton languageButton = findViewById(R.id.languageButton);
                                        Locale locale = new Locale("en");
                                        Configuration config = getBaseContext().getResources().getConfiguration();
                                        switch (language){
                                            case "cs":
                                                languageButton.setImageResource(R.drawable.cz_flag);
                                                locale = new Locale("cs");
                                                config = getBaseContext().getResources().getConfiguration();
                                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                break;
                                            case "en":
                                                languageButton.setImageResource(R.drawable.english_flag);
                                                locale = new Locale("en");
                                                config = getBaseContext().getResources().getConfiguration();
                                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                break;
                                            default:
                                                languageButton.setVisibility(View.INVISIBLE);
                                                locale = new Locale("en");
                                                config = getBaseContext().getResources().getConfiguration();
                                                if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                break;
                                        }
                                        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                                        i.putExtra("logostring", logoString);
                                        startActivity(i);
                                        // Do something with value!
                                    }
                                });

                                alert_lang.show();
                                break;

                            case 1:
                                //zarizeni nalezeno. povoleno prihlaseni
                                toast_message = "device found";


                                allowLogin = true;
                                logoString = rs.getString(2);
                                if(logoString == null || logoString == ""){

                                }else{
                                    logoData = hexStringToByteArray(logoString);
                                    logo = BitmapFactory.decodeByteArray(logoData, 0,
                                            logoData.length);
                                }
                                editor.putString(LOGO, logoString);
                                editor.apply();

                                languageButton = findViewById(R.id.languageButton);
                                locale = new Locale("cs");
                                config = getBaseContext().getResources().getConfiguration();
                                langString = rs.getString(3);
                                switch (langString){
                                    case "cs":
                                        languageButton.setImageResource(R.drawable.cz_flag);
                                        locale = new Locale("cs");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                    case "en":
                                        languageButton.setImageResource(R.drawable.english_flag);
                                        locale = new Locale("en");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                    default:
                                        languageButton.setVisibility(View.INVISIBLE);
                                        locale = new Locale("en");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                }
                                statusTextView.setText(login_allowed);
                                break;

                            case 2:
                                toast_message ="";
                                logoString = rs.getString(2);
                                if(logoString == null || logoString == ""){

                                }else{
                                    logoData = hexStringToByteArray(logoString);
                                    logo = BitmapFactory.decodeByteArray(logoData, 0,
                                            logoData.length);
                                }
                                editor.putString(LOGO, logoString);
                                editor.apply();

                                languageButton = findViewById(R.id.languageButton);
                                locale = new Locale("cs");
                                config = getBaseContext().getResources().getConfiguration();
                                langString = rs.getString(3);
                                switch (langString){
                                    case "cs":
                                        languageButton.setImageResource(R.drawable.cz_flag);
                                        locale = new Locale("cs");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                    case "en":
                                        languageButton.setImageResource(R.drawable.english_flag);
                                        locale = new Locale("en");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                    default:
                                        languageButton.setVisibility(View.INVISIBLE);
                                        locale = new Locale("en");
                                        config = getBaseContext().getResources().getConfiguration();
                                        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                        break;
                                }

                                statusTextView.setText(login_denied);

                                break;
                            default:
                                error_code = "Chyba v ověření";
                                statusTextView.setText(con_error_msg);


                                //nemelo by se dit, just-in-case reseni
                        }
                    } else {
                        error_code = "no resultSet";
                    }
                }
            }
            catch (Exception ex)
            {
                isSuccess = false;



            }
            hiddenButton.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick (View arg0){
                    final AlertDialog.Builder alertPw = new AlertDialog.Builder(MainActivity.this);

                    alertPw.setTitle(app_settings_title);
                    alertPw.setMessage(app_settings_notification);

// Set an EditText view to get user input
                    final EditText adminPasswordEdittext = new EditText(MainActivity.this);
                    final TextView adminPwTextView = new TextView(MainActivity.this);
                    adminPwTextView.setText(str_password);
                    adminPasswordEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    LinearLayout ll=new LinearLayout(MainActivity.this);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.addView(adminPwTextView);
                    ll.addView(adminPasswordEdittext);

                    alertPw.setView(ll);
                    alertPw.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String heslo = adminPasswordEdittext.getText().toString();

                            if(heslo.matches("fjord") || heslo.matches("defAdmPw")){

                                correct = true;

                                alertPw.setTitle(registration_title);
                                alertPw.setMessage(registration_text);
                                final EditText inputServer = new EditText(MainActivity.this);
                                final EditText inputDB = new EditText(MainActivity.this);
                                final TextView inServer = new TextView(MainActivity.this);
                                final TextView inDB = new TextView(MainActivity.this);
                                inServer.setText(str_server);
                                inDB.setText(str_database);
                                inputServer.setText(server);
                                inputDB.setText(database);
                                LinearLayout ll=new LinearLayout(MainActivity.this);
                                ll.setOrientation(LinearLayout.VERTICAL);
                                ll.addView(inServer);
                                ll.addView(inputServer);
                                ll.addView(inDB);
                                ll.addView(inputDB);

                                alertPw.setView(ll);
                                alertPw.setPositiveButton(str_set, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {


                                        String edServer = inputServer.getText().toString();
                                        String edDB = inputDB.getText().toString();


                                        if (edServer == "") {
                                            Toast.makeText(MainActivity.this, str_server, Toast.LENGTH_SHORT);

                                        } else if (edDB == "") {
                                            Toast.makeText(MainActivity.this, str_database, Toast.LENGTH_SHORT);


                                        } else {
                                            server = edServer;
                                            editor.putString(SERVER,server);
                                            database = edDB;
                                            editor.putString(DATABASE,database);
                                            editor.apply();
                                        }


                                        // Do something with value!
                                    }
                                });

                                alertPw.setNegativeButton(close, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        return;
                                    }
                                });
                                alertPw.show();

                                // Intent intent_pw = new Intent(MainActivity.this, DebugMenuActivity.class);
                                // startActivity(intent_pw);
                            }else{


                                correct = false;}
                        }


                    });
                    alertPw.setNegativeButton(close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            return;
                        }
                    });

                    alertPw.show();

                    return true;

                }





            });

            if (correct == true){

                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                alert.setTitle(registration_title);
                alert.setMessage(registration_text);

// Set an EditText view to get user input
                final EditText inputServer = new EditText(MainActivity.this);
                final EditText inputDB = new EditText(MainActivity.this);
                final TextView inServer = new TextView(MainActivity.this);
                final TextView inDB = new TextView(MainActivity.this);
                inServer.setText(str_server);
                inDB.setText(str_database);
                inputServer.setText(server);
                inputDB.setText(database);
                LinearLayout ll=new LinearLayout(MainActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(inServer);
                ll.addView(inputServer);
                ll.addView(inDB);
                ll.addView(inputDB);

                alert.setView(ll);

                alert.setPositiveButton(str_set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        String edServer = inputServer.getText().toString();
                        String edDB = inputDB.getText().toString();


                        if (edServer == "") {
                            Toast.makeText(MainActivity.this, enter_server_msg, Toast.LENGTH_SHORT);

                        } else if (edDB == "") {
                            Toast.makeText(MainActivity.this, enter_db_msg, Toast.LENGTH_SHORT);


                        } else {
                            server = edServer;
                            editor.putString(SERVER,server);
                            database = edDB;
                            editor.putString(DATABASE,database);
                            editor.apply();
                        }


                        // Do something with value!
                    }
                });

                alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });

                alert.show();
            }


            connectButton = findViewById(R.id.mainConnectButton);

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Clicked..", Toast.LENGTH_SHORT);
                    if (loginPwEditText.getText().toString() == ""){
                        statusTextView.setText(enter_pw_msg);
                    }else {

                        userpassword = loginPwEditText.getText().toString();


                        try {
                            z = "trying to connect";
                            Connection con = connectionClass.CONN();
                            z = "yeet";

                            if (con == null) {
                                z = "Error in connection with SQL server";
                                statusTextView.setText(con_error_msg);

                            } else {
                                z = "wow, such else";

                                Statement cs = null;
                                cs = con.createStatement();
                                rs = cs.executeQuery("exec MOBILNI_LOGIN 2,'" + androidId +"'");


                                if (rs.next()){

                                    switch(rs.getInt(1)){

                                        case 0:
                                            //zarezeni neexistuje, zobrazit registracni formular
                                            toast_message = "device not found";
                                            logoString = rs.getString(2);
                                            if(logoString == null || logoString == ""){

                                            }else{
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);
                                            }
                                            editor.putString(LOGO, logoString);
                                            editor.apply();
                                            final String[] jazyky = new String[2];
                                            jazyky[0] = "Čeština";
                                            jazyky[1] = "English";
                                            final String[] jazyky_code = new String[2];
                                            jazyky_code[0] = "cs";
                                            jazyky_code[1] = "en";

                                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_dropdown_item_1line, jazyky);

                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            dropdown.setAdapter(adapter);
                                            dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                                public void onItemSelected(AdapterView<?> parentView,
                                                                           View selectedItemView, int position, long id) {
                                                    // Object item = parentView.getItemAtPosition(position);
                                                    jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                                }

                                                public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                                                    jazyk = jazyky_code[dropdown.getSelectedItemPosition()];
                                                }

                                            });
                                            AlertDialog.Builder alert_lang = new AlertDialog.Builder(MainActivity.this);

                                            alert_lang.setTitle(languageTitle);
                                            alert_lang.setMessage(languageMessage);

                                            LinearLayout ll=new LinearLayout(MainActivity.this);
                                            ll.setOrientation(LinearLayout.VERTICAL);
                                            ll.addView(dropdown);

                                            alert_lang.setView(ll);

                                            alert_lang.setCancelable(false);
                                            ImageButton languageButton = findViewById(R.id.languageButton);
                                            Locale locale = new Locale("cs");
                                            Configuration config = getBaseContext().getResources().getConfiguration();

                                            alert_lang.setPositiveButton(save, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    language = jazyk;
                                                    editor.putString(LANGUAGE_TO_SEND, language);
                                                    editor.putString(LANGUAGE, language);
                                                    editor.apply();
                                                    ImageButton languageButton = findViewById(R.id.languageButton);
                                                    Locale locale = new Locale("en");
                                                    Configuration config = getBaseContext().getResources().getConfiguration();
                                                    switch (language){
                                                        case "cs":
                                                            languageButton.setImageResource(R.drawable.cz_flag);
                                                            locale = new Locale("cs");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                        case "en":
                                                            languageButton.setImageResource(R.drawable.english_flag);
                                                            locale = new Locale("en");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                        default:
                                                            languageButton.setVisibility(View.INVISIBLE);
                                                            locale = new Locale("en");
                                                            config = getBaseContext().getResources().getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                            break;
                                                    }
                                                    Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                                                    i.putExtra("logostring", logoString);
                                                    startActivity(i);
                                                    // Do something with value!
                                                }
                                            });

                                            alert_lang.show();
                                            break;

                                        case 1:
                                            //zarizeni nalezeno. povoleno prihlaseni
                                            toast_message = "device found";

                                            allowLogin = true;
                                            logoString = rs.getString(2);
                                            if(logoString == null || logoString == ""){

                                            }else {
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);

                                                editor.putString(LOGO, logoString);

                                                if (logoString.startsWith("0x") || logoString.startsWith("0X")) {

                                                    logoString = logoString.substring(2);

                                                }

                                                editor.putString(LOGOSTRING, logoString);
                                                editor.apply();

                                            }

                                            languageButton = findViewById(R.id.languageButton);
                                            locale = new Locale("cs");
                                            config = getBaseContext().getResources().getConfiguration();
                                            langString = rs.getString(3);
                                            switch (langString){
                                                case "cs":
                                                    languageButton.setImageResource(R.drawable.cz_flag);
                                                    locale = new Locale("cs");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                case "en":
                                                    languageButton.setImageResource(R.drawable.english_flag);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                default:
                                                    languageButton.setVisibility(View.INVISIBLE);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                            }
                                            statusTextView.setText(device_not_found);



                                            query_st = "exec MOBILNI_LOGIN 1,'" + androidId + "' , '" + userpassword + "'";
                                            rs = cs.executeQuery(query_st);


                                            if (rs.next()) {


                                                switch (rs.getInt("PASS")) {

                                                    case 0:
                                                        //Spatne heslo
                                                        toast_message = "Zadali jste špatné heslo";
                                                        statusTextView.setText(pw_wrong);


                                                        break;

                                                    case 1:
                                                        //zarizeni nalezeno. povoleno prihlaseni
                                                        toast_message = "device found";
                                                        allowLogin = true;
                                                        statusTextView.setText(login_allowed);


                                                        guid = rs.getString("guid");
                                                        printerIPaddress = rs.getString("TISKARNA_ETIKET_IP");
                                                        String jazyk = rs.getString("JAZYK");
                                                        editor.putString(GUID, guid);
                                                        editor.putString(PRINTER_IP, printerIPaddress);
                                                        editor.putString(LANGUAGE, jazyk);
                                                        editor.apply();
                                                        Integer skl = rs.getInt("SKLAD_ID");
                                                        terminal_id = rs.getString("ID");
                                                        editor.putString(TERMINAL_ID, terminal_id);
                                                        editor.apply();



                                                        if (rs.getInt("SKLAD_ID")== 0){
                                                            sklad_present = false;
                                                            try {
                                                                Connection conn = connectionClass.CONN();
                                                                if (conn == null){
                                                                    z = "zero connection";
                                                                }else {


                                                                    PreparedStatement ps = null;
                                                                    ps = conn.prepareStatement("exec MOBILNI_LOGIN 4,'" + androidId  + "','','','','','','','" + guid + "'");
                                                                    rs = ps.executeQuery();
                                                                    ResultSetMetaData rsmd = rs.getMetaData();
                                                                    rowCount = 0;
                                                                    while (rs.next()) {
                                                                        rowCount = rs.getRow();
                                                                    }

                                                                    rs = ps.executeQuery();
                                                                    ResultSetMetaData rsmd2 = rs.getMetaData();
                                                                    final String[] sklady = new String[rowCount];
                                                                    final Integer[] skladyID = new Integer[rowCount];
                                                                    final String[] sklady_string = new String[rowCount];
                                                                    counter = 0;
                                                                    while (rs.next()) {
                                                                        skladyID[counter] = rs.getInt(1);
                                                                        sklady[counter] = rs.getString(2);
                                                                        sklady_string[counter] = rs.getString(1) + " - " + rs.getString(2);
                                                                        counter++;
                                                                    }


                                                                    ArrayAdapter<String> adapter_b = new ArrayAdapter<String>(MainActivity.this,
                                                                            android.R.layout.simple_dropdown_item_1line, sklady);

                                                                    adapter_b.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                    dropdown.setAdapter(adapter_b);
                                                                    dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                                                        public void onItemSelected(AdapterView<?> parentView,
                                                                                                   View selectedItemView, int position, long id) {
                                                                            // Object item = parentView.getItemAtPosition(position);

                                                                            sklad = skladyID[dropdown.getSelectedItemPosition()];
                                                                            nazev_skladu = sklady[dropdown.getSelectedItemPosition()];

                                                                        }

                                                                        public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                                                                            sklad = skladyID[dropdown.getSelectedItemPosition()];
                                                                            nazev_skladu = sklady[dropdown.getSelectedItemPosition()];
                                                                        }

                                                                    });
                                                                    AlertDialog.Builder alert_sklad = new AlertDialog.Builder(MainActivity.this);

                                                                    alert_sklad.setTitle(wh_alert_title);
                                                                    alert_sklad.setMessage(wh_alert_text);

                                                                    LinearLayout ll_b=new LinearLayout(MainActivity.this);
                                                                    ll_b.setOrientation(LinearLayout.VERTICAL);
                                                                    ll_b.addView(dropdown);

                                                                    alert_sklad.setView(ll_b);
                                                                    alert_sklad.setCancelable(false);
                                                                    alert_sklad.setPositiveButton(str_set, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            editor.putInt(SKLAD, sklad);
                                                                            editor.putString(SKLAD_NAZEV, nazev_skladu);
                                                                            editor.apply();

                                                                            try {
                                                                                z = "trying to connect";
                                                                                Connection con_2 = connectionClass.CONN();

                                                                                z = "yeet";

                                                                                if (con_2 == null) {
                                                                                    statusTextView.setText(con_error_msg);

                                                                                    z = "Error in connection with SQL server";
                                                                                } else {
                                                                                    z = "wow, such else";
                                                                                    query_st = "exec MOBILNI_LOGIN 5,'" + androidId  + "','','','','','','" + sklad+ "','" + guid + "'";
                                                                                    Statement cs_2 = null;
                                                                                    cs_2 = con_2.createStatement();
                                                                                    rs = cs_2.executeQuery(query_st);

                                                                                    if (rs.next()){

                                                                                        switch(rs.getInt(1)){

                                                                                            case 0:
                                                                                                Toast.makeText(MainActivity.this, "Chybný přístup, zkontrolujte připojení a restartujte aplikaci", Toast.LENGTH_SHORT);
                                                                                                sklad_present = false;
                                                                                                break;
                                                                                            /*default:
                                                                                                error_code = "Chyba v ověření";
                                                                                                //nemelo by se dit, just-in-case reseni
                                                                                                break;*/

                                                                                        }
                                                                                    } else {
                                                                                        error_code = "no resultSet";
                                                                                    }
                                                                                    if (sklad_present == true){
                                                                                        Intent i = new Intent(MainActivity.this, AppActivity.class);
                                                                                        startActivity(i);

                                                                                    }
                                                                                }
                                                                            }
                                                                            catch (Exception ex)
                                                                            {
                                                                                isSuccess = false;


                                                                            }
                                                                            // Do something with value!
                                                                        }
                                                                    });
                                                                    alert_sklad.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            Intent failsafeIntent = new Intent(MainActivity.this, MainActivity.class);
                                                                            startActivity(failsafeIntent);
                                                                        }
                                                                    });
                                                                    alert_sklad.show();
                                                                }

                                                            }catch (Exception ex)
                                                            {
                                                                isSuccess = false;
                                                                z = "Exceptions in regCon";


                                                            }

                                                        }else{
                                                            sklad = rs.getInt("SKLAD_ID");
                                                            editor.putInt(SKLAD, sklad);
                                                            editor.apply();
                                                            statusTextView.setText(login_success);
                                                            Intent i2 = new Intent(MainActivity.this, AppActivity.class);
                                                            startActivity(i2);
                                                            break;
                                                        }
                                                        break;


                                                    default:
                                                        error_code = "Chyba v ověření";
                                                        statusTextView.setText(con_error_msg);
                                                        break;

                                                    //nemelo by se dit, just-in-case reseni
                                                }
                                            } else {
                                                error_code = "no resultSet";
                                            }
                                            break;



                                        case 2:
                                            toast_message ="Toto zařízení nemá povolený přístup nebo bylo deaktivováno, vyčkejte na potvrzující e-mail nebo se obraťte na servis.sql@winshop.cz";
                                            logoString = rs.getString(2);
                                            if(logoString == null || logoString == ""){

                                            }else{
                                                logoData = hexStringToByteArray(logoString);
                                                logo = BitmapFactory.decodeByteArray(logoData, 0,
                                                        logoData.length);
                                            }
                                            editor.putString(LOGO, logoString);
                                            editor.apply();

                                            languageButton = findViewById(R.id.languageButton);
                                            locale = new Locale("cs");
                                            config = getBaseContext().getResources().getConfiguration();
                                            langString = rs.getString(3);
                                            switch (langString){
                                                case "cs":
                                                    languageButton.setImageResource(R.drawable.cz_flag);
                                                    locale = new Locale("cs");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                case "en":
                                                    languageButton.setImageResource(R.drawable.english_flag);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                default:
                                                    languageButton.setVisibility(View.INVISIBLE);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    config.locale = locale;
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                            }

                                            statusTextView.setText(login_denied);
                                            break;

                                        default:
                                            error_code = "Chyba v ověření";
                                            languageButton = findViewById(R.id.languageButton);
                                            locale = new Locale("cs");
                                            config = getBaseContext().getResources().getConfiguration();
                                            langString = rs.getString(3);
                                            switch (langString){
                                                case "cs":
                                                    languageButton.setImageResource(R.drawable.cz_flag);
                                                    locale = new Locale("cs");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                case "en":
                                                    languageButton.setImageResource(R.drawable.english_flag);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                                default:
                                                    languageButton.setVisibility(View.INVISIBLE);
                                                    locale = new Locale("en");
                                                    config = getBaseContext().getResources().getConfiguration();
                                                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                                                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                                    break;
                                            }
                                            //nemelo by se dit, just-in-case reseni
                                            break;

                                    }
                                } else {
                                    error_code = "no resultSet";
                                }

                            }
                        } catch (Exception ex) {
                            isSuccess = false;




                        }

                    }
                }


            });{

            }

        }









    } // End of onCreate

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
} // End of App