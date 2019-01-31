package io.happybugs.happybugs.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.happybugs.happybugs.APIInterface.APIInterface;
import io.happybugs.happybugs.R;
import io.happybugs.happybugs.network.RetrofitInstance;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignInActivity extends AppCompatActivity {

    private Context curContext;
    private Button btnOpenSignUp;
    private Button btnStartSignIn;
    private EditText etUserEmail;
    private EditText etUserPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        curContext = this;

        etUserEmail = (EditText) findViewById(R.id.editText_user_email);
        etUserPW = (EditText) findViewById(R.id.editText_user_pw);
        btnStartSignIn = (Button) findViewById(R.id.button_sign_in);
        btnOpenSignUp = (Button) findViewById(R.id.button_open_sign_up);

        btnStartSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(Jelldo): make counter for preventing multiple login attempts
                //Will be defined in Backend
                startSignIn();
            }
        });

        btnOpenSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(curContext, SignUpActivity.class));
            }
        });
    }

    public void startSignIn() {
        if (!isValidSignInForm()) {
            Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
            //btnStartSignIn.setEnabled(true);
            return;
        }
        //btnStartSignIn.setEnabled(false);

        String userEmail = etUserEmail.getText().toString();
        String userPwd = etUserPW.getText().toString();

        Retrofit rfInstance = RetrofitInstance.getInstance();
        APIInterface service = rfInstance.create(APIInterface.class);

        Call<ResponseBody> requestSignIn = service.signin(userEmail, userPwd);
        requestSignIn.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //TODO(Jelldo): get ResponseBody too
                //Sign-In success
                if (response.code() == 200) {
                    //GET Body here
                    //Toast.makeText(getBaseContext(), "Login success", Toast.LENGTH_LONG).show();
                    //TODO(Jelldo): add progressbar, make async
                    final int DELAY_MILLIS = 3000;
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //TODO(Jelldo): need to change into HomeActivity
                                    startActivity(new Intent(curContext, MainActivity.class));
                                    //finish();
                                    //dismiss dialogs, close cursors, close search dialogs
                                }
                            }, DELAY_MILLIS);
                } else if (response.code() == 401) {
                    //TODO(Jelldo): show msg under the textfield
                    Toast.makeText(getBaseContext(), "Failed to login", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Network Failure
                Toast.makeText(getBaseContext(), "Login failed due to network error", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public boolean isValidSignInForm() {
        boolean isValid = true;

        String userEmail = etUserEmail.getText().toString();
        String userPwd = etUserPW.getText().toString();

        if (userEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            etUserEmail.setError("Enter valid email address");
            isValid = false;
        } else {
            etUserEmail.setError(null);
        }

        if (userPwd.isEmpty() || userPwd.length() < 8) {
            etUserPW.setError("Longer than alphanumeric characters");
            isValid = false;
        } else {
            etUserPW.setError(null);
        }
        return isValid;
    }
}
