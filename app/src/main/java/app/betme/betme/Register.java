package app.betme.betme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    Button Register;
    EditText RegName,RegEmail,RegPass,RegConfPass;
    TextView  emptyEmail,  emptyConfPassword,  emailAlreadyExists;
    ProgressBar progressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RegEmail = (EditText) findViewById(R.id.registerEmail);
        RegPass = (EditText) findViewById(R.id.registerPassword);
        RegConfPass = (EditText) findViewById(R.id.registerConfirmPassword);
        Register = (Button) findViewById(R.id.registerButton);
        RegName = (EditText) findViewById(R.id.registerName);
        emptyEmail = (TextView) findViewById(R.id.EmptyEmail) ;
        emptyConfPassword = (TextView) findViewById(R.id.EmptyConfPass);
        emailAlreadyExists = (TextView) findViewById(R.id.emailExists);
        progressbar = (ProgressBar) findViewById(R.id.progressSignUp);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        Register.setOnClickListener(this);

    }

//Create a new user in firebase
    private void registerUser()
    {
        Register.setVisibility(View.INVISIBLE);
        progressbar.setVisibility(View.VISIBLE);


        emptyEmail.setVisibility(View.GONE);
        emptyConfPassword.setVisibility(View.GONE);
        emailAlreadyExists.setVisibility(View.GONE);


        String email = RegEmail.getText().toString();
        String password = RegPass.getText().toString();
        String confpassword = RegConfPass.getText().toString();
        final String name = RegName.getText().toString();


        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            progressbar.setVisibility(View.GONE);
            Register.setVisibility(View.VISIBLE);
            RegEmail.setError("Please enter a valid email");
            RegEmail.requestFocus();
            return;

        }


        if(email.isEmpty())
    {
        progressbar.setVisibility(View.GONE);
        Register.setVisibility(View.VISIBLE);
        RegEmail.setError("Email is empty");
        RegEmail.requestFocus();
        return;
    }

    if(password.isEmpty())
    {
        progressbar.setVisibility(View.GONE);
        Register.setVisibility(View.VISIBLE);
        RegPass.setError("Password is empty");
        RegPass.requestFocus();
        return;
    }
    if(password.length()<6)
    {
        progressbar.setVisibility(View.GONE);
        Register.setVisibility(View.VISIBLE);
        RegPass.setError("Minimum password length is 6");
        RegPass.requestFocus();
        return;
    }



    if(confpassword.isEmpty())
    {
        progressbar.setVisibility(View.GONE);
        Register.setVisibility(View.VISIBLE);
        RegConfPass.setError("Confirm Password");
        RegConfPass.requestFocus();
        return;
    }

    if(!confpassword.matches(password))
    {
        progressbar.setVisibility(View.GONE);
        Register.setVisibility(View.VISIBLE);
        RegConfPass.setError("Passwords don't match");
        RegConfPass.requestFocus();
        return;
    }

         auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {


                 if(task.isSuccessful())
                 {
                     String Uid = task.getResult().getUser().getUid().toString();


                     FirebaseUser user = auth.getCurrentUser();

                     UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                             .setDisplayName(name).build();

                     user.updateProfile(profileUpdates);
                     FirebaseAuth.getInstance().signOut();


                     mDatabase.child("users").child(Uid).child("balance").setValue(Long.valueOf(0));

                     Intent intent = new Intent(Register.this, MainActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);

                 }
                 else
                 {
                     progressbar.setVisibility(View.GONE);
                     Register.setVisibility(View.VISIBLE);

                     if(task.getException() instanceof FirebaseAuthUserCollisionException)
                     {
                         RegEmail.setError("Email already registered!");
                         RegEmail.requestFocus();
                         return;
                     }
                     else {
                         Toast.makeText(getApplicationContext(), "Error Registering User", Toast.LENGTH_SHORT).show();
                     }
                 }




             }
         });




    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.registerButton:
                registerUser();

                break;



        }
    }
}
