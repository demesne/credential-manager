package `in`.demesne.credentialmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException // For callback type
import com.auth0.android.callback.Callback // For callback type
import com.auth0.android.provider.WebAuthProvider
import `in`.demesne.credentialmanager.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var account: Auth0 // For Auth0 operations

    // Use the same placeholders as in LoginActivity for consistency,
    // though only domain is strictly needed for logout redirect.
    private val auth0Domain = "YOUR_AUTH0_DOMAIN" 
    private val auth0ClientId = "YOUR_AUTH0_CLIENT_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        account = Auth0(auth0ClientId, auth0Domain) // Use clientID and domain from LoginActivity
        account.isOIDCConformant = true


        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        // TODO: Clear locally stored tokens first (e.g., from EncryptedSharedPreferences)
        // Example: TokenManager.clearCredentials(this)

        WebAuthProvider.logout(account)
            .withScheme("https") // Scheme of your callback URL in Auth0 dashboard (usually https)
            // .withReturnTo("YOUR_LOGOUT_CALLBACK_URL_IF_DIFFERENT_FROM_LOGIN") // Optional: if different, ensure it's in allowed list
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    Log.d("DashboardActivity", "Logout successful")
                    // Tokens are cleared by Auth0 session termination.
                    // Local stored tokens should also be cleared.
                    navigateToLogin()
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e("DashboardActivity", "Logout failed: \${error.getDescription()}")
                    // Still navigate to login, or show error and stay?
                    // Forcing logout locally and navigating is safer.
                    Toast.makeText(this@DashboardActivity, "Logout failed, but signing out locally.", Toast.LENGTH_LONG).show()
                    navigateToLogin()
                }
            })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
