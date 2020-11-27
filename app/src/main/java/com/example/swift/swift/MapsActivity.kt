package com.example.swift.swift

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.swift.swift.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.swift.swift.PermissionUtils.isPermissionGranted
import com.example.swift.swift.PermissionUtils.requestPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var permissionDenied = false
    private var locationPermissionGranted = false
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest: LocationRequest

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    var mapView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        /*  val locationButton = (mapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View)
                 .findViewById<View>(Integer.parseInt("2"))
             val layoutParams =
                 locationButton.layoutParams as RelativeLayout.LayoutParams
             layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
             layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
             layoutParams.setMargins(0, 0, 20, 20)
     */
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.setAllGesturesEnabled(true)

        enableMyLocation()

        if (ActivityCompat.checkSelfPermission(
                        this,
                        ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.isMyLocationEnabled = true
        mMap.isTrafficEnabled = false
        mMap.isIndoorEnabled = false
        mMap.isBuildingsEnabled = true
        mMap.isTrafficEnabled = false
        // mGoogleMap.setIndoorEnabled(false);
        // mGoogleMap.setBuildingsEnabled(true);
        mMap.uiSettings.isZoomControlsEnabled = false




        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
        val locationResult = mFusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                // Set the map's camera position to the current location of the device.
                val location = task.result
                if (location != null) {
                    val place= LatLng(location.latitude,
                            location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            place, 16f))

                    mMap.addMarker(MarkerOptions().position(place).title("Current Location")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar)))

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(place))
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom((place), 16f)
                    //Animate the zoom using the animateCamera() method
                    //Animate the zoom using the animateCamera() method
                    googleMap.animateCamera(cameraUpdate)
                    val fab: FloatingActionButton =
                            findViewById<View>(R.id.myLocationButton) as FloatingActionButton
                    fab.setOnClickListener {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                place, 16f))

                    }

                }
                else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)

                }

            }
        }

    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun  enableMyLocation() {
        if (!::mMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]


    // [END maps_current_place_get_device_location]
    /**
     * Shows a [Snackbar].
     *
     * @param snackStrId The id for the string resource for the Snackbar text.
     * @param actionStrId The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
            snackStrId: Int,
            actionStrId: Int = 0,
            listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId),
                LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }
    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults,  ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            permissionDenied = true
            // [END_EXCLUDE]
            showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", "com.example.swift.swift", null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    })
        }
        updateLocationUI()
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                enableMyLocation()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_update_location_ui]
    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private val TAG = MapsActivity::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 16
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}