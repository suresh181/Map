package com.my.mapactivity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.my.mapactivity.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleMap.OnMapClickListener,
    GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val multipleMarker: ArrayList<LatLng> = ArrayList()
    private val options = MarkerOptions()
    private var count = 0
    private lateinit var edittext:EditText

    private var locationUpdateState = false


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edittext=binding.etAddress
        locationRequest = LocationRequest().setInterval(10000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setSmallestDisplacement(10f)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)






        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        val fab = findViewById<FloatingActionButton>(R.id.fab)
//        fab.setOnClickListener {
//            loadPlacePicker()
//        }
        createLocationRequest()


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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val myPlace = LatLng(40.73, -73.99)
        mMap.addMarker(MarkerOptions().position(mMap.cameraPosition.target).title("My Favorite City").draggable(true))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraMoveListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        mMap.uiSettings.isZoomGesturesEnabled = false





        setUpMap()
        multipleMarker()
        getDeviceCenterLocation()


    }

    private fun multipleMarker() {
        multipleMarker.add(LatLng(13.0583, 80.2478))
        multipleMarker.add(LatLng(13.0105, 80.2206))
        multipleMarker.add(LatLng(12.6208, 80.1945))
        multipleMarker.add(LatLng(13.0473, 80.0945))
        multipleMarker.add(LatLng(13.0332, 80.2712))
        multipleMarker.add(LatLng(13.0555, 80.2581))
        multipleMarker.add(LatLng(13.0735, 80.2214))
        multipleMarker.add(LatLng(13.0500, 80.2121))
        multipleMarker.add(LatLng(13.0694, 80.1948))
        multipleMarker.add(LatLng(12.9905, 80.2170))
        multipleMarker.add(LatLng(13.0585, 80.2642))

        for (point in multipleMarker) {

            options.position(point)
            options.title(getAddress(point))
            options.snippet("someDesc")
            mMap.addMarker(options.title(getAddress(point)))
        }
    }

    private fun getDeviceCenterLocation() {
        mMap.setOnCameraIdleListener{

                val position = mMap.cameraPosition.target
//                mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.mipmap.ic_user_location))))
                Log.d("centerLocation", "Position: $position")
            val lat = mMap.cameraPosition.target.latitude
            val lan = mMap.cameraPosition.target.longitude
            val result = StringBuilder()
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lan, 1)
                Log.e("TAG", "address: $addresses")
                if (addresses.size > 0) {
                    val address = addresses[0]
                    result.append(address.getAddressLine(0)).append(",")
//                    result.append(address.locality).append(",")
//                    result.append(address.adminArea).append(",")
//                    result.append(address.countryName).append(",")

                    val add: Address = addresses.get(0)
                    edittext.setText(result)


                }
            } catch (e: IOException) {
                e.message?.let { Log.e("tag", it) }
            }


        }

//        mMap.setOnCameraChangeListener(object: GoogleMap.OnCameraChangeListener{
//            override fun onCameraChange(p0: CameraPosition) {
//                val position = mMap.cameraPosition.target
//            }
//        })
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {


        val markerOptions = MarkerOptions().position(location)

//        val titleStr = getAddress(location)
//        markerOptions.title(titleStr)

//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.mipmap.ic_user_location)))
        mMap.addMarker(markerOptions)
    }

    @SuppressLint("MissingPermission")
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = false


        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                placeMarkerOnMap(currentLatLng)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    private fun getAddress(latLng: LatLng): String {

        var addressText = ""

        val lat = mMap.cameraPosition.target.latitude
        val lan = mMap.cameraPosition.target.longitude
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lan, 1)
            Log.e("TAG", "address: $addresses")
            if (addresses.size > 0) {
                val address = addresses[0]
                result.append(address.getAddressLine(0)).append(",")
//                    result.append(address.locality).append(",")
//                    result.append(address.adminArea).append(",")
//                    result.append(address.countryName).append(",")

                val add: Address = addresses.get(0)
                addressText = "$result"
                edittext.setText(addressText)


            }
        } catch (e: IOException) {
            e.message?.let { Log.e("tag", it) }
        }

        return addressText
    }

    @SuppressLint("MissingPermission")


    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest()

        locationRequest.interval = 0

        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)


        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->

            if (e is ResolvableApiException) {

                try {

                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText = addressText.plus("\n" + place.address.toString())

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                createLocationRequest()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    override fun onMarkerClick(p0: Marker) = false


    override fun onMapClick(point: LatLng) {

        if (mMap != null) {
            Log.d("TAG", "onMapClick: kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk")
            var color = BitmapDescriptorFactory.HUE_GREEN.toString()
            val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            var dbcolor = sharedPreference.getString("Colors", "")
            //click marker color change Repeated Green , Blue , Magenta
            count += 1
            if (count % 3 == 1) {
                color = "GREEN"
                dbcolor = "BLUE"
                mMap.addMarker(point.let {
                    MarkerOptions().title(getAddress(point))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .position(point)
                })
            } else if (count % 3 == 2) {
                color = "BLUE"
                dbcolor = "MEGENTA"
                mMap.addMarker(point.let {
                    MarkerOptions().title(getAddress(point))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        .position(point)
                })
            } else if (count % 3 != 3) {
                color = "MEGENTA"
                dbcolor = "GREEN"
                mMap.addMarker(point.let {
                    MarkerOptions().title(getAddress(point))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(point)
                })
            }
        }
    }

    override fun onCameraIdle() {

    }

    override fun onCameraMove() {
        val i = 0
        val bounds = mMap.projection.visibleRegion.latLngBounds

    }
}