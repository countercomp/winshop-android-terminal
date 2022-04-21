package com.winshop.sqlterminal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.PreparedStatement;

public class AppActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String BARCODE = "barcode";
    public static final String GUID = "guid";
    public static final String ID = "id";
    public static final String BARCODE_IS_NEW = "barcode_is_new";
    public static final String BAREVNE_VELIKOSTNI = "barevne_velikostni";
    public static final String SKLAD = "sklad";
    public static final String DOKLAD_STAV = "doklad_stav";
    public static final String DOKLAD_HLAVICKA_ID = "doklad_hlavicka_id";
    public static final String CURRENT_HLAVICKA_ID = "current_hlavicka_id";
    public static final String SERVER = "server";
    public static final String DATABASE = "database";
    public static final String TERMINAL_ID = "terminal_id";
    public static final String SKLAD_NAZEV = "sklad_nazev";

    String guid, androidID;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ResultSet rs;
    String z;
    String[] resultSet;
    String query;
    ArrayList querydata;
    public static Context contextOfApplication;
    Button logoutButton, PDTButton, formViewButton, warehouseButton;
    ImageView winshopLogoView;
    Boolean correct = false;
    String server, database, username, password, version, sklad, terminalID;
    TextView versionTV, warehouseTV, terminalIDTV;
    Integer skladInt;
    String error_code;
    //String z = "";
    Boolean isSuccess = false;
    Boolean sklad_present = true;
    String androidId;
    Integer rowCount = 0;
    Integer counter, skladID;
    String query_st;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_app);
        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = preferences.edit();
        server = preferences.getString(SERVER, "");
        database = preferences.getString(DATABASE, "");
        skladInt = preferences.getInt(SKLAD, 0);
        sklad = skladInt.toString();
        terminalID = preferences.getString(TERMINAL_ID, "");
        final String connection_error_string = getResources().getString(R.string.connection_error_alt);
        String error = getResources().getString(R.string.error);
        String ok = getResources().getString(R.string.ok);
        final String preferenceErrorMsg = getResources().getString(R.string.preferencesErrorMsg);
        //final String error = getResources().getString(R.string.error);
        final String close = getResources().getString(R.string.close);
        final String cancel = getResources().getString(R.string.cancel);
        final ConnectionClass connectionClass = new ConnectionClass();
        final Spinner dropdown = new Spinner(AppActivity.this);
        final String wh_alert_title = getResources().getString(R.string.wh_alert_title);
        final String wh_alert_text = getResources().getString(R.string.wh_alert_text);
        version = "";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        versionTV = (TextView) findViewById(R.id.versionTextView);
        warehouseTV = (TextView) findViewById(R.id.appSkladTextView);
        terminalIDTV = (TextView) findViewById(R.id.terminalIDTextView);
        versionTV.setText(version);
        warehouseTV.setText(sklad);
        terminalIDTV.setText("Terminál " + terminalID.toString());
        guid = preferences.getString(GUID, "");
        androidID = preferences.getString(ID, Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));

        warehouseButton = (Button) findViewById(R.id.warehouseSelectionButton);
        final String app_settings_title = getResources().getString(R.string.app_settings_title);
        final String app_settings_notification = getResources().getString(R.string.app_settings_notification);
        final String registration_title = getResources().getString(R.string.registration_title);
        String amd_pw = getResources().getString(R.string.adm_pw);
        final String device_not_found = getResources().getString(R.string.device_not_found);
        final String pw_wrong = getResources().getString(R.string.pw_wrong);
        final String str_server = getResources().getString(R.string.Server);
        final String str_database = getResources().getString(R.string.Database);
        final String str_set = getResources().getString(R.string.string_set);
        final String registration_text = getResources().getString(R.string.registration_text);

        winshopLogoView = findViewById(R.id.winShopLogoImageVIew);
        winshopLogoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder alertPw = new AlertDialog.Builder(AppActivity.this);

                alertPw.setTitle(app_settings_title);
                alertPw.setMessage(app_settings_notification);
                final String str_password = getResources().getString(R.string.password);

// Set an EditText view to get user input
                final EditText adminPasswordEdittext = new EditText(AppActivity.this);
                final TextView adminPwTextView = new TextView(AppActivity.this);
                adminPwTextView.setText(str_password);
                adminPasswordEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                LinearLayout ll=new LinearLayout(AppActivity.this);
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
                            final EditText inputServer = new EditText(AppActivity.this);
                            final EditText inputDB = new EditText(AppActivity.this);
                            final TextView inServer = new TextView(AppActivity.this);
                            final TextView inDB = new TextView(AppActivity.this);
                            inServer.setText(str_server);
                            inDB.setText(str_database);
                            inputServer.setText(server);
                            inputDB.setText(database);
                            LinearLayout ll=new LinearLayout(AppActivity.this);
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
                                        Toast.makeText(AppActivity.this, str_server, Toast.LENGTH_SHORT);

                                    } else if (edDB == "") {
                                        Toast.makeText(AppActivity.this, str_database, Toast.LENGTH_SHORT);


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

                return false;
            }
        });

        logoutButton = (Button) findViewById(R.id.logoutButton);
        PDTButton = (Button) findViewById(R.id.pdtButton);
        formViewButton = (Button) findViewById(R.id.formViewButton);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        PDTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                Intent PDTIntent = new Intent(AppActivity.this, PDTSelectionActivity.class);
                startActivity(PDTIntent);
            }
        });

        formViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                Intent formViewIntent = new Intent(AppActivity.this, FormViewActivity.class);
                startActivity(formViewIntent);
            }
        });

        warehouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sklad_present = false;
                try {
                    Connection conn = connectionClass.CONN();
                    if (conn == null){
                        z = "zero connection";
                    }else {


                        PreparedStatement ps = null;
                        ps = conn.prepareStatement("exec MOBILNI_LOGIN 4,'" + androidID  + "','','','','','','','" + guid + "'");
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


                        ArrayAdapter<String> adapter_b = new ArrayAdapter<String>(AppActivity.this,
                                android.R.layout.simple_dropdown_item_1line, sklady);

                        adapter_b.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dropdown.setAdapter(adapter_b);
                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            public void onItemSelected(AdapterView<?> parentView,
                                                       View selectedItemView, int position, long id) {
                                // Object item = parentView.getItemAtPosition(position);

                                skladInt = skladyID[dropdown.getSelectedItemPosition()];
                                sklad = sklady[dropdown.getSelectedItemPosition()];

                            }

                            public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                                skladInt = skladyID[dropdown.getSelectedItemPosition()];
                                sklad = sklady[dropdown.getSelectedItemPosition()];
                            }

                        });
                        AlertDialog.Builder alert_sklad = new AlertDialog.Builder(AppActivity.this);

                        alert_sklad.setTitle(wh_alert_title);
                        alert_sklad.setMessage(wh_alert_text);

                        LinearLayout ll_b=new LinearLayout(AppActivity.this);
                        ll_b.setOrientation(LinearLayout.VERTICAL);
                        if(dropdown.getParent() != null) {
                            ((ViewGroup)dropdown.getParent()).removeView(dropdown); // <- fix
                        }
                        ll_b.addView(dropdown);

                        alert_sklad.setView(ll_b);
                        alert_sklad.setCancelable(false);
                        alert_sklad.setPositiveButton(str_set, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                editor.putInt(SKLAD, skladInt);
                                editor.putString(SKLAD_NAZEV, sklad);
                                editor.apply();

                                try {
                                    z = "trying to connect";
                                    Connection con_2 = connectionClass.CONN();

                                    z = "yeet";

                                    if (con_2 == null) {
                                        //statusTextView.setText(con_error_msg);

                                        z = "Error in connection with SQL server";
                                    } else {
                                        z = "wow, such else";
                                        query_st = "exec MOBILNI_LOGIN 5,'" + androidID  + "','','','','','','" + skladInt+ "','" + guid + "'";
                                        Statement cs_2 = null;
                                        cs_2 = con_2.createStatement();
                                        rs = cs_2.executeQuery(query_st);

                                        if (rs.next()){

                                            switch(rs.getInt(1)){

                                                case 0:
                                                    Toast.makeText(AppActivity.this, "Chybný přístup, zkontrolujte připojení a restartujte aplikaci", Toast.LENGTH_SHORT);
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
                                            Intent i = new Intent(AppActivity.this, AppActivity.class);
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

                            }
                        });
                        alert_sklad.show();
                    }

                }catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions in regCon";


                }
            }
        });

        formViewButton.setVisibility(View.VISIBLE);


    } // end of onCreate

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String ok = getResources().getString(R.string.ok);
        final String close = getResources().getString(R.string.close);
        final String amount = getResources().getString(R.string.logout_notice);
        final String amount_msg = getResources().getString(R.string.logout_msg);
        alert.setTitle(amount);
        alert.setMessage(amount_msg);
        alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finishAffinity();
                Intent leIntent = new Intent(AppActivity.this, MainActivity.class);
                startActivity(leIntent);
            }
        });
        alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();




    }

    private void getAllowedApplications(){
        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = preferences.edit();
        guid = preferences.getString(GUID, "");
        androidID = preferences.getString(ID,"");

        final ConnectionClass connectionClass = new ConnectionClass();

        try {
            Connection con = connectionClass.CONN();
            if (con == null){
                String z = "chyba v propojeni";
                //errorTv.setText(connection_error_string);
                //errorTv.setVisibility(View.VISIBLE);
            }else{
                query = "exec MOBILNI_APLIKACE '" + androidID + "','" + guid + "',''";
                Statement cs = null;
                cs = con.createStatement();
                rs = cs.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                Integer columnCOunt = rsmd.getColumnCount();
                Integer rowCount = 0;

                while (rs.next()){
                    rowCount = rs.getRow();
                }
                rs = cs.executeQuery(query);
                String[] resultSet = new String[columnCOunt];
                //Integer[] poradi = new Integer[rowCount];
                final String[] nazev = new String[rowCount];
                String[] popis = new String[rowCount];
                final Integer[] id = new Integer[rowCount];
                //final Integer[] typ_grafu = new Integer[rowCount];
                //Boolean[] aktivni = new Boolean[rowCount];
                int count = 0;


                while (rs.next()){
                    nazev[count] = rs.getString("NAZEV");
                    //poradi[count] = rs.getInt(2);
                    popis[count] = rs.getString("POPIS");
                    id[count] = rs.getInt("ID");
                    // typ_grafu[count] = rs.getInt(5);
                    // aktivni[count] = rs.getBoolean(6);
                    count++;
                }


            }
        }catch (Exception e){
            final String connection_error_string = getResources().getString(R.string.connection_error_alt);
            String error = getResources().getString(R.string.error);
            String ok = getResources().getString(R.string.ok);
            final String preferenceErrorMsg = getResources().getString(R.string.preferencesErrorMsg);
            e.printStackTrace();
            z = e.getMessage();
            System.out.println(rs);
            z = "the end";

            if(z.toLowerCase().contains(("android.content.SharedPreferences").toLowerCase())){

                AlertDialog.Builder alert = new AlertDialog.Builder(AppActivity.this);

                alert.setTitle(error);
                alert.setMessage(preferenceErrorMsg);

                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        finishAffinity();
                        Intent intent = new Intent(AppActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                alert.show();

            }else {

                AlertDialog.Builder alert = new AlertDialog.Builder(AppActivity.this);

                alert.setTitle(error);
                alert.setMessage(e.toString());

                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();

            }


        }
    }

} // end of activity