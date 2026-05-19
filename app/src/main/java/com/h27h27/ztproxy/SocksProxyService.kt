package com.h27h27.ztproxy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class SocksProxyService : Service() {
    private val PORT = 1080
    private val executor = Executors.newCachedThreadPool()

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()

        // Start SOCKS server in background
        CoroutineScope(Dispatchers.IO).launch {
            runSocksServer()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "ztproxy_channel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(channelId, "ZTProxy", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(c)
        }
        val notif = Notification.Builder(this, channelId)
            .setContentTitle("ZTProxy")
            .setContentText("SOCKS proxy running")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .build()
        startForeground(1, notif)
    }

    private fun runSocksServer() {
        val server = ServerSocket()
        // Bind to all interfaces. Optionally change to ZeroTier virtual address if available.
        server.reuseAddress = true
        server.bind(InetSocketAddress(PORT))

        while (!server.isClosed) {
            val client = server.accept()
            executor.submit { handleClient(client) }
        }
    }

    private fun handleClient(client: Socket) {
        client.soTimeout = 30000
        try {
            val `in` = client.getInputStream()
            val out = client.getOutputStream()

            // Simple SOCKS5 no-auth implementation (CONNECT only)
            val ver = `in`.read()
            if (ver != 0x05) {
                client.close()
                return
            }
            val nMethods = `in`.read()
            val methods = ByteArray(nMethods)
            readAll(`in`, methods)
            // Reply: no auth
            out.write(byteArrayOf(0x05, 0x00))

            // Read request
            val reqHeader = ByteArray(4)
            readAll(`in`, reqHeader)
            val cmd = reqHeader[1].toInt()
            val atyp = reqHeader[3].toInt()

            if (cmd != 0x01) { // only CONNECT
                // reply: command not supported
                out.write(byteArrayOf(0x05, 0x07, 0x00, 0x01, 0,0,0,0, 0,0))
                client.close()
                return
            }

            val destAddr = when (atyp) {
                0x01 -> { // IPv4
                    val addr = ByteArray(4); readAll(`in`, addr); "${addr[0].toInt() and 0xFF}.${addr[1].toInt() and 0xFF}.${addr[2].toInt() and 0xFF}.${addr[3].toInt() and 0xFF}"
                }
                0x03 -> { // domain
                    val len = `in`.read()
                    val b = ByteArray(len); readAll(`in`, b); String(b)
                }
                0x04 -> { // IPv6
                    val b = ByteArray(16); readAll(`in`, b); // simplified: use socket connect with byte array
                    // Convert ipv6 bytes to string by InetAddress later
                    null
                }
                else -> null
            }
            val portBytes = ByteArray(2); readAll(`in`, portBytes)
            val destPort = ((portBytes[0].toInt() and 0xFF) shl 8) or (portBytes[1].toInt() and 0xFF)

            if (destAddr == null) {
                out.write(byteArrayOf(0x05, 0x08, 0x00, 0x01, 0,0,0,0, 0,0))
                client.close()
                return
            }

            // Connect to destination
            val remote = Socket()
            remote.connect(InetSocketAddress(destAddr, destPort), 15000)

            // Reply: succeeded
            out.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 0,0,0,0, 0,0))

            // Pipe data between client and remote
            exchangeStreams(client.getInputStream(), remote.getOutputStream())
            exchangeStreams(remote.getInputStream(), client.getOutputStream())

        } catch (e: Exception) {
            try { client.close() } catch (_: Exception) {}
        }
    }

    private fun exchangeStreams(`in`: InputStream, out: OutputStream) {
        executor.submit {
            try {
                val buf = ByteArray(8192)
                var r: Int
                while (`in`.read(buf).also { r = it } > 0) {
                    out.write(buf, 0, r)
                    out.flush()
                }
            } catch (_: Exception) {
            } finally {
                try { out.close() } catch (_: Exception) {}
            }
        }
    }

    private fun readAll(`in`: InputStream, buf: ByteArray) {
        var off = 0
        while (off < buf.size) {
            val r = `in`.read(buf, off, buf.size - off)
            if (r < 0) throw java.io.EOFException()
            off += r
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
