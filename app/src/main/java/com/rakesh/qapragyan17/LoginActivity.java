package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private Spinner spinner;
    private EditText username, password;
    Retrofit retrofit;
    Observable<Response> loginObservable;
    private String un, pw;
    private String baseUrl = "https://api.pragyan.org/";
    public static String[] events = new String[]{"General Response", "Construction Mgmt", "Cross-Platform Dev",
            "Process Design", "Swarm Robotics", "Quadcopter", "Autotrix", "Quadbot",
            "Network Designing", "Nikon Photography", "Texas Instruments", "SEBI", "NI IoT",
            "PTC Creo", "Data Analytics", "IBM Cloud"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        spinner = (Spinner) findViewById(R.id.spinner);
        Button button = (Button) findViewById(R.id.button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_dropdown_item, events);
        spinner.setAdapter(adapter);

        button.setOnClickListener(view -> {
            un = username.getText().toString();
            pw = password.getText().toString();
            if (!un.equals("")) {
                if (!pw.equals("")) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Logging in..");
                    progressDialog.show();

                    retrofit = new Retrofit.Builder()
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(baseUrl)
                            .build();

                    NetworkService networkService = retrofit.create(NetworkService.class);

                    loginObservable = networkService.authenticate(un, pw);

                    loginObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                progressDialog.dismiss();
                                if (response.getStatusCode() == 200) {
                                    onLogin(response.getMessage(), response.getUserId());
                                } else {
                                    displayDialog("Error : " + response.getStatusCode()
                                            + " Please Try Again");
                                }
                            }, throwable -> {
                                progressDialog.dismiss();
                                displayDialog("Network Error. Please Try Again");
                                Log.e("debug", throwable.getMessage());
                            });
                } else {
                    displayDialog("Enter the password!");
                }
            } else {
                displayDialog("Enter the username!");
            }
        });
    }

    private void onLogin(String token, int id) {
        Intent intent = new Intent(this, PinActivity.class);
        intent.putExtra("eventId", spinner.getSelectedItemPosition());
        intent.putExtra("user_id", id);
        intent.putExtra("user_token", token);
        startActivity(intent);
        finish();
    }

    private void displayDialog(String text) {
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Feedback Pragyan '17");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        alertDialog.show();
    }

}
