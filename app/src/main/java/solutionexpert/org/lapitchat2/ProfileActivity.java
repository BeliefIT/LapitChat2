package solutionexpert.org.lapitchat2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    ImageView mProfileImage;
    TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRegDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private ProgressDialog progressDialog;

    private String mCurrent_state;

    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String profile_userID = getIntent().getStringExtra("user_id");
        // final String profile_userID="SgaCs7HdsRbpRMTg6vLj6wZkRmA3";

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(profile_userID);
        mUserDatabase.keepSynced(true);


        mFriendRegDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_btn);
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mCurrent_state = "not_friends"; // not friends

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please Wait while we load the User data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                // Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                    }
                });


                // .........friend list / request feature............

                mFriendRegDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(profile_userID)) {
                            String req_type = dataSnapshot.child(profile_userID).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            progressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(profile_userID)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {


                mProfileSendReqBtn.setEnabled(false);


                switch (mCurrent_state) {
                    case "not_friends":
                        //................not friend state................................................
                        mFriendRegDatabase.child(mCurrent_user.getUid()).child(profile_userID).child("request_type")
                                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    mFriendRegDatabase.child(profile_userID).child(mCurrent_user.getUid())
                                            .child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            HashMap<String, String> notificationData = new HashMap<>();
                                            notificationData.put("from", mCurrent_user.getUid());
                                            notificationData.put("type", "request");

                                            mNotificationDatabase.child(profile_userID).push().setValue(notificationData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mProfileSendReqBtn.setEnabled(true);
                                                            mCurrent_state = "req_sent";
                                                            mProfileSendReqBtn.setText("Cancel Friend Request");
                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                            mDeclineBtn.setEnabled(false);

                                                            Toast.makeText(ProfileActivity.this, "sending friend request", Toast.LENGTH_SHORT).show();


                                                        }
                                                    });


                                        }
                                    });
                                }
                            }
                        });
                        break;
                    case "req_sent":
                        //......................cancel friend request.....................
                        mFriendRegDatabase.child(mCurrent_user.getUid()).child(profile_userID).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mFriendRegDatabase.child(profile_userID).child(mCurrent_user.getUid()).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mProfileSendReqBtn.setEnabled(true);
                                                        mCurrent_state = "not_friends";
                                                        mProfileSendReqBtn.setText("Send Friend Request");
                                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                                        mDeclineBtn.setEnabled(false);

                                                        Toast.makeText(ProfileActivity.this, "canceling friend request", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    }
                                });
                        break;

                    case "req_received":
                        //................req received state...........................
                        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                        mFriendDatabase.child(mCurrent_user.getUid()).child(profile_userID).setValue(currentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mFriendDatabase.child(profile_userID).child(mCurrent_user.getUid()).setValue(currentDate)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mFriendRegDatabase.child(mCurrent_user.getUid()).child(profile_userID).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        mFriendRegDatabase.child(profile_userID).child(mCurrent_user.getUid()).removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        mProfileSendReqBtn.setEnabled(true);
                                                                                        mCurrent_state = "friends";
                                                                                        mProfileSendReqBtn.setText("Unfriend this person");
                                                                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                                                                        mDeclineBtn.setEnabled(false);

                                                                                        Toast.makeText(ProfileActivity.this, "canceling friend request", Toast.LENGTH_SHORT).show();

                                                                                    }
                                                                                });

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });



                    default:
                        Toast.makeText(ProfileActivity.this, "default", Toast.LENGTH_SHORT).show();


                }
            }
        });
    }
}