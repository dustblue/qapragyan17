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

    AlertDialog alertDialog;
    ProgressDialog progressDialog;
    Spinner spinner;
    EditText username, password, editText;
    Button button;
    Retrofit retrofit;
    Observable<Response> loginObservable;
    String un, pw;
    public static String baseUrl;
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
        editText = (EditText) findViewById(R.id.url);

        spinner = (Spinner) findViewById(R.id.spinner);
        button = (Button) findViewById(R.id.button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, events);
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
                    baseUrl = editText.getText().toString();

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
                                    onLogin(response.getMessage());
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

    private void onLogin(String token) {
        Intent intent;
        if (spinner.getSelectedItemPosition() == 0) {
            intent = new Intent(this, GeneralActivity.class);
        } else {
            intent = new Intent(this, WorkshopsActivity.class);
        }
        intent.putExtra("eventId", spinner.getSelectedItemPosition());
        intent.putExtra("adminId", username.getText().toString());
        intent.putExtra("token", token);
        startActivity(intent);
        finish();
    }

    private void displayDialog(String text) {
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
