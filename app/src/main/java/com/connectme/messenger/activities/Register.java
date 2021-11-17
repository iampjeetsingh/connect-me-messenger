package com.connectme.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.connectme.messenger.App;
import com.connectme.messenger.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {
    private Context context = Register.this;
    TextView headtxt;
    EditText otp1txt,otp2txt,otp3txt,otp4txt,otp5txt,otp6txt;
    String otp1,otp2,otp3,otp4,otp5,otp6;
    private String mVerificationId;
    private static final String TAG = "PhoneAuthActivity";
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private static PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    EditText phonetxt;
    String phone;
    Button nextbtn;
    boolean link;
    boolean delete;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        setContentView(R.layout.activity_register);
        app = (App) getApplication();
        link = getIntent().getBooleanExtra("link",false);
        delete = getIntent().getBooleanExtra("delete",false);
        phonetxt = (EditText)findViewById(R.id.phonetxt);
        nextbtn = (Button) findViewById(R.id.nextbtn);
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(Register.this,"Wrong credential",Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(Register.this,"SMS Limit Reached",Toast.LENGTH_LONG).show();
                }
                Toast.makeText(Register.this,"Verification Failed",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(Register.this,"Verification code sent",Toast.LENGTH_LONG).show();
                mVerificationId = verificationId;
                mResendToken = token;
            }};
    }
    public void nextclick(View v){
        phone = "+91"+phonetxt.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setCancelable(false);
        builder.setMessage("We will be verifying the phone number:\n\n"+phone+"\n\nIs this OK, or would you like to edit the number?");
        builder.setPositiveButton("OK", (dialog, which) -> {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,60,TimeUnit.SECONDS,Register.this,mCallbacks);
            startotp();
        });
        builder.setNegativeButton("Edit", (dialog, which) -> {

        });
        builder.create().show();
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Register.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if(delete){
                            startActivity(new Intent(Register.this, AccountSettings.class));
                        }else
                            startActivity(new Intent(Register.this, Username.class));
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(Register.this,"Invalid Verification Code",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    public static PhoneAuthProvider.OnVerificationStateChangedCallbacks getCallback(){
        return mCallbacks;}
    public void startotp(){
        setContentView(R.layout.activity_otp);
        Toolbar otptoolbar = (Toolbar) findViewById(R.id.otptoolbar);
        setSupportActionBar(otptoolbar);
        ((TextView)findViewById(R.id.toolbartxt)).setText("Verify "+phone);
        ((TextView)findViewById(R.id.headtxt)).setText(phone);
        otp1txt = (EditText) findViewById(R.id.otp1);
        otp2txt = (EditText) findViewById(R.id.otp2);
        otp3txt = (EditText) findViewById(R.id.otp3);
        otp4txt = (EditText) findViewById(R.id.otp4);
        otp5txt = (EditText) findViewById(R.id.otp5);
        otp6txt = (EditText) findViewById(R.id.otp6);
        headtxt = (TextView) findViewById(R.id.headtxt);
        headtxt.setText(phone);
        findViewById(R.id.editbtn).setOnClickListener(v -> recreate());
        otp1txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(otp1txt.getText().toString().length()==1){otp2txt.requestFocus();
                }
            }@Override public void afterTextChanged(Editable s) {}@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}});
        otp2txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(otp2txt.getText().toString().length()==1){otp3txt.requestFocus();
                }
            }@Override public void afterTextChanged(Editable s) {}@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}});
        otp3txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(otp3txt.getText().toString().length()==1){otp4txt.requestFocus();
                }
            }@Override public void afterTextChanged(Editable s) {}@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}});
        otp4txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(otp4txt.getText().toString().length()==1){otp5txt.requestFocus();
                }
            }@Override public void afterTextChanged(Editable s) {}@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}});
        otp5txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(otp5txt.getText().toString().length()==1){otp6txt.requestFocus();
                }
            }@Override public void afterTextChanged(Editable s) {}@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}});
        findViewById(R.id.verifyclick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp1 = ((EditText) findViewById(R.id.otp1)).getText().toString();
                otp2 = ((EditText) findViewById(R.id.otp2)).getText().toString();
                otp3 = ((EditText) findViewById(R.id.otp3)).getText().toString();
                otp4 = ((EditText) findViewById(R.id.otp4)).getText().toString();
                otp5 = ((EditText) findViewById(R.id.otp5)).getText().toString();
                otp6 = ((EditText) findViewById(R.id.otp6)).getText().toString();
                String OTP = otp1+otp2+otp3+otp4+otp5+otp6;
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,OTP);
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
    }
}
