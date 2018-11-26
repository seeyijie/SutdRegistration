package com.example.benjamin.sutdregistration;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.gson.JsonObject;
import com.publit.publit_io.utils.PublitioCallback;
import com.publit.publit_io.utils.Publitio;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CameraActivity extends AppCompatActivity {

    private Button buttonImage;
    private Button newRegistration;
    private ImageView imageView;
    ContentValues values;
    Uri imageUri;
    String imageurl;
    private String studentID;
    Publitio mPublitio;


    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 100;

    //creating reference to firebase storage
    //THIS IS NEEDED!!!
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_page);

        //initialize firebase, this is not needed
//        storage = FirebaseStorage.getInstance();
//        storageReference = storage.getReference();


        //To fix permission issues
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int readPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int internetPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_NETWORK_STATE);
            int networkPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_NETWORK_STATE);


            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED ||
                    internetPermission != PackageManager.PERMISSION_GRANTED ||
                    networkPermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.INTERNET},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
            }
        }


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            studentID = extras.getString("STUDENT_ID");
        }

        //new registration button: brings back to main_activity
        newRegistration = this.findViewById(R.id.button_image2);
        newRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


        this.buttonImage = this.findViewById(R.id.button_image);
        this.imageView = this.findViewById(R.id.imageView);

        this.buttonImage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    private void captureImage() {
        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // Create an implicit intent, for image capture.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // Start camera and wait for the results.
        startActivityForResult(intent, REQUEST_ID_IMAGE_CAPTURE);



    }


    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // When results returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPublitio = new Publitio(this);
        Map<String, String> create = new HashMap<>();

        if (requestCode == REQUEST_ID_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap pic = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    this.imageView.setImageBitmap(pic);
                    imageurl = getRealPathFromURI(imageUri);

                    //encoding the image to base64 string
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    pic.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    mPublitio.files().uploadFile(imageUri, create, new PublitioCallback<JsonObject>() {
                        @Override
                        public void success(JsonObject result) {
                            Log.d("Publitio", "file uploaded: " + result.toString());
                        }

                        @Override
                        public void failure(String message) {

                            Log.d("Publitio", "file upload error: " + message.toString());

                        }
                    });
                    /*



                    //TODO, create unique ID as parent class - Done
                    String uniqueID = UUID.randomUUID().toString();
                     */



                    //TODO, send pic to firebase - Done
                    //we send the string over to firebase
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("registration");
                    myRef.child(studentID).child("Photo").setValue(imageEncoded);

                    //TODO send studentID to firebase - Done
//                    DatabaseReference myRef2 = database.getReference(uniqueID);
//                    myRef2.child("Student Id").setValue(studentID);

                    Toast.makeText(CameraActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

//                        final ProgressDialog progressDialog = new ProgressDialog(this);
//                        progressDialog.setTitle("Uploading...");
//                        progressDialog.show();

                } catch (Exception e) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
                }

            }   else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}