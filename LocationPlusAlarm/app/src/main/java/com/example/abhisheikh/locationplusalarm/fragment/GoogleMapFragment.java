package com.example.abhisheikh.locationplusalarm.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    MapFragment mapFragment;
    GoogleMap m_map;
    boolean mapReady = false;
    Circle circle;
    Marker marker;
    Location myLocation;
    Context context;
    GoogleApiClient mGoogleApiClient;

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

        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
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

    private void setMapClickListeners(){
        m_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(circle!=null)
                    circle.remove();
                if(marker!=null)
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
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(!addresses.isEmpty()){
                        Toast.makeText(getContext(),addresses.get(0).getFeatureName() + ", " +
                                addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " +
                                addresses.get(0).getCountryName(),Toast.LENGTH_SHORT).show();
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
                    addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String locationName = addresses.get(0).getFeatureName() + ", " +
                        addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " +
                        addresses.get(0).getCountryName();
                Alarm alarm = new Alarm(latLng, locationName);
                Intent intent = new Intent(getContext(),EditAlarmActivity.class);
                intent.putExtra("alarm",alarm);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                Alarm alarm = data.getParcelableExtra("alarm");
                String position = data.getStringExtra("position");
                Intent intent = new Intent();
                intent.putExtra("alarm",alarm);
                intent.putExtra("position",position);
                ((Activity)context).setResult(RESULT_OK,intent);
                ((Activity)context).finish();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng location = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        CameraPosition target = CameraPosition.builder().target(location).zoom(10).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
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

}
