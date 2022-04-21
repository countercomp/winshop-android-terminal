package com.winshop.sqlterminal;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import static java.lang.System.exit;

public class PDTCreationActivity extends AppCompatActivity {
    Button onePieceToggleButton, variableAmountToggleButton;
    Button finishFormButton, renameButton, settingsButton;

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
    public static final String PDT_AUTOENTER_STATUS = "pdt_autoenter_status";
    public static final String PDT_SHOW_LIST_DETAIL = "pdt_show_list_detail";

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String guid, androidID, priceOfDisplayedItem, stockOfDisplayedItem;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ResultSet rs;
    ArrayList querydata;
    private static DokladAdapter adapter;
    ArrayList<Doklad> arrayOfUsers;
    ArrayList<String> scannedBarcodes;
    Integer polozka_mnozstvi_new = new Integer(0);
    String previousBarcode, barcode;
    Integer indexOfDisplayedItem = -1;
    boolean arraycontains = false;
    boolean offlineScanning = false;
    Switch offlineScanningSwitch;
    TextView offlineScanningSwitchTextview, barcodesToSendTextView;

    Integer runCounter;

    TextView titleTextView, dividerTV, delkaTextView, rozmerTextView, hiddenPDTTextView, totalPriceTextView;
    TextView horizontalDividerLineTV, mnozstviTextView, sectionTextView, totalAmountTextView;

    String barcodeString;
    String z, typ_akce, note, form, item_count_string, stav_dokladu, doklad_hlavicka_id;
    Integer form_type_id,target_warehouse, hlavicka_id, polozka_id, zalozeno, item_count, total_item_count;
    Float totalItemsPrice = new Float("0.00");
    BigDecimal totalItemsPriceBD = new BigDecimal("0.00");

    Date datum;
    BigDecimal amount;
    String query, device_id;
    Boolean existence, poKuse;
    Boolean barevne_velikostni;
    Boolean showListItemsDetail = true;
    Integer sklad_id;
    Vector cache;
    ListView listView;
    TextView eanTextView, nameTextView, thirdTextView, fourthTextView, fifthTextView, warehousepriceTextview, warehouseAmountTextView;

    String nazev, kod, ean_polozky, barva_polozky, velikost, delka, rozmer, polozka_nazev, pc, stav;
    Integer mnozstvi;
    TextToSpeech textToSpeech;
    Locale czechLocale = new Locale("cs");
    Locale czechLocale2 = new Locale("cs_CZ");
    Timestamp staticTS, dynamicTS;
    Boolean errorStopper = false;

    Long lastSentTimestamp;
    ConnectionClass connectionClass;

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_p_d_t_creation);
        SharedPreferences sharedPreferences;
        cameraView = (SurfaceView) findViewById(R.id.surface_view_pdt_creation);
        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = preferences.edit();
        onePieceToggleButton = (Button) findViewById(R.id.onePieceButton);
        variableAmountToggleButton = (Button) findViewById(R.id.multiplePiecesButton);
        onePieceToggleButton.setVisibility(View.GONE);
        variableAmountToggleButton.setVisibility(View.GONE);
        cameraView = (SurfaceView) findViewById(R.id.surface_view_pdt_creation);
        cameraView.setVisibility(View.GONE);
        item_count = 1;
        zalozeno = 1;
        item_count_string = "1";
        poKuse = preferences.getBoolean(PDT_AUTOENTER_STATUS, true);
        showListItemsDetail = preferences.getBoolean(PDT_SHOW_LIST_DETAIL, false);
        barcodeString = preferences.getString(BARCODE, "");
        guid = preferences.getString(GUID, "");
        androidID = preferences.getString(ID,"");
        sklad_id = preferences.getInt(SKLAD, 0);
        stav_dokladu = preferences.getString(DOKLAD_STAV, "old");
        doklad_hlavicka_id = preferences.getString(DOKLAD_HLAVICKA_ID, "0");
        listView = findViewById(R.id.listViewPolozky);
        total_item_count = 0;
        dividerTV = findViewById(R.id.itemCountPDTTextView);
        horizontalDividerLineTV = (TextView) findViewById(R.id.verticalDividerPDTTextView);
        mnozstviTextView = (TextView) findViewById(R.id.mnozstviTextView);
        eanTextView = (TextView) findViewById(R.id.eanTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        thirdTextView = (TextView) findViewById(R.id.thirdTextView);
        fourthTextView = (TextView) findViewById(R.id.fourthTextView);
        fifthTextView = (TextView) findViewById(R.id.fifthTextView);
        delkaTextView = (TextView) findViewById(R.id.delkaTextView);
        rozmerTextView = (TextView) findViewById(R.id.rozmerTextView);
        hiddenPDTTextView = (TextView) findViewById(R.id.hiddenTextViewPDT);
        warehouseAmountTextView = (TextView) findViewById(R.id.warehouseAmountTextView);
        warehousepriceTextview = (TextView) findViewById(R.id.warehousePriceTextView);
        totalPriceTextView = (TextView) findViewById(R.id.totalPriceTextView);
        scannedBarcodes = new ArrayList<String>();
        offlineScanningSwitch = (Switch) findViewById(R.id.offlineScanSwitch);
        offlineScanningSwitchTextview = (TextView) findViewById(R.id.offlineSwitchTextView);
        offlineScanningSwitch.setChecked(offlineScanning);
        barcodesToSendTextView = (TextView) findViewById(R.id.barcodesToSendTextview);

        final DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        eanTextView.setVisibility(View.GONE);
        nameTextView.setVisibility(View.GONE);
        thirdTextView.setVisibility(View.GONE);
        mnozstviTextView.setVisibility(View.GONE);
        fourthTextView.setVisibility(View.GONE);
        fifthTextView.setVisibility(View.GONE);
        delkaTextView.setVisibility(View.GONE);
        rozmerTextView.setVisibility(View.GONE);
        warehouseAmountTextView.setVisibility(View.GONE);
        warehousepriceTextview.setVisibility(View.GONE);

        connectionClass = new ConnectionClass();

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(); // your code
                pullToRefresh.setRefreshing(false);
            }
        });

        offlineScanningSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                offlineScanning = isChecked;
            }
        });

        settingsButton = (Button) findViewById(R.id.pdtSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                poKuse = preferences.getBoolean(PDT_AUTOENTER_STATUS, true);
                showListItemsDetail = preferences.getBoolean(PDT_SHOW_LIST_DETAIL, false);

                LayoutInflater inflater = LayoutInflater.from(PDTCreationActivity.this);
                View v = inflater.inflate(R.layout.pdt_creaton_menu_layout, null, false);
                final Switch autoEnterSwitch = v.findViewById(R.id.pdtMenu1vXwitch);
                final Switch showListDetailSwitch = v.findViewById(R.id.pdtMenuShowListItemDetail);
                autoEnterSwitch.setChecked(poKuse);
                showListDetailSwitch.setChecked(showListItemsDetail);


                final AlertDialog dialog = new AlertDialog.Builder(PDTCreationActivity.this)
                        .setTitle(R.string.settings)
                        .setView(v)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                poKuse = autoEnterSwitch.isChecked();
                                showListItemsDetail = showListDetailSwitch.isChecked();
                                editor.putBoolean(PDT_AUTOENTER_STATUS, poKuse);
                                editor.putBoolean(PDT_SHOW_LIST_DETAIL, showListItemsDetail);
                                editor.apply();
                                adapter.notifyDataSetChanged();

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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

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


        hiddenPDTTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!nameTextView.getText().toString().matches("") && !nameTextView.getText().toString().matches("Neznámá položka") && indexOfDisplayedItem != -1) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                    alert.setTitle("Výběr akce");
                    //alert.setMessage(e.toString());
                    alert.setPositiveButton("Změna množství", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            AlertDialog.Builder innerAlert = new AlertDialog.Builder(PDTCreationActivity.this);
                            final String ok = getResources().getString(R.string.ok);
                            final String close = getResources().getString(R.string.close);
                            final String amount = getResources().getString(R.string.amount_pcs);
                            final String amount_msg = getResources().getString(R.string.amount_message);
                            innerAlert.setTitle(amount);
                            innerAlert.setMessage(amount_msg);
                            final EditText input = new EditText(PDTCreationActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            input.setRawInputType(Configuration.KEYBOARD_12KEY);
                            input.setHint(arrayOfUsers.get(indexOfDisplayedItem).getMnozstvi_polozky().toString());
                            innerAlert.setView(input);
                            innerAlert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if(input.getText().toString().matches("")){
                                        polozka_mnozstvi_new = Integer.parseInt(arrayOfUsers.get(indexOfDisplayedItem).getMnozstvi_polozky().toString());
                                    }else {
                                        polozka_mnozstvi_new = Integer.parseInt(input.getText().toString());
                                    }
                                    try {
                                        final ConnectionClass connectionClass_1 = new ConnectionClass();
                                        Connection con = connectionClass_1.CONN();
                                        if (con == null) {
                                            String z = "chyba v propojeni";
                                        } else {
                                            query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Změna_množství','" + hlavicka_id + "','" + arrayOfUsers.get(indexOfDisplayedItem).getEan().toString() + "','" + polozka_mnozstvi_new +"','1'";
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
                                            String[] item2 = new String[11];
                                            String referenceEan = arrayOfUsers.get(indexOfDisplayedItem).getEan().toString();
                                            arrayOfUsers = new ArrayList<Doklad>();
                                            Integer indexCounter = 0;
                                            total_item_count = 0;
                                            totalItemsPrice = new Float("0.00");
                                            totalItemsPriceBD = new BigDecimal("0.00");


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
                                                try{
                                                    pc = rs.getString("CENA");
                                                    stav = rs.getString("STAV");
                                                    String pcString = pc.split(" ")[1];
                                                    Float innerItemPrice = new Float(pcString);
                                                    innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                                    totalItemsPrice = totalItemsPrice + innerItemPrice;

                                                    BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                                    BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                                    innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                                    totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                                }catch(Exception ex){
                                                    ex.printStackTrace();
                                                }



                                                if(referenceEan.matches(ean_polozky)){
                                                    indexOfDisplayedItem = indexCounter;
                                                    eanTextView.setVisibility(View.VISIBLE);
                                                    nameTextView.setVisibility(View.VISIBLE);
                                                    thirdTextView.setVisibility(View.VISIBLE);
                                                    mnozstviTextView.setVisibility(View.VISIBLE);
                                                    eanTextView.setText("EAN: " + ean_polozky);
                                                    nameTextView.setText(nazev);
                                                    thirdTextView.setText("Kód : " + kod);
                                                    mnozstviTextView.setText("Množství: " + mnozstvi.toString());
                                                    fourthTextView.setText(velikost);
                                                    fifthTextView.setText(barva_polozky);
                                                    delkaTextView.setText(delka);
                                                    rozmerTextView.setText(rozmer);
                                                    try{
                                                        warehousepriceTextview.setText(pc);
                                                        warehouseAmountTextView.setText(stav);
                                                    }catch(Exception ex){
                                                        ex.printStackTrace();
                                                    }

                                                    if(velikost.matches("")){
                                                        fourthTextView.setVisibility(View.GONE);
                                                    }else{
                                                        fourthTextView.setVisibility(View.VISIBLE);
                                                    }
                                                    if(barva_polozky.matches("")){
                                                        fifthTextView.setVisibility(View.GONE);
                                                    }else{
                                                        fifthTextView.setVisibility(View.VISIBLE);
                                                    }
                                                    if(delka.matches("")){
                                                        delkaTextView.setVisibility(View.GONE);
                                                    }else{
                                                        delkaTextView.setVisibility(View.VISIBLE);
                                                    }
                                                    if(rozmer.matches("")){
                                                        rozmerTextView.setVisibility(View.GONE);
                                                    }else{
                                                        rozmerTextView.setVisibility(View.VISIBLE);
                                                    }




                                                }

                                                item2[0] = hlavicka_id.toString();
                                                item2[1] = polozka_id.toString();
                                                item2[2] = polozka_nazev;
                                                item2[6] = ean_polozky;
                                                item2[4] = mnozstvi.toString();
                                                item2[5] = kod;
                                                item2[3] = nazev;
                                                item2[7] = barva_polozky;
                                                item2[8] = velikost;
                                                item2[9] = delka;
                                                item2[10] = rozmer;
                                                arrayOfUsers.add(new Doklad(item2[0],item2[1],item2[2],item2[3],item2[4], item2[5], item2[6], item2[7],item2[8], item2[9], item2[10]));
                                                total_item_count = total_item_count + mnozstvi;
                                                indexCounter++;
                                            }
                                            adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                                            //Attach the adapter to a ListView
                                            listView = findViewById(R.id.listViewPolozky);
                                            dividerTV.setText(total_item_count.toString() + " položek");
                                            totalPriceTextView.setText(totalItemsPrice.toString() + " Kč");
                                            listView.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                            registerForContextMenu(listView);
                                        }
                                    }catch (Exception ex){
                                        String f = ex.getMessage();
                                    }
                                }
                            });
                            innerAlert.setNegativeButton(close, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    exit(0);
                                }
                            });
                            //Looper.prepare();
                            AlertDialog innerDialog = innerAlert.create();
                            innerDialog.show();
                        }
                    });
                    alert.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    alert.setNeutralButton("Smazat položku", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                final ConnectionClass connectionClass_1 = new ConnectionClass();
                                Connection con = connectionClass_1.CONN();
                                if (con == null) {
                                    String z = "chyba v propojeni";
                                } else {
                                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Odstranění_položky','" + hlavicka_id + "','" + arrayOfUsers.get(indexOfDisplayedItem).getEan().toString() + "','0','1'";
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
                                    String[] item2 = new String[11];
                                    arrayOfUsers = new ArrayList<Doklad>();
                                    total_item_count = 0;
                                    totalItemsPrice = new Float("0.00");
                                    totalItemsPriceBD = new BigDecimal("0.00");

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
                                        total_item_count = total_item_count + mnozstvi;
                                        try{
                                            pc = rs.getString("CENA");
                                            stav = rs.getString("STAV");
                                            String pcString = pc.split(" ")[1];
                                            Float innerItemPrice = new Float(pcString);
                                            innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                            totalItemsPrice = totalItemsPrice + innerItemPrice;

                                            BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                            BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                            innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                            totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }




                                        item2[0] = hlavicka_id.toString();
                                        item2[1] = polozka_id.toString();
                                        item2[2] = polozka_nazev;
                                        item2[6] = ean_polozky;
                                        item2[4] = mnozstvi.toString();
                                        item2[5] = kod;
                                        item2[3] = nazev;
                                        item2[7] = barva_polozky;
                                        item2[8] = velikost;
                                        item2[9] = delka;
                                        item2[10] = rozmer;
                                        arrayOfUsers.add(new Doklad(item2[0],item2[1],item2[2],item2[3],item2[4], item2[5], item2[6], item2[7],item2[8], item2[9], item2[10]));
                                    }

                                    indexOfDisplayedItem = -1;
                                    eanTextView.setVisibility(View.VISIBLE);
                                    nameTextView.setVisibility(View.VISIBLE);
                                    thirdTextView.setVisibility(View.GONE);
                                    mnozstviTextView.setVisibility(View.GONE);
                                    eanTextView.setText("");
                                    nameTextView.setText("Položka smazána");
                                    thirdTextView.setText("");
                                    mnozstviTextView.setText("");
                                    fourthTextView.setText("");
                                    fifthTextView.setText("");
                                    delkaTextView.setText("");
                                    rozmerTextView.setText("");
                                    warehouseAmountTextView.setText("");
                                    warehousepriceTextview.setText("");

                                    adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                                    //Attach the adapter to a ListView
                                    listView = findViewById(R.id.listViewPolozky);
                                    dividerTV.setText(total_item_count.toString() + " položek");
                                    totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    registerForContextMenu(listView);
                                }
                            }catch (Exception ex){
                                String f = ex.getMessage();
                            }
                        }
                    });
                    alert.show();
                    return false;
                }else{
                    return false;
                }
            }
        });


        final ConstraintLayout alertBoxLayout =  findViewById(R.id.include);
        try{
            alertBoxLayout.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }
        hlavicka_id = -1;
        titleTextView = (TextView) findViewById(R.id.pdt_creation_title_textView);
        errorStopper = false;

        runCounter = 0;

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(czechLocale2);
                    //textToSpeech.setVoice();
                }
            }
        });

        finishFormButton = (Button) findViewById(R.id.pdtCreationFinishButton);
        finishFormButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        finishFormButton.setVisibility(View.VISIBLE);
        renameButton = (Button) findViewById(R.id.renameButton);
        finishFormButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String barcodeCluster = "";
                for(int i = 0; i < scannedBarcodes.size(); i++){
                    barcodeCluster = barcodeCluster + scannedBarcodes.get(i).toString() + ",";
                }

                try {

                    new AsyncGroupSQLQuery().execute(barcodeCluster, "1");




                } catch (Exception ex) {
                    cache = new Vector();
                    ex.printStackTrace();
                }

                return false;
            }
        });

        renameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                openSave();
                /*
                final String ok = getResources().getString(R.string.ok);
                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                alert.setTitle("Změna názvu");
                alert.setMessage("Zadejte nový název");
                final EditText nameEditText = new EditText(PDTCreationActivity.this);
                nameEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTqQuUvVwWxXyYzZ"));
                alert.setView(nameEditText);

                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String inputName = nameEditText.getText().toString();
                        String innerInputName = inputName;
                        if(inputName.matches("")){
                            Toast.makeText(PDTCreationActivity.this, "Zadejte název zásobníku", Toast.LENGTH_SHORT).show();

                        }else{
                            try {
                                final ConnectionClass connectionClass_1 = new ConnectionClass();
                                Connection con = connectionClass_1.CONN();
                                if (con == null) {
                                    String z = "chyba v propojeni";
                                } else {
                                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Prejmenovani_dokladu','" + hlavicka_id + "','','0','0','','" + inputName + "'";
                                    Boolean success = false;
                                    Statement cs = null;
                                    cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                    rs = cs.executeQuery(query);
                                    if(rs.next()){
                                        Integer zalozeno = rs.getInt("ZALOZENO");
                                        if(zalozeno == 0){
                                            success = false;
                                        }else{
                                            success = true;
                                        }
                                    }
                                    if(success == true){
                                        //doklad zalozen
                                        //recreate();
                                        titleTextView.setText(inputName);
                                    }else{
                                        //doklad nebyl zalozen
                                        Toast.makeText(PDTCreationActivity.this, "Přejmenování se nezdařilo", Toast.LENGTH_SHORT);
                                    }
                                }
                            }catch (Exception ex){
                                String f = ex.getMessage();
                            }
                        }


                    }
                });


                alert.show();

                */


            }
        });
        renameButton.setVisibility(View.VISIBLE);
        try {
            unregisterReceiver(myBroadcastReceiver);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action_alternative));
        registerReceiver(myBroadcastReceiver, filter);

       final TextView EANtextView = (TextView) findViewById(R.id.pdtCreationEANTextView);
       final EditText alertAmountEditText = (EditText) findViewById(R.id.pdtCReationAlertBoxAmountEditText);
       final Button  amountOKbutton = (Button) findViewById(R.id.pdtCreationOkButton);
       final Button  amountCancelButton = (Button) findViewById(R.id.pdtCreationAlertCancelButton);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                alert.setTitle("Smazat zásobník?");
                alert.setMessage("Opravdu chcete zásobník smazat? Tento krok nelze vrátit!");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(stav_dokladu == "unfinished"){
                            Integer i = Integer.parseInt(doklad_hlavicka_id);
                            hlavicka_id = i;
                        }
                        try {
                            final ConnectionClass connectionClass_1 = new ConnectionClass();
                            Connection con = connectionClass_1.CONN();
                            if (con == null) {
                                String z = "chyba v propojeni";
                            } else {
                                query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Odstranit_doklad','" + hlavicka_id + "','','0','1'";
                                Boolean success = false;
                                Statement cs = null;
                                cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                rs = cs.executeQuery(query);
                                if(rs.next()){
                                    Integer odstraneno = rs.getInt("ODSTRANENO");
                                    if(odstraneno == 0){
                                        success = false;
                                    }else{
                                        success = true;
                                    }
                                }
                                if(success == true){
                                    //doklad zalozen
                                    finishAffinity();
                                    Intent leIntent = new Intent(PDTCreationActivity.this, AppActivity.class);
                                    startActivity(leIntent);
                                }else{
                                    //doklad nebyl zalozen
                                }
                            }
                        }catch (Exception ex){
                            String f = ex.getMessage();
                        }
                    }
                });
                alert.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.setCancelable(false);
                alert.show();


            }
        });
        //Button finishButton = (Button) findViewById(R.id.pdtCreationFinishButton);
        //finishButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        if( querydata == null){
        //            Toast.makeText(getApplicationContext(), "arraylist is empty", Toast.LENGTH_LONG);
        //        }else if (querydata.size() >= 1){
        //            try {
        //                final ConnectionClass connectionClass_1 = new ConnectionClass();
        //                Connection con = connectionClass_1.CONN();
        //                if (con == null) {
        //                    String z = "chyba v propojeni";
        //                } else {
        //                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Konec_dokladu','" + hlavicka_id + "','','0','1'";
        //                    Boolean success = false;
        //                    Statement cs = null;
        //                    cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //                    rs = cs.executeQuery(query);
        //                    if(rs.next()){
        //                        Integer zalozeno = rs.getInt("ZALOZENO");
        //                        if(zalozeno == 0){
        //                            success = false;
        //                        }else{
        //                            success = true;
        //                        }
        //                    }
        //                    if(success == true){
        //                        //doklad zalozen
        //                        recreate();
        //                    }else{
        //                        //doklad nebyl zalozen
        //                    }
        //                }
        //            }catch (Exception ex){
        //                String f = ex.getMessage();
        //            }
        //        }else{
        //            Toast.makeText(PDTCreationActivity.this, "arraylist is not empty", Toast.LENGTH_LONG);
        //        }
        //    }
        //});
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

        onePieceToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onePieceToggleButton.setBackgroundColor(getResources().getColor(R.color.winshop_dark_blue));
                onePieceToggleButton.setTextColor(getResources().getColor(R.color.winshop_white));
                variableAmountToggleButton.setBackgroundColor(Color.LTGRAY);
                variableAmountToggleButton.setTextColor(getResources().getColor(R.color.black));
                poKuse = true;
            }
        });
        variableAmountToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                variableAmountToggleButton.setBackgroundColor(getResources().getColor(R.color.winshop_dark_blue));
                variableAmountToggleButton.setTextColor(getResources().getColor(R.color.winshop_white));
                onePieceToggleButton.setBackgroundColor(Color.LTGRAY);
                onePieceToggleButton.setTextColor(getResources().getColor(R.color.black));
                poKuse = false;
            }
        });
        try {
            Connection con = connectionClass.CONN();
            if (con == null){
                String z = "chyba v propojeni";

            }else{
                if(stav_dokladu == "new"){
                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Nový_doklad','-1'" ;
                    titleTextView.setText("");

                }else if(stav_dokladu == "unfinished"){

                    Integer i = Integer.parseInt(doklad_hlavicka_id);
                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Otevřít_doklad','" + i +"'" ;
                    editor.putInt(CURRENT_HLAVICKA_ID, i);
                    hlavicka_id = i;
                    existence = false;
                }else{
                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Nový_doklad','','" + "'" ;
                }
                editor.putString(DOKLAD_STAV, "old");
                editor.apply();
                Statement cs = null;
                cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                rs = cs.executeQuery(query);
                Boolean isValid = true;
                String columnName = "CHYBNY_PRISTUP";
                ResultSetMetaData rsmd = rs.getMetaData();
                Integer columnCOunt = rsmd.getColumnCount();
                Integer rowCount = 0;
                for (int x = 1; x <= columnCOunt; x++) {
                    if (columnName.equals(rsmd.getColumnName(x))) {
                        isValid = false;
                        break;
                    }
                }
                if(stav_dokladu == "unfinished"){
                    existence = false;
                    ////////////

                    ////////////
                }else {
                    for (int y = 1; y <= columnCOunt; y++) {
                        if (rsmd.getColumnName(y).equals("ZALOZENO")) {
                            existence = false;
                            //hlavicka_id = rs.getInt("HLAVICKA_ID");
                        }
                    }
                }
                String[] resultSet = new String[columnCOunt];
                int count = 0;

                if(existence == true) {
                    //jiz existuje neuzavreny doklad, aplikace se zepta na pokracovani
                    Intent selectionIntent = new Intent(PDTCreationActivity.this, PDTSelectionActivity.class);
                    releaseCameraAndPreview();
                    startActivity(selectionIntent);
                }else{
                    if (rs.next()) {
                        if(stav_dokladu == "unfinished" /*|| stav_dokladu == "old" */){
                            //rs = cs.executeQuery(query);
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            String typ_vysledku;
                            Integer rowcount = new Integer(0);
                            ResultSetMetaData rsmd2 = rs.getMetaData();
                            Integer columnCount = rsmd.getColumnCount();
                            Integer rowCount2 = 0;
                            String[] resultSet2 = new String[columnCOunt];
                            Integer rowCounter = 0;
                            querydata = new ArrayList<String>();
                            String[] item = new String[11];
                            //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                            //rs = stmt.executeQuery(query);
                            Integer pocitadlo = 0;
                            total_item_count = 0;
                            totalItemsPrice = new Float("0.00");
                            totalItemsPriceBD = new BigDecimal("0.00");

                            String nazev = rs.getString("NAZEV");
                            titleTextView.setText(nazev);

                            arrayOfUsers = new ArrayList<Doklad>();
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            polozka_id = rs.getInt("POLOZKA_ID");
                            //polozka_nazev = rs.getString("POLOZKA");
                            ean_polozky = rs.getString("EAN");
                            mnozstvi = rs.getInt("MNOZSTVI");
                            kod = rs.getString("KOD_ZBOZI");
                            nazev = rs.getString("NAZEV_ZBOZI");
                            barva_polozky = rs.getString("NAZEV_BARVY");
                            velikost = rs.getString("NAZEV_VELIKOSTI");
                            delka = rs.getString("NAZEV_DELKY");
                            rozmer = rs.getString("NAZEV_ROZMERU");
                            try{
                                pc = rs.getString("CENA");
                                stav = rs.getString("STAV");
                                String pcString = pc.split(" ")[1];
                                Float innerItemPrice = new Float(pcString);
                                innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                String innerItemString = innerItemPrice.toString();
                                totalItemsPrice = totalItemsPrice + innerItemPrice;

                                BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }



                            item[0] = hlavicka_id.toString();
                            item[1] = polozka_id.toString();
                            item[2] = "";
                            item[6] = ean_polozky;
                            item[4] = mnozstvi.toString();
                            item[5] = kod;
                            item[3] = nazev;
                            item[7] = barva_polozky;
                            item[8] = velikost;
                            item[9] = delka;
                            item[10] = rozmer;
                            arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));

                            total_item_count = total_item_count + mnozstvi;

                            while(rs.next()){
                                querydata.add(resultSet2);
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

                                try{
                                    pc = rs.getString("CENA");
                                    stav = rs.getString("STAV");
                                    String pcString = pc.split(" ")[1];
                                    Float innerItemPrice = new Float(pcString);
                                    innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                    totalItemsPrice = totalItemsPrice + innerItemPrice;

                                    BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                    BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                    innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                    totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                }catch(Exception ex){
                                    ex.printStackTrace();
                                }




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
                                arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                total_item_count = total_item_count + mnozstvi;

                            }
                            adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                            //Attach the adapter to a ListView
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                            dividerTV.setText(total_item_count.toString() + " položek");
                            totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                            registerForContextMenu(listView);
                            editor.putInt(CURRENT_HLAVICKA_ID, hlavicka_id);
                            editor.apply();
                        }else {
                            zalozeno = rs.getInt("ZALOZENO");
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            String nazev = rs.getString("NAZEV");
                            titleTextView.setText(nazev);
                            editor.putInt(CURRENT_HLAVICKA_ID, hlavicka_id);
                            editor.apply();
                        }
                    }
                    if (zalozeno == 0 && stav_dokladu != "unfinished"){
                        //error catch v procedure
                        querydata = new ArrayList<String>();
                    }else{

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            z = e.getMessage();
            System.out.println(rs);
            z = "the end";
            AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
            alert.setTitle(error);
            alert.setMessage(e.toString());
            alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                indexOfDisplayedItem = i;
                String innerName = arrayOfUsers.get(i).getNazev_polozky();
                String innerAmount = arrayOfUsers.get(i).getMnozstvi_polozky();
                String innerEan = arrayOfUsers.get(i).getEan();
                String innerCode = arrayOfUsers.get(i).getKod_zbozi();
                String innerSize = arrayOfUsers.get(i).getNazev_velikosti();
                String innerColour = arrayOfUsers.get(i).getBarva();
                String innerLength = arrayOfUsers.get(i).getNazev_delky();
                String innerDimensions = arrayOfUsers.get(i).getNazev_rozmeru();

                eanTextView.setVisibility(View.VISIBLE);
                nameTextView.setVisibility(View.VISIBLE);
                thirdTextView.setVisibility(View.VISIBLE);
                mnozstviTextView.setVisibility(View.VISIBLE);


                eanTextView.setText("EAN: " + innerEan);
                nameTextView.setText(innerName);
                thirdTextView.setText("Kód : " + innerCode);
                mnozstviTextView.setText("Množství: " + innerAmount.toString());
                fourthTextView.setText(innerSize);
                fifthTextView.setText(innerColour);
                delkaTextView.setText(innerLength);
                rozmerTextView.setText(innerDimensions);
                warehousepriceTextview.setText("");
                warehouseAmountTextView.setText("");
                if(innerSize.matches("")){
                    fourthTextView.setVisibility(View.GONE);
                }else{
                    fourthTextView.setVisibility(View.VISIBLE);
                }
                if(innerColour.matches("")){
                    fifthTextView.setVisibility(View.GONE);
                }else{
                    fifthTextView.setVisibility(View.VISIBLE);
                }
                if(innerLength.matches("")){
                    delkaTextView.setVisibility(View.GONE);
                }else{
                    delkaTextView.setVisibility(View.VISIBLE);
                }
                if(innerDimensions.matches("")){
                    rozmerTextView.setVisibility(View.GONE);
                }else{
                    rozmerTextView.setVisibility(View.VISIBLE);
                }
            }
        });



    } //Konec onCreate

    private void releaseCameraAndPreview() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private void refreshData() {

        String error = getResources().getString(R.string.error);
        String ok = getResources().getString(R.string.ok);
        try {
            Connection con = connectionClass.CONN();
            if (con == null){
                String z = "chyba v propojeni";

            }else{


                Integer i = Integer.parseInt(doklad_hlavicka_id);
                query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Otevřít_doklad','" + i +"'" ;
                editor.putInt(CURRENT_HLAVICKA_ID, i);
                hlavicka_id = i;
                existence = false;

                editor.putString(DOKLAD_STAV, "old");
                editor.apply();
                Statement cs = null;
                cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                rs = cs.executeQuery(query);
                Boolean isValid = true;
                String columnName = "CHYBNY_PRISTUP";
                ResultSetMetaData rsmd = rs.getMetaData();
                Integer columnCOunt = rsmd.getColumnCount();
                Integer rowCount = 0;
                for (int x = 1; x <= columnCOunt; x++) {
                    if (columnName.equals(rsmd.getColumnName(x))) {
                        isValid = false;
                        break;
                    }
                }

                for (int y = 1; y <= columnCOunt; y++) {
                    if (rsmd.getColumnName(y).equals("ZALOZENO")) {
                        existence = false;
                        //hlavicka_id = rs.getInt("HLAVICKA_ID");
                    }
                }

                String[] resultSet = new String[columnCOunt];
                int count = 0;

                if(existence == true) {
                    //jiz existuje neuzavreny doklad, aplikace se zepta na pokracovani
                    Intent selectionIntent = new Intent(PDTCreationActivity.this, PDTSelectionActivity.class);
                    releaseCameraAndPreview();
                    startActivity(selectionIntent);
                }else{
                    if (rs.next()) {
                        if(stav_dokladu == "unfinished" /*|| stav_dokladu == "old" */){
                            //rs = cs.executeQuery(query);
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            String typ_vysledku;
                            Integer rowcount = new Integer(0);
                            ResultSetMetaData rsmd2 = rs.getMetaData();
                            Integer columnCount = rsmd.getColumnCount();
                            Integer rowCount2 = 0;
                            String[] resultSet2 = new String[columnCOunt];
                            Integer rowCounter = 0;
                            querydata = new ArrayList<String>();
                            String[] item = new String[11];
                            //query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','0'";
                            //rs = stmt.executeQuery(query);
                            Integer pocitadlo = 0;
                            total_item_count = 0;
                            totalItemsPrice = new Float("0.00");
                            totalItemsPriceBD = new BigDecimal("0.00");

                            String nazev = rs.getString("NAZEV");
                            titleTextView.setText(nazev);

                            arrayOfUsers = new ArrayList<Doklad>();
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            polozka_id = rs.getInt("POLOZKA_ID");
                            //polozka_nazev = rs.getString("POLOZKA");
                            ean_polozky = rs.getString("EAN");
                            mnozstvi = rs.getInt("MNOZSTVI");
                            kod = rs.getString("KOD_ZBOZI");
                            nazev = rs.getString("NAZEV_ZBOZI");
                            barva_polozky = rs.getString("NAZEV_BARVY");
                            velikost = rs.getString("NAZEV_VELIKOSTI");
                            delka = rs.getString("NAZEV_DELKY");
                            rozmer = rs.getString("NAZEV_ROZMERU");
                            try{
                                pc = rs.getString("CENA");
                                stav = rs.getString("STAV");
                                String pcString = pc.split(" ")[1];
                                DecimalFormat df = new DecimalFormat("#.##");
                                df.setRoundingMode(RoundingMode.CEILING);
                                Float innerItemPrice = new Float(pcString);
                                innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                totalItemsPrice = totalItemsPrice + innerItemPrice;

                                BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }




                            item[0] = hlavicka_id.toString();
                            item[1] = polozka_id.toString();
                            item[2] = "";
                            item[6] = ean_polozky;
                            item[4] = mnozstvi.toString();
                            item[5] = kod;
                            item[3] = nazev;
                            item[7] = barva_polozky;
                            item[8] = velikost;
                            item[9] = delka;
                            item[10] = rozmer;
                            arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));

                            total_item_count = total_item_count + mnozstvi;

                            while(rs.next()){
                                querydata.add(resultSet2);
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

                                try{
                                    pc = rs.getString("CENA");
                                    stav = rs.getString("STAV");
                                    String pcString = pc.split(" ")[1];
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    df.setRoundingMode(RoundingMode.CEILING);
                                    Float innerItemPrice = new Float(pcString);
                                    innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                    totalItemsPrice = totalItemsPrice + innerItemPrice;

                                    BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                    BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                    innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                    totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                }catch(Exception ex){
                                    ex.printStackTrace();
                                }



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
                                arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                total_item_count = total_item_count + mnozstvi;

                            }
                            adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                            //Attach the adapter to a ListView
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                            dividerTV.setText(total_item_count.toString() + " položek");
                            totalPriceTextView.setText(totalItemsPriceBD.setScale(2,BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                            registerForContextMenu(listView);
                            editor.putInt(CURRENT_HLAVICKA_ID, hlavicka_id);
                            editor.apply();
                        }else {
                            zalozeno = rs.getInt("ZALOZENO");
                            hlavicka_id = rs.getInt("HLAVICKA_ID");
                            String nazev = rs.getString("NAZEV");
                            titleTextView.setText(nazev);
                            editor.putInt(CURRENT_HLAVICKA_ID, hlavicka_id);
                            editor.apply();
                        }
                    }
                    if (zalozeno == 0 && stav_dokladu != "unfinished"){
                        //error catch v procedure
                        querydata = new ArrayList<String>();
                    }else{

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            z = e.getMessage();
            System.out.println(rs);
            z = "the end";
            AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
            alert.setTitle(error);
            alert.setMessage(e.toString());
            alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        }

    }

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
                Intent leIntent = new Intent(PDTCreationActivity.this, AppActivity.class);
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


    private void openSave() {
        final String ok = getResources().getString(R.string.ok);


        AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
        alert.setTitle("Změna názvu");
        alert.setMessage("Zadejte nový název");
        final EditText nameEditText = new EditText(PDTCreationActivity.this);
        nameEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTqQuUvVwWxXyYzZ "));
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        alert.setView(nameEditText);

        alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputName = nameEditText.getText().toString();
                String innerInputName = inputName;
                if(inputName.matches("")){
                    Toast.makeText(PDTCreationActivity.this, "Název nesmí být prázdný", Toast.LENGTH_SHORT).show();
                    openSave();
                }else{
                    try {
                        final ConnectionClass connectionClass_1 = new ConnectionClass();
                        Connection con = connectionClass_1.CONN();
                        if (con == null) {
                            String z = "chyba v propojeni";
                        } else {
                            query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Prejmenovani_dokladu','" + hlavicka_id + "','','0','0','','" + inputName + "'";
                            Boolean success = false;
                            Statement cs = null;
                            cs = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                            rs = cs.executeQuery(query);
                            if(rs.next()){
                                Integer zalozeno = rs.getInt("ZALOZENO");
                                if(zalozeno == 0){
                                    success = false;
                                }else{
                                    success = true;
                                }
                            }
                            if(success == true){
                                //doklad zalozen
                                //recreate();
                                titleTextView.setText(inputName);
                            }else{
                                //doklad nebyl zalozen
                                Toast.makeText(PDTCreationActivity.this, "Přejmenování se nezdařilo", Toast.LENGTH_SHORT);
                            }
                        }
                    }catch (Exception ex){
                        String f = ex.getMessage();
                    }
                }


            }
        });
        alert.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        alert.show();

    }




    public void showAlertDialogButtonClicked(View view) {
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
        alert.setView(input);
        alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                item_count_string = input.getText().toString();
            }
        });
        alert.setNegativeButton(close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                exit(0);
            }
        });
        Looper.prepare();
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
        final String mnozstvi_string = arrayOfUsers.get(position).getMnozstvi_polozky();
        Integer polozka_mnozstvi = Integer.valueOf(mnozstvi_string);
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
                input.setHint(mnozstvi_string);
                alert.setView(input);
                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText().toString().matches("")){
                            polozka_mnozstvi_new = Integer.parseInt(mnozstvi_string);
                        }else {
                            polozka_mnozstvi_new = Integer.parseInt(input.getText().toString());
                        }
                        try {
                            final ConnectionClass connectionClass_1 = new ConnectionClass();
                            Connection con = connectionClass_1.CONN();
                            if (con == null) {
                                String z = "chyba v propojeni";
                            } else {
                                query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Změna_množství','" + hlavicka_id + "','" + polozka_ean + "','" + polozka_mnozstvi_new +"','1'";
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
                                String[] item2 = new String[11];
                                arrayOfUsers = new ArrayList<Doklad>();
                                Integer indexCounter = 0;
                                total_item_count = 0;
                                totalItemsPrice = new Float("0.00");
                                totalItemsPriceBD = new BigDecimal("0.00");

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
                                    try{
                                        pc = rs.getString("CENA");
                                        stav = rs.getString("STAV");
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        df.setRoundingMode(RoundingMode.CEILING);
                                        String pcString = pc.split(" ")[1];
                                        Float innerItemPrice = new Float(pcString);
                                        innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                        totalItemsPrice = totalItemsPrice + innerItemPrice;

                                        BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                        BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                        innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                        totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                    }catch(Exception ex){
                                        ex.printStackTrace();
                                    }



                                    if(polozka_ean.matches(ean_polozky)){
                                        indexOfDisplayedItem = indexCounter;
                                        eanTextView.setVisibility(View.VISIBLE);
                                        nameTextView.setVisibility(View.VISIBLE);
                                        thirdTextView.setVisibility(View.VISIBLE);
                                        mnozstviTextView.setVisibility(View.VISIBLE);
                                        eanTextView.setText("EAN: " + ean_polozky);
                                        nameTextView.setText(nazev);
                                        thirdTextView.setText("Kód : " + kod);
                                        mnozstviTextView.setText("Množství: " + mnozstvi.toString());
                                        fourthTextView.setText(velikost);
                                        fifthTextView.setText(barva_polozky);
                                        delkaTextView.setText(delka);
                                        rozmerTextView.setText(rozmer);
                                        try{
                                            warehousepriceTextview.setText(pc);
                                            warehouseAmountTextView.setText(stav);
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }

                                        if(velikost.matches("")){
                                            fourthTextView.setVisibility(View.GONE);
                                        }else{
                                            fourthTextView.setVisibility(View.VISIBLE);
                                        }
                                        if(barva_polozky.matches("")){
                                            fifthTextView.setVisibility(View.GONE);
                                        }else{
                                            fifthTextView.setVisibility(View.VISIBLE);
                                        }
                                        if(delka.matches("")){
                                            delkaTextView.setVisibility(View.GONE);
                                        }else{
                                            delkaTextView.setVisibility(View.VISIBLE);
                                        }
                                        if(rozmer.matches("")){
                                            rozmerTextView.setVisibility(View.GONE);
                                        }else{
                                            rozmerTextView.setVisibility(View.VISIBLE);
                                        }




                                    }

                                    item2[0] = hlavicka_id.toString();
                                    item2[1] = polozka_id.toString();
                                    item2[2] = polozka_nazev;
                                    item2[6] = ean_polozky;
                                    item2[4] = mnozstvi.toString();
                                    item2[5] = kod;
                                    item2[3] = nazev;
                                    item2[7] = barva_polozky;
                                    item2[8] = velikost;
                                    item2[9] = delka;
                                    item2[10] = rozmer;
                                    arrayOfUsers.add(new Doklad(item2[0],item2[1],item2[2],item2[3],item2[4], item2[5], item2[6], item2[7],item2[8], item2[9], item2[10]));
                                    total_item_count = total_item_count + mnozstvi;
                                    indexCounter++;
                                }
                                adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                                //Attach the adapter to a ListView
                                listView = findViewById(R.id.listViewPolozky);
                                dividerTV.setText(total_item_count.toString() + " položek");
                                totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                                listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
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
                try {
                    final ConnectionClass connectionClass_1 = new ConnectionClass();
                    Connection con = connectionClass_1.CONN();
                    if (con == null) {
                        String z = "chyba v propojeni";
                    } else {
                        query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Odstranění_položky','" + hlavicka_id + "','" + polozka_ean + "','0','1'";
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
                        String[] item2 = new String[11];
                        arrayOfUsers = new ArrayList<Doklad>();
                        total_item_count = 0;
                        totalItemsPrice = new Float("0.00");
                        totalItemsPriceBD = new BigDecimal("0.00");
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
                            total_item_count = total_item_count + mnozstvi;
                            try{
                                pc = rs.getString("CENA");
                                stav = rs.getString("STAV");
                                DecimalFormat df = new DecimalFormat("#.##");
                                df.setRoundingMode(RoundingMode.CEILING);
                                String pcString = pc.split(" ")[1];
                                Float innerItemPrice = new Float(pcString);
                                innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                totalItemsPrice = totalItemsPrice + innerItemPrice;

                                BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }



                            item2[0] = hlavicka_id.toString();
                            item2[1] = polozka_id.toString();
                            item2[2] = polozka_nazev;
                            item2[6] = ean_polozky;
                            item2[4] = mnozstvi.toString();
                            item2[5] = kod;
                            item2[3] = nazev;
                            item2[7] = barva_polozky;
                            item2[8] = velikost;
                            item2[9] = delka;
                            item2[10] = rozmer;
                            arrayOfUsers.add(new Doklad(item2[0],item2[1],item2[2],item2[3],item2[4], item2[5], item2[6], item2[7],item2[8], item2[9], item2[10]));
                        }

                        indexOfDisplayedItem = -1;
                        eanTextView.setVisibility(View.VISIBLE);
                        nameTextView.setVisibility(View.VISIBLE);
                        thirdTextView.setVisibility(View.GONE);
                        mnozstviTextView.setVisibility(View.GONE);
                        eanTextView.setText("");
                        nameTextView.setText("Položka smazána");
                        thirdTextView.setText("");
                        mnozstviTextView.setText("");
                        fourthTextView.setText("");
                        fifthTextView.setText("");
                        delkaTextView.setText("");
                        rozmerTextView.setText("");

                        adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                        //Attach the adapter to a ListView
                        listView = findViewById(R.id.listViewPolozky);
                        dividerTV.setText(total_item_count.toString() + " položek");
                        totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        registerForContextMenu(listView);
                    }
                }catch (Exception ex){
                    String f = ex.getMessage();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        //mySender.detachDuplexOutputChannel();
        try {
            unregisterReceiver(myBroadcastReceiver);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        super.onDestroy();
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();



            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))||action.equals(getResources().getString(R.string.activity_intent_filter_action_alternative))) {
                //  Received a barcode scan

                if(offlineScanning){
                    String decodedData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                    final String scanResult_1=intent.getStringExtra("SCAN_BARCODE1");
                    final String scanResult_2=intent.getStringExtra("SCAN_BARCODE2");
                    final int barcodeType = intent.getIntExtra("SCAN_BARCODE_TYPE", -1);
                    final String scanStatus=intent.getStringExtra("SCAN_STATE");
                    if("ok".equals(scanStatus)){
//Success
                        barcode = scanResult_1;
                    }else{
                        barcode = decodedData;
                    }
                    if(barcode.isEmpty()){
                        barcode = "";
                    }
                    if(!barcode.matches("")){
                        scannedBarcodes.add(barcode);
                        Integer barcodesN = scannedBarcodes.size();
                        barcodesToSendTextView.setText(barcodesN.toString() + " položek v offline zásobníku");
                    }
                }else {
                    try {
                        //imageView.setVisibility(View.GONE);
                        dynamicTS = new Timestamp(System.currentTimeMillis());

                        if (!errorStopper) {
                            displayScanResultAsync(intent, "via Broadcast");
                            Environment.getExternalStorageState();
                        } else {
                            MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                            mp.start();
                        }

                        //Intermediary solution attempt for multiple scan attempts per scan - didn't work
                    /*
                    if(staticTS == null){
                        staticTS = new Timestamp(System.currentTimeMillis());
                        displayScanResult(intent, "via Broadcast");
                        Environment.getExternalStorageState();
                        Long stat = staticTS.getTime();
                        Long dyn = dynamicTS.getTime();
                        stat = stat;
                        dyn = dyn;
                    }else if(staticTS.getTime() < dynamicTS.getTime() - 2000){
                        displayScanResult(intent, "via Broadcast");
                        Environment.getExternalStorageState();
                        Long stat = staticTS.getTime();
                        Long dyn = dynamicTS.getTime();
                        stat = stat;
                        dyn = dyn;
                    }else{
                        Long stat = staticTS.getTime();
                        Long dyn = dynamicTS.getTime();
                        stat = stat;
                        dyn = dyn;
                    } */

                        //generateNoteOnSD(context, "IN.txt", barcode);


                    } catch (Exception e) {
                        //  Catch if the UI does not exist when we receive the broadcast
                        String err = e.getMessage();
                    }
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
        Long currentTimeLong = System.currentTimeMillis();
        String currentTime = String.valueOf(currentTimeLong);
            //lastSentTimestamp = System.currentTimeMillis();
            try {
                lastSentTimestamp = System.currentTimeMillis();
                barcodeString = decodedData;
                final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                if (poKuse == true){
                    item_count = 1;
                    item_count_string = "1";
                    if (!barcode.matches("")) {

                        //Update barcode value to TextView
                        final String val = barcode;


                        cache = new Vector();
                        lastSentTimestamp = System.currentTimeMillis();
                        try {
                            barcodeString = val;
                            editor.putBoolean(BARCODE_IS_NEW, false);
                            editor.apply();
                            try {
                                Connection con = connectionClass.CONN();
                                if (con == null){
                                    String z = "chyba v propojeni";

                                }else{
                                    hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','" + item_count + "'";
                                    Statement stmt = null;
                                    PreparedStatement pstmt =
                                            con.prepareStatement(query,
                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                    ResultSet.CONCUR_READ_ONLY);
                                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                    lastSentTimestamp = System.currentTimeMillis();
                                    rs = pstmt.executeQuery();
                                    lastSentTimestamp = System.currentTimeMillis();
                                    ResultSetMetaData rsmd = rs.getMetaData();
                                    Integer columnCOunt = rsmd.getColumnCount();
                                    String[] resultSet = new String[columnCOunt];
                                    querydata = new ArrayList<String>();
                                    String[] item = new String[11];
                                    arrayOfUsers = new ArrayList<Doklad>();
                                    boolean arraycontains = false;
                                    lastSentTimestamp = System.currentTimeMillis();
                                    char tester = 'c';
                                    total_item_count = 0;
                                    totalItemsPrice = new Float("0.00");
                                    String existujici = "EXISTUJICI";
                                    for (int y=0;y<columnCOunt;y++){
                                        if (rsmd.getColumnName(y+1).matches(existujici)) {
                                            arraycontains = true;
                                            break;
                                        }
                                    }
                                    Integer indexCounter = 0;




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
                                        //barcodeIsValid = rs.getInt("EXISTUJICI");
                                        total_item_count += mnozstvi;
                                        try{
                                            pc = rs.getString("CENA");
                                            stav = rs.getString("STAV");
                                            DecimalFormat df = new DecimalFormat("#.##");
                                            df.setRoundingMode(RoundingMode.CEILING);
                                            String pcString = pc.split(" ")[1];
                                            Float innerItemPrice = new Float(pcString);
                                            innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                            totalItemsPrice = totalItemsPrice + innerItemPrice;
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }



                                        if(val.matches(ean_polozky)){
                                            indexOfDisplayedItem = indexCounter;
                                            eanTextView.setVisibility(View.VISIBLE);
                                            nameTextView.setVisibility(View.VISIBLE);
                                            thirdTextView.setVisibility(View.VISIBLE);
                                            mnozstviTextView.setVisibility(View.VISIBLE);
                                            eanTextView.setText("EAN: " + ean_polozky);
                                            nameTextView.setText(nazev);
                                            thirdTextView.setText("Kód : " + kod);
                                            mnozstviTextView.setText("Množství: " + mnozstvi.toString());
                                            fourthTextView.setText(velikost);
                                            fifthTextView.setText(barva_polozky);
                                            delkaTextView.setText(delka);
                                            rozmerTextView.setText(rozmer);
                                            try{
                                                warehousepriceTextview.setText(pc);
                                                warehouseAmountTextView.setText(stav);
                                            }catch(Exception ex){
                                                ex.printStackTrace();
                                            }

                                            if(velikost.matches("")){
                                                fourthTextView.setVisibility(View.GONE);
                                            }else{
                                                fourthTextView.setVisibility(View.VISIBLE);
                                            }
                                            if(barva_polozky.matches("")){
                                                fifthTextView.setVisibility(View.GONE);
                                            }else{
                                                fifthTextView.setVisibility(View.VISIBLE);
                                            }
                                            if(delka.matches("")){
                                                delkaTextView.setVisibility(View.GONE);
                                            }else{
                                                delkaTextView.setVisibility(View.VISIBLE);
                                            }
                                            if(rozmer.matches("")){
                                                rozmerTextView.setVisibility(View.GONE);
                                            }else{
                                                rozmerTextView.setVisibility(View.VISIBLE);
                                            }




                                        }

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
                                        lastSentTimestamp = System.currentTimeMillis();
                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                        indexCounter++;
                                    }
                                    adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                                    //Attach the adapter to a ListView
                                    registerForContextMenu(listView);
                                    adapter.notifyDataSetChanged();
                                    dividerTV.setText(total_item_count.toString() + " položek");
                                    totalPriceTextView.setText(totalItemsPrice.toString() + " Kč");
                                    listView.setAdapter(adapter);
                                    lastSentTimestamp = System.currentTimeMillis();
                                    if(arraycontains){
                                        try {

                                            //MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.companion.R.raw.beep_01a);
                                            //5
                                            errorStopper = true;

                                            if(val.matches(ean_polozky)){
                                                eanTextView.setText("");
                                                nameTextView.setText("Neznámá položka");
                                                thirdTextView.setText("");
                                                fourthTextView.setText("");
                                                fifthTextView.setText("");

                                            }


                                            String toSpeak = "Neznámá položka";
                                            Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                                            //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                                            mp.start();
                                            indexOfDisplayedItem = -1;

                                            AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this, R.style.AlertDialogStyle);
                                            alert.setTitle("Chyba");
                                            alert.setMessage("Neznámý čárový kód");
                                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    errorStopper = false;
                                                }
                                            });
                                            alert.setCancelable(false);
                                            alert.show();




                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }

                                    return;


                                }

                            }catch (Exception ex){
                                editor.putBoolean(BARCODE_IS_NEW, false);
                                editor.apply();
                                ex.printStackTrace();
                                z = ex.getMessage();
                                System.out.println(rs);
                                z = "the end";
                                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                                alert.setTitle("Chyba");
                                alert.setMessage(ex.toString());
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                alert.show();
                            }



                        } catch (Exception ex) {
                            cache = new Vector();
                            ex.printStackTrace();
                        }
                    }
                }else{
                    //showAlertDialogButtonClicked(cameraView);
                    if (!barcode.matches("")) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        final String close = getResources().getString(R.string.close);
                        final String amount = getResources().getString(R.string.amount_pcs);
                        final String amount_msg = getResources().getString(R.string.amount_message);
                        alert.setTitle(amount);
                        alert.setMessage(amount_msg);
                        final EditText input = new EditText(this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setRawInputType(Configuration.KEYBOARD_12KEY);
                        input.setText("");
                        input.setHint("1");
                        alert.setView(input);
                        alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String inputStringIntermediary = input.getText().toString();
                                if(inputStringIntermediary.matches("")){
                                    inputStringIntermediary = "1";
                                }

                                item_count = Integer.parseInt(inputStringIntermediary);
                                item_count_string = inputStringIntermediary;

                                final String val = barcode;
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(50);
                                }



                                cache = new Vector();
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
                                                    total_item_count = 0;
                                                    boolean arraycontains = false;
                                                    total_item_count = 0;
                                                    totalItemsPrice = new Float("0.00");
                                                    String existujici = "EXISTUJICI";
                                                    for (int y=0;y<columnCOunt;y++){
                                                        if (rsmd.getColumnName(y+1).matches(existujici)) {
                                                            arraycontains = true;
                                                            break;
                                                        }
                                                    }
                                                    Integer indexCounter = 0;
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
                                                        total_item_count += mnozstvi;
                                                        try{
                                                            pc = rs.getString("CENA");
                                                            stav = rs.getString("STAV");
                                                            DecimalFormat df = new DecimalFormat("#.##");
                                                            df.setRoundingMode(RoundingMode.CEILING);
                                                            String pcString = pc.split(" ")[1];
                                                            Float innerItemPrice = new Float(pcString);
                                                            innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                                                            totalItemsPrice = totalItemsPrice + innerItemPrice;

                                                            BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                                                            BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                                                            innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                                                            totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                                                        }catch(Exception ex){
                                                            ex.printStackTrace();
                                                        }


                                                        if(val.matches(ean_polozky)){
                                                            indexOfDisplayedItem = indexCounter;
                                                            eanTextView.setVisibility(View.VISIBLE);
                                                            nameTextView.setVisibility(View.VISIBLE);
                                                            thirdTextView.setVisibility(View.VISIBLE);
                                                            mnozstviTextView.setVisibility(View.VISIBLE);
                                                            eanTextView.setText("EAN: " + ean_polozky);
                                                            nameTextView.setText(nazev);
                                                            thirdTextView.setText("Kód : " + kod);
                                                            mnozstviTextView.setText("Množství: " + mnozstvi.toString());
                                                            fourthTextView.setText(velikost);
                                                            fifthTextView.setText(barva_polozky);
                                                            delkaTextView.setText(delka);
                                                            rozmerTextView.setText(rozmer);
                                                            try{
                                                                warehousepriceTextview.setText(pc);
                                                                warehouseAmountTextView.setText(stav);
                                                            }catch(Exception ex){
                                                                ex.printStackTrace();
                                                            }

                                                            if(velikost.matches("")){
                                                                fourthTextView.setVisibility(View.GONE);
                                                            }else{
                                                                fourthTextView.setVisibility(View.VISIBLE);
                                                            }
                                                            if(barva_polozky.matches("")){
                                                                fifthTextView.setVisibility(View.GONE);
                                                            }else{
                                                                fifthTextView.setVisibility(View.VISIBLE);
                                                            }
                                                            if(delka.matches("")){
                                                                delkaTextView.setVisibility(View.GONE);
                                                            }else{
                                                                delkaTextView.setVisibility(View.VISIBLE);
                                                            }
                                                            if(rozmer.matches("")){
                                                                rozmerTextView.setVisibility(View.GONE);
                                                            }else{
                                                                rozmerTextView.setVisibility(View.VISIBLE);
                                                            }
                                                        }

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
                                                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                                                        indexCounter++;
                                                    }
                                                    adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
                                                    //Attach the adapter to a ListView
                                                    adapter.notifyDataSetChanged();
                                                    listView.setAdapter(adapter);
                                                    dividerTV.setText(total_item_count.toString() + " položek");
                                                    totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
                                                    registerForContextMenu(listView);

                                                    if(arraycontains){
                                                        try {

                                                            //MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.companion.R.raw.beep_01a);
                                                            //5
                                                            errorStopper = true;

                                                            if(val.matches(ean_polozky)){
                                                                eanTextView.setText("");
                                                                nameTextView.setText("Neznámá položka");
                                                                thirdTextView.setText("");
                                                                fourthTextView.setText("");
                                                                fifthTextView.setText("");

                                                            }
                                                            indexOfDisplayedItem = -1;

                                                            String toSpeak = "Neznámá položka";
                                                            Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                                                            //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                            MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                                                            mp.start();

                                                            AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this, R.style.AlertDialogStyle);
                                                            alert.setTitle("Chyba");
                                                            alert.setMessage("Neznámý čárový kód");
                                                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                                    errorStopper = false;
                                                                }
                                                            });
                                                            alert.setCancelable(false);
                                                            alert.show();




                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                    }


                                                }
                                            }catch (Exception ex){
                                                editor.putBoolean(BARCODE_IS_NEW, false);
                                                editor.apply();
                                                ex.printStackTrace();
                                                z = ex.getMessage();
                                                System.out.println(rs);
                                                z = "the end";
                                                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
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
                                    cache = new Vector();
                                    ex.printStackTrace();
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


                    }
                }
            } catch (Exception ex) {

                ex.printStackTrace();
            }







    }


    private void displayScanResultAsync(Intent initiatingIntent, String howDataReceived)  {
        String decodedSource = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        final ConnectionClass connectionClass = new ConnectionClass();
        final String error = getResources().getString(R.string.error);
        final String ok = getResources().getString(R.string.ok);
        final String ean_not_found = getResources().getString(R.string.ean_not_found);

        final String scanResult_1=initiatingIntent.getStringExtra("SCAN_BARCODE1");
        final String scanResult_2=initiatingIntent.getStringExtra("SCAN_BARCODE2");
        final int barcodeType = initiatingIntent.getIntExtra("SCAN_BARCODE_TYPE", -1);
        final String scanStatus=initiatingIntent.getStringExtra("SCAN_STATE");
        if("ok".equals(scanStatus)){
//Success
            barcode = scanResult_1;
        }else{
            barcode = decodedData;
        }
        if(barcode.isEmpty()){
            barcode = "";
        }

        Integer i = 0;
        Long currentTimeLong = System.currentTimeMillis();
        String currentTime = String.valueOf(currentTimeLong);
        //lastSentTimestamp = System.currentTimeMillis();
        try {
            lastSentTimestamp = System.currentTimeMillis();
            barcodeString = barcode;
            final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
            if (poKuse == true){
                item_count = 1;
                item_count_string = "1";
                if (!barcode.matches("")) {

                    //Update barcode value to TextView
                    final String val = barcode;


                    cache = new Vector();
                    lastSentTimestamp = System.currentTimeMillis();
                    try {

                        new AsyncSQLQuery().execute(barcodeString, "1");




                    } catch (Exception ex) {
                        cache = new Vector();
                        ex.printStackTrace();
                    }
                }
            }else{
                //showAlertDialogButtonClicked(cameraView);
                if (!barcode.matches("")) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    final String close = getResources().getString(R.string.close);
                    final String amount = getResources().getString(R.string.amount_pcs);
                    final String amount_msg = getResources().getString(R.string.amount_message);
                    alert.setTitle(amount);
                    alert.setMessage(amount_msg);
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    input.setText("");
                    input.setHint("1");
                    alert.setView(input);
                    alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            String inputStringIntermediary = input.getText().toString();
                            if(inputStringIntermediary.matches("")){
                                inputStringIntermediary = "1";
                            }

                            item_count = Integer.parseInt(inputStringIntermediary);
                            item_count_string = inputStringIntermediary;

                            final String val = barcode;
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                v.vibrate(50);
                            }



                            cache = new Vector();
                            try {
                                final ConnectionClass connectionClass = new ConnectionClass();
                                barcodeString = val;
                                editor.putBoolean(BARCODE_IS_NEW, false);
                                editor.apply();
                                final String unknownBarcodeMsg = getResources().getString(R.string.unknown_barcode_msg);
                                try {

                                    new AsyncSQLQuery().execute(barcodeString, item_count_string);



                                }catch (Exception ex){
                                    editor.putBoolean(BARCODE_IS_NEW, false);
                                    editor.apply();
                                    ex.printStackTrace();
                                    z = ex.getMessage();
                                    System.out.println(rs);
                                    z = "the end";
                                    AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                                    alert.setTitle("Chyba");
                                    alert.setMessage(ex.toString());
                                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    });
                                    alert.show();
                                }
                            } catch (Exception ex) {
                                cache = new Vector();
                                ex.printStackTrace();
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


                }
            }
        } catch (Exception ex) {

            ex.printStackTrace();
        }







    }

    public class Doklad {

        public String hlavicka_id;
        public String polozka_id;
        public String polozka_name;
        public String nazev_polozky;
        public String mnozstvi_polozky;
        public String kod_zbozi;
        public String ean;
        public String barva;
        public String nazev_velikosti;
        public String nazev_delky;
        public String nazev_rozmeru;

        public Doklad(String hlavicka_id, String polozka_id, String polozka_nazev, String nazev_polozky, String mnozstvi_polozky, String kod_zbozi, String ean, String barva , String nazev_velikosti, String nazev_delky, String nazev_rozmeru){
            this.hlavicka_id = hlavicka_id;
            this.polozka_id = polozka_id;
            this.nazev_polozky = polozka_nazev ;
            this.nazev_polozky = nazev_polozky;
            this.nazev_velikosti = nazev_velikosti;
            this.mnozstvi_polozky = mnozstvi_polozky;
            this.ean = ean;
            this.kod_zbozi = kod_zbozi;
            this.barva = barva;
            this.nazev_delky = nazev_delky;
            this.nazev_rozmeru = nazev_rozmeru;
        }
        public String getBarva() {return barva; }
        public String getNazev_velikosti() {
            return nazev_velikosti;
        }
        public String getMnozstvi_polozky() {
            return mnozstvi_polozky;
        }
        public String getEan() {
            return ean;
        }
        public String getNazev_polozky(){return nazev_polozky;}
        public String getKod_zbozi(){return kod_zbozi;}
        public String getNazev_delky(){return nazev_delky;}
        public String getNazev_rozmeru(){return nazev_rozmeru;}
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
            TextView txtCena;
            ConstraintLayout txtPolozkaLegacyLayout;
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.doklad_polozka_item_legacy, parent, false);
                viewHolder.txtPolozka = convertView.findViewById(R.id.dokladPolozkaNazev);
                viewHolder.txtMnozstvi = convertView.findViewById(R.id.dokladPolozkaMnozstvi);
                viewHolder.txtEan = convertView.findViewById(R.id.dokladPolozkaEAN);
                viewHolder.txtBarva = convertView.findViewById(R.id.dokladPolozkaBarva);
                viewHolder.txtKodZbozi = convertView.findViewById(R.id.dokladPolozkaZboziID);
                viewHolder.txtVelikost = convertView.findViewById(R.id.dokladPolozkaVelikost);
                viewHolder.txtDelka = convertView.findViewById(R.id.dokladPolozkaDelka);
                viewHolder.txtRozmer = convertView.findViewById(R.id.dokladPolozkaRozmer);
                viewHolder.txtCena = convertView.findViewById(R.id.dokladPolozkaCena);
                viewHolder.txtPolozkaLegacyLayout = convertView.findViewById(R.id.doklad_polozka_legacy_layout);

                viewHolder.txtCena.setVisibility(View.GONE);

                result = convertView;
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }
            viewHolder.txtKodZbozi.setText("Kod zbozi: " + doklad.getKod_zbozi());
            // Lookup view for data population
            viewHolder.txtMnozstvi.setText("Mnozstvi: " + doklad.getMnozstvi_polozky());
            viewHolder.txtEan.setText("EAN: " + doklad.getEan());
            viewHolder.txtPolozka.setText("" + doklad.getNazev_polozky());
            viewHolder.txtBarva.setText(doklad.getBarva());
            viewHolder.txtVelikost.setText(doklad.getNazev_velikosti());
            viewHolder.txtDelka.setText(doklad.getNazev_delky());
            viewHolder.txtRozmer.setText(doklad.getNazev_rozmeru());
            viewHolder.txtCena.setText("");

            if(showListItemsDetail){
                viewHolder.txtDelka.setVisibility(View.VISIBLE);
                viewHolder.txtRozmer.setVisibility(View.VISIBLE);
                viewHolder.txtKodZbozi.setVisibility(View.VISIBLE);
                viewHolder.txtEan.setVisibility(View.VISIBLE);
            }else{
                viewHolder.txtDelka.setVisibility(View.GONE);
                viewHolder.txtRozmer.setVisibility(View.GONE);
                viewHolder.txtKodZbozi.setVisibility(View.GONE);
                viewHolder.txtEan.setVisibility(View.GONE);
            }

            if (position % 2 == 0) {
                viewHolder.txtPolozkaLegacyLayout.setBackgroundColor(Color.argb(200, 200, 200, 240));
            }else{
                viewHolder.txtPolozkaLegacyLayout.setBackgroundColor(Color.argb(200, 255, 255, 255));
            }

            // Populate the data into the template view using the data object
            // Return the completed view to render on screen
            return convertView;
        }
    }


    private class AsyncSQLQuery extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionClass = new ConnectionClass();
        }

        @Override
        protected String doInBackground(String... params) {
            String innerBarcodeString = params[0];
            String innerItemAmount = params[1];

            barcodeString = innerBarcodeString;
            editor.putBoolean(BARCODE_IS_NEW, false);
            editor.apply();
            try {
                Connection con = connectionClass.CONN();
                if (con == null){
                    String z = "chyba v propojeni";

                }else{
                    hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN','" + hlavicka_id +"','" + barcodeString+ "','" + innerItemAmount + "'";
                    Statement stmt = null;
                    PreparedStatement pstmt =
                            con.prepareStatement(query,
                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                    ResultSet.CONCUR_READ_ONLY);
                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    lastSentTimestamp = System.currentTimeMillis();
                    rs = pstmt.executeQuery();
                    lastSentTimestamp = System.currentTimeMillis();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    Integer columnCOunt = rsmd.getColumnCount();
                    String[] resultSet = new String[columnCOunt];
                    querydata = new ArrayList<String>();
                    String[] item = new String[11];
                    arrayOfUsers = new ArrayList<Doklad>();
                    arraycontains = false;
                    lastSentTimestamp = System.currentTimeMillis();
                    char tester = 'c';
                    total_item_count = 0;
                    totalItemsPrice = new Float("0.00");
                    totalItemsPriceBD = new BigDecimal("0.00");
                    String existujici = "EXISTUJICI";
                    for (int y=0;y<columnCOunt;y++){
                        if (rsmd.getColumnName(y+1).matches(existujici)) {
                            arraycontains = true;
                            break;
                        }
                    }
                    Integer indexCounter = 0;




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
                        //barcodeIsValid = rs.getInt("EXISTUJICI");
                        total_item_count += mnozstvi;
                        try{
                            pc = rs.getString("CENA");
                            stav = rs.getString("STAV");
                            DecimalFormat df = new DecimalFormat("#.##");
                            df.setRoundingMode(RoundingMode.CEILING);
                            String pcString = pc.split(" ")[1];
                            Float innerItemPrice = new Float(pcString);
                            innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                            totalItemsPrice = totalItemsPrice + innerItemPrice;

                            BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                            BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                            innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                            totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }



                        if(barcodeString.matches(ean_polozky)){
                            indexOfDisplayedItem = indexCounter;
                            priceOfDisplayedItem = pc;
                            stockOfDisplayedItem = stav;
                            publishProgress();


                        }

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
                        lastSentTimestamp = System.currentTimeMillis();
                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                        indexCounter++;
                    }

                    lastSentTimestamp = System.currentTimeMillis();
                    if(arraycontains){
                        try {

                            //MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.companion.R.raw.beep_01a);
                            //5
                            errorStopper = true;

                            if(barcodeString.matches(ean_polozky)){
                                eanTextView.setText("");
                                nameTextView.setText("Neznámá položka");
                                thirdTextView.setText("");
                                fourthTextView.setText("");
                                fifthTextView.setText("");

                            }


                            String toSpeak = "Neznámá položka";
                            Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                            //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                            MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                            mp.start();
                            indexOfDisplayedItem = -1;

                            AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this, R.style.AlertDialogStyle);
                            alert.setTitle("Chyba");
                            alert.setMessage("Neznámý čárový kód");
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    errorStopper = false;
                                }
                            });
                            alert.setCancelable(false);
                            alert.show();




                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    return null;


                }

            }catch (Exception ex){
                editor.putBoolean(BARCODE_IS_NEW, false);
                editor.apply();
                ex.printStackTrace();
                z = ex.getMessage();
                System.out.println(rs);
                z = "the end";
                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                alert.setTitle("Chyba");
                alert.setMessage(ex.toString());
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                //alert.show();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            eanTextView.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            thirdTextView.setVisibility(View.VISIBLE);
            mnozstviTextView.setVisibility(View.VISIBLE);
            warehousepriceTextview.setVisibility(View.VISIBLE);
            warehouseAmountTextView.setVisibility(View.VISIBLE);
            eanTextView.setText("EAN: " + ean_polozky);
            nameTextView.setText(nazev);
            thirdTextView.setText("Kód : " + kod);
            mnozstviTextView.setText("Množství: " + mnozstvi.toString());
            fourthTextView.setText(velikost);
            fifthTextView.setText(barva_polozky);
            delkaTextView.setText(delka);
            rozmerTextView.setText(rozmer);
            try{
                warehousepriceTextview.setText(priceOfDisplayedItem);
                warehouseAmountTextView.setText(stockOfDisplayedItem);
            }catch(Exception ex){
                //ex.printStackTrace();
                String exString = ex.toString();
            }

            if(velikost.matches("")){
                fourthTextView.setVisibility(View.GONE);
            }else{
                fourthTextView.setVisibility(View.VISIBLE);
            }
            if(barva_polozky.matches("")){
                fifthTextView.setVisibility(View.GONE);
            }else{
                fifthTextView.setVisibility(View.VISIBLE);
            }
            if(delka.matches("")){
                delkaTextView.setVisibility(View.GONE);
            }else{
                delkaTextView.setVisibility(View.VISIBLE);
            }
            if(rozmer.matches("")){
                rozmerTextView.setVisibility(View.GONE);
            }else{
                rozmerTextView.setVisibility(View.VISIBLE);
            }
        }


        @Override
        protected void onPostExecute(String result) {

            adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
            //Attach the adapter to a ListView
            registerForContextMenu(listView);
            adapter.notifyDataSetChanged();
            dividerTV.setText(total_item_count.toString() + " položek");
            totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
            listView.setAdapter(adapter);

            if(arraycontains){
                try {

                    //MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.companion.R.raw.beep_01a);
                    //5
                    errorStopper = true;

                    if(barcodeString.matches(ean_polozky)){
                        eanTextView.setText("");
                        nameTextView.setText("Neznámá položka");
                        thirdTextView.setText("");
                        fourthTextView.setText("");
                        fifthTextView.setText("");

                    }


                    String toSpeak = "Neznámá položka";
                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                    //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                    mp.start();
                    indexOfDisplayedItem = -1;

                    AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this, R.style.AlertDialogStyle);
                    alert.setTitle("Chyba");
                    alert.setMessage("Neznámý čárový kód");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            errorStopper = false;
                        }
                    });
                    alert.setCancelable(false);
                    alert.show();




                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
            // do something with result
        }
    }


    private class AsyncGroupSQLQuery extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionClass = new ConnectionClass();
        }

        @Override
        protected String doInBackground(String... params) {
            String innerBarcodeString = params[0];


            barcodeString = innerBarcodeString;
            editor.putBoolean(BARCODE_IS_NEW, false);
            editor.apply();
            try {
                Connection con = connectionClass.CONN();
                if (con == null){
                    String z = "chyba v propojeni";

                }else{
                    hlavicka_id = preferences.getInt(CURRENT_HLAVICKA_ID, 0);
                    query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','EAN_GROUP','" + hlavicka_id +"','','1','0','" + barcodeString + "'";
                    Statement stmt = null;
                    PreparedStatement pstmt =
                            con.prepareStatement(query,
                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                    ResultSet.CONCUR_READ_ONLY);
                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    lastSentTimestamp = System.currentTimeMillis();
                    rs = pstmt.executeQuery();
                    lastSentTimestamp = System.currentTimeMillis();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    Integer columnCOunt = rsmd.getColumnCount();
                    String[] resultSet = new String[columnCOunt];
                    querydata = new ArrayList<String>();
                    String[] item = new String[11];
                    arrayOfUsers = new ArrayList<Doklad>();
                    arraycontains = false;
                    lastSentTimestamp = System.currentTimeMillis();
                    char tester = 'c';
                    total_item_count = 0;
                    totalItemsPrice = new Float("0.00");
                    totalItemsPriceBD = new BigDecimal("0.00");
                    String existujici = "EXISTUJICI";
                    for (int y=0;y<columnCOunt;y++){
                        if (rsmd.getColumnName(y+1).matches(existujici)) {
                            arraycontains = true;
                            break;
                        }
                    }
                    Integer indexCounter = 0;




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
                        //barcodeIsValid = rs.getInt("EXISTUJICI");
                        total_item_count += mnozstvi;
                        try{
                            pc = rs.getString("CENA");
                            stav = rs.getString("STAV");
                            DecimalFormat df = new DecimalFormat("#.##");
                            df.setRoundingMode(RoundingMode.CEILING);
                            String pcString = pc.split(" ")[1];
                            Float innerItemPrice = new Float(pcString);
                            innerItemPrice = innerItemPrice * Float.parseFloat(mnozstvi.toString());
                            totalItemsPrice = totalItemsPrice + innerItemPrice;

                            BigDecimal innerItemPriceBD = new BigDecimal(pcString);
                            BigDecimal innerItemAmountBD = new BigDecimal(mnozstvi.toString());
                            innerItemPriceBD = innerItemPriceBD.multiply(innerItemAmountBD);
                            totalItemsPriceBD = totalItemsPriceBD.add(innerItemPriceBD);
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }



                        if(barcodeString.matches(ean_polozky)){
                            indexOfDisplayedItem = indexCounter;
                            priceOfDisplayedItem = pc;
                            stockOfDisplayedItem = stav;
                            publishProgress();


                        }

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
                        lastSentTimestamp = System.currentTimeMillis();
                        arrayOfUsers.add(new Doklad(item[0],item[1],item[2],item[3],item[4], item[5], item[6], item[7],item[8], item[9], item[10]));
                        indexCounter++;
                    }

                    lastSentTimestamp = System.currentTimeMillis();


                    return null;


                }

            }catch (Exception ex){
                editor.putBoolean(BARCODE_IS_NEW, false);
                editor.apply();
                ex.printStackTrace();
                z = ex.getMessage();
                System.out.println(rs);
                z = "the end";
                AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this);
                alert.setTitle("Chyba");
                alert.setMessage(ex.toString());
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                //alert.show();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            eanTextView.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            thirdTextView.setVisibility(View.VISIBLE);
            mnozstviTextView.setVisibility(View.VISIBLE);
            warehousepriceTextview.setVisibility(View.VISIBLE);
            warehouseAmountTextView.setVisibility(View.VISIBLE);
            eanTextView.setText("EAN: " + ean_polozky);
            nameTextView.setText(nazev);
            thirdTextView.setText("Kód : " + kod);
            mnozstviTextView.setText("Množství: " + mnozstvi.toString());
            fourthTextView.setText(velikost);
            fifthTextView.setText(barva_polozky);
            delkaTextView.setText(delka);
            rozmerTextView.setText(rozmer);
            try{
                warehousepriceTextview.setText(priceOfDisplayedItem);
                warehouseAmountTextView.setText(stockOfDisplayedItem);
            }catch(Exception ex){
                //ex.printStackTrace();
                String exString = ex.toString();
            }

            if(velikost.matches("")){
                fourthTextView.setVisibility(View.GONE);
            }else{
                fourthTextView.setVisibility(View.VISIBLE);
            }
            if(barva_polozky.matches("")){
                fifthTextView.setVisibility(View.GONE);
            }else{
                fifthTextView.setVisibility(View.VISIBLE);
            }
            if(delka.matches("")){
                delkaTextView.setVisibility(View.GONE);
            }else{
                delkaTextView.setVisibility(View.VISIBLE);
            }
            if(rozmer.matches("")){
                rozmerTextView.setVisibility(View.GONE);
            }else{
                rozmerTextView.setVisibility(View.VISIBLE);
            }
        }


        @Override
        protected void onPostExecute(String result) {

            adapter = new DokladAdapter(arrayOfUsers, PDTCreationActivity.this);
            //Attach the adapter to a ListView
            registerForContextMenu(listView);
            adapter.notifyDataSetChanged();
            dividerTV.setText(total_item_count.toString() + " položek");
            totalPriceTextView.setText(totalItemsPriceBD.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " Kč");
            listView.setAdapter(adapter);
            scannedBarcodes.clear();
            barcodesToSendTextView.setText("");

            if(arraycontains){
                try {

                    //MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.companion.R.raw.beep_01a);
                    //5
                    errorStopper = true;

                    if(barcodeString.matches(ean_polozky)){
                        eanTextView.setText("");
                        nameTextView.setText("Neznámá položka");
                        thirdTextView.setText("");
                        fourthTextView.setText("");
                        fifthTextView.setText("");

                    }


                    String toSpeak = "Neznámá položka";
                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                    //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    MediaPlayer mp = MediaPlayer.create(PDTCreationActivity.this, com.winshop.sqlterminal.R.raw.beep_01a);
                    mp.start();
                    indexOfDisplayedItem = -1;

                    AlertDialog.Builder alert = new AlertDialog.Builder(PDTCreationActivity.this, R.style.AlertDialogStyle);
                    alert.setTitle("Chyba");
                    alert.setMessage("Neznámý čárový kód");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            errorStopper = false;
                        }
                    });
                    alert.setCancelable(false);
                    alert.show();




                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
            // do something with result
        }
    }





}