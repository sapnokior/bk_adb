// In com/pluto/adbtest/AdbManager.kt

package com.pluto.adb

import android.content.Context
import android.os.Build
import android.sun.security.x509.*
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

class AdbManager private constructor(private val context: Context) : AbsAdbConnectionManager() {

    private lateinit var privateKey: PrivateKey
    private lateinit var certificate: Certificate

    init {
        // Set API level to the current device's SDK version
        setApi(Build.VERSION.SDK_INT)
        loadOrGenerateKeys()
    }

    override fun getPrivateKey(): PrivateKey = privateKey
    override fun getCertificate(): Certificate = certificate
    override fun getDeviceName(): String = "PlutoAdbClient"

    private fun loadOrGenerateKeys() {
        val privateKeyFile = File(context.filesDir, "private.key")
        val certFile = File(context.filesDir, "cert.pem")

        try {
            if (privateKeyFile.exists() && certFile.exists()) {
                val keyFactory = KeyFactory.getInstance("RSA")
                val privateKeySpec = PKCS8EncodedKeySpec(privateKeyFile.readBytes())
                privateKey = keyFactory.generatePrivate(privateKeySpec)

                val certificateFactory = CertificateFactory.getInstance("X.509")
                certificate = certificateFactory.generateCertificate(certFile.inputStream())
            } else {
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(2048, SecureRandom())
                val keyPair = keyPairGenerator.generateKeyPair()

                privateKey = keyPair.private
                certificate = generateCertificate(keyPair)

                privateKeyFile.writeBytes(privateKey.encoded)
                certFile.writeBytes(certificate.encoded)
            }
        } catch (e: Exception) {
            // Handle exceptions appropriately
            throw RuntimeException("Failed to load or generate keys", e)
        }
    }

    /**
     * Generates a self-signed X.509 certificate.
     * This version corrects the class type for the subject and issuer.
     */
    private fun generateCertificate(keyPair: java.security.KeyPair): X509Certificate {
        val subject = "CN=PlutoAdbTest"
        val algorithmName = "SHA512withRSA"

        val notBefore = Date()
        val notAfter = Date(notBefore.time + 365 * 24 * 60 * 60 * 1000L) // 1 year validity

        val x500Name = X500Name(subject)

        val certInfo = X509CertInfo()
        certInfo.set(X509CertInfo.VERSION, CertificateVersion(CertificateVersion.V3))
        certInfo.set(X509CertInfo.SERIAL_NUMBER, CertificateSerialNumber(Random().nextInt() and Int.MAX_VALUE))
        certInfo.set(X509CertInfo.ALGORITHM_ID, CertificateAlgorithmId(AlgorithmId.get(algorithmName)))

        // --- FIX IS HERE ---
        // Wrap the X500Name in the correct type
        certInfo.set(X509CertInfo.SUBJECT, CertificateSubjectName(x500Name))
        certInfo.set(X509CertInfo.ISSUER, CertificateIssuerName(x500Name))
        // --- END FIX ---

        certInfo.set(X509CertInfo.KEY, CertificateX509Key(keyPair.public))
        certInfo.set(X509CertInfo.VALIDITY, CertificateValidity(notBefore, notAfter))

        val cert = X509CertImpl(certInfo)
        cert.sign(keyPair.private, algorithmName)
        return cert
    }


    companion object {
        @Volatile
        private var INSTANCE: AbsAdbConnectionManager? = null

        fun getInstance(context: Context): AbsAdbConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdbManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}