package edu.temple.grpr

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Stream

class DashboardFragment : Fragment() {

    lateinit var fab: FloatingActionButton; lateinit var fabRecording: FloatingActionButton
    var mr : MediaRecorder? = null;         var isRecording = false;
    lateinit var FILENAME : String;         var myFile : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Let the system know that this fragment wants to contribute to the app menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val layout =  inflater.inflate(R.layout.fragment_dashboard, container, false)
        fab = layout.findViewById(R.id.startFloatingActionButton)
        fabRecording = layout.findViewById(R.id.fabRecording)

        // Query the server for the current Group ID (if available)
        // and use it to close the group
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java).getGroupId().observe(requireActivity()) {
            if(!it.isNullOrEmpty())
            fabRecording.setOnClickListener {
                if (!isRecording) {
                    startRecording()
                    fabRecording.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#e91e63"))
                } else {
                    stopRecording()
                    fabRecording.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                }
            }
        }
        fab.setOnLongClickListener {
            Helper.api.queryStatus(requireContext(),
            Helper.user.get(requireContext()),
            Helper.user.getSessionKey(requireContext())!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.api.closeGroup(requireContext(),
                        Helper.user.get(requireContext()),
                        Helper.user.getSessionKey(requireContext())!!,
                        response.getString("group_id"),
                        null)
                }
            })
            true
        }

        layout.findViewById<View>(R.id.startFloatingActionButton)
            .setOnClickListener{
                (activity as DashboardInterface).createGroup()
            }

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Use ViewModel to determine if we're in an active Group
        // Change FloatingActionButton behavior depending on if we're
        // currently in a group
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java).getGroupId().observe(requireActivity()) {
            if (it.isNullOrEmpty()) {
                fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                fab.setImageResource(android.R.drawable.ic_input_add)
                fab.setOnClickListener {(activity as DashboardInterface).createGroup()}

                fabRecording.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#D3D3D3"))
            } else {
                fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                fab.setOnClickListener {(activity as DashboardInterface).endGroup()}
            }

        }
    }

    private fun startRecording() {
        //FILENAME = "file_brock_" + SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.US).format(Date()) +".3gp"
        FILENAME = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.US).format(Date()).toString() + "file_brock.3gp"
        mr = MediaRecorder()
        myFile = File(context?.filesDir, FILENAME)
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java)
            .audioMssgList.add(
            AudioMssg(myFile!!, Helper.user.get(requireContext()).username, SimpleDateFormat("hh:mm:ss", Locale.US).format(Date()).toString())
        )
        mr?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mr?.setOutputFile(myFile)
        mr?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) //AMR_NB
        mr?.prepare(); mr?.start(); isRecording = true
        Toast.makeText(requireContext(), "Recording started!", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording(){
        mr?.stop();mr?.release();mr = null
        isRecording = false
        Toast.makeText(requireContext(), "You are not recording right now!", Toast.LENGTH_SHORT).show()
    }
    // This fragment places a menu item in the app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard, menu)
        menu.findItem(R.id.action_join_group).isVisible = Helper.user.getGroupId(requireContext()).isNullOrBlank()
        menu.findItem(R.id.action_leave_group).isVisible = !Helper.user.getGroupId(requireContext()).isNullOrBlank()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_logout -> {
                (activity as DashboardInterface).logout()
                return true
            }
            R.id.action_join_group -> {
                (activity as DashboardInterface).joinGroup()
                return true
            }
            R.id.action_leave_group -> {
                (activity as DashboardInterface).leaveGroup()
                return true
            }
            R.id.action_records -> {
                (activity as DashboardInterface).seeRecords()
                return true
            }
        }
        return false
    }

    interface DashboardInterface {
        fun createGroup()
        fun endGroup()
        fun joinGroup()
        fun leaveGroup()
        fun logout()
        fun seeRecords()
    }

}