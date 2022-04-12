package com.winshop.sqlterminal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

public class PDTSelectionActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String BARCODE = "barcode";
    public static final String GUID = "guid";
    public static final String ID = "id";
    public static final String BARCODE_IS_NEW = "barcode_is_new";
    public static final String BAREVNE_VELIKOSTNI = "barevne_velikostni";
    public static final String SKLAD = "sklad";
    public static final String DOKLAD_STAV = "doklad_stav";
    public static final String DOKLAD_HLAVICKA_ID = "doklad_hlavicka_id";

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    ConnectionClass connectionClass;
    String guid, androidID;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ResultSet rs;
    String[] resultSet;
    String query,z;
    ArrayList querydata;
    String nazev_reportu;
    //Button newFormButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_p_d_t_selection);
        ScrollView horLayout = findViewById(R.id.horLayout_pdt_selection);
        final Button newpdtButton = (Button) findViewById(R.id.newpdtButton);
        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = preferences.edit();
        final ConnectionClass connectionClass = new ConnectionClass();
        guid = preferences.getString(GUID, "");
        androidID = preferences.getString(ID,"");
        newpdtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newFormIntent = new Intent(PDTSelectionActivity.this, PDTCreationActivity.class);
                editor.putString(DOKLAD_STAV, "new");
                editor.apply();
                startActivity(newFormIntent);

            }
        });


        horLayout.setVisibility(View.VISIBLE);

        try {
            Connection con = connectionClass.CONN();
            if (con == null){
                String z = "chyba v propojeni";
            }else{
                query = "exec MOBILNI_PDT '" + androidID + "','" + guid + "','Nový_doklad','" + "'";
                Statement cs = null;
                cs = con.createStatement();
                rs = cs.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                Integer columnCOunt = rsmd.getColumnCount();
                Integer rowCount = 0;
                querydata = new ArrayList<String>();



                String[] headers = new String[columnCOunt];
                String[] headersType = new String[columnCOunt];
                for (int h = 1; h <= columnCOunt; h++) {
                    headers[h - 1] = rsmd.getColumnName(h);
                    headersType[h - 1] = rsmd.getColumnTypeName(h);
                }
                TableLayout myTable = findViewById(R.id.producttablelayout_pdt_selection);
                myTable.setVisibility(View.VISIBLE);
                myTable.removeAllViews();

                myTable.setStretchAllColumns(true);
                myTable.bringToFront();
                int counter = 1;
                TableRow headersRow = new TableRow(getBaseContext());
                for (int i = 0; i < columnCOunt; i++) {
                    TextView t1 = new TextView(getBaseContext());
                    if (headersType[i] == "decimal" | headersType[i] == "int")
                        t1.setGravity(Gravity.RIGHT);
                    t1.setTextColor(Color.BLACK);
                    t1.setTypeface(null, Typeface.BOLD);
                    t1.setTextSize(16);
                    t1.setEllipsize(TextUtils.TruncateAt.END);
                    t1.setHorizontallyScrolling(false);
                    t1.setPadding(35, 25, 35, 25);
                    t1.setText(headers[i]);
                    t1.setId(10000 + i);
                    t1.setBackgroundColor(Color.argb(80, 180, 180, 255));
                    headersRow.addView(t1);
                }
                myTable.addView(headersRow);
                int count = 0;
                while (rs.next()){
                    String[] row = new String[columnCOunt];
                    for (int i = 0; i < columnCOunt;i++  ){
                        row[i] = rs.getString(i+1);
                    }
                    querydata.add(row);
                    count++;
                    rowCount = rs.getRow();


                    String[] record = new String[columnCOunt];
                    Number[] cisla = new Number[columnCOunt];
                    for (int i = 0; i < columnCOunt; i++) {
                        if (headersType[i] == "decimal") {
                            BigDecimal bd = rs.getBigDecimal(i + 1);
                            //MathContext m = new MathContext(3);
                            cisla[i] = bd;
                        } else {
                            if (headersType[i] == "int")
                                cisla[i] = rs.getInt(i + 1);
                            else
                                record[i] = rs.getString(i + 1);
                        }

                    }
                    //cache.addElement(record);

                    final TableRow newRow = new TableRow(getBaseContext());
                    newRow.setClickable(true);

                    for (int i = 0; i < columnCOunt; i++) {
                        TextView t1 = new TextView(getBaseContext());
                        t1.setPadding(35, 15, 35, 15);
                        t1.setTextColor(Color.BLACK);
                        if (headersType[i] == "decimal") {
                            t1.setGravity(Gravity.RIGHT);
                            t1.setText(cisla[i].toString());
                        } else {
                            if (headersType[i] == "int") {
                                t1.setGravity(Gravity.RIGHT);
                                t1.setText(cisla[i].toString());
                            } else {
                                t1.setText(record[i]);
                            }
                        }

                        newRow.addView(t1);
                    }
                    counter++;

                    if (counter % 2 == 0) {
                        newRow.setBackgroundColor(Color.argb(200, 240, 240, 240));
                    }
                    newRow.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent unfinishedFormIntent = new Intent(PDTSelectionActivity.this, PDTCreationActivity.class);
                            TableRow t = (TableRow) v;
                            TextView hlavickaTV = (TextView)  ((TableRow) v).getChildAt(1);
                            String hlavicka_id = hlavickaTV.getText().toString();
                            editor.putString(DOKLAD_STAV, "unfinished");
                            editor.putString(DOKLAD_HLAVICKA_ID, hlavicka_id);
                            editor.apply();
                            startActivity(unfinishedFormIntent);
                        }
                    });


                    myTable.addView(newRow);

                }


                Integer length = querydata.size();


                for(int i = 0; i < 2; i++){

                    TableRow newRow = new TableRow(getBaseContext());

                    for (int j = 0; j < columnCOunt; j++) {
                        TextView t1 = new TextView(getBaseContext());
                        t1.setPadding(35, 25, 35, 25);
                        t1.setTextColor(Color.BLACK);
                        if (headersType[j] == "decimal") {
                            t1.setGravity(Gravity.RIGHT);
                            t1.setText("");
                        } else {
                            if (headersType[j] == "int") {
                                t1.setGravity(Gravity.RIGHT);
                                t1.setText("");
                            } else {
                                t1.setText("");
                            }
                        }

                        newRow.addView(t1);
                    }
                    counter++;
                    myTable.addView(newRow);
                }



            }
        }catch (Exception e){
            e.printStackTrace();


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
                Intent leIntent = new Intent(PDTSelectionActivity.this, AppActivity.class);
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


}
