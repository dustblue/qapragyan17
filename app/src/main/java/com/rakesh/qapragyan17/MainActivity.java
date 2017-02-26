package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Switch toggle;
    TextView phoneNumber;
    RatingBar qRating[];
    Map<String, String> params = new HashMap<>();
    String adminId;
    int eventId, i;
    Retrofit retrofit;
    Observable<Response> feedbackObservable;
    Button submit;
    AlertDialog alertDialog;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submit = (Button) findViewById(R.id.button2);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        toggle = (Switch) findViewById(R.id.switch1);
        toggle.setTextOff("No");
        toggle.setTextOn("Yes");
        qRating = new RatingBar[]{
                (RatingBar) findViewById(R.id.q1_rating), (RatingBar) findViewById(R.id.q2_rating),
                (RatingBar) findViewById(R.id.q3_rating), (RatingBar) findViewById(R.id.q4_rating),
                (RatingBar) findViewById(R.id.q5_rating)
        };

        Intent intent = getIntent();
        adminId = intent.getStringExtra("adminId");
        eventId = intent.getIntExtra("eventId", 0);
        submit.setOnClickListener(view -> {

            if (isValidated()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Sending Feedback..");
                progressDialog.show();

                retrofit = new Retrofit.Builder()
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(LoginActivity.baseUrl)
                        .build();

                NetworkService networkService = retrofit.create(NetworkService.class);

                params.put("user_phoneno", phoneNumber.getText().toString());
                params.put("event_name", LoginActivity.events[eventId]);
                params.put("admin_id", adminId);

                for (i = 0; i < 5; i++) {
                    params.put("question_id", Integer.toString(i));
                    params.put("response", Float.toString(qRating[i].getRating()));
                    feedbackObservable = networkService.sendFeedback(params);
                }

                feedbackObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.getStatusCode() == 200) {
                                progressDialog.dismiss();
                                displayDialog("Submitted Successfully!!");
                                phoneNumber.setText(null);
                                resetRatings((ViewGroup) findViewById(R.id.activity_main));
                            } else {
                                displayDialog(response.getMessage() + "Feedback submission failed. Please Try Again");
                            }
                        });
            }
        });
    }

    private boolean isValidated() {
        if (phoneNumber.getText() != null) {
            if (qRating[0].getRating() != 0) {
                if (qRating[1].getRating() != 0) {
                    if (qRating[2].getRating() != 0) {
                        if (qRating[3].getRating() != 0) {
                            if (qRating[4].getRating() != 0) {
                                return true;
                            } else {
                                displayDialog("");
                                return false;
                            }
                        } else {
                            displayDialog("");
                            return false;
                        }
                    } else {
                        displayDialog("");
                        return false;
                    }
                } else {
                    displayDialog("");
                    return false;
                }
            } else {
                displayDialog("");
                return false;
            }
        } else {
            displayDialog("Please enter your phone number");
            return false;
        }
    }

    private void resetRatings(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof RatingBar) {
                ((RatingBar) view).setRating(0);
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                resetRatings((ViewGroup) view);
        }
    }

    private void displayDialog(String text) {
        if (alertDialog!=null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        alertDialog = new AlertDialog.Builder(this).create();
        if (text.equals("")) {
            alertDialog.setMessage("Please rate all questions");
        }
        alertDialog.setTitle("Feedback Pragyan '17");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(MainActivity.this, "Go to Menu to Logout", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent m = new Intent(this, LoginActivity.class);
        switch (item.getOrder()) {
            case 100: {
                startActivity(m);
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
