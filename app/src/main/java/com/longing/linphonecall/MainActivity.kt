package com.longing.linphonecall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.longing.linphonecall.databinding.ActivityMainBinding
import org.linphone.core.*
import org.linphone.core.tools.Log

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var core: Core

    // Create a Core listener to listen for the callback we need
    // In this case, we want to know about the account registration status
    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            // If account has been configured correctly, we will go through Progress and Ok states
            // Otherwise, we will be Failed.
            binding.registrationStatus.text = message

            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                binding.connect.isEnabled = true
            } else if (state == RegistrationState.Ok) {
                binding.disconnect.isEnabled = true
                binding.callLayout.visibility = View.VISIBLE
            }
        }

        override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
            // This callback will be triggered when a successful audio device has been changed
        }

        override fun onAudioDevicesListUpdated(core: Core) {
            // This callback will be triggered when the available devices list has changed,
            // for example after a bluetooth headset has been connected/disconnected.
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            binding.callStatus.text = message

            // When a call is received
            when (state) {
                Call.State.IncomingReceived -> {
                    binding.hangUp.isEnabled = true
                    binding.answer.isEnabled = true
                    binding.remoteAddress.setText(call.remoteAddress.asStringUriOnly())
                }
                Call.State.Connected -> {
                    binding.muteMic.isEnabled = true
                    binding.toggleSpeaker.isEnabled = true
                }
                Call.State.Released -> {
                    binding.hangUp.isEnabled = false
                    binding.answer.isEnabled = false
                    binding.muteMic.isEnabled = false
                    binding.toggleSpeaker.isEnabled = false
                    binding.remoteAddress.text.clear()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Core is the main object of the SDK. You can't do much without it.
        // To create a Core, we need the instance of the Factory.
        val factory = Factory.instance()

        // Some configuration can be done before the Core is created, for example enable debug logs.
        factory.setDebugMode(true, "Hello Linphone")

        // On Android the Core needs to have the application context to work.
        // If you don't, the following method call will crash.
        core = factory.createCore(null, null, this)

        binding.connect.setOnClickListener {
            login()
            it.isEnabled = false
        }

        binding.disconnect.setOnClickListener {
            unregister()
            it.isEnabled = false
        }

        binding.delete.setOnClickListener {
            delete()
            it.isEnabled = false
        }

        binding.version.text = core.version

    }

    private fun login() {
        val username = binding.account.text.toString()
        val password = binding.password.text.toString()
        val domain = binding.domain.text.toString()
        // Get the transport protocol to use.
        // TLS is strongly recommended
        // Only use UDP if you don't have the choice
        val transportType = when (binding.transport.checkedRadioButtonId) {
            R.id.udp -> TransportType.Udp
            R.id.tcp -> TransportType.Tcp
            else -> TransportType.Tls
        }

        // To configure a SIP account, we need an Account object and an AuthInfo object
        // The first one is how to connect to the proxy server, the second one stores the credentials

        // The auth info can be created from the Factory as it's only a data class
        // userID is set to null as it's the same as the username in our case
        // ha1 is set to null as we are using the clear text password. Upon first register, the hash will be computed automatically.
        // The realm will be determined automatically from the first register, as well as the algorithm
        val authInfo =
            Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)

        // Account object replaces deprecated ProxyConfig object
        // Account object is configured through an AccountParams object that we can obtain from the Core
        val accountParams = core.createAccountParams()

        // A SIP account is identified by an identity address that we can construct from the username and domain
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        accountParams.identityAddress = identity

        // We also need to configure where the proxy server is located
        val address = Factory.instance().createAddress("sip:$domain")
        // We use the Address object to easily set the transport protocol
        address?.transport = transportType
        accountParams.serverAddress = address
        // And we ensure the account will start the registration process
        accountParams.registerEnabled = true

        // Now that our AccountParams is configured, we can create the Account object
        val account = core.createAccount(accountParams)

        // Now let's add our objects to the Core
        core.addAuthInfo(authInfo)
        core.addAccount(account)

        // Also set the newly added account as default
        core.defaultAccount = account

        // Allow account to be removed
        binding.delete.isEnabled = true

        // To be notified of the connection status of our account, we need to add the listener to the Core
        core.addListener(coreListener)
        // We can also register a callback on the Account object
        account.addListener { _, state, message ->
            // There is a Log helper in org.linphone.core.tools package
            Log.i("[Account] Registration state changed: $state, $message")
        }

        // Finally we need the Core to be started for the registration to happen (it could have been started before)
        core.start()

        // We will need the RECORD_AUDIO permission for video call
        if (packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }
    }

    private fun unregister() {
        // Here we will disable the registration of our Account
        val account = core.defaultAccount
        account ?: return

        val params = account.params
        // Returned params object is const, so to make changes we first need to clone it
        val clonedParams = params.clone()

        // Now let's make our changes
        clonedParams.registerEnabled = false

        // And apply them
        account.params = clonedParams
    }

    private fun delete() {
        // To completely remove an Account
        val account = core.defaultAccount
        account ?: return
        core.removeAccount(account)

        // To remove all accounts use
        core.clearAccounts()

        // Same for auth info
        core.clearAllAuthInfo()
    }
}