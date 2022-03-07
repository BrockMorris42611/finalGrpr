package edu.temple.grpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

// A single View Model is used to store all data we want to retain
// and/or observe
class GrPrViewModel : ViewModel() {

    // This property will store all users displayed in the map
    // and is not observed; as such it is not a LiveData
    private val participants by lazy {
        Group()
    }

    private val location by lazy {
        MutableLiveData<LatLng>()
    }

    private val groupId by lazy {
        MutableLiveData<String>()
    }

    private val group by lazy {
        MutableLiveData<Group>()
    }

    fun setGroupId(id: String) {
        groupId.value = id
    }

    fun setLocation(latLng: LatLng) {
        location.value = latLng
    }

    fun getLocation(): LiveData<LatLng> {
        return location
    }

    fun getGroupId(): LiveData<String> {
        return groupId
    }

    fun setGroup(group: Group) {
        participants.updateGroup(group)

        // Inform observers that a new group was provided
        this.group.value = group
    }

    // LiveData to observe
    fun getGroupToObserve(): LiveData<Group> {
        return group
    }

    // Actual data
    fun getGroup(): Group {
        return participants
    }

}