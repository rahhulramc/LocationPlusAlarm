package com.example.abhisheikh.locationplusalarm.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;
import com.example.abhisheikh.locationplusalarm.activity.EditAlarmActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GoogleMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GoogleMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    MapFragment mapFragment;
    GoogleMap m_map;
    boolean mapReady = false;
    Circle circle;
    Marker marker;
    Location myLocation;
    Context context;
    GoogleApiClient mGoogleApiClient;
    Button searchLocationButton, setLocationButton;
    ImageView searchImage;
    EditText searchLocationEditText;
    LinearLayout searchLinearLayout;
    LatLng searchedLocation;

    private OnFragmentInteractionListener mListener;

    public GoogleMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GoogleMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GoogleMapFragment newInstance(String param1, String param2) {
        GoogleMapFragment fragment = new GoogleMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_google_map, container, false);

        context = getActivity();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        setIDs(view);
        setOnClickListeners();

        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    private void setIDs(View view){
        searchLocationButton = (Button)view.findViewById(R.id.searchLocationButton);
        setLocationButton = (Button)view.findViewById(R.id.setLocationButton);
        searchImage = (ImageView) view.findViewById(R.id.searchImage);
        searchLocationEditText = (EditText)view.findViewById(R.id.searchEditText);
        searchLinearLayout = (LinearLayout)view.findViewById(R.id.searchLinearLayout);
    }

    private void setOnClickListeners(){
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchImage.setVisibility(View.GONE);
                searchLinearLayout.setVisibility(View.VISIBLE);
                searchLocationEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchLocationEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        searchLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circle != null)
                    circle.remove();
                if (marker != null)
                    marker.remove();
                String url = "http://maps.google.com/maps/api/geocode/json?address=" +
                        searchLocationEditText.getText().toString()+ "&sensor=false";
                new FetchLatLng().execute(url);
                setLocationButton.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });

        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(searchedLocation.latitude, searchedLocation.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String locationName = addresses.get(0).getFeatureName() + ", " +
                        addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " +
                        addresses.get(0).getCountryName();
                Alarm alarm = new Alarm(searchedLocation, locationName);

                if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                //Toast.makeText(context,""+mGoogleApiClient.isConnected(),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getContext(), EditAlarmActivity.class);
                intent.putExtra("alarm", alarm);
                startActivityForResult(intent, 1);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        m_map = googleMap;
        setMapClickListeners();
    }

    private void setMapClickListeners() {
        m_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                searchImage.setVisibility(View.VISIBLE);
                searchLinearLayout.setVisibility(View.GONE);
                setLocationButton.setVisibility(View.VISIBLE);
                searchedLocation = latLng;
                if (circle != null)
                    circle.remove();
                if (marker != null)
                    marker.remove();
                MarkerOptions mMarkerOption = new MarkerOptions().position(latLng);
                CircleOptions mCircleOption = new CircleOptions()
                        .center(latLng)
                        .fillColor(getResources().getColor(R.color.circleFill))
                        .strokeColor(getResources().getColor(R.color.circleStroke))
                        .radius(100);
                marker = m_map.addMarker(mMarkerOption);
                circle = m_map.addCircle(mCircleOption);

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (!addresses.isEmpty()) {
                        Toast.makeText(getContext(), addresses.get(0).getFeatureName() + ", " +
                                addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " +
                                addresses.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        m_map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String locationName = addresses.get(0).getFeatureName() + ", " +
                        addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " +
                        addresses.get(0).getCountryName();
                Alarm alarm = new Alarm(latLng, locationName);

                if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                //Toast.makeText(context,""+mGoogleApiClient.isConnected(),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getContext(), EditAlarmActivity.class);
                intent.putExtra("alarm", alarm);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Alarm alarm = data.getParcelableExtra("alarm");
                String position = data.getStringExtra("position");
                Intent intent = new Intent();
                intent.putExtra("alarm", alarm);
                intent.putExtra("position", position);
                ((Activity) context).setResult(RESULT_OK, intent);
                ((Activity) context).finish();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(myLocation!=null) {
            if(searchedLocation==null) {
                LatLng location = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions().position(location);
                CameraPosition target = CameraPosition.builder().target(location).zoom(10).build();
                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                if (marker != null)
                    marker.remove();
                marker = m_map.addMarker(markerOptions);
            }
            else{
                MarkerOptions markerOptions = new MarkerOptions().position(searchedLocation);
                CameraPosition target = CameraPosition.builder().target(searchedLocation).zoom(10).build();
                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                if (marker != null)
                    marker.remove();
                marker = m_map.addMarker(markerOptions);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    private class FetchLatLng extends AsyncTask<String,Void,Void> {
        private HttpClient Client;
        private String Content;
        private String Error = null;

        @Override
        protected Void doInBackground(String... urls) {
            try {

                // Call long running operations here (perform background computation)
                // NOTE: Don't call UI Element here.

                // Server url call by GET method
                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Client = new DefaultHttpClient();
                Content = Client.execute(httpget, responseHandler);

            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            searchedLocation = jsonParse(Content);
            MarkerOptions markerOptions = new MarkerOptions().position(searchedLocation);
            if(marker!=null){
                marker.remove();
            }
            CameraPosition target = CameraPosition.builder().target(searchedLocation).zoom(10).build();
            m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
            marker = m_map.addMarker(markerOptions);
        }

        private LatLng jsonParse(String Content){
            LatLng latLng = null;
            try {
                JSONObject baseOject = new JSONObject(Content);
                String status = baseOject.getString("status");
                if(status.equals("OK")){
                    JSONArray results= baseOject.getJSONArray("results");
                    JSONObject resultObj = results.getJSONObject(0);
                    JSONObject loc = resultObj.getJSONObject("geometry").getJSONObject("location");
                    double lat = loc.getDouble("lat");
                    double lng = loc.getDouble("lng");
                    latLng = new LatLng(lat,lng);

//                    Toast.makeText(getContext(),"Lat: "+lat+" Lng: "+lng,LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return latLng;
        }
    }

}
