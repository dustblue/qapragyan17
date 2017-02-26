package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    EditText username, password;
    Button button;
    Retrofit retrofit;
    Observable<Response> loginObservable;
    public static String baseUrl = "http://80bd0b47.ngrok.io";
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
        button = (Button) findViewById(R.id.button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, events);
        spinner.setAdapter(adapter);

        button.setOnClickListener(view -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in ");
            progressDialog.show();
            if (username.getText() != null) {
                if (password.getText() != null) {

                    retrofit = new Retrofit.Builder()
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(baseUrl)
                            .build();

                    NetworkService networkService = retrofit.create(NetworkService.class);

                    loginObservable = networkService.authenticate(username.getText().toString(),
                            password.getText().toString());

                    loginObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                if (response.getStatusCode() == 200) {
                                    progressDialog.dismiss();
                                    onLogin();
                                }
                            });
                } else {
                    displayDialog("Enter the password!");
                }
            } else {
                displayDialog("Enter the username!");
            }
        });
    }

    private void onLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("eventId", spinner.getSelectedItemId());
        intent.putExtra("adminId", username.getText().toString());
        startActivity(intent);
        finish();
    }

    private void displayDialog(String text) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
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
