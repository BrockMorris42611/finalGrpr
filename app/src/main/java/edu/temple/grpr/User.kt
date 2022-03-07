package edu.temple.grpr

// Data class to store user info
// Using a User object even when only needing username makes code more maintainable
data class User (val username: String, val firstname: String?, val lastname: String?)
