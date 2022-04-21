package com.winshop.sqlterminal;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cleveroad.adaptivetablelayout.OnItemLongClickListener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import static java.lang.System.exit;

public class FormViewActivity extends AppCompatActivity {
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

    String guid, androidID;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ResultSet rs;
    ArrayList querydata;
    Object[][] resultSet;
    String server, database, username, password;
    String userpassword;
    public static Context contextOfApplication;
    Integer counter, sklad;
    String nazev_skladu;
    Boolean sklad_present = true;
    String barcodeString, prefix;
    Integer intcislo;
    String z, typ_akce, note, form, item_count_string, stav_dokladu, doklad_hlavicka_id;
    String nazev, kod, ean_polozky, barva_polozky, velikost, delka, rozmer, polozka_nazev;
    Integer mnozstvi, sklad_id;
    String previousBarcode, barcode;
    Boolean existence, poKuse;
    Integer form_type_id,target_warehouse, hlavicka_id, polozka_id, zalozeno, item_count;
    String query, device_id;
    private static DokladAdapter adapter;
    ArrayList<Doklad> arrayOfUsers;
    Integer polozka_mnozstvi_new = new Integer(0);
    ListView listView;
    Button onePieceToggleButton, variableAmountToggleButton, formSettingsButton;
    Button finishFormButton;
    ScrollView horLayout;
    String currentFormBarcode;
    TextView scanFormTextview, formViewTitleTextView;
    String formViewTitle;
    Button manualEntryButton;
    Boolean showFullfilled;
    Integer maxBeforeAutoenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_form_view);
        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = preferences.edit();
        onePieceToggleButton = (Button) findViewById(R.id.onePieceButton);
        variableAmountToggleButton = (Button) findViewById(R.id.multiplePiecesButton);
        onePieceToggleButton.setVisibility(View.GONE);
        variableAmountToggleButton.setVisibility(View.GONE);
        item_count = 1;
        zalozeno = 1;
        item_count_string = "1";
        poKuse = true;
        barcodeString = preferences.getString(BARCODE, "");
        guid = preferences.getString(GUID, "");
        androidID = preferences.getString(ID,"");
        sklad_id = preferences.getInt(SKLAD, 0);
        listView = findViewById(R.id.listViewPolozky);
        registerForContextMenu(listView);
        final ConstraintLayout alertBoxLayout =  findViewById(R.id.include);
        try{
            alertBoxLayout.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }
        hlavicka_id = -1;

        showFullfilled = false;
        maxBeforeAutoenter = 5;
        currentFormBarcode = "";
        scanFormTextview = (TextView) findViewById(R.id.scanFormBarcodeTextview);
        scanFormTextview.setVisibility(View.VISIBLE);
        formViewTitleTextView = (TextView) findViewById(R.id.formview_title_textView);
        formViewTitle = getResources().getString(R.string.formview_title);

        formSettingsButton = findViewById(R.id.formSettingsButton);
        manualEntryButton = findViewById(R.id.manualEntryButton);

        finishFormButton = (Button) findViewById(R.id.formViewFinishButton);
        finishFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFormTextview.setVisibility(View.VISIBLE);

                currentFormBarcode = "";
                arrayOfUsers.clear();
                adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                //Attach the adapter to a ListView
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                formViewTitleTextView.setText(formViewTitle);


                //onBackPressed();
            }
        });


        formSettingsButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(FormViewActivity.this);
                View v = inflater.inflate(R.layout.form_view_menu_layout, null, false);
                Switch showFilledItemsSwitch = v.findViewById(R.id.showFilledItemsSwitch);
                EditText maxAutoEnterEditText = v.findViewById(R.id.maxAutoenterEditText);
                showFilledItemsSwitch.setChecked(showFullfilled);
                maxAutoEnterEditText.setText(maxBeforeAutoenter.toString());


                final AlertDialog dialog = new AlertDialog.Builder(FormViewActivity.this)
                        .setTitle(R.string.settings)
                        .setView(v)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();

                dialog.show();

                //Dialog's positive button
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                //Custom button




                dialog.show();


                /*AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                final String ok = getResources().getString(R.string.ok);
                final String close = getResources().getString(R.string.close);
                final String settings = getResources().getString(R.string.settings);
                //final String manual_entry_msg = getResources().getString(R.string.manual_entry_msg);
                alert.setTitle(settings);
                //alert.setMessage(manual_entry_msg);
                final ConstraintLayout formViewLayout = findViewById(R.id.formViewConstraintLayout);


                alert.setView(formViewLayout);
                final Switch showFilledItemsSwitch = formViewLayout.findViewById(R.id.showFilledItemsSwitch);
                EditText maxAutoEnterEditText = formViewLayout.findViewById(R.id.maxAutoenterEditText);
                showFilledItemsSwitch.setChecked(showFullfilled);
                maxAutoEnterEditText.setText(maxBeforeAutoenter.toString());
                alert.setCancelable(false);


                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showFullfilled = showFilledItemsSwitch.isChecked();
                    }
                });
                alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                AlertDialog dialog = alert.create();

                dialog.show();

                */
            }
        });

        manualEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                final String ok = getResources().getString(R.string.ok);
                final String close = getResources().getString(R.string.close);
                final String manual_entry = getResources().getString(R.string.manual_entry);
                final String manual_entry_msg = getResources().getString(R.string.manual_entry_msg);
                alert.setTitle(manual_entry);
                alert.setMessage(manual_entry_msg);
                final EditText input = new EditText(FormViewActivity.this);
                //input.setRawInputType(Configuration.KEYBOARD_12KEY);
                input.setText("");
                input.setHint("OBE123");
                alert.setView(input);
                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        displayEntryResult(input.getText().toString());
                    }
                });
                alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        finishFormButton.setVisibility(View.GONE);
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);

        final TextView EANtextView = (TextView) findViewById(R.id.pdtCreationEANTextView);
        final EditText alertAmountEditText = (EditText) findViewById(R.id.pdtCReationAlertBoxAmountEditText);
        final Button  amountOKbutton = (Button) findViewById(R.id.pdtCreationOkButton);
        final Button  amountCancelButton = (Button) findViewById(R.id.pdtCreationAlertCancelButton);
        Button finishButton = (Button) findViewById(R.id.deleteButton);
        final ConnectionClass connectionClass = new ConnectionClass();
        final String ok = getResources().getString(R.string.ok);
        final String close = getResources().getString(R.string.close);
        final String error = getResources().getString(R.string.error);
        final String unfinished_form_title = getResources().getString(R.string.unfinished_pdt);
        final String unfinished_form_message = getResources().getString(R.string.unfinished_pdt_existing_items_message);
        final String unfinished_form = getResources().getString(R.string.error);
        final String amount = getResources().getString(R.string.amount_pcs);
        final String amount_msg = getResources().getString(R.string.amount_message);
        existence = true;

        onePieceToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onePieceToggleButton.setBackgroundColor(getResources().getColor(R.color.winshop_dark_blue));
                onePieceToggleButton.setTextColor(getResources().getColor(R.color.winshop_white));
                variableAmountToggleButton.setBackgroundColor(Color.LTGRAY);
                variableAmountToggleButton.setTextColor(getResources().getColor(R.color.black));
                poKuse = true;
            }
        });
        variableAmountToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                variableAmountToggleButton.setBackgroundColor(getResources().getColor(R.color.winshop_dark_blue));
                variableAmountToggleButton.setTextColor(getResources().getColor(R.color.winshop_white));
                onePieceToggleButton.setBackgroundColor(Color.LTGRAY);
                onePieceToggleButton.setTextColor(getResources().getColor(R.color.black));
                poKuse = false;
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });






    } // End of onCreate

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String ok = getResources().getString(R.string.ok);
        final String close = getResources().getString(R.string.close);
        final String amount = getResources().getString(R.string.return_to_menu);
        final String amount_msg = getResources().getString(R.string.return_to_menu_msg);
        alert.setTitle(amount);
        alert.setMessage(amount_msg);
        alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finishAffinity();
                Intent leIntent = new Intent(FormViewActivity.this, AppActivity.class);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.listViewPolozky) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.form_creation_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Integer position = info.position;
        final String polozka_ean = arrayOfUsers.get(position).getEan();
        String mnozstvi_string = arrayOfUsers.get(position).getZvazeno();
        //Integer polozka_mnozstvi = Integer.valueOf(mnozstvi_string);
        final String[] polozka_id = {arrayOfUsers.get(position).getPolozka_id()};
        switch(item.getItemId()) {
            case R.id.edit_item_amount:
                // add stuff here
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final String ok = getResources().getString(R.string.ok);
                final String close = getResources().getString(R.string.close);
                final String amount = getResources().getString(R.string.amount_pcs);
                final String amount_msg = getResources().getString(R.string.amount_message);
                alert.setTitle(amount);
                alert.setMessage(amount_msg);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);
                input.setText(mnozstvi_string);
                alert.setView(input);
                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //polozka_mnozstvi_new = Integer.parseInt(input.getText().toString());
                        try {
                            final ConnectionClass connectionClass_1 = new ConnectionClass();
                            Connection con = connectionClass_1.CONN();
                            if (con == null) {
                                String z = "chyba v propojeni";
                            } else {
                                query = "exec MOBILNI_TERMINAL_DODAK '" + androidID + "','" + guid + "','DOKLAD_ZMENA_MNOZSTVI','" + prefix + "','" + intcislo.toString() + "','','" + input.getText().toString() + "','" + polozka_id[0] + "'";
                                Boolean success = false;
                                Statement cs = null;
                                cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                rs = cs.executeQuery(query);
                                Integer pocitadlo = 0;
                                ResultSetMetaData rsmd = rs.getMetaData();
                                Integer columnCOunt = rsmd.getColumnCount();
                                String[] resultSet = new String[columnCOunt];
                                Integer rowCounter = 0;
                                querydata = new ArrayList<String>();
                                String[] item = new String[13];
                                //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                //rs = stmt.executeQuery(query);
                                arrayOfUsers = new ArrayList<Doklad>();



                                while (rs.next()) {
                                    int[] row = new int[columnCOunt];
                                    String[] headers = new String[columnCOunt];
                                    String[] headersType = new String[columnCOunt];
                                    for (int j = 0; j < columnCOunt; j++) {
                                        resultSet[j] = rs.getString(j + 1);
                                        headers[j] = rsmd.getColumnName(j + 1);
                                        headersType[j] = rsmd.getColumnTypeName(j + 1);
                                    }
                                    querydata.add(resultSet);

                                    Integer intcisloIN = 0;
                                    String skladIN = "";
                                    String skladCilIN = "";
                                    Integer hlavickaIdIN = 0;
                                    String naskladeIN = "";
                                    String zamekVLIN = "";
                                    Integer polozkaIdIN = 0;
                                    Integer cislopolozkyIN = 0;
                                    Integer zboziIdIN = 0;
                                    String eanIN = "";
                                    String nazevIN = "";
                                    String polozkaMnozstviIN = "";
                                    String zvazenoIN = "";
                                    String typIN = "";
                                    String nalezenoIN = "";

                                    Integer nalezenoOUT = rs.getInt("NALEZENO");

                                    if (nalezenoOUT == 1){
                                        currentFormBarcode = barcodeString;
                                    }else{
                                        break;
                                    }

                                    switch (prefix) {
                                        case "PRI":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = "";    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = zvazenoIN;
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;

                                        case "PRE":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            skladCilIN = rs.getString("SKLAD_CIL");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            //zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = skladCilIN;    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = "";
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;

                                        case "ROV":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            //skladCilIN = rs.getString("SKLAD_CIL");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            //eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            //zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = "";    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = "";
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = "";
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));

                                            break;

                                        case "OBE":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = "";    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = zvazenoIN;
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;

                                        case "OBO":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = "";    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = zvazenoIN;
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;

                                        case "OBI":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            skladCilIN = rs.getString("SKLAD_CIL");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = skladCilIN;    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = zvazenoIN;
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;


                                        case "OBD":

                                            intcisloIN = rs.getInt("INTCISLO");
                                            skladIN = rs.getString("SKLAD");
                                            //skladCilIN = rs.getString("SKLAD_CIL");
                                            hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                            naskladeIN = rs.getString("NA_SKLADE");
                                            zamekVLIN = rs.getString("ZAMEK_VL");
                                            polozkaIdIN = rs.getInt("POLOZKA_ID");
                                            cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                            zboziIdIN = rs.getInt("ZBOZI_ID");
                                            eanIN = rs.getString("EAN");
                                            nazevIN = rs.getString("NAZEV");
                                            polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                            zvazenoIN = rs.getString("ZVAZENO");
                                            typIN = rs.getString("TYP");
                                            nalezenoIN = rs.getString("NALEZENO");

                                            item[0] = intcisloIN.toString();
                                            item[1] = skladIN.toString();
                                            item[2] = "";    //sklad cil
                                            item[3] = naskladeIN;
                                            item[4] = zamekVLIN.toString();
                                            item[5] = hlavickaIdIN.toString();
                                            item[6] = polozkaIdIN.toString();
                                            item[7] = nazevIN;
                                            item[8] = polozkaMnozstviIN;
                                            item[9] = zboziIdIN.toString();
                                            item[10] = eanIN;
                                            item[11] = cislopolozkyIN.toString();
                                            item[12] = zvazenoIN;
                                            arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                            break;

                                    }


                                }
                                adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                //Attach the adapter to a ListView
                                adapter.notifyDataSetChanged();
                                listView.setAdapter(adapter);
                                registerForContextMenu(listView);
                            }
                        }catch (Exception ex){
                            String f = ex.getMessage();
                        }
                    }
                });
                alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exit(0);
                    }
                });
                //Looper.prepare();
                AlertDialog dialog = alert.create();
                dialog.show();
                return true;
            case R.id.delete_item:
                // edit stuff here
                Toast.makeText(FormViewActivity.this, "Tato možnost není ve vaší instalaci povolena", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        //mySender.detachDuplexOutputChannel();
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    //imageView.setVisibility(View.GONE);
                    displayScanResult(intent, "via Broadcast");
                    Environment.getExternalStorageState();
                    //generateNoteOnSD(context, "IN.txt", barcode);



                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                    String err = e.getMessage();
                }
            }
        }
    };

    private void displayScanResult(Intent initiatingIntent, String howDataReceived) {
        String decodedSource = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        final ConnectionClass connectionClass = new ConnectionClass();
        final String error = getResources().getString(R.string.error);
        final String ok = getResources().getString(R.string.ok);
        final String ean_not_found = getResources().getString(R.string.ean_not_found);

        barcode = decodedData;
        Integer i = 0;

        if(currentFormBarcode.matches("")){
            //scan a form barcode
            if (!barcode.matches("")) {

                //Update barcode value to TextView
                final String val = barcode;


                //cache = new Vector();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ConnectionClass connectionClass = new ConnectionClass();
                            barcodeString = val;
                            editor.putBoolean(BARCODE_IS_NEW, false);
                            editor.apply();
                            prefix = barcodeString.substring(0, 3);
                            intcislo = Integer.parseInt(barcodeString.substring(3, barcodeString.length()));

                            final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                            try {
                                Connection con = connectionClass.CONN();
                                if (con == null){
                                    String z = "chyba v propojeni";

                                }else {
                                    hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                                    query = "exec MOBILNI_TERMINAL_DODAK '" + androidID + "','" + guid + "','DOKLAD','" + prefix + "','" + intcislo.toString() + "','','" + item_count + "',''";
                                    Statement stmt = null;
                                    PreparedStatement pstmt =
                                            con.prepareStatement(query,
                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                    ResultSet.CONCUR_READ_ONLY);
                                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                    rs = pstmt.executeQuery();
                                    String typ_vysledku;
                                    Integer rowcount = new Integer(0);
                                    ResultSetMetaData rsmd = rs.getMetaData();
                                    Integer columnCOunt = rsmd.getColumnCount();
                                    String[] resultSet = new String[columnCOunt];
                                    Integer rowCounter = 0;
                                    Integer rowCount = 0;
                                    querydata = new ArrayList<String>();
                                    String[] item = new String[13];
                                    //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                    //rs = stmt.executeQuery(query);
                                    Integer pocitadlo = 0;
                                    arrayOfUsers = new ArrayList<Doklad>();



                                        while (rs.next()) {
                                            int[] row = new int[columnCOunt];
                                            String[] headers = new String[columnCOunt];
                                            String[] headersType = new String[columnCOunt];
                                            for (int j = 0; j < columnCOunt; j++) {
                                                resultSet[j] = rs.getString(j + 1);
                                                headers[j] = rsmd.getColumnName(j + 1);
                                                headersType[j] = rsmd.getColumnTypeName(j + 1);
                                            }
                                            querydata.add(resultSet);

                                            Integer intcisloIN = 0;
                                            String skladIN = "";
                                            String skladCilIN = "";
                                            Integer hlavickaIdIN = 0;
                                            String naskladeIN = "";
                                            String zamekVLIN = "";
                                            Integer polozkaIdIN = 0;
                                            Integer cislopolozkyIN = 0;
                                            Integer zboziIdIN = 0;
                                            String eanIN = "";
                                            String nazevIN = "";
                                            String polozkaMnozstviIN = "";
                                            String zvazenoIN = "";
                                            String typIN = "";
                                            String nalezenoIN = "";

                                            Integer nalezenoOUT = rs.getInt("NALEZENO");

                                            if (nalezenoOUT == 1){
                                                currentFormBarcode = barcodeString;
                                                scanFormTextview.setVisibility(View.GONE);
                                                formViewTitleTextView.setText(barcodeString);
                                            }else{
                                                break;
                                            }

                                            switch (prefix) {
                                                case "PRI":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = "";    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = zvazenoIN;
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;

                                                case "PRE":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    skladCilIN = rs.getString("SKLAD_CIL");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    //zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = skladCilIN;    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = "";
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;

                                                case "ROV":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    //skladCilIN = rs.getString("SKLAD_CIL");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    //eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    //zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = "";    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = "";
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = "";
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));

                                                    break;

                                                case "OBE":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = "";    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = zvazenoIN;
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;

                                                case "OBO":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = "";    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = zvazenoIN;
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;

                                                case "OBI":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    skladCilIN = rs.getString("SKLAD_CIL");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = skladCilIN;    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = zvazenoIN;
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;


                                                case "OBD":

                                                    intcisloIN = rs.getInt("INTCISLO");
                                                    skladIN = rs.getString("SKLAD");
                                                    //skladCilIN = rs.getString("SKLAD_CIL");
                                                    hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                    naskladeIN = rs.getString("NA_SKLADE");
                                                    zamekVLIN = rs.getString("ZAMEK_VL");
                                                    polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                    cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                    zboziIdIN = rs.getInt("ZBOZI_ID");
                                                    eanIN = rs.getString("EAN");
                                                    nazevIN = rs.getString("NAZEV");
                                                    polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                    zvazenoIN = rs.getString("ZVAZENO");
                                                    typIN = rs.getString("TYP");
                                                    nalezenoIN = rs.getString("NALEZENO");

                                                    item[0] = intcisloIN.toString();
                                                    item[1] = skladIN.toString();
                                                    item[2] = "";    //sklad cil
                                                    item[3] = naskladeIN;
                                                    item[4] = zamekVLIN.toString();
                                                    item[5] = hlavickaIdIN.toString();
                                                    item[6] = polozkaIdIN.toString();
                                                    item[7] = nazevIN;
                                                    item[8] = polozkaMnozstviIN;
                                                    item[9] = zboziIdIN.toString();
                                                    item[10] = eanIN;
                                                    item[11] = cislopolozkyIN.toString();
                                                    item[12] = zvazenoIN;
                                                    arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                    break;

                                            }


                                        }
                                    adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                    //Attach the adapter to a ListView
                                    registerForContextMenu(listView);
                                    adapter.notifyDataSetChanged();
                                    listView.setAdapter(adapter);

                                    return;



                                }

                            }catch (Exception ex){
                                editor.putBoolean(BARCODE_IS_NEW, false);
                                editor.apply();
                                ex.printStackTrace();
                                z = ex.getMessage();
                                System.out.println(rs);
                                z = "the end";
                                android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                alert.setTitle("Chyba");
                                alert.setMessage(ex.toString());
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                alert.show();
                            }
                        }
                    });
                } catch (Exception ex) {
                    //cache = new Vector();
                    ex.printStackTrace();
                }
            }


        }else{
            //scan items
            try {

                final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                if (poKuse == true){
                    item_count = 1;
                    item_count_string = "1";
                    if (!barcode.matches("")) {

                        //Update barcode value to TextView
                        final String val = barcode;


                        //cache = new Vector();
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final ConnectionClass connectionClass = new ConnectionClass();
                                    barcodeString = val;
                                    editor.putBoolean(BARCODE_IS_NEW, false);
                                    editor.apply();
                                    final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                                    try {
                                        Connection con = connectionClass.CONN();
                                        if (con == null){
                                            String z = "chyba v propojeni";

                                        }else{
                                            hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                                            query = "exec MOBILNI_TERMINAL_DODAK '" + androidID + "','" + guid + "','DOKLAD_POLOZKA','" + prefix +"','" + intcislo.toString()+ "','" + barcodeString + "','" + item_count + "',''";
                                            Statement stmt = null;
                                            PreparedStatement pstmt =
                                                    con.prepareStatement(query,
                                                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                            ResultSet.CONCUR_READ_ONLY);
                                            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                            rs = pstmt.executeQuery();
                                            String typ_vysledku;
                                            Integer rowcount = new Integer(0);
                                            ResultSetMetaData rsmd = rs.getMetaData();
                                            Integer columnCOunt = rsmd.getColumnCount();
                                            String[] resultSet = new String[columnCOunt];
                                            Integer rowCounter = 0;
                                            Integer rowCount = 0;
                                            querydata = new ArrayList<String>();
                                            String[] item = new String[13];
                                            //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                            //rs = stmt.executeQuery(query);
                                            Integer pocitadlo = 0;
                                            arrayOfUsers = new ArrayList<Doklad>();

                                            while(rs.next()){
                                                int[] row = new int[columnCOunt];
                                                String[] headers = new String[columnCOunt];
                                                String[] headersType = new String[columnCOunt];
                                                for (int j=0; j< columnCOunt; j++){
                                                    resultSet[j] = rs.getString(j+1);
                                                    headers[j] = rsmd.getColumnName(j+1);
                                                    headersType[j] = rsmd.getColumnTypeName(j+1);
                                                }
                                                querydata.add(resultSet);

                                                Integer intcisloIN = 0;
                                                String skladIN = "";
                                                String skladCilIN = "";
                                                Integer hlavickaIdIN = 0;
                                                String naskladeIN = "";
                                                String zamekVLIN = "";
                                                Integer polozkaIdIN = 0;
                                                Integer cislopolozkyIN =0;
                                                Integer zboziIdIN = 0;
                                                String eanIN = "";
                                                String nazevIN = "";
                                                String polozkaMnozstviIN = "";
                                                String zvazenoIN = "";
                                                String typIN = "";
                                                String nalezenoIN = "";

                                                switch(prefix){
                                                    case "PRI":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "PRE":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        //zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = skladCilIN;    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = "";
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "ROV":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        //skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        //eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        //zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = "";
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = "";
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));

                                                        break;

                                                    case "OBE":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "OBO":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "OBI":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = skladCilIN;    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;


                                                    case "OBD":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        //skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                }




                                            }
                                            adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                            //Attach the adapter to a ListView
                                            registerForContextMenu(listView);
                                            adapter.notifyDataSetChanged();
                                            listView.setAdapter(adapter);
                                            return;


                                        }

                                    }catch (Exception ex){
                                        editor.putBoolean(BARCODE_IS_NEW, false);
                                        editor.apply();
                                        ex.printStackTrace();
                                        z = ex.getMessage();
                                        System.out.println(rs);
                                        z = "the end";
                                        android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                        alert.setTitle("Chyba");
                                        alert.setMessage(ex.toString());
                                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                            }
                                        });
                                        alert.show();
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            //cache = new Vector();
                            ex.printStackTrace();
                        }
                    }
                }else{
                    //showAlertDialogButtonClicked(cameraView);
                    if (barcode.matches("")) {

                        final TextView EANtextView = (TextView) findViewById(R.id.pdtCreationEANTextView);

                        EANtextView.setText(barcode);
                        final Button amountOKbutton = (Button) findViewById(R.id.pdtCreationOkButton);

                        amountOKbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View vOnClick) {
                                final EditText alertAmountEditText = (EditText) findViewById(R.id.pdtCReationAlertBoxAmountEditText);

                                item_count = Integer.parseInt(alertAmountEditText.getText().toString());
                                item_count_string = alertAmountEditText.getText().toString();
                                final ConstraintLayout alertBoxLayout = (ConstraintLayout) findViewById(R.id.pdtalertBoxLayout);

                                alertBoxLayout.setVisibility(View.GONE);
                                final String val = barcode;
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(50);
                                }



                                //cache = new Vector();
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final ConnectionClass connectionClass = new ConnectionClass();
                                            barcodeString = val;
                                            editor.putBoolean(BARCODE_IS_NEW, false);
                                            editor.apply();
                                            final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                                            try {
                                                Connection con = connectionClass.CONN();
                                                if (con == null){
                                                    String z = "chyba v propojeni";

                                                }else{
                                                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','" + item_count + "'";
                                                    Statement stmt = null;
                                                    PreparedStatement pstmt =
                                                            con.prepareStatement(query,
                                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                    ResultSet.CONCUR_READ_ONLY);
                                                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                                    rs = pstmt.executeQuery();
                                                    String typ_vysledku;
                                                    Integer rowcount = new Integer(0);
                                                    ResultSetMetaData rsmd = rs.getMetaData();
                                                    Integer columnCOunt = rsmd.getColumnCount();
                                                    String[] resultSet = new String[columnCOunt];
                                                    Integer rowCounter = 0;
                                                    querydata = new ArrayList<String>();
                                                    String[] item = new String[11];
                                                    //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                                    //rs = stmt.executeQuery(query);
                                                    Integer pocitadlo = 0;
                                                    arrayOfUsers = new ArrayList<Doklad>();

                                                    while(rs.next()){
                                                        querydata.add(resultSet);
                                                        hlavicka_id = rs.getInt("HLAVICKA_ID");
                                                        polozka_id = rs.getInt("POLOZKA_ID");
                                                        polozka_nazev = "";
                                                        ean_polozky = rs.getString("EAN");
                                                        mnozstvi = rs.getInt("MNOZSTVI");
                                                        kod = rs.getString("KOD_ZBOZI");
                                                        nazev = rs.getString("NAZEV_ZBOZI");
                                                        barva_polozky = rs.getString("NAZEV_BARVY");
                                                        velikost = rs.getString("NAZEV_VELIKOSTI");
                                                        delka = rs.getString("NAZEV_DELKY");
                                                        rozmer = rs.getString("NAZEV_ROZMERU");

                                                        item[0] = hlavicka_id.toString();
                                                        item[1] = polozka_id.toString();
                                                        item[2] = polozka_nazev;
                                                        item[6] = ean_polozky;
                                                        item[4] = mnozstvi.toString();
                                                        item[5] = kod;
                                                        item[3] = nazev;
                                                        item[7] = barva_polozky;
                                                        item[8] = velikost;
                                                        item[9] = delka;
                                                        item[10] = rozmer;
                                                        //arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                                    }
                                                    adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                                    //Attach the adapter to a ListView
                                                    adapter.notifyDataSetChanged();
                                                    listView.setAdapter(adapter);
                                                    registerForContextMenu(listView);


                                                }
                                            }catch (Exception ex){
                                                editor.putBoolean(BARCODE_IS_NEW, false);
                                                editor.apply();
                                                ex.printStackTrace();
                                                z = ex.getMessage();
                                                System.out.println(rs);
                                                z = "the end";
                                                android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                                alert.setTitle("Chyba");
                                                alert.setMessage(ex.toString());
                                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                    }
                                                });
                                                alert.show();
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    //cache = new Vector();
                                    ex.printStackTrace();
                                }

                            }

                        });
                        final Button  amountCancelButton = (Button) findViewById(R.id.pdtCreationAlertCancelButton);

                        amountCancelButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View onCancelClick) {
                                final ConstraintLayout alertBoxLayout = (ConstraintLayout) findViewById(R.id.pdtalertBoxLayout);

                                alertBoxLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            } catch (Exception ex) {

                ex.printStackTrace();
            }

        }






    }

    private void displayEntryResult(String entryBarcode) {

        final ConnectionClass connectionClass = new ConnectionClass();
        final String error = getResources().getString(R.string.error);
        final String ok = getResources().getString(R.string.ok);
        final String ean_not_found = getResources().getString(R.string.ean_not_found);

        barcode = entryBarcode;
        Integer i = 0;

        if(currentFormBarcode.matches("")){
            //scan a form barcode
            if (!barcode.matches("")) {

                //Update barcode value to TextView
                final String val = barcode;


                //cache = new Vector();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ConnectionClass connectionClass = new ConnectionClass();
                            barcodeString = val;
                            editor.putBoolean(BARCODE_IS_NEW, false);
                            editor.apply();
                            prefix = barcodeString.substring(0, 3);
                            intcislo = Integer.parseInt(barcodeString.substring(3, barcodeString.length()));

                            final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                            try {
                                Connection con = connectionClass.CONN();
                                if (con == null){
                                    String z = "chyba v propojeni";

                                }else {
                                    hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                                    query = "exec MOBILNI_TERMINAL_DODAK '" + androidID + "','" + guid + "','DOKLAD','" + prefix + "','" + intcislo.toString() + "','','" + item_count + "',''";
                                    Statement stmt = null;
                                    PreparedStatement pstmt =
                                            con.prepareStatement(query,
                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                    ResultSet.CONCUR_READ_ONLY);
                                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                    rs = pstmt.executeQuery();
                                    String typ_vysledku;
                                    Integer rowcount = new Integer(0);
                                    ResultSetMetaData rsmd = rs.getMetaData();
                                    Integer columnCOunt = rsmd.getColumnCount();
                                    String[] resultSet = new String[columnCOunt];
                                    Integer rowCounter = 0;
                                    Integer rowCount = 0;
                                    querydata = new ArrayList<String>();
                                    String[] item = new String[13];
                                    //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                    //rs = stmt.executeQuery(query);
                                    Integer pocitadlo = 0;
                                    arrayOfUsers = new ArrayList<Doklad>();



                                    while (rs.next()) {
                                        int[] row = new int[columnCOunt];
                                        String[] headers = new String[columnCOunt];
                                        String[] headersType = new String[columnCOunt];
                                        for (int j = 0; j < columnCOunt; j++) {
                                            resultSet[j] = rs.getString(j + 1);
                                            headers[j] = rsmd.getColumnName(j + 1);
                                            headersType[j] = rsmd.getColumnTypeName(j + 1);
                                        }
                                        querydata.add(resultSet);

                                        Integer intcisloIN = 0;
                                        String skladIN = "";
                                        String skladCilIN = "";
                                        Integer hlavickaIdIN = 0;
                                        String naskladeIN = "";
                                        String zamekVLIN = "";
                                        Integer polozkaIdIN = 0;
                                        Integer cislopolozkyIN = 0;
                                        Integer zboziIdIN = 0;
                                        String eanIN = "";
                                        String nazevIN = "";
                                        String polozkaMnozstviIN = "";
                                        String zvazenoIN = "";
                                        String typIN = "";
                                        String nalezenoIN = "";

                                        Integer nalezenoOUT = rs.getInt("NALEZENO");

                                        if (nalezenoOUT == 1){
                                            currentFormBarcode = barcodeString;
                                            scanFormTextview.setVisibility(View.GONE);
                                            formViewTitleTextView.setText(barcodeString);
                                        }else{
                                            break;
                                        }

                                        switch (prefix) {
                                            case "PRI":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = "";    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = zvazenoIN;
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;

                                            case "PRE":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                skladCilIN = rs.getString("SKLAD_CIL");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                //zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = skladCilIN;    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = "";
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;

                                            case "ROV":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                //skladCilIN = rs.getString("SKLAD_CIL");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                //eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                //zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = "";    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = "";
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = "";
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));

                                                break;

                                            case "OBE":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = "";    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = zvazenoIN;
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;

                                            case "OBO":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = "";    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = zvazenoIN;
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;

                                            case "OBI":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                skladCilIN = rs.getString("SKLAD_CIL");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = skladCilIN;    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = zvazenoIN;
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;


                                            case "OBD":

                                                intcisloIN = rs.getInt("INTCISLO");
                                                skladIN = rs.getString("SKLAD");
                                                //skladCilIN = rs.getString("SKLAD_CIL");
                                                hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                naskladeIN = rs.getString("NA_SKLADE");
                                                zamekVLIN = rs.getString("ZAMEK_VL");
                                                polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                zboziIdIN = rs.getInt("ZBOZI_ID");
                                                eanIN = rs.getString("EAN");
                                                nazevIN = rs.getString("NAZEV");
                                                polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                zvazenoIN = rs.getString("ZVAZENO");
                                                typIN = rs.getString("TYP");
                                                nalezenoIN = rs.getString("NALEZENO");

                                                item[0] = intcisloIN.toString();
                                                item[1] = skladIN.toString();
                                                item[2] = "";    //sklad cil
                                                item[3] = naskladeIN;
                                                item[4] = zamekVLIN.toString();
                                                item[5] = hlavickaIdIN.toString();
                                                item[6] = polozkaIdIN.toString();
                                                item[7] = nazevIN;
                                                item[8] = polozkaMnozstviIN;
                                                item[9] = zboziIdIN.toString();
                                                item[10] = eanIN;
                                                item[11] = cislopolozkyIN.toString();
                                                item[12] = zvazenoIN;
                                                arrayOfUsers.add(new Doklad(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12]));


                                                break;

                                        }


                                    }
                                    adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                    //Attach the adapter to a ListView
                                    registerForContextMenu(listView);
                                    adapter.notifyDataSetChanged();
                                    listView.setAdapter(adapter);

                                    return;



                                }

                            }catch (Exception ex){
                                editor.putBoolean(BARCODE_IS_NEW, false);
                                editor.apply();
                                ex.printStackTrace();
                                z = ex.getMessage();
                                System.out.println(rs);
                                z = "the end";
                                android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                alert.setTitle("Chyba");
                                alert.setMessage(ex.toString());
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                alert.show();
                            }
                        }
                    });
                } catch (Exception ex) {
                    //cache = new Vector();
                    ex.printStackTrace();
                }
            }


        }else{
            //scan items
            try {

                final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                if (poKuse == true){
                    item_count = 1;
                    item_count_string = "1";
                    if (!barcode.matches("")) {

                        //Update barcode value to TextView
                        final String val = barcode;


                        //cache = new Vector();
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final ConnectionClass connectionClass = new ConnectionClass();
                                    barcodeString = val;
                                    editor.putBoolean(BARCODE_IS_NEW, false);
                                    editor.apply();
                                    final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                                    try {
                                        Connection con = connectionClass.CONN();
                                        if (con == null){
                                            String z = "chyba v propojeni";

                                        }else{
                                            hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                                            query = "exec MOBILNI_TERMINAL_DODAK '" + androidID + "','" + guid + "','DOKLAD_POLOZKA','" + prefix +"','" + intcislo.toString()+ "','" + barcodeString + "','" + item_count + "',''";
                                            Statement stmt = null;
                                            PreparedStatement pstmt =
                                                    con.prepareStatement(query,
                                                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                            ResultSet.CONCUR_READ_ONLY);
                                            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                            rs = pstmt.executeQuery();
                                            String typ_vysledku;
                                            Integer rowcount = new Integer(0);
                                            ResultSetMetaData rsmd = rs.getMetaData();
                                            Integer columnCOunt = rsmd.getColumnCount();
                                            String[] resultSet = new String[columnCOunt];
                                            Integer rowCounter = 0;
                                            Integer rowCount = 0;
                                            querydata = new ArrayList<String>();
                                            String[] item = new String[13];
                                            //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                            //rs = stmt.executeQuery(query);
                                            Integer pocitadlo = 0;
                                            arrayOfUsers = new ArrayList<Doklad>();

                                            while(rs.next()){
                                                int[] row = new int[columnCOunt];
                                                String[] headers = new String[columnCOunt];
                                                String[] headersType = new String[columnCOunt];
                                                for (int j=0; j< columnCOunt; j++){
                                                    resultSet[j] = rs.getString(j+1);
                                                    headers[j] = rsmd.getColumnName(j+1);
                                                    headersType[j] = rsmd.getColumnTypeName(j+1);
                                                }
                                                querydata.add(resultSet);

                                                Integer intcisloIN = 0;
                                                String skladIN = "";
                                                String skladCilIN = "";
                                                Integer hlavickaIdIN = 0;
                                                String naskladeIN = "";
                                                String zamekVLIN = "";
                                                Integer polozkaIdIN = 0;
                                                Integer cislopolozkyIN =0;
                                                Integer zboziIdIN = 0;
                                                String eanIN = "";
                                                String nazevIN = "";
                                                String polozkaMnozstviIN = "";
                                                String zvazenoIN = "";
                                                String typIN = "";
                                                String nalezenoIN = "";

                                                switch(prefix){
                                                    case "PRI":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "PRE":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        //zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = skladCilIN;    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = "";
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "ROV":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        //skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        //eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        //zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = "";
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = "";
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));

                                                        break;

                                                    case "OBE":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "OBO":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                    case "OBI":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = skladCilIN;    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;


                                                    case "OBD":

                                                        intcisloIN = rs.getInt("INTCISLO");
                                                        skladIN = rs.getString("SKLAD");
                                                        //skladCilIN = rs.getString("SKLAD_CIL");
                                                        hlavickaIdIN = rs.getInt("HLAVICKA_ID");
                                                        naskladeIN = rs.getString("NA_SKLADE");
                                                        zamekVLIN = rs.getString("ZAMEK_VL");
                                                        polozkaIdIN = rs.getInt("POLOZKA_ID");
                                                        cislopolozkyIN = rs.getInt("CISLO_POLOZKY");
                                                        zboziIdIN = rs.getInt("ZBOZI_ID");
                                                        eanIN = rs.getString("EAN");
                                                        nazevIN = rs.getString("NAZEV");
                                                        polozkaMnozstviIN = rs.getString("POLOZKA_MNOZSTVI");
                                                        zvazenoIN = rs.getString("ZVAZENO");
                                                        typIN = rs.getString("TYP");
                                                        nalezenoIN = rs.getString("NALEZENO");

                                                        item[0] = intcisloIN.toString();
                                                        item[1] = skladIN.toString();
                                                        item[2] = "";    //sklad cil
                                                        item[3] = naskladeIN;
                                                        item[4] = zamekVLIN.toString();
                                                        item[5] = hlavickaIdIN.toString();
                                                        item[6] = polozkaIdIN.toString();
                                                        item[7] = nazevIN;
                                                        item[8] = polozkaMnozstviIN;
                                                        item[9] = zboziIdIN.toString();
                                                        item[10] = eanIN;
                                                        item[11] = cislopolozkyIN.toString();
                                                        item[12] = zvazenoIN;
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10],item[11],item[12]));



                                                        break;

                                                }




                                            }
                                            adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                            //Attach the adapter to a ListView
                                            registerForContextMenu(listView);
                                            adapter.notifyDataSetChanged();
                                            listView.setAdapter(adapter);
                                            return;


                                        }

                                    }catch (Exception ex){
                                        editor.putBoolean(BARCODE_IS_NEW, false);
                                        editor.apply();
                                        ex.printStackTrace();
                                        z = ex.getMessage();
                                        System.out.println(rs);
                                        z = "the end";
                                        android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                        alert.setTitle("Chyba");
                                        alert.setMessage(ex.toString());
                                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                            }
                                        });
                                        alert.show();
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            //cache = new Vector();
                            ex.printStackTrace();
                        }
                    }
                }else{
                    //showAlertDialogButtonClicked(cameraView);
                    if (barcode.matches("")) {

                        final TextView EANtextView = (TextView) findViewById(R.id.pdtCreationEANTextView);

                        EANtextView.setText(barcode);
                        final Button amountOKbutton = (Button) findViewById(R.id.pdtCreationOkButton);

                        amountOKbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View vOnClick) {
                                final EditText alertAmountEditText = (EditText) findViewById(R.id.pdtCReationAlertBoxAmountEditText);

                                item_count = Integer.parseInt(alertAmountEditText.getText().toString());
                                item_count_string = alertAmountEditText.getText().toString();
                                final ConstraintLayout alertBoxLayout = (ConstraintLayout) findViewById(R.id.pdtalertBoxLayout);

                                alertBoxLayout.setVisibility(View.GONE);
                                final String val = barcode;
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(50);
                                }



                                //cache = new Vector();
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final ConnectionClass connectionClass = new ConnectionClass();
                                            barcodeString = val;
                                            editor.putBoolean(BARCODE_IS_NEW, false);
                                            editor.apply();
                                            final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                                            try {
                                                Connection con = connectionClass.CONN();
                                                if (con == null){
                                                    String z = "chyba v propojeni";

                                                }else{
                                                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','" + item_count + "'";
                                                    Statement stmt = null;
                                                    PreparedStatement pstmt =
                                                            con.prepareStatement(query,
                                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                    ResultSet.CONCUR_READ_ONLY);
                                                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                                    rs = pstmt.executeQuery();
                                                    String typ_vysledku;
                                                    Integer rowcount = new Integer(0);
                                                    ResultSetMetaData rsmd = rs.getMetaData();
                                                    Integer columnCOunt = rsmd.getColumnCount();
                                                    String[] resultSet = new String[columnCOunt];
                                                    Integer rowCounter = 0;
                                                    querydata = new ArrayList<String>();
                                                    String[] item = new String[11];
                                                    //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                                                    //rs = stmt.executeQuery(query);
                                                    Integer pocitadlo = 0;
                                                    arrayOfUsers = new ArrayList<Doklad>();

                                                    while(rs.next()){
                                                        querydata.add(resultSet);
                                                        hlavicka_id = rs.getInt("HLAVICKA_ID");
                                                        polozka_id = rs.getInt("POLOZKA_ID");
                                                        polozka_nazev = "";
                                                        ean_polozky = rs.getString("EAN");
                                                        mnozstvi = rs.getInt("MNOZSTVI");
                                                        kod = rs.getString("KOD_ZBOZI");
                                                        nazev = rs.getString("NAZEV_ZBOZI");
                                                        barva_polozky = rs.getString("NAZEV_BARVY");
                                                        velikost = rs.getString("NAZEV_VELIKOSTI");
                                                        delka = rs.getString("NAZEV_DELKY");
                                                        rozmer = rs.getString("NAZEV_ROZMERU");

                                                        item[0] = hlavicka_id.toString();
                                                        item[1] = polozka_id.toString();
                                                        item[2] = polozka_nazev;
                                                        item[6] = ean_polozky;
                                                        item[4] = mnozstvi.toString();
                                                        item[5] = kod;
                                                        item[3] = nazev;
                                                        item[7] = barva_polozky;
                                                        item[8] = velikost;
                                                        item[9] = delka;
                                                        item[10] = rozmer;
                                                        //arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                                    }
                                                    adapter = new DokladAdapter(arrayOfUsers, FormViewActivity.this);
                                                    //Attach the adapter to a ListView
                                                    adapter.notifyDataSetChanged();
                                                    listView.setAdapter(adapter);
                                                    registerForContextMenu(listView);


                                                }
                                            }catch (Exception ex){
                                                editor.putBoolean(BARCODE_IS_NEW, false);
                                                editor.apply();
                                                ex.printStackTrace();
                                                z = ex.getMessage();
                                                System.out.println(rs);
                                                z = "the end";
                                                android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FormViewActivity.this);
                                                alert.setTitle("Chyba");
                                                alert.setMessage(ex.toString());
                                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                    }
                                                });
                                                alert.show();
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    //cache = new Vector();
                                    ex.printStackTrace();
                                }

                            }

                        });
                        final Button  amountCancelButton = (Button) findViewById(R.id.pdtCreationAlertCancelButton);

                        amountCancelButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View onCancelClick) {
                                final ConstraintLayout alertBoxLayout = (ConstraintLayout) findViewById(R.id.pdtalertBoxLayout);

                                alertBoxLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            } catch (Exception ex) {

                ex.printStackTrace();
            }

        }






    }







    public class Doklad {
        public String intcislo;
        public String sklad;
        public String sklad_cil;
        public String na_sklade;
        public String zamek_vl;
        public String hlavicka_id;
        public String polozka_id;
        public String nazev_polozky;
        public String mnozstvi_polozky;
        public String id_zbozi;
        public String ean;
        public String cislo_polozky;
        public String zvazeno;


        public Doklad( String intcislo, String sklad, String sklad_cil, String na_sklade , String zamek_vl ,String hlavicka_id, String polozka_id,
                       String nazev_polozky, String mnozstvi_polozky, String id_zbozi, String ean, String cislo_polozky, String zvazeno){
            this.hlavicka_id = hlavicka_id;
            this.polozka_id = polozka_id;
            this.nazev_polozky = nazev_polozky;
            this.intcislo = intcislo;
            this.mnozstvi_polozky = mnozstvi_polozky;
            this.ean = ean;
            this.sklad = sklad;
            this.sklad_cil = sklad_cil;
            this.na_sklade = na_sklade;
            this.zamek_vl = zamek_vl;
            this.id_zbozi = id_zbozi;
            this.cislo_polozky = cislo_polozky;
            this.zvazeno = zvazeno;
        }
        public String getIntcislo() {return intcislo; }
        public String getSklad() {
            return sklad;
        }
        public String getMnozstvi_polozky() {
            return mnozstvi_polozky;
        }
        public String getEan() {
            return ean;
        }
        public String getNazev_polozky(){return nazev_polozky;}
        public String getSklad_cil(){return sklad_cil;}
        public String getNa_sklade(){return na_sklade;}
        public String getZamek_vl(){return zamek_vl;}
        public String getHlavicka_id(){return hlavicka_id;}
        public String getPolozka_id(){return polozka_id;}
        public String getId_zbozi(){return id_zbozi;}
        public String getCislo_polozky(){return cislo_polozky;}
        public String getZvazeno() {return zvazeno;}
    }


    public class DokladAdapter extends ArrayAdapter<Doklad> {
        private ArrayList<Doklad> dataSet;
        Context mContext;

        private  class ViewHolder{
            TextView txtPolozka;
            TextView txtVelikost;
            TextView txtMnozstvi;
            TextView txtEan;
            TextView txtBarva;
            TextView txtDelka;
            TextView txtRozmer;
            TextView txtKodZbozi;
            TextView txtColorStrip;
        }

        public DokladAdapter(ArrayList<Doklad> doklady, Context context) {
            super(context, R.layout.doklad_polozka_item, doklady);
            this.dataSet = doklady;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Doklad doklad = getItem(position);
            ViewHolder viewHolder;
            final View result;
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.doklad_polozka_item, parent, false);
                viewHolder.txtPolozka = convertView.findViewById(R.id.dokladPolozkaNazev);
                viewHolder.txtMnozstvi = convertView.findViewById(R.id.dokladPolozkaMnozstvi);

                viewHolder.txtKodZbozi = convertView.findViewById(R.id.dokladPolozkaZboziID);
                viewHolder.txtColorStrip = convertView.findViewById(R.id.colorStripTextView);

                result = convertView;
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }
            Float zvazeno = Float.valueOf(doklad.getZvazeno());
            Float mnozstvi = Float.valueOf(doklad.getMnozstvi_polozky());

            if(zvazeno.equals(mnozstvi)){
                viewHolder.txtColorStrip.setBackgroundColor(Color.GREEN);
            }else if(zvazeno.compareTo(mnozstvi)>0){
                viewHolder.txtColorStrip.setBackgroundColor(Color.RED);
            }

            viewHolder.txtKodZbozi.setText("Nacteno: " + doklad.getZvazeno());
            // Lookup view for data population
            viewHolder.txtMnozstvi.setText("Mnozstvi: " + doklad.getMnozstvi_polozky());
            //viewHolder.txtEan.setText("EAN: " + doklad.getEan());
            viewHolder.txtPolozka.setText("Polozka: " + doklad.getNazev_polozky());

            // Populate the data into the template view using the data object
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
} // End of activity