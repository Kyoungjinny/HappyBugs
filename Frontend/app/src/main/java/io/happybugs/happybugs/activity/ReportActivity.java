package io.happybugs.happybugs.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.happybugs.happybugs.APIInterface.APIInterface;
import io.happybugs.happybugs.R;
import io.happybugs.happybugs.network.RetrofitInstance;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    ReportButtons buttons;
    ReportEditTexts editTexts;
    ReportCheckBoxes checkBoxes;
    ReportViews views;
    Context currContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Create question buttons and 'Save and Exit' button.
        buttons = new ReportButtons((Button) findViewById(R.id.whatBtn),
                (Button) findViewById(R.id.whereBtn), (Button) findViewById(R.id.whenBtn),
                (Button) findViewById(R.id.whoBtn), (Button) findViewById(R.id.detailsBtn),
                (Button) findViewById(R.id.saveBtn));

        // Create answer texts.
        editTexts = new ReportEditTexts((EditText) findViewById(R.id.whatText),
                (EditText) findViewById(R.id.whereText), (EditText) findViewById(R.id.whenText),
                (EditText) findViewById(R.id.whoText),
                (EditText) findViewById(R.id.facebook_edit_text),
                (EditText) findViewById(R.id.detailsText));

        // Create checkboxes.
        // Checkboxes are enabled whenever a question is answered.
        checkBoxes = new ReportCheckBoxes((CheckBox) findViewById(R.id.whatCheck),
                (CheckBox) findViewById(R.id.whereCheck), (CheckBox) findViewById(R.id.whenCheck),
                (CheckBox) findViewById(R.id.whoCheck), (CheckBox) findViewById(R.id.detailsCheck));

        // Create underlines separating questions.
        views = new ReportViews((View) findViewById(R.id.whatView), (View) findViewById(R.id.whereView),
                (View) findViewById(R.id.whenView), (View) findViewById(R.id.whoView),
                (View) findViewById(R.id.facebook_text_input),
                (View) findViewById(R.id.detailsView));

        buttons.whatBtn.setOnClickListener(this);
        buttons.whereBtn.setOnClickListener(this);
        buttons.whenBtn.setOnClickListener(this);
        buttons.whoBtn.setOnClickListener(this);
        buttons.detailsBtn.setOnClickListener(this);
        buttons.saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.whatBtn:
                questionClickEvent(editTexts, editTexts.whatText, checkBoxes.whatCheck,
                        buttons.saveBtn, views, views.whatView, views.facebookView);
                break;
            case R.id.whereBtn:
                questionClickEvent(editTexts, editTexts.whereText, checkBoxes.whereCheck,
                        buttons.saveBtn, views, views.whereView, views.facebookView);
                break;
            case R.id.whenBtn:
                questionClickEvent(editTexts, editTexts.whenText, checkBoxes.whenCheck,
                        buttons.saveBtn, views, views.whenView, views.facebookView);
                break;
            case R.id.whoBtn:
                enableAnswerText(editTexts, editTexts.whoText, views.facebookView);
                enableUnderline(views, views.whoView, editTexts.whoText);

                editTexts.whoText.addTextChangedListener(new WhoTextChange(editTexts.whoText, editTexts.facebookIDText,
                        editTexts.whoText, checkBoxes.whoCheck, buttons.saveBtn));
                editTexts.facebookIDText.addTextChangedListener(new WhoTextChange(editTexts.whoText, editTexts.facebookIDText,
                        editTexts.whoText, checkBoxes.whoCheck, buttons.saveBtn));
                break;
            case R.id.detailsBtn:
                questionClickEvent(editTexts, editTexts.detailsText, checkBoxes.detailsCheck,
                        buttons.saveBtn, views, views.detailsView, views.facebookView);
                break;
            case R.id.saveBtn:
                sendReportData();
                break;
        }
    }

    // This is an event handler function of question click event.
    // It deals with all the actions needed on clicking a question.
    public void questionClickEvent(ReportEditTexts editTexts, EditText editText,
                                   CheckBox checkBox, Button saveBtn,
                                   ReportViews views, View view, View facebookView) {
        enableAnswerText(editTexts, editText, facebookView);
        enableBoxes(editText, checkBox, saveBtn);
        enableUnderline(views, view, editText);
    }

    // Open and collapse edit text on clicking question buttons.
    // Opens answer text view for the corresponding question, while closing other answer views.
    // Closes answer text view if it is already opened.
    protected void enableAnswerText(ReportEditTexts editTexts,
                                    TextView textView, View facebookView) {
        if (textView.getVisibility() == View.GONE) {
            editTexts.openAndCollapse(editTexts);
            facebookView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);

            if (textView == editTexts.whoText) {
                views.facebookView.setVisibility(View.VISIBLE);
                editTexts.facebookIDText.setVisibility(View.VISIBLE);
            }

        } else {
            textView.setVisibility(View.GONE);
            views.facebookView.setVisibility(View.GONE);
            editTexts.facebookIDText.setVisibility(View.GONE);

            if (textView == editTexts.whoText) {
                views.facebookView.setVisibility(View.GONE);
                editTexts.facebookIDText.setVisibility(View.GONE);
            }
        }
    }

    // Enable checkbox and save button when questions answered.
    protected void enableBoxes(final EditText editText, final CheckBox checkBox, final Button saveBtn) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                checkBox.setChecked((editText.getText().length() > 0) ? true : false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkBox.setChecked((editText.getText().length() > 0) ? true : false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                saveBtn.setEnabled(true);
            }
        });
    }

    // Set underline view to invisibility when edit text shown.
    protected void enableUnderline(ReportViews views, View view, TextView textView) {
        if (textView.getVisibility() == View.VISIBLE) {
            views.makeUnderline(views);
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    // POST report data to server.
    protected void sendReportData() {
        JSONObject what = new JSONObject();
        what.put("what", editTexts.getWhatText());
        JSONObject location = new JSONObject();
        location.put("location", editTexts.getWhereText());
        JSONObject time = new JSONObject();
        time.put("time", editTexts.getWhenText());
        JSONObject who = new JSONObject();
        who.put("who", editTexts.getWhoText());
        JSONObject details = new JSONObject();
        details.put("details", editTexts.getDetailsText());

        JSONArray dataArray = new JSONArray();
        dataArray.add(what);
        dataArray.add(location);
        dataArray.add(time);
        dataArray.add(who);
        dataArray.add(details);

        JSONObject userReport = new JSONObject();
        userReport.put("data", dataArray);

        Retrofit rfInstance;
        rfInstance = RetrofitInstance.getInstance(currContext);
        APIInterface service = rfInstance.create(APIInterface.class);

        Call<ResponseBody> request = service.createReport(userReport);
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Response rb = response;
                System.out.println(rb.body());
                //success
                startActivity(new Intent(currContext, MainActivity.class));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getBaseContext(), "Creating report failed due to network error",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
