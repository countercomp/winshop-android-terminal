package com.winshop.sqlterminal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;

//import android.support.v4.os.ConfigurationCompat;
import androidx.core.os.ConfigurationCompat;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Locale;

import static com.winshop.sqlterminal.MainActivity.hexStringToByteArray;

public class RegisterActivity extends AppCompatActivity {
    String z, column_names, error_code, toast_message;
    ResultSet rs;
    ConnectionClass connectionClass = new ConnectionClass();
    Integer numberOfColumns;
    Boolean isSuccess = false;

    String PINPass;
    String DBusername;
    boolean isFilled ;
    Boolean regSent = false;
String androidID;




    private EditText regUsernameEditText;
    private EditText regPwEditText;
    private EditText regPhoneEditText;
    private EditText regEmailEditText;
    private Button regSavebutton;


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SERVER = "server";
    public static final String DATABASE = "database";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String USERPASSWORD = "userpassword";
    public static final String PHONE = "phone" ;
    public static final String EMAIL = "email" ;
    public static final String SKLAD = "sklad";
    public static final String LOGO = "logo";
    public static final String LOGOSTRING = "logostring";
    public static final String LANGUAGE = "language";



    private String server;
    private String database;
    private String username;
    private String password;
    private String phone;
    private String email;
    private Integer sklad;
     String userpassword;
     Integer[] skladyID;
     String[] sklady;
     String[] sklady_string;
     Integer rowCount, counter;
     Integer position;
    String registration_title;
    String registration_msg;
    String enter_email;
    String enter_all_fields;
    String device_exists;
    String registration_successful;
    String ok;
    String error;
    byte[] logoData;
    Bitmap logo;
    ImageView logoView;
    String logoString, lString, languageString;
    SharedPreferences preferences;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        /* Get ANDROID_ID identifier, used to authenticate the device on launch */
         androidID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
         Intent intent = getIntent();

        regUsernameEditText = findViewById(R.id.register_username_editText);
        regPwEditText = findViewById(R.id.register_pw_editText);
        regPhoneEditText = findViewById(R.id.register_phone_editText);
        regEmailEditText = findViewById(R.id.register_email_editText);
        regSavebutton = findViewById(R.id.register_save_button);
        isFilled = false;
        logoString = intent.getStringExtra("logostring");
        if (logoString == null || logoString == ""){
            logoView = findViewById(R.id.registerLogoView);
            logoView.setImageResource(R.color.winshop_dark_blue);
        }else{
            logoData = hexStringToByteArray(logoString);
            logo = BitmapFactory.decodeByteArray(logoData, 0,
                    logoData.length);
            logoView = findViewById(R.id.registerLogoView);
            if(logo == null){
                logoView.setImageResource(R.color.winshop_dark_blue);
            }else{
                logoView.setImageBitmap(logo);
            }
        }

        preferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        languageString = preferences.getString(LANGUAGE, "en");
        Locale current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        String currentLocale = new String();
        currentLocale = current.getLanguage().toString().toLowerCase();

        Locale locale = new Locale(languageString);
        Configuration config = getBaseContext().getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


        regSavebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {saveData();


            }
        });

        updateViews();

          registration_title = getResources().getString(R.string.registration_title);
          registration_msg = getResources().getString(R.string.register_msg);
          enter_email = getResources().getString(R.string.enter_email);
          enter_all_fields = getResources().getString(R.string.fill_all_fields);
          device_exists = getResources().getString(R.string.device_exists);
          registration_successful = getResources().getString(R.string.registration_successful);
          ok = getResources().getString(R.string.ok);
          error = getResources().getString(R.string.error);








    }

    boolean isEmail(EditText text) {
        CharSequence email_f = text.getText().toString();
        return (!TextUtils.isEmpty(email_f) && Patterns.EMAIL_ADDRESS.matcher(email_f).matches());
    }


    private void ConfigureRegisterButton(){
        Button button = findViewById(R.id.register_save_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {saveData();





            }
        });


    }


    public void saveData(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        logoString = sharedPreferences.getString(LOGO, "");
        lString = sharedPreferences.getString(LOGOSTRING, "");
        logoData = hexStringToByteArray(lString);
        logo = BitmapFactory.decodeByteArray(logoData, 0,
                logoData.length);
        logoView = findViewById(R.id.registerLogoView);
        logoView.setImageBitmap(logo);


        String text = regUsernameEditText.getText().toString();
        String passw = regPwEditText.getText().toString();
        String phoneN = regPhoneEditText.getText().toString();
        String e_mail = regEmailEditText.getText().toString();



     /*   if(text.matches("")) {
            isFilled = false;
            z = "no user";
            Toast.makeText(RegisterActivity.this, "Vyplňte prosím všechna pole", Toast.LENGTH_SHORT);
            return;
            }
        if(passw.matches("")) {
            isFilled = false;
            z = "no pw";
            Toast.makeText(RegisterActivity.this, "Vyplňte prosím všechna pole", Toast.LENGTH_SHORT);
            return;
                }
        if(phoneN.matches("")) {
            isFilled = false;
            z = "no phone";
            Toast.makeText(RegisterActivity.this, "Vyplňte prosím všechna pole", Toast.LENGTH_SHORT);
            return;
                 }
        if(e_mail.matches("")) {
            isFilled = false;
            z = "no email";
            Toast.makeText(RegisterActivity.this, "Vyplňte prosím všechna pole", Toast.LENGTH_SHORT);
            return;
                    }*/
        if(text.matches("")|| passw.matches("")|| phoneN.matches("")|| e_mail.matches("")) {
            isFilled = false;
            z = "got stuff";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(registration_title);
            alert.setMessage(registration_msg);

            alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    return;                                    }
            });
            alert.show();
            return;
        }else if(isEmail(regEmailEditText) == false){
            isFilled = false;
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(registration_title);
            alert.setMessage(enter_email);

            alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    return;                                    }
            });
            alert.show();            return;
        }else {isFilled = true;}

    //    if(text != null && passw !=  null && phoneN != null && e_mail != null ){isFilled = true;}else{isFilled = false;}



        if (isFilled == false){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(registration_title);
            alert.setMessage(enter_all_fields);
            alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    return;                                    }
            });
            alert.show();

        } else{

             sklad = 0;

            editor.putInt(SKLAD, sklad);
            editor.putString(USERNAME,regUsernameEditText.getText().toString());
            editor.putString(USERPASSWORD,regPwEditText.getText().toString());
            editor.putString(PHONE, regPhoneEditText.getText().toString());
            editor.putString(EMAIL, regEmailEditText.getText().toString());
            editor.apply();

            server = sharedPreferences.getString(SERVER, "");
            database = sharedPreferences.getString(DATABASE, "");
            username = sharedPreferences.getString(USERNAME, "");
            userpassword = sharedPreferences.getString(USERPASSWORD, "");
            password = sharedPreferences.getString(PASSWORD, "");
            phone = sharedPreferences.getString(PHONE, "");
            email = sharedPreferences.getString(EMAIL, "");
            sklad = sharedPreferences.getInt(SKLAD, 0);


            try {
                Connection con = connectionClass.CONN();

                if (con == null){
                    z = "zero connection";
                }else{


                    PreparedStatement cs = null;
                    cs = con.prepareStatement("exec MOBILNI_LOGIN 3,'" + androidID +"' , '"+ userpassword +"' , '"+ username +"' , '"+ "" +"' , '"+ phone +"' , '"+ email +"' , '" + sklad + "','','" + languageString + "'");
                    rs = cs.executeQuery();
                    ResultSetMetaData rsmd = rs.getMetaData();


                    while (rs.next()){

                        switch (rs.getInt(1)){

                            case 0:
                                Toast.makeText(this, device_exists, Toast.LENGTH_SHORT);

                            case 1:
                              //  Toast.makeText(this, "Registrace proběhla v pořádku, vyčkejte prosím na potvrzující e-mail o registraci. Potvrzení proběhne v následujících 24 hodinách", Toast.LENGTH_LONG);
                                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                                alert.setTitle(registration_title);
                                alert.setMessage(registration_successful);

                                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);

                                        intent.putExtra("regSent", regSent);
                                        startActivity(intent);
                                        return;                                    }
                                });
                                alert.show();

// Set an EditText view to get user input
                                regSent = true;

                        }


                    }




                }
            }catch (Exception ex)
            {
                isSuccess = false;
                z = "Exceptions in regCon";
                AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);
                z = ex.toString();
                alert.setTitle(error);
                alert.setMessage(ex.toString());

                alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

            }









        }






    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        server = sharedPreferences.getString(SERVER, "");
        database = sharedPreferences.getString(DATABASE, "");
        username = sharedPreferences.getString(USERNAME, "");
        userpassword = sharedPreferences.getString(USERPASSWORD, "");
        password = sharedPreferences.getString(PASSWORD, "");
        phone = sharedPreferences.getString(PHONE, "");
        email = sharedPreferences.getString(EMAIL, "");


    }

    public void updateViews(){

        regUsernameEditText.setText(username);
        regPwEditText.setText(userpassword);
        regPhoneEditText.setText(phone);
        regEmailEditText.setText(email);
    }

}


