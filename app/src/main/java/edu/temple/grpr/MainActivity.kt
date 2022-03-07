package edu.temple.grpr

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), DashboardFragment.DashboardInterface, GroupFragment.GroupControlInterface {

    private var serviceIntent: Intent? = null
    private val grprViewModel : GrPrViewModel by lazy {
        ViewModelProvider(this).get(GrPrViewModel::class.java)
    }

    // Update ViewModel with location data whenever received from LocationService
    private var locationHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            grprViewModel.setLocation(msg.obj as LatLng)
        }
    }

    // Receiver to get data of group participants from FCM
    private val groupBroadcastReceiver = object: BroadcastReceiver () {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val participantArray = JSONArray(p1!!.getStringExtra(FCMService.UPDATE_KEY))
            val group = Group()
            var participantObject: JSONObject
            for (i in 0 until participantArray.length()) {
                participantObject = participantArray.getJSONObject(i)
                group.addParticipant(
                    Participant(
                        participantObject.getString("username"),
                        LatLng(
                            participantObject.getDouble("latitude"),
                            participantObject.getDouble("longitude")
                        )
                    )
                )
            }

            grprViewModel.setGroup(group)
        }
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

            // Provide service with handler
            (iBinder as LocationService.LocationBinder).setHandler(locationHandler)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        serviceIntent = Intent(this, LocationService::class.java)

        grprViewModel.getGroupId().observe(this) {
            if (!it.isNullOrEmpty())
                supportActionBar?.title = "GRPR - $it"
            else
                supportActionBar?.title = "GRPR"
        }

        Helper.user.getGroupId(this)?.run {
            grprViewModel.setGroupId(this)
            startLocationService()
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(groupBroadcastReceiver, IntentFilter(FCMService.UPDATE_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(groupBroadcastReceiver)
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("default", "Active Convoy", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun createGroup() {
        Helper.api.createGroup(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
            override fun processResponse(response: JSONObject) {
                if (Helper.api.isSuccess(response)) {
                    grprViewModel.setGroupId(response.getString("group_id"))
                    Helper.user.saveGroupId(this@MainActivity, grprViewModel.getGroupId().value!!)
                    startLocationService()
                } else {
                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    override fun endGroup() {
        AlertDialog.Builder(this).setTitle("Close Group")
            .setMessage("Are you sure you want to close the group?")
            .setPositiveButton("Yes"
            ) { _, _ -> Helper.api.closeGroup(
                this,
                Helper.user.get(this),
                Helper.user.getSessionKey(this)!!,
                grprViewModel.getGroupId().value!!,
                object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            grprViewModel.setGroupId("")
                            Helper.user.clearGroupId(this@MainActivity)
                            stopLocationService()
                        } else
                            Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                    }

                }
            )}
            .setNegativeButton("Cancel") { p0, _ -> p0.cancel() }
            .show()
    }

    override fun joinGroup() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_groupFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", true)
            })
    }

    override fun leaveGroup() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_groupFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", false)
            })
    }

    override fun logout() {
        Helper.user.clearSessionData(this)
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_loginFragment)
    }

    private fun startLocationService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

    override fun joinGroupFlow(groupId: String) {
        Helper.api.joinGroup(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            groupId,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.saveGroupId(this@MainActivity, groupId)
                    startLocationService()
                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

    override fun leaveGroupFlow(groupId: String) {
        Helper.api.leaveGroup(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            Helper.user.getGroupId(this)!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.clearGroupId(this@MainActivity)
                    stopLocationService()

                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

}