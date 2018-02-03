package solutionexpert.org.lapitchat2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int MAX_LENGTH =10;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private CircleImageView mCircleImageView;
    private TextView mName, mStatus;

    private Button change_img_btn, change_status_btn;
    private static int GALLERY_PICK=1;

    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCircleImageView=findViewById(R.id.settings_image);
        mName=findViewById(R.id.settings_display_name);
        mStatus=findViewById(R.id.settings_status);
        change_img_btn=findViewById(R.id.settings_image_btn);
        change_status_btn=findViewById(R.id.settings_status_btn);

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=currentUser.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("user").child(current_uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                Picasso.with(SettingsActivity.this).load(image).into(mCircleImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        change_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textStatus=mStatus.getText().toString();
                Intent changeStatusIntent= new Intent(SettingsActivity.this, StatusActivity.class);
                changeStatusIntent.putExtra("status",textStatus);
                startActivity(changeStatusIntent);
            }
        });
        change_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
*/
               CropImage.activity().setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);


            }
        });


        mStorageRef = FirebaseStorage.getInstance().getReference();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog=new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save changes");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                Uri resultUri = result.getUri();

                String current_user=currentUser.getUid();

                StorageReference filepath = mStorageRef.child("profile_images").child(current_user+".jpg");

                filepath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content


                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                databaseReference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(SettingsActivity.this, "working", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                progressDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "error", Toast.LENGTH_SHORT).show();
                                // ...
                            }
                        });



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
