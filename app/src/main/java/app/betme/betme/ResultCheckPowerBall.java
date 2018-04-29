package app.betme.betme;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;



//Reference https://stackoverflow.com/questions/9605913/how-to-parse-json-in-android

public class ResultCheckPowerBall extends AppCompatActivity implements View.OnClickListener {
    ProgressDialog pd;
    TextView previous1,previous2,previous3,previous4,previous5,previouspb;
    Button checkresult,showprevious;
    NumberPicker num1,num2,num3,num4,num5,numpb;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    long balance;
    FirebaseUser user;



    int count = 0; //count normal numbers
    int pb = 0; //count powerball;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_check_power_ball);

        checkresult = (Button) findViewById(R.id.resultbutton);
        showprevious = (Button) findViewById(R.id.checkold);
        num1 = (NumberPicker) findViewById(R.id.number1);
        num2 = (NumberPicker) findViewById(R.id.number2);
        num3 = (NumberPicker) findViewById(R.id.number3);
        num4 = (NumberPicker) findViewById(R.id.number4);
        num5 = (NumberPicker) findViewById(R.id.number5);
        numpb = (NumberPicker) findViewById(R.id.number6);

        num1.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num2.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num3.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num4.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num5.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        numpb.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        num1.setMinValue(1);
        num1.setMaxValue(69);
        num2.setMinValue(1);
        num2.setMaxValue(69);
        num3.setMinValue(1);
        num3.setMaxValue(69);
        num4.setMinValue(1);
        num4.setMaxValue(69);
        num5.setMinValue(1);
        num5.setMaxValue(69);
        numpb.setMinValue(1);
        numpb.setMaxValue(26);
        setDividerColor(numpb, Color.RED);
        setNumberPickerTextColor(numpb, Color.RED);


        user = auth.getCurrentUser();

        mDatabase.child("users").child(user.getUid().toString()).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                balance = dataSnapshot.getValue(Long.class);


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        showprevious.setOnClickListener(this);
        checkresult.setOnClickListener(this);







    }


    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }




    @SuppressLint("LongLogTag")
    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }




    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.resultbutton:
                new CheckResults().execute("http://data.ny.gov/resource/d6yy-54nr.json");
                break;

            case R.id.checkold:
                new CheckPrevious().execute("http://data.ny.gov/resource/d6yy-54nr.json");

                break;
        }
    }








    private class CheckResults extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ResultCheckPowerBall.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }


            int number1 = num1.getValue();
            int number2 = num2.getValue();
            int number3 = num3.getValue();
            int number4 = num4.getValue();
            int number5 = num5.getValue();
            int numpower = numpb.getValue();



            try {
                JSONArray  jArray = new JSONArray(result);
                JSONObject OneObject = jArray.getJSONObject(0);
                result = OneObject.getString("winning_numbers");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            String[] parts = result.split(" ");
            int[] n1 = new int[parts.length];
            for(int n = 0; n < parts.length; n++) {
                n1[n] = Integer.parseInt(parts[n]);
            }

            String num1 = String.valueOf(n1[0]);
            String num2 = String.valueOf(n1[1]);
            String num3  = String.valueOf(n1[2]);
            String num4  = String.valueOf(n1[3]);
            String num5  = String.valueOf(n1[4]);
            String numpb = String.valueOf(n1[5]);

            user = auth.getCurrentUser();

            boolean testcontain = Arrays.asList(n1).contains(17);

            if ( ArrayUtils.contains( n1, number1 )  ) {

                count = count + 1;
            }


            if ( ArrayUtils.contains( n1, number2 ) && number2 != number1 && number2 != number3 && number2 != number4 && number2 != number5 && number2 != numpower) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number3 ) && number3 != number1 && number3 != number2 && number3 != number4 && number3 != number5 && number3 != numpower ) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number4 ) && number4 != number1 && number4 != number3 && number4 != number2 && number4 != number5 && number4 != numpower) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number5 ) && number5 != number1 && number5 != number2 && number5 != number3 && number5 != number4 && number5 != numpower ) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, numpower ) && numpower != number1 && numpower != number2 && numpower != number3 && numpower != number4 && numpower != number5) {
                pb = pb + 1;
            }







            if(count == 0 && pb == 1 || count == 1 && pb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar4_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(4);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);





            }

            else if(count == 3 && pb == 0 || count == 2 && pb ==  1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar7_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(7);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }
            else if(count ==3 && pb == 1 || count == 4 && pb == 0)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar100_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(100);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);


            }
            else if(count == 4 && pb == 1 )
            {
                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar50k_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(50000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }

            else if(count == 5 && pb == 0)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar1mil_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(1000000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);


            }


            else if(count == 5 && pb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarjackpot_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(105600000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }


           else
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckPowerBall.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar0_layout, null);
                TextView current1 = (TextView) dollar7.findViewById(R.id.dollar7number1);
                TextView current2 = (TextView) dollar7.findViewById(R.id.dollar7number2);
                TextView current3 = (TextView) dollar7.findViewById(R.id.dollar7number3);
                TextView current4 = (TextView) dollar7.findViewById(R.id.dollar7number4);
                TextView current5 = (TextView) dollar7.findViewById(R.id.dollar7number5);
                TextView currentnumpb = (TextView) dollar7.findViewById(R.id.dollar7numberpb);

                current1.setText(num1);
                current2.setText(num2);
                current3.setText(num3);
                current4.setText(num4);
                current5.setText(num5);
                currentnumpb.setText(numpb);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

            }


            count = 0;
            pb = 0;






        }
    }






    private class CheckPrevious extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ResultCheckPowerBall.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }


            try {
                JSONArray  jArray = new JSONArray(result);
                JSONObject OneObject = jArray.getJSONObject(1);
                result = OneObject.getString("winning_numbers");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            String[] parts = result.split(" ");
            int[] n1 = new int[parts.length];
            for(int n = 0; n < parts.length; n++) {
                n1[n] = Integer.parseInt(parts[n]);
            }


           String first = String.valueOf(n1[0]);
            String second= String.valueOf(n1[1]);
            String third = String.valueOf(n1[2]);
            String fourth = String.valueOf(n1[3]);
            String fifth = String.valueOf(n1[4]);
           String powerball = String.valueOf(n1[5]);




            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ResultCheckPowerBall.this);
            View mView = getLayoutInflater().inflate(R.layout.oldresult_layout, null);
            previous1 = (TextView) mView.findViewById(R.id.previousnum1);
            previous2 = (TextView) mView.findViewById(R.id.previousnum2);
            previous3 = (TextView) mView.findViewById(R.id.previousnum3);
            previous4 = (TextView) mView.findViewById(R.id.previousnum4);
            previous5 = (TextView) mView.findViewById(R.id.previousnum5);
            previouspb = (TextView) mView.findViewById(R.id.previousnum6);

            previous1.setText(first);
            previous2.setText(second);
            previous3.setText(third);
            previous4.setText(fourth);
            previous5.setText(fifth);
            previouspb.setText(powerball);

            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();




        }
    }


}











