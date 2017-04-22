package edu.sdu.rnyati.assignment4;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddUserActivity extends AppCompatActivity {

    private static final int INTENT_MAP = 123;

    TextView latTextView, longTextView;
    EditText nicknameText, passwordText, cityText, yearText;
    Spinner countrySpinner, stateSpinner;
    String nickName, password, city, country, state, checkYear;
    int year;
    double latitutde, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        latTextView = (TextView) findViewById(R.id.latTextView);
        longTextView = (TextView) findViewById(R.id.longTextView);
        yearText = (EditText) findViewById(R.id.yearEditText);
        nicknameText = (EditText) findViewById(R.id.nicknameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        cityText = (EditText) findViewById(R.id.cityText);
        countrySpinner = (Spinner) findViewById(R.id.countrySpinner);
        stateSpinner = (Spinner) findViewById(R.id.stateSpinner);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getCountriesRequest();
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country = parent.getItemAtPosition(position).toString();
                getStateRequest(country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public String[] convertToArray(String listString)
    {
        String[] list = listString.split("\",\"");
        String[] realList = new String[list.length+1];
        realList[0]=" ";
        int lastElement = list.length-1;
        list[0]=list[0].substring(2);
        list[lastElement]=list[lastElement].substring(0,list[lastElement].length()-2);
        for (int i=1;i<list.length;i++) {
            realList[i]=list[i-1];
        }
        return list;
    }

    public void getCountriesRequest() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = convertToArray(response.toString());
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(AddUserActivity.this, android.R.layout.simple_spinner_item, strings);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(dataAdapter);
            }
            //return countryArray;
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        String url = "http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public void getStateRequest(String selectedCountry) {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = convertToArray(response.toString());
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(AddUserActivity.this, android.R.layout.simple_spinner_item, strings);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(dataAdapter);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String url = " http://bismarck.sdsu.edu/hometown/states?country="+selectedCountry;
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public void postReq() {
        JSONObject data = new JSONObject();
        try {
            data.put("nickname", nickName);
            data.put("password", password);
            data.put("country", country);
            data.put("state", state);
            data.put("city", city);
            data.put("year", year);
            if(!latTextView.getText().equals("") && !longTextView.getText().equals("")){
                data.put("latitude", latitutde);
                data.put("longitude", longitude);
            }
        } catch (JSONException error) {
            Log.e("rag", "JSON error", error);
            return;
        }

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                createDialog("Success", "User created successfully.", false);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rag", "post fail " + new String(error.networkResponse.data));
                createDialog("Uh oh!", new String(error.networkResponse.data), true);
            }
        };
        String url = "http://bismarck.sdsu.edu/hometown/adduser";
        JsonObjectRequest postRequest = new JsonObjectRequest(url, data, success, failure);
        VolleyQueue.instance(this).add(postRequest);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == INTENT_MAP){
            switch (resultCode) {
                case RESULT_OK:
                    latitutde = data.getDoubleExtra("lat", -1);
                    longitude = data.getDoubleExtra("long", -1);
                    latTextView.setText(String.valueOf(latitutde));
                    longTextView.setText(String.valueOf(longitude));
                    break;
                case RESULT_CANCELED:
                    break;
            }
        } else {
            return;
        }
    }

    public void locateUserClicked(View v){
        Intent setLocation = new Intent(this, MapsActivity.class);
        setLocation.putExtra("country", country);
        setLocation.putExtra("state", state);
        setLocation.putExtra("nickname", nickName);
        setLocation.putExtra("city", city);
        startActivityForResult(setLocation, INTENT_MAP);
    }


    private boolean checkYear(){
        checkYear = yearText.getText().toString();
        if (checkYear.matches("")) {
            createDialog("Uh oh!","Enter year.", true);
            return false;
        }
        else {
            year = Integer.parseInt(yearText.getText().toString());

            if (year > 2017 || year < 1970) {
                createDialog("Sorry!","Enter year between 1970 and 2017.", true);
                return  false;
            }
            else {
                return true;
            }
        }
    }

    private boolean checkNickName(){
        nickName = nicknameText.getText().toString();
        if (nickName.equals("")) {
            createDialog("Uh oh!","Enter Nickname. ", true);
            return false;
        }
        else {
            return true;
        }
    }

    public void addNewUserClicked(View v){
        nickName = nicknameText.getText().toString();
        password = passwordText.getText().toString();
        city = cityText.getText().toString();
        if(checkNickName()) {
            if(checkYear()) {
                year = Integer.parseInt(yearText.getText().toString());
                postReq();
            }
        }
    }

    private void createDialog(String title, String message, boolean isError){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
        if(isError){
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        }else{
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    finish();
                }
            });
        }
        builder.setMessage(message);
        builder.setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
