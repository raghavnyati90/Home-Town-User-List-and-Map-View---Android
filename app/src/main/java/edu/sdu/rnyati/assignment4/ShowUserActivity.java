package edu.sdu.rnyati.assignment4;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ShowUserActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    Bundle bundle;

    String country, state, yearText;
    Spinner countrySpinner, stateSpinner, yearSpinner;
    boolean isListView;
    String url;
    int newStrLength;
    TextView filterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        countrySpinner = (Spinner) findViewById(R.id.countryFilter);
        stateSpinner = (Spinner) findViewById(R.id.stateFilter);
        yearSpinner = (Spinner) findViewById(R.id.yearFilter);
        filterTextView = (TextView) findViewById(R.id.filterText);

        url = "http://bismarck.sdsu.edu/hometown/users?reverse=true";

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

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setYearFilter();
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearText = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        isListView = true;
        bundle = new Bundle();
        bundle.putString("url", url);
        UserListViewFragment frag = new UserListViewFragment();
        frag.setArguments(bundle);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_holder, frag).commit();
    }

    public void onApplyFilter(View v){
        Log.d("CountryStateYear", country + state + yearText);

        if (country == "All Countries" && yearText == "All Years") {
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true";
            filterTextView.setText("Filter not applied.");
        }
        else if(country != "All Countries" && state == "All States" && yearText == "All Years") {
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + country;
            filterTextView.setText("Filter applied");
        }
        else if(country != "All Countries" && state != "All States" && yearText == "All Years") {
            filterTextView.setText("Filter applied");
            state = state.replaceAll(" ", "%20");
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + country + "&state=" + state;
        }
        else if(country != "All Countries" && state != "All States" && yearText != "All Years") {
            state = state.replaceAll(" ", "%20");
            filterTextView.setText("Filter applied");
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + country + "&state=" + state + "&year=" + yearText;
        }
        else if(country != "All Countries" && state == "All States" && yearText != "All Years") {
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + country + "&year=" + yearText;
            filterTextView.setText("Filter applied");
        }
        else if(country == "All Countries" && yearText != "All Years") {
            url = "http://bismarck.sdsu.edu/hometown/users?year=" + yearText;
            filterTextView.setText("Filter applied");
        }

        if(isListView){
            bundle = new Bundle();
            bundle.putString("url", url);
            UserListViewFragment frag = new UserListViewFragment();
            frag.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.fragment_holder, frag).commit();
        }else{
            bundle = new Bundle();
            bundle.putString("url", url);
            UserMapViewFragment fragment = new UserMapViewFragment();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.fragment_holder, fragment).commit();
        }
    }


    public void setYearFilter(){
        String yearStr[] = new String[49];
        int startYear = 1970;
        yearStr[0] = "All Years";
        for(int i = 1; i<=48; i++){
            yearStr[i] = String.valueOf(startYear);
            startYear++;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this, android.R.layout.simple_spinner_item, yearStr);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(dataAdapter);
    }


    public void getCountriesRequest() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = convertToArray(response.toString());
                newStrLength = strings.length + 1;
                String newStr[] = new String[newStrLength];
                newStr[0] = "All Countries";
                for(int i=1; i<newStr.length; i++){
                    newStr[i] = strings[i-1];
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this, android.R.layout.simple_spinner_item, newStr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(dataAdapter);
            }
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

    public void getStateRequest(final String selectedCountry) {

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                if (selectedCountry != "All Countries") {
                    String strings[] = convertToArray(response.toString());
                    newStrLength = strings.length + 1;
                    String newStr[] = new String[newStrLength];
                    newStr[0] = "All States";
                    for(int i=1; i<newStr.length; i++){
                        newStr[i] = strings[i-1];
                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this, android.R.layout.simple_spinner_item, newStr);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stateSpinner.setAdapter(dataAdapter);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        if (selectedCountry != "All Countries") {
            String url = "http://bismarck.sdsu.edu/hometown/states?country="+selectedCountry;
            JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(getRequest);
        } else {
            stateSpinner.setAdapter(null);
            List<String> spinnerArray =  new ArrayList<>();
            spinnerArray.add("All States");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stateSpinner.setAdapter(adapter);
        }
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

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.listViewMenu:
                isListView = true;
                bundle = new Bundle();
                bundle.putString("url", url);
                UserListViewFragment frag = new UserListViewFragment();
                frag.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.fragment_holder, frag).commit();
                return true;
            case R.id.mapViewMenu:
                isListView = false;
                bundle = new Bundle();
                bundle.putString("url", url);
                UserMapViewFragment fragment = new UserMapViewFragment();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.fragment_holder, fragment).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

}
