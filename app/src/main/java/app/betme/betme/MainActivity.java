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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Button LoginButton;
    EditText LogEmail,LogPassword;
    TextView Error, SignUp;
    FirebaseAuth auth;
    FirebaseUser current;
    ProgressBar progress;



    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) {
                Intent intent = new Intent(MainActivity.this, LoggedIn.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent); // User is signed in.
            } else {
                // No user is signed in.

        LogEmail = (EditText) findViewById(R.id.logemail);
        LogPassword = (EditText) findViewById(R.id.password);
        LoginButton = (Button) findViewById(R.id.Login);
        Error = (TextView) findViewById(R.id.Incorrect);
        SignUp = (TextView) findViewById(R.id.signUp);
        progress = (ProgressBar) findViewById(R.id.progressLogIn);
        LoginButton.setOnClickListener(this);
        SignUp.setOnClickListener(this);
            }

    }

    //Code executed when button is pressed
    public void UserLogin()
    {
        //reset the visibility of the error
        Error.setVisibility(View.INVISIBLE);
        final String email = LogEmail.getText().toString();
        final String password = LogPassword.getText().toString();


        //error checking
        if(email.isEmpty())
        {
            LogEmail.setError("Email is empty");
            LogEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            LogEmail.setError("Please enter a valid email");
            LogEmail.requestFocus();
            return;

        }
        if(password.isEmpty())
        {
            LogPassword.setError("Password is empty");
            LogPassword.requestFocus();
            return;
        }
        if(password.length()<6)
        {
            LogPassword.setError("Minimum password length is 6");
            LogPassword.requestFocus();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        LoginButton.setVisibility(View.INVISIBLE);
        SignUp.setVisibility(View.INVISIBLE);
         //attempt to sign in
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                //if logged in
                if(task.isSuccessful())
                {//starts new activity
                    Intent intent = new Intent(MainActivity.this, LoggedIn.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                //if login failed even after the error check
                else
                {

                    progress.setVisibility(View.INVISIBLE);
                    LoginButton.setVisibility(View.VISIBLE);
                    SignUp.setVisibility(View.VISIBLE);

                    //if the credentials are incorrect
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        LogPassword.setError("Invalid Password");
                        LogPassword.requestFocus();
                        return;
                    }
                   //if there is no such user
                    if(task.getException() instanceof FirebaseAuthInvalidUserException)
                    {
                        LogEmail.setError("User does not exist!");
                        LogEmail.requestFocus();
                        return;
                    }

                    //otherwise (Rare occasion)
                    else {
                        //toast pops up
                        Toast.makeText(getApplicationContext(), "Error Logging in User", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });




    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            //When login button is pressed
            case R.id.Login:
                UserLogin();
                break;
            case R.id.signUp:
                startActivity(new Intent(this,Register.class));
                break;

        }



    }











    }




