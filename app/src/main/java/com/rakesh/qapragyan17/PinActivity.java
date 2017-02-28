package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PinActivity extends AppCompatActivity {

    AlertDialog alertDialog;
    ProgressDialog progressDialog;
    EditText pinText;
    String userToken, pin;
    int eventId, userId;
    Button button;
    Retrofit retrofit;
    Observable<Response> pinObservable;
    public static String baseUrl = "https://api.pragyan.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        pinText = (EditText) findViewById(R.id.pin);
        button = (Button) findViewById(R.id.button2);

        Intent intent = getIntent();
        eventId = intent.getIntExtra("eventId", 0);
        userId = intent.getIntExtra("user_id", 0);
        userToken = intent.getStringExtra("user_token");

        button.setOnClickListener(view -> {
            pin = pinText.getText().toString();
            if (!pin.equals("")) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating Pin..");
                progressDialog.show();

                retrofit = new Retrofit.Builder()
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(baseUrl)
                        .build();

                NetworkService networkService = retrofit.create(NetworkService.class);

                pinObservable = networkService.authPin("QA", pin);

                pinObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            progressDialog.dismiss();
                            if (response.getStatusCode() == 200) {
                                onPinAuth();
                            } else {
                                displayDialog("Error : " + response.getStatusCode()
                                        + " Try Again");
                            }
                        }, throwable -> {
                            progressDialog.dismiss();
                            displayDialog("Network Error. Please Try Again");
                            Log.e("debug", throwable.getMessage());
                        });
            } else {
                displayDialog("Enter the pin!");
            }

        });
    }

    private void onPinAuth() {
        Intent intent;
        if (eventId == 0) {
            intent = new Intent(this, GeneralActivity.class);
        } else {
            intent = new Intent(this, WorkshopsActivity.class);
        }
        intent.putExtra("eventId", eventId);
        intent.putExtra("user_token", userToken);
        intent.putExtra("user_id", userId);

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
