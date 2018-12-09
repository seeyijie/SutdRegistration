package com.example.benjamin.sutdregistration;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    Button register;
    EditText studentid;
    String studentidContent;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MainActivity.this, CameraActivity.class);
        takePictureIntent.putExtra("STUDENT_ID", studentidContent);
        startActivity(takePictureIntent);
    }


    public boolean isStringInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex)
        {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        studentid = findViewById(R.id.idfield);

        //TODO make it so that i can press enter button to hide keyboard - Done
        studentid.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            //added single line function in xml
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.e("Ben","Enter key pressed");

                }
                return false;
            }
        });




        register = findViewById(R.id.registerbutton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                studentidContent = studentid.getText().toString();
                if (studentidContent.length() == 0  || !isStringInt(studentidContent)) {   //take note that getString == "" or null, does not work
                    //TODO make it so that it only accept digits - Done
                    Toast.makeText(MainActivity.this, "Please key in your student id", Toast.LENGTH_LONG).show();
                    Log.i("Ben", "failure");
                    Log.i("Ben", studentidContent);

                } else {
                    //TODO testing only
                    // Write a message to the database
//                    FirebaseDatabase database = FirebaseDatabase.getInstance();
//                    DatabaseReference myRef = database.getReference("message");
//
//                    myRef.setValue("Please show up");
                    //TODO END
                    dispatchTakePictureIntent();
                    Log.i("Ben", "success");
                    Log.i("Ben", studentidContent);
                }
            }
        });

    }
}


