package edu.temple.grpr

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class Participant (_username: String, _latLng: LatLng){

    val username = _username
    var latLng = _latLng
    var marker : Marker? = null

    fun removeMarker() {
        marker?.remove()
    }

    override fun equals(other: Any?): Boolean {
        return (other as Participant).username == username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

}