package edu.temple.grpr

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

/**
 * A helper class to store all functions relating to:
 * API Control
 * User Management
 */
class Helper {

    object api {

        val ENDPOINT_GROUP = "group.php"
        val ENDPOINT_USER = "account.php"

        val API_BASE = "https://kamorris.com/lab/grpr/"

        interface Response {
            fun processResponse(response: JSONObject)
        }

        fun createAccount(context: Context, user: User, password: String, response: Response?){
            val params = mutableMapOf(
                Pair("action", "REGISTER"),
                Pair("username", user.username),
                Pair("password", password),
                Pair("firstname", user.firstname!!),
                Pair("lastname", user.lastname!!)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }

        fun login(context: Context, user: User, password: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "LOGIN"),
                Pair("username", user.username),
                Pair("password", password)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }

        fun createGroup(context: Context, user: User, sessionKey: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "CREATE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun closeGroup(context: Context, user: User, sessionKey: String, groupId: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "CLOSE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("group_id", groupId)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun queryStatus(context: Context, user:User, sessionKey: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "QUERY"),
                Pair("username", user.username),
                Pair("session_key", sessionKey)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun uploadToken(context: Context, user:User, sessionKey: String, token: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "UPDATE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("fcm_token", token)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }

        fun updateLocation(context: Context, user:User, sessionKey: String, latLng: LatLng, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "UPDATE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("group_id", Helper.user.getGroupId(context)!!),
                Pair("latitude", latLng.latitude.toString()),
                Pair("longitude", latLng.longitude.toString())
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun joinGroup(context: Context, user:User, sessionKey: String, groupId:String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "JOIN"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("group_id", groupId)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun leaveGroup(context: Context, user:User, sessionKey: String, groupId: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "LEAVE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("group_id", groupId)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        private fun makeRequest(context: Context, endPoint: String, params: MutableMap<String, String>, responseCallback: Response?) {
            Volley.newRequestQueue(context)
                .add(object: StringRequest(Request.Method.POST, API_BASE + endPoint, {
                    Log.d("Server Response", it)
                    responseCallback?.processResponse(JSONObject(it))
                }, {}){
                    override fun getParams(): MutableMap<String, String> {
                            return params;
                    }
                })
        }

        fun isSuccess(response: JSONObject): Boolean {
            return response.getString("status").equals("SUCCESS")
        }

        fun getErrorMessage(response: JSONObject): String {
            return response.getString("message")
        }

    }

    object user {
        private val SHARED_PREFERENCES_FILE = "shared_prefs"
        private val KEY_SESSION_KEY = "session_key"
        private val KEY_USERNAME = "username"
        private val KEY_FIRSTNAME = "firstname"
        private val KEY_LASTNAME = "lastname"
        private val KEY_GROUP_ID = "group_id"
        private val KEY_TOKEN = "fcm_token"

        fun saveSessionData(context: Context, sessionKey: String) {
            getSP(context).edit()
                .putString(KEY_SESSION_KEY, sessionKey)
                .apply()
        }

        fun saveGroupId(context: Context, groupId: String) {
            getSP(context).edit()
                .putString(KEY_GROUP_ID, groupId)
                .apply()
        }

        fun getGroupId(context: Context): String? {
            return getSP(context).getString(KEY_GROUP_ID, null)
        }

        fun clearGroupId(context: Context) {
            getSP(context).edit().remove(KEY_GROUP_ID)
                .apply()
        }

        fun clearSessionData(context: Context) {
            getSP(context).edit().remove(KEY_SESSION_KEY)
                .apply()
        }

        fun getSessionKey(context: Context): String? {
            return getSP(context).getString(KEY_SESSION_KEY, null)
        }

        fun saveUser(context: Context, user: User) {
            getSP(context).edit()
                .putString(KEY_USERNAME, user.username)
                .putString(KEY_FIRSTNAME, user.firstname)
                .putString(KEY_LASTNAME, user.lastname)
                .apply()
        }

        fun get(context: Context) : User {
            return User (
                        getSP(context).getString(KEY_USERNAME, "")!!,
                        getSP(context).getString(KEY_FIRSTNAME, ""),
                        getSP(context).getString(KEY_LASTNAME, ""),
                    )
        }

        fun saveToken(context: Context, fcmToken: String) {
            getSP(context).edit()
                .putString(KEY_TOKEN, fcmToken)
                .apply()
        }

        @SuppressLint("ApplySharedPref")
        fun clearToken(context: Context) {
            getSP(context).edit()
                .remove(KEY_TOKEN)
                .commit()
        }

        fun getToken(context: Context): String? {
            return getSP(context).getString(KEY_TOKEN, null)
        }

        private fun getSP (context: Context) : SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        }

        fun registerTokenFlow(context: Context, token: String) {
            if (getToken(context).isNullOrEmpty() && getSessionKey(context) != null) {
                api.uploadToken(
                    context,
                    get(context),
                    getSessionKey(context)!!,
                    token,
                    object: api.Response {
                        override fun processResponse(response: JSONObject) {
                            saveToken(context, token)
                        }

                    }
                )
            }
        }
    }


}