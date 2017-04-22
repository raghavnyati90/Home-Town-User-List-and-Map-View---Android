package edu.sdu.rnyati.assignment4;

import android.os.Bundle;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Geocoder;
import android.location.Address;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

/**
 * Created by raghavnyati on 3/17/17.
 */

public class UserMapViewFragment extends SupportMapFragment implements OnMapReadyCallback {

    GoogleMap mMap;

    int id;
    String nickname, country, state, city;
    double latitude, longitude;
    LatLng latLng;
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(null != getArguments()){
            url = getArguments().getString("url");
        }

         this.getFragmentManager().findFragmentById(R.id.map);
          this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker for all valid users
        getAllUsers();
    }

    public void getAllUsers() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject rec = null;
                    try {
                        rec = response.getJSONObject(i);
                        id = rec.getInt("id");
                        city = rec.getString("city");
                        state = rec.getString("state");
                        country = rec.getString("country");
                        nickname = rec.getString("nickname");
                        latitude = rec.getDouble("latitude");
                        longitude = rec.getDouble("longitude");
                        latLng = new LatLng(latitude, longitude);
                        if(latitude != 0 && longitude !=0){
                            mMap.addMarker(new MarkerOptions().position(latLng).title(id + " " + nickname));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        }else{
                            getUserLocation();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
              }
            };

            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.d("rag", "User Map Fragment Response Error");
                    error.printStackTrace();
                }
            };

            JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(getRequest);
    }

    private void getUserLocation(){
        if(country != null && state != null){
            String address = state + ", " + country;
            Geocoder locator = new Geocoder(this.getActivity());
            try{
                List<Address> state = locator.getFromLocationName(address, 1);
                for (Address stateLocation: state){
                    if(stateLocation.hasLatitude())
                        latitude = stateLocation.getLatitude();
                    if(stateLocation.hasLongitude())
                        longitude = stateLocation.getLongitude();
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.e("rag", "Address lookup Error.", e);
            }
            LatLng stateLatLng = new LatLng(latitude, longitude);
            CameraUpdate newLocation = CameraUpdateFactory.newLatLngZoom(stateLatLng, 6);
            mMap.addMarker(new MarkerOptions().position(stateLatLng).title(nickname));
            mMap.moveCamera(newLocation);
        }
    }
}
