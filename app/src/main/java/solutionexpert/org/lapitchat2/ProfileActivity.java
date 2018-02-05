package solutionexpert.org.lapitchat2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView mDisplayUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String profile_userID = getIntent().getStringExtra("user_id");
        mDisplayUserID = findViewById(R.id.profile_DisplayName);
        mDisplayUserID.setText(profile_userID);
    }
}
