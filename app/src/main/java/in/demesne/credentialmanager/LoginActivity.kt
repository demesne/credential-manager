package `in`.demesne.credentialmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback // This is for database auth
import com.auth0.android.provider.WebAuthProvider // For Web Auth
import com.auth0.android.result.Credentials
import `in`.demesne.credentialmanager.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val auth0Domain = "YOUR_AUTH0_DOMAIN" // Placeholder
    private val auth0ClientId = "YOUR_AUTH0_CLIENT_ID" // Placeholder

    private lateinit var account: Auth0
    private lateinit var authenticationAPIClient: AuthenticationAPIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Implement secure token storage and check here.
        // If valid tokens exist, navigate directly to DashboardActivity.

        account = Auth0(auth0ClientId, auth0Domain)
        account.isOIDCConformant = true // Recommended for newer Auth0 applications
        authenticationAPIClient = AuthenticationAPIClient(account)

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.buttonLoginPasskey.setOnClickListener {
            loginWithPasskey()
        }
    }

    private fun loginUser() {
        // ... (existing username/password login code remains the same)
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        val databaseConnection = "Username-Password-Authentication"

        binding.progressBarLogin.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false
        binding.buttonLoginPasskey.isEnabled = false

        authenticationAPIClient.login(email, password, databaseConnection)
            .start(object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(credentials: Credentials) {
                    Log.d("LoginActivity", "DB Login Successful: \${credentials.accessToken}")
                    handleSuccessfulLogin(credentials)
                }

                override fun onFailure(exception: AuthenticationException) {
                    Log.e("LoginActivity", "DB Login Failed: \${exception.getDescription()}")
                    handleLoginFailure(exception)
                }
            })
    }

    private fun loginWithPasskey() {
        binding.progressBarLogin.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false
        binding.buttonLoginPasskey.isEnabled = false

        val redirectUri = "https://\${auth0Domain}/android/\${packageName}/callback"

        WebAuthProvider.login(account)
            // .withScheme("https") // Scheme is derived from redirectUri
            // .withScope("openid profile email offline_access") // Add scopes as needed
            // .withConnection("passkey") // Optional: if you have a specific passkey connection
            .withRedirectUri(redirectUri)
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(credentials: Credentials) {
                    Log.d("LoginActivity", "Passkey/Web Auth Successful: \${credentials.accessToken}")
                    handleSuccessfulLogin(credentials)
                }

                override fun onFailure(exception: AuthenticationException) {
                    Log.e("LoginActivity", "Passkey/Web Auth Failed: \${exception.getDescription()}")
                    handleLoginFailure(exception)
                }
            })
    }
    
    private fun handleSuccessfulLogin(credentials: Credentials) {
        // TODO: Store credentials securely (e.g., using EncryptedSharedPreferences)
        runOnUiThread {
            binding.progressBarLogin.visibility = View.GONE
            // Buttons remain disabled as we navigate away
            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear back stack
            startActivity(intent)
            finish()
        }
    }

    private fun handleLoginFailure(exception: AuthenticationException) {
        runOnUiThread {
            binding.progressBarLogin.visibility = View.GONE
            binding.buttonLogin.isEnabled = true
            binding.buttonLoginPasskey.isEnabled = true
            Toast.makeText(this@LoginActivity, "Login Failed: \${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (WebAuthProvider.resume(intent)) {
            // Handled by WebAuthProvider, the callback above will be triggered.
            return
        }
        // Handle other intents if necessary
    }
}
