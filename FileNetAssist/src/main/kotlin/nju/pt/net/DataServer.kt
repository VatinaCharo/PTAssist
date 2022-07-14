package nju.pt.net


import java.io.*

import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*


class DataServer(private val port: Int, val packet: Packet) : ServerSocket(port) {
    private var flag = true

    init {
        println("服务器启动 port: $port")
    }

    fun service(): Thread {
        return Thread() {
            while (flag) {
                Thread(Task(this.accept())).start()
            }
        }
    }

    class Task(private val socket: Socket) : Runnable {
        override fun run() {
            socket.use {
                DataInputStream(socket.getInputStream()).use { dis ->
                    println("client link: ${socket.inetAddress.hostName}:${socket.port}")
                    val isAlive = dis.readBoolean()
                    if (isAlive) {
                        val upload = dis.readBoolean()
                        val availableBytes = dis.available()
                        if (availableBytes > 0) {
                            println("$availableBytes bytes from ${socket.inetAddress.hostName}:${socket.port}")
                            val roomId = dis.readInt()
                            if (upload) {
                                acceptFile(dis, roomId)

                            } else {
                                DataOutputStream(socket.getOutputStream()).use { dos ->
                                    sendFile(dos, roomId)
                                }
                            }
                        } else {
                            println("no bytes from ${socket.inetAddress.hostName}:${socket.port}")
                        }
                    } else {
                        println("exit")
                    }
                }
            }
        }

        private fun sendFile(dos: DataOutputStream, roomId: Int) {
            val fileName = "${nju.pt.R.SERVER_SEND_FILE_DIR_PATH}/${roomId}_match.json"
            if (File(fileName).exists()) {
                FileInputStream(fileName).use {
                    dos.writeBoolean(true)
                    dos.flush()
                    it.copyTo(dos)
                    println("发送${roomId}_match.json完毕")
                }
                val sdf = SimpleDateFormat().apply {
                    applyPattern("HH_mm_ss_SSS")
                }
                File(fileName).copyTo(File("${nju.pt.R.SERVER_BACKUP_FILE_DIR_PATH}/${roomId}_${sdf.format(Date())}_match.json"), true)
                File(fileName).delete()
            } else {
                dos.writeBoolean(false)
                dos.flush()
            }
        }

        private fun acceptFile(dis: DataInputStream, roomId: Int) {
            val fileName = dis.readUTF()
            FileOutputStream("${nju.pt.R.SERVER_ACCEPT_FILE_TEMP_DIR_PATH}/${roomId}_${fileName}").use {
                dis.copyTo(it)
                println("接受${roomId}_${fileName}完毕")
                R.ROOM_DATA_UPDATE_QUEUE.offer(roomId)
            }
        }
    }

    fun shutdown() {
        flag = false
        // 自行连接Server Socket 用于中断accept的线程阻塞状态
        // TODO: 2022/7/14 这个ip得放到某个模块的R里
        Socket("127.0.0.1", port).use { socket ->
            DataOutputStream(socket.getOutputStream()).use {
                it.writeBoolean(false)
                it.flush()
            }
        }
    }
}