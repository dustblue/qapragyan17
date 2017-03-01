package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PinActivity extends AppCompatActivity {

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private EditText pinText;
    private String userToken, pin;
    private int eventId, userId;
    Retrofit retrofit;
    Observable<Response> pinObservable;
    private String baseUrl = "https://api.pragyan.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        pinText = (EditText) findViewById(R.id.pin);
        Button button = (Button) findViewById(R.id.button2);

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
                                onPinAuth(pin);
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

    private void onPinAuth(String pin) {
        Intent intent;
        if (eventId == 0) {
            intent = new Intent(this, GeneralActivity.class);
        } else {
            intent = new Intent(this, WorkshopsActivity.class);
        }
        intent.putExtra("eventId", eventId);
        intent.putExtra("user_token", userToken);
        intent.putExtra("user_id", userId);
        intent.putExtra("team_pin", Integer.parseInt(pin));

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

    private void displayDialog(String text, String reply) {
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Feedback Pragyan '17");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, reply,
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent m = new Intent(this, LoginActivity.class);
                    startActivity(m);
                    finish();
                });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            case 100: {
                displayDialog("Do you want to logout?", "Yes");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
