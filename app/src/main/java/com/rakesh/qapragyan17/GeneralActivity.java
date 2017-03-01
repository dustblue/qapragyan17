package com.rakesh.qapragyan17;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GeneralActivity extends AppCompatActivity {

    private Spinner spinner;
    private RatingBar qRating[];
    private String userToken;
    private int eventId, userId, teamPin;
    private Retrofit retrofit;
    private Observable<Response> feedbackObservable;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private String baseUrl = "https://api.pragyan.org/";
    public static String[] sources = new String[]{"Newspapers", "Posters", "Social Media",
            "Website", "Friends", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        Button submit = (Button) findViewById(R.id.submit);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_dropdown_item, sources);
        spinner.setAdapter(adapter);

        qRating = new RatingBar[]{
                (RatingBar) findViewById(R.id.q1_rating), (RatingBar) findViewById(R.id.q2_rating),
                (RatingBar) findViewById(R.id.q3_rating), (RatingBar) findViewById(R.id.q4_rating),
                (RatingBar) findViewById(R.id.q5_rating)
        };

        Intent intent = getIntent();
        eventId = intent.getIntExtra("eventId", 0);
        userId = intent.getIntExtra("user_id", 0);
        userToken = intent.getStringExtra("user_token");
        teamPin = intent.getIntExtra("team_pin", 0);

        submit.setOnClickListener(view -> {
                    if (isValidated()) {
                        progressDialog = new ProgressDialog(this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Sending Feedback..");
                        progressDialog.show();

                        retrofit = new Retrofit.Builder()
                                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                .addConverterFactory(GsonConverterFactory.create())
                                .baseUrl(baseUrl)
                                .build();

                        NetworkService networkService = retrofit.create(NetworkService.class);

                        feedbackObservable = networkService.sendFeedback(getFeedback());

                        feedbackObservable.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(response -> {
                                    progressDialog.dismiss();
                                    if (response.getStatusCode() == 200) {
                                        displayDialog("Submitted Successfully!!");
                                        resetRatings((ViewGroup) findViewById(R.id.activity_main));
                                    } else {
                                        displayDialog("Feedback submission failed, " + response.getStatusCode()
                                                + " : " + response.getMessage());
                                    }
                                }, throwable -> {
                                    progressDialog.dismiss();
                                    displayDialog("Network Error. Please Try Again");
                                    Log.e("debug", throwable.getMessage());
                                });
                    }
                }
        );
    }

    private Feedback getFeedback() {
        Feedback feedback = new Feedback();
        feedback.user_id = userId;
        feedback.user_token = userToken;
        feedback.team_name = "QA";
        feedback.team_pin = teamPin;
        feedback.event_name = LoginActivity.events[eventId];

        feedback.data = new ArrayList<>();
        int i;
        for (i = 0; i < 6; i++) {
            Data data = new Data();
            data.question_id = i + 1;
            if (i == 5) {
                data.response = sources[spinner.getSelectedItemPosition()];
            } else {
                data.response = Float.toString(qRating[i].getRating());
            }
            feedback.data.add(data);
        }
        return feedback;
    }

    private boolean isValidated() {
        if (qRating[0].getRating() != 0) {
            if (qRating[1].getRating() != 0) {
                if (qRating[2].getRating() != 0) {
                    if (qRating[3].getRating() != 0) {
                        if (qRating[4].getRating() != 0) {
                            return true;
                        } else {
                            displayDialog("Please rate all questions");
                            return false;
                        }
                    } else {
                        displayDialog("Please rate all questions");
                        return false;
                    }
                } else {
                    displayDialog("Please rate all questions");
                    return false;
                }
            } else {
                displayDialog("Please rate all questions");
                return false;
            }
        } else {
            displayDialog("Please rate all questions");
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
        if (text.equals("")) {
            alertDialog.setMessage("Please rate all questions");
        }
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
    public void onBackPressed() {
        Toast.makeText(GeneralActivity.this, "Logout to exit", Toast.LENGTH_LONG).show();
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
