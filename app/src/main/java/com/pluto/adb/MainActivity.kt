
package com.pluto.adb

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pluto.adb.databinding.ActivityMainBinding
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connectButton.setOnClickListener {
            val originalHost = binding.hostEditText.text.toString()
            val host = originalHost.replace("\\s".toRegex(), "")
            val port = binding.portEditText.text.toString().toIntOrNull()

            if (host.isNotBlank() && port != null) {
                connectAndRunAutomator(host, port)
            } else {
                binding.outputTextView.text = "Please enter a valid host and port."
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun connectAndRunAutomator(host: String, port: Int) {
        lifecycleScope.launch {
            binding.outputTextView.text = "Attempting to connect to $host:$port..."
            try {
                val adbManager = withContext(Dispatchers.IO) {
                    AdbManager.getInstance(applicationContext)
                }

                // --- FIX IS HERE ---
                // Always disconnect first to clear any stale connections from previous sessions.
                withContext(Dispatchers.IO) {
                    try {
                        adbManager.disconnect()
                    } catch (e: Exception) {
                        // Ignore exceptions during disconnect, as the connection might already be dead.
                        Log.w("AdbAutomator", "Ignoring error during preemptive disconnect: ${e.message}")
                    }

                    // Now, attempt a fresh connection.
                    adbManager.connect(host, port)
                }
                // --- END FIX ---

                withContext(Dispatchers.Main) {
                    binding.outputTextView.append("\nConnection successful.")
                }

                Log.d("AdbAutomator", "Successfully connected to ADB host.")
                runUiAutomatorTest(adbManager)

            } catch (e: Exception) {
                Log.e("AdbAutomator", "Failed to connect or run command", e)
                val errorMessage = e.message
                withContext(Dispatchers.Main) {
                    binding.outputTextView.text = "Error: $errorMessage"
                }
            }
        }
    }

    private suspend fun runUiAutomatorTest(adbManager: AbsAdbConnectionManager) {
        withContext(Dispatchers.Main) {
            binding.outputTextView.append("\nRunning uiautomator test...")
        }

        try {
            val command = "uiautomator dump /sdcard/out.xml"
            val stream = withContext(Dispatchers.IO) {
                adbManager.openStream("shell:$command")
            }

            val reader = BufferedReader(InputStreamReader(stream.openInputStream()))
            val output = StringBuilder()
            withContext(Dispatchers.IO) {
                reader.useLines { lines ->
                    lines.forEach { line ->
                        output.append(line).append("\n")
                        Log.d("UiAutomatorOutput", line)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                binding.outputTextView.append("\nTest output:\n$output")
            }

        } catch (e: Exception) {
            Log.e("AdbAutomator", "Error running uiautomator test", e)
            withContext(Dispatchers.Main) {
                binding.outputTextView.append("\nError running test: ${e.message}")
            }
        }
    }
}