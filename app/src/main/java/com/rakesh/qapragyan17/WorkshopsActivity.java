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
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WorkshopsActivity extends AppCompatActivity {

    Switch checkBox;
    RatingBar qRating[];
    Map<String, String> params = new HashMap<>();
    String adminId;
    int eventId;
    Retrofit retrofit;
    Observable<Response> feedbackObservable;
    Button submit;
    AlertDialog alertDialog;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshops);
        submit = (Button) findViewById(R.id.submit);
        checkBox = (Switch) findViewById(R.id.checkbox);
        checkBox.setChecked(true);

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

                params.put("event_name", LoginActivity.events[eventId]);
                params.put("admin_id", adminId);

                sendFeedback(0);
            }
        });
    }

    private void sendFeedback(int i) {
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoginActivity.baseUrl)
                .build();

        NetworkService networkService = retrofit.create(NetworkService.class);
        params.put("question_id", Integer.toString(i + 1));
        if (i == 5) {
            if (checkBox.isChecked()) {
                params.put("response", "Yes");
            } else {
                params.put("response", "No");
            }
        } else {
            params.put("response", Float.toString(qRating[i].getRating()));
        }
        try {
            feedbackObservable = networkService.sendFeedback(params);

            feedbackObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.getStatusCode() == 200) {
                            if (i == 5) {
                                progressDialog.dismiss();
                                displayDialog("Submitted Successfully!!");
                                resetRatings((ViewGroup) findViewById(R.id.activity_main));
                            } else {
                                sendFeedback(i + 1);
                            }
                        } else {
                            displayDialog("Feedback submission failed, " + response.getStatusCode()
                                    + " : " + response.getMessage());
                        }
                    }, throwable -> {
                        progressDialog.dismiss();
                        displayDialog(throwable.getMessage() + "Network Error. Please Try Again");
                        Log.e("debug", throwable.getMessage());
                    });
        } catch (Exception e) {
            Log.e("debug", e.getMessage());
        }
    }

    private boolean isValidated() {
        if (qRating[0].getRating() != 0) {
            if (qRating[1].getRating() != 0) {
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
        Toast.makeText(WorkshopsActivity.this, "Logout to exit", Toast.LENGTH_LONG).show();
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
                displayDialog("Do you want to logout?", "Yes");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
