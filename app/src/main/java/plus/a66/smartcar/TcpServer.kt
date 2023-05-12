package plus.a66.smartcar

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

const val MESSAGE_NEW_CLIENT = 114514

class TcpServer(
    private val PORT: Int,
    private var handler: Handler
) : HandlerThread("TCPServer-thread") {
    private lateinit var server: ServerSocket
    private val clientsList = mutableListOf<ClientThread>()

    fun isAnyClientConnected() = clientsList.any { it.isAlive }

    override fun run() {
        try {
            server = ServerSocket(PORT)
            while (true) {
                val clientSocket = server.accept()
                val client = ClientThread(clientSocket)
                client.start()
                clientsList.add(client)
                handler.apply {
                    val msg = obtainMessage(MESSAGE_NEW_CLIENT)
                    msg.obj = client
                    sendMessage(msg)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessageToClients(message: String) {
        if (clientsList.isNotEmpty()) {
            for (client in clientsList) {
                if (client.isAlive) {
                    client.sendMessage(message)
                }
            }
        }
    }

    override fun quit(): Boolean {
        while (clientsList.isNotEmpty()) {
            clientsList.last().apply {
                if (isAlive) {
                    interrupt()
                }
            }
            clientsList.removeLast()
        }
        server.close()
        return true
    }

    inner class ClientThread(private val socket: Socket) : Thread() {
        private lateinit var input: BufferedReader
        private lateinit var output: PrintWriter

        override fun run() {
            try {
                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output = PrintWriter(socket.getOutputStream(), true)
                while (socket.isConnected) {
                    val message = input.readLine()
                    if (message != null) {
                        // 打印客户端消息
                        Log.e("===TcpServer.ClientThread===", "Client message: $message")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                input.close()
                output.close()
                socket.close()
                clientsList.remove(this)
            }
        }

        fun sendMessage(message: String) {
            output.println(message)
        }
    }
}