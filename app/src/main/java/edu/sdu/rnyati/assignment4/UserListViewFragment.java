package edu.sdu.rnyati.assignment4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by raghavnyati on 3/17/17.
 */

public class UserListViewFragment extends Fragment{

    SparseArray<Group> groups = new SparseArray<Group>();
    int id, year;
    String nickname, country, state, city;
    double latitude, longitude;
    View showUserView;
    String url;

    public UserListViewFragment(){}

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        showUserView = inflater.inflate(R.layout.fragment_list_view, container, false);

        if(null != getArguments()){
            url = getArguments().getString("url");
        }
        return showUserView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllUsers();
    }

    public void getAllUsers() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("rag", "inResponse");
                for (int i = 0; i < response.length(); i++) {
                    JSONObject rec = null;
                    try {
                        rec = response.getJSONObject(i);
                        id = rec.getInt("id");
                        city = rec.getString("city");
                        state = rec.getString("state");
                        country = rec.getString("country");
                        nickname = rec.getString("nickname");
                        year = rec.getInt("year");
                        latitude = rec.getDouble("latitude");
                        longitude = rec.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Group group = new Group(String.valueOf(id) + ": " + nickname);
                    group.children.add("Year: " + String.valueOf(year)) ;
                    group.children.add("Country: " + country);
                    group.children.add("State: "  + state);
                    group.children.add("City: " + city);
                    group.children.add("Latitude: " + String.valueOf(latitude));
                    group.children.add("Longitude: " + String.valueOf(longitude) );
                    groups.append(i, group);
                }

                ExpandableListView listView = (ExpandableListView) showUserView.findViewById(R.id.listView1);
                MyExpandableListAdapter adapter = new MyExpandableListAdapter(getActivity(),groups);
                listView.setAdapter(adapter);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rag", "User List View Fragment Response Error.");
                error.printStackTrace();
            }
        };

        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(getRequest);
    }

}
