package com.winshop.sqlterminal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.winshop.sqlterminal.MainActivity;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionClass extends MainActivity {




    String server;
    String database;

    String username = "Mobil_app";
    String password = "m0b-rePorty&stat";


    String userpassword;


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SERVER = "server";
    public static final String DATABASE = "database";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    SharedPreferences prefs;



    Context applicationContext = MainActivity.getContextOfApplication();



    String z="";


    @SuppressLint("NewApi")
    public Connection CONN() {
        prefs = applicationContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);


        server = prefs.getString(SERVER, "");
        database = prefs.getString(DATABASE, "");

        server = server.replace("\\","&");

        String[] serverArray = server.split("&&", 2);






        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL;
        Integer columnCount = 0;
        Integer rowNumber = 0;
         z = "start" ;
        DriverManager.setLoginTimeout(60);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            z = "getting connection";

                if ( serverArray.length == 1 ){
                    conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + server + "/" + database+ ";user=" + username + ";password=" + password + ";");

                }else{
                    conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + serverArray[0] + "/" + database+ ";instance=" + serverArray[1]  + ";user=" + username + ";password=" + password + ";");

                }

            z = "got connection";



        } catch (Exception e) {
            e.printStackTrace();
            z = e.getMessage();
            if (z.contains("Network error IOException: failed to connect to")||(z.contains("connect failed: ECONNREFUSED (Connection refused)"))){



            }else{


            }
            System.out.println(resultSet);
            z = "the end";
        }







        return conn;

    }











    /*
    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;


        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            ConnURL = "jdbc:jtds:sqlserver://" + server + "/" + database+ ";user=" + username + ";password=" + password + ";";

            conn = DriverManager.getConnection(ConnURL);

        }catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());
        }

        return conn;
    } */



    /* z = "making query";
            String query = "select *  from VIEW_ZAMESTNANCI";

            Statement statement = conn.createStatement();
            z = "making statement";



            ResultSet result = statement.executeQuery(query);
            z = "executed statement";
            ResultSetMetaData rsmeta = result.getMetaData();
            z = "got metadata";
            columnCount = rsmeta.getColumnCount();




            while (result.next()) {

                rowNumber = result.getRow();

            }



            z =" gor column count and row number=0";
            result = statement.executeQuery(query);
            z = "executed statement";
            rsmeta = result.getMetaData();
            z = "got metadata";
            columnCount = rsmeta.getColumnCount() ;






            String[][] values = new String[rowNumber][columnCount];
            String[] stringRow = new String[columnCount];





                z = "inside if";
                int rownumber = 0;
                int a = 0;

                while (result.next()) {

                    z = " in while";

                    for (int b = 0; b < columnCount; b++) {
                            values[a][b] = result.getString(b+1);
                            z = "inside for";

                        }

                    a++;




                }



            stringRow = null;
            Intent intent = new Intent();
            intent.putExtra("values", values);
            intent.putExtra("columnCount", columnCount);
            intent.putExtra("rowNumber", rowNumber);



                       /* while (rs.next()) {
                            int i = 1;
                            while(i <= numberOfColumns) {
                                arrayList.add(rs.getString(i++));
                            }

                        }

    z = "jsou data";

                System.out.println(arrayList); */


}
