package edu.temple.grpr

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject

class DashboardFragment : Fragment() {

    lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let the system know that this fragment
        // wants to contribute to the app menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout =  inflater.inflate(R.layout.fragment_dashboard, container, false)

        fab = layout.findViewById(R.id.startFloatingActionButton)

        // Query the server for the current Group ID (if available)
        // and use it to close the group
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
            } else {
                fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                fab.setOnClickListener {(activity as DashboardInterface).endGroup()}
            }

        }
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
        }
        return false
    }

    interface DashboardInterface {
        fun createGroup()
        fun endGroup()
        fun joinGroup()
        fun leaveGroup()
        fun logout()
    }

}