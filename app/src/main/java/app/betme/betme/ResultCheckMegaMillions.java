package app.betme.betme;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
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



public class ResultCheckMegaMillions extends AppCompatActivity implements View.OnClickListener {


    NumberPicker num1,num2,num3,num4,num5,megaball;
    Button checkresult,showprevious;
    int count = 0;
    int mb = 0;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    FirebaseUser user;
    long balance;

    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_mega_millions);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();


        num1 = (NumberPicker) findViewById(R.id.numberpick1);
        num2 = (NumberPicker) findViewById(R.id.numberpick2);
        num3 = (NumberPicker) findViewById(R.id.numberpick3);
        num4 = (NumberPicker) findViewById(R.id.numberpick4);
        num5 = (NumberPicker) findViewById(R.id.numberpick5);
        megaball = (NumberPicker) findViewById(R.id.numberpick6);
        num1.setMinValue(1);
        num1.setMaxValue(70);
        num2.setMinValue(1);
        num2.setMaxValue(70);
        num3.setMinValue(1);
        num3.setMaxValue(70);
        num4.setMinValue(1);
        num4.setMaxValue(70);
        num5.setMinValue(1);
        num5.setMaxValue(70);
        megaball.setMinValue(1);
        megaball.setMaxValue(25);




        num1.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num2.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num3.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num4.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        num5.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        megaball.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        int r = 188;
        int g = 143;
        int b = 7;
        setDividerColor(megaball, Color.rgb(r,g,b));
        setNumberPickerTextColor(megaball, Color.rgb(r,g,b));


        checkresult = (Button) findViewById(R.id.resultcheck);
        showprevious = (Button) findViewById(R.id.checkoldmega);



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



        checkresult.setOnClickListener(this);
        showprevious.setOnClickListener(this);










    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            case R.id.resultcheck:
                new CheckResults().execute("https://data.ny.gov/resource/h6w8-42p9.json");

                break;
            case R.id.checkoldmega:
                new CheckPrevious().execute("https://data.ny.gov/resource/h6w8-42p9.json");

               break;

        }
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


    private class CheckResults extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();







            pd = new ProgressDialog(ResultCheckMegaMillions.this);
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

            String megaballnum = "";


            int number1 = num1.getValue();
            int number2 = num2.getValue();
            int number3 = num3.getValue();
            int number4 = num4.getValue();
            int number5 = num5.getValue();
            int mega = megaball.getValue();



            try {
                JSONArray jArray = new JSONArray(result);
                JSONObject OneObject = jArray.getJSONObject(0);
                result = OneObject.getString("winning_numbers");
                megaballnum = OneObject.getString("mega_ball");
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
            int meganum = Integer.valueOf(megaballnum);



            if ( ArrayUtils.contains( n1, number1 )  ) {

                count = count + 1;
            }


            if ( ArrayUtils.contains( n1, number2 ) && number2 != number1 && number2 != number3 && number2 != number4 && number2 != number5) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number3 ) && number3 != number1 && number3 != number2 && number3 != number4 && number3 != number5 ) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number4 ) && number4 != number1 && number4 != number3 && number4 != number2 && number4 != number5) {
                count = count + 1;
            }

            if ( ArrayUtils.contains( n1, number5 ) && number5 != number1 && number5 != number2 && number5 != number3 && number5 != number4) {
                count = count + 1;
            }

            if (mega == meganum) {
                mb = mb + 1;
            }





            user = auth.getCurrentUser();

            if(count == 0 && mb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarmega2_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(2);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);





            }

            else if(count == 1 && mb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar4mega_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(4);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }
            else if(count == 2 && mb == 1 || count == 3 && mb == 0)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarmega10_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(10);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);


            }
            else if(count == 3 && mb == 1 )
            {
                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar200_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(200);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }

            else if(count == 4 && mb == 0)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar500_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(500);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);


            }


            else if(count == 4 && mb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollar10k_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(10000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }

            else if(count == 5 && mb == 0)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarmega1mil_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(1000000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }

            else if(count == 5 && mb == 1)
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarmegajackpot_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

                balance = balance + Long.valueOf(74000000);
                mDatabase.child("users").child(user.getUid().toString()).child("balance").setValue(balance);

            }


            else
            {

                AlertDialog.Builder d7builder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
                View dollar7 = getLayoutInflater().inflate(R.layout.dollarmega0_layout, null);
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
                currentnumpb.setText(megaballnum);

                d7builder.setView(dollar7);
                AlertDialog dialog = d7builder.create();
                dialog.show();

            }


            count = 0;
            mb = 0;






        }
    }





    private class CheckPrevious extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ResultCheckMegaMillions.this);
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

            String megaballnum= "";


            if (pd.isShowing()){
                pd.dismiss();
            }


            try {
                JSONArray  jArray = new JSONArray(result);
                JSONObject OneObject = jArray.getJSONObject(1);
                result = OneObject.getString("winning_numbers");
                megaballnum = OneObject.getString("mega_ball");
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



            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ResultCheckMegaMillions.this);
            View mView = getLayoutInflater().inflate(R.layout.oldresultmega_layout, null);
            TextView previous1 = (TextView) mView.findViewById(R.id.previousnum1);
            TextView previous2 = (TextView) mView.findViewById(R.id.previousnum2);
            TextView previous3 = (TextView) mView.findViewById(R.id.previousnum3);
            TextView previous4 = (TextView) mView.findViewById(R.id.previousnum4);
            TextView previous5 = (TextView) mView.findViewById(R.id.previousnum5);
            TextView previousmb = (TextView) mView.findViewById(R.id.previousnum6);

            previous1.setText(first);
            previous2.setText(second);
            previous3.setText(third);
            previous4.setText(fourth);
            previous5.setText(fifth);
            previousmb.setText(megaballnum);

            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();




        }
    }













}
