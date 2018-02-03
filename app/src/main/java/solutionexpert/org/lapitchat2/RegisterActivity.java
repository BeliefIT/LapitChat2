package solutionexpert.org.lapitchat2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName, mEmail, mPassword;
    private Button mCreateBtn;
    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mRegToolbar;
    //progress dialog
    private ProgressDialog mRegProgress;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mRegToolbar=findViewById(R.id.register_toolbar);
        setSupportActionBar(mRegToolbar);
        getSupportActionBar().setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress=new ProgressDialog(this);



        mDisplayName=findViewById(R.id.reg_display_name);
        mEmail=findViewById(R.id.reg_email);
        mPassword=findViewById(R.id.reg_password);
        mCreateBtn=findViewById(R.id.reg_create_button);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DisplayName=mDisplayName.getEditText().getText().toString();
                String Email=mEmail.getEditText().getText().toString();
                String Password=mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(DisplayName)&& !TextUtils.isEmpty(Email) && !TextUtils.isEmpty(Password))
                {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(DisplayName, Email, Password);

                }

            }
        });
    }

    private void register_user(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(RegisterActivity.this, "Registered Succesfully.",
                                    Toast.LENGTH_SHORT).show();
                            mRegProgress.dismiss();

                            FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=currentUser.getUid();

                            databaseReference= FirebaseDatabase.getInstance().getReference().child("user").child(uid);

                            HashMap<String, String> userMap=new HashMap<>();
                            userMap.put("name", displayName);
                            userMap.put("status", "hi i am happy");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            databaseReference.setValue(userMap);


                            /*
                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                            */
                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();


                        }

                        // ...
                    }
                });

    }
}
