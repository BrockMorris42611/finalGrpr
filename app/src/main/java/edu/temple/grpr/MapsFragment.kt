package edu.temple.grpr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsFragment : Fragment() {

    lateinit var map: GoogleMap
    lateinit var grprViewModel: GrPrViewModel
    var myMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grprViewModel = ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        // Update location on map whenever ViewModel is updated
        grprViewModel.getLocation()
            .observe(requireActivity()) {
                if (myMarker == null) myMarker = map.addMarker(
                    MarkerOptions().position(it)
                ) else myMarker?.setPosition(it)
            }

        // Add/update participants on map whenever ViewModel is updated
        grprViewModel.getGroupToObserve()
            .observe(requireActivity()) {
                val group = grprViewModel.getGroup()
                var participant: Participant
                val boundBuilder = LatLngBounds.Builder()
                for (i in 0 until group.size) {
                    participant = group.getParticipant(i)

                    //Filter out own marker from map, but include in BoundedBox
                    if (participant.username != Helper.user.get(requireContext()).username) {
                        if (participant.marker == null)
                            participant.marker = map.addMarker(
                                MarkerOptions().position(participant.latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_48))
                                    .title(participant.username)
                            )!!
                        else
                            participant.marker?.position = participant.latLng
                    }

                    boundBuilder.include(participant.latLng)
                }
                if (group.size > 0)
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 150))
            }
    }
}