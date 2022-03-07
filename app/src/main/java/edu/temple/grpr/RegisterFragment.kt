package edu.temple.grpr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import org.json.JSONObject

class RegisterFragment : Fragment() {

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        layout = inflater.inflate(R.layout.fragment_register, container, false)
        val usernameEditText = layout.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = layout.findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = layout.findViewById<EditText>(R.id.confirmPasswordEditText)
        val firstnameEditText = layout.findViewById<EditText>(R.id.firstnameEditText)
        val lastnameEditText = layout.findViewById<EditText>(R.id.lastnameEditText)

        layout.findViewById<Button>(R.id.createAccountButton)
            .setOnClickListener{

                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val passwordConfirm = confirmPasswordEditText.text.toString()
                val firstname = firstnameEditText.text.toString()
                val lastname = lastnameEditText.text.toString()

                if (password.equals(passwordConfirm)) {

                    Helper.api.createAccount(
                        requireContext(),
                        User(
                            username,
                            firstname,
                            lastname
                        ),
                        password,

                        object : Helper.api.Response {
                            override fun processResponse(response: JSONObject) {
                                if (Helper.api.isSuccess(response)) {
                                    Helper.user.saveSessionData(
                                        requireContext(),
                                        response.getString("session_key")
                                    )
                                    Helper.user.saveUser(
                                        requireContext(), User(
                                            username,
                                            firstname,
                                            lastname
                                        )
                                    )
                                    goToDashboard()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        Helper.api.getErrorMessage(response),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "Please ensure your passwords match", Toast.LENGTH_SHORT).show()
                }
            }
        return layout
    }

    private fun goToDashboard() {
        Navigation
            .findNavController(layout)
            .navigate(R.id.action_registerFragment_to_dashboardFragment)
    }

}