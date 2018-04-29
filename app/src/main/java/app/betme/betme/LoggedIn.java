package app.betme.betme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoggedIn extends AppCompatActivity implements View.OnClickListener {
    static EditText Money;
    EditText User;
    FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;
    FirebaseDatabase db;
    public DatabaseReference betting;
    ImageButton powerball,buttonmega;
    TextView logoff;


    public long timeleft2hrs = 7200000;



    public long balance;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);



        Money = (EditText) findViewById(R.id.Balance);
        User = (EditText) findViewById(R.id.Player);
        logoff = (TextView) findViewById(R.id.Logout);
        powerball = (ImageButton) findViewById(R.id.powerballbutton);
        buttonmega = (ImageButton) findViewById(R.id.megabutton);
        Money.setInputType(0);
        User.setInputType(0);

        db = FirebaseDatabase.getInstance();
        betting = db.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();



        mDatabase.child("users").child(current.getUid().toString()).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                balance = dataSnapshot.getValue(Long.class);
                String b = String.valueOf(balance);
                Money.setText(b);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Money.setText("Error");

            }
        });




        User.setText(current.getDisplayName().toString());



        //ADAPTER


        powerball.setOnClickListener(this);
        logoff.setOnClickListener(this);
        buttonmega.setOnClickListener(this);




    }






    public void updateTimer(long idInDB)
    {
        int hours = (int) timeleft2hrs / 3600000;
        int minutes = (int) timeleft2hrs % 3600000 / 60000;
        int seconds = (int) timeleft2hrs % 3600000 % 60000 / 1000;

        String timeleft;

        timeleft = "" + hours;
        timeleft += ":";
        if(minutes < 10) timeleft += "0";
        timeleft += minutes;
        if(seconds < 10) timeleft += "0";
        timeleft += seconds;



    }





    public static void updateTextWinnings(String balance)
    {
        Money.setText(balance);
        Money.setInputType(0);

    }



    @Override
    public void onClick(View view) {
        switch(view.getId())
        {

            case R.id.powerballbutton:
                startActivity(new Intent(this,ResultCheckPowerBall.class));

                break;
            case R.id.Logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this,MainActivity.class));


                break;

            case R.id.megabutton:
                startActivity(new Intent(this,ResultCheckMegaMillions.class));

                break;


        }
    }

}
