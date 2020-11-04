package com.example.twitontoday;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;


public class RegisterActivity extends AppCompatActivity {


    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputName;
    private EditText inputSurname;
    private EditText inputPosition;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputConfirmPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLogin);
        inputName = (EditText) findViewById(R.id.inputName);
        inputSurname = (EditText) findViewById(R.id.inputSurname);
        inputPosition = (EditText) findViewById(R.id.inputPosition);
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputPassword = (EditText) findViewById(R.id.inputPassword);
        inputConfirmPassword = (EditText) findViewById(R.id.inputConfirmPassword);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());

        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String surname = inputSurname.getText().toString().trim();
                String position = inputPosition.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String confirmPassword = inputConfirmPassword.getText().toString().trim();

                if (password.equals(confirmPassword)) {
                    if (!name.isEmpty() && !surname.isEmpty() && !position.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
                        registerUser(name, surname, position, email, password);
                    } else {
                        Toast.makeText(getApplicationContext(), "Заполните обязательные поля!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают!", Toast.LENGTH_LONG).show();
                }
            }
        });


        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void registerUser(final String name, final String surname,
                              final String position, final String email, final String password) {
        String tag_string_req = "req_register";
        pDialog.setMessage("Регистрирую...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "REGISTER RESPONCE: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String surname = user.getString("surname");
                        String position = user.getString("position");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");

                        db.addUser(name, surname, position, email, uid, created_at);
                        Toast.makeText(getApplicationContext(), "User registered", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: "+error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
    }){
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("surname", surname);
                params.put("position", position);
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq,tag_string_req);
    }
    private void showDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(){
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}