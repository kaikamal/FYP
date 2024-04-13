package com.example.loginactivity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class Register extends AppCompatActivity {
    private static final String TAG = MainActivity2.class.getSimpleName();
    EditText mEmail, mPassword, mConfirmPassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.Password);
        mConfirmPassword = findViewById(R.id.Phone);
        mRegisterBtn = findViewById(R.id.button);
        mLoginBtn = findViewById(R.id.textView5);
        ImageView signInButton = findViewById(R.id.imageView);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if(googleSignInAccount != null){
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(o.getData());
                handleSignedInTask(task);
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(signInIntent);
            }
        });



        mPassword.setTransformationMethod(new AsteriskPasswordTransformation());
        mConfirmPassword.setTransformationMethod(new AsteriskPasswordTransformation());
        fAuth = FirebaseAuth.getInstance();
//        progressBar = findViewById(R.id.progressBar);
        if (fAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), MainActivity2.class));
            finish();
        }


    }
    public class AsteriskPasswordTransformation extends PasswordTransformationMethod{
        public CharSequence getTransformation(CharSequence source, View view){
            return new AsteriskPasswordTransformation.PasswordCharSequence(source);
        }
        public class PasswordCharSequence implements CharSequence{
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                return mSource.length(); // Return default
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }

    public void onClickLoginFromRegister(View view){
        startActivity(new Intent(getApplicationContext(), Login.class));
    }
    public void onClickRegister(View view) {
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmedPassword = mConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is required!");
            return;
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError("Enter a valid email address!");
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is required!");
            return;
        }
        if (password.length() < 6) {
            mPassword.setError("Password must be >= 6 characters");
            return;
        }
        if (!password.equals(confirmedPassword)) {
            mPassword.setError("Passwords do not match!");
            mConfirmPassword.setError("Passwords do not match!");
        }
            progressBar.setVisibility(View.VISIBLE);

            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity2.class));

                    } else {
                        String errorMessage = task.getException().getMessage();
                        if(errorMessage.contains("email address is already in use")){
                            mEmail.setError("Email is already in use!");
                        }
                        else{
                            Toast.makeText(Register.this, "Error: "+ errorMessage, Toast.LENGTH_SHORT).show();
                        }
//                        Toast.makeText(Register.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
        private void handleSignedInTask (Task<GoogleSignInAccount> task){
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                final String getFullName = account.getDisplayName();
                final String getEmail = account.getEmail();
//                final String getPhotoUrl = account.getPhotoUrl();
                startActivity(new Intent(Register.this, MainActivity2.class));
                finish();
            }catch (ApiException e){
//                Toast.makeText(this, Toast.LENGTH_SHORT).show();
            }


        }

}