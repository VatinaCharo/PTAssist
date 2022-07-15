package nju.pt.net

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class FileNetServer(private val port: Int, private val fileRouter: FileRouterInterface) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var flag = true

    fun service(): Thread {
        logger.info("===================== FileNetServer =====================")
        val server = ServerSocket(port)
        return Thread {
            while (flag) {
                // 此处的Thread()不要写作了Thread{}，否则会关不掉
                Thread(Task(server.accept(), fileRouter)).start()
            }
        }
    }

    fun shutdown() {
        logger.info("shutdown")
        flag = false
        // 自行连接Server Socket 用于中断accept的线程阻塞状态 进而关闭服务器的线程
        Socket("127.0.0.1", port).use { socket ->
            DataOutputStream(socket.getOutputStream()).use {
                it.writeUTF(Json.encodeToString(Packet(0, -1, null)))
                it.flush()
            }
        }
    }
}

class Task(private val socket: Socket, private val fileRouter: FileRouterInterface) : Runnable {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun run() {
        socket.use {
            DataInputStream(it.getInputStream()).use { dis ->
                logger.info("client link: ${it.inetAddress.hostName}:${it.port}")
                val post = Json.decodeFromString<Packet>(dis.readUTF())
                logger.info("服务端接收请求")
                logger.info("post = $post")
                // 利用回环地址筛出合法的自身的关闭请求
                if (post.roomID == 0 && post.round == -1 && it.inetAddress == InetAddress.getLoopbackAddress()) {
                    logger.info("服务器关闭")
                } else {
                    if (post.data == null) {
                        DataOutputStream(it.getOutputStream()).use { dos ->
                            val msg = Json.encodeToString(fileRouter.getPacket(post))
                            logger.info("服务端发送数据")
                            logger.info("msg = $msg")
                            dos.writeUTF(msg)
                            dos.flush()
                        }
                    } else {
                        logger.info("服务端存储数据包")
                        fileRouter.savePacket(post)
                    }
                }
            }
        }
    }
}

class FileNetClient(private val ip: String, private val port: Int) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    fun upload(packet: Packet) {
        Socket(ip, port).use { socket ->
            DataOutputStream(socket.getOutputStream()).use { dos ->
                val msg = Json.encodeToString(packet)
                logger.info("客户端发送数据")
                logger.info("msg = $msg")
                dos.writeUTF(msg)
                dos.flush()
            }
        }
    }

    fun download(postPacket: Packet): Packet {
        Socket(ip, port).use { socket ->
            // DataIOStream不要用use自动释放资源，因为会顺手关闭socket
            try {
                DataOutputStream(socket.getOutputStream()).apply {
                    val post = Json.encodeToString(postPacket)
                    logger.info("客户端发送请求")
                    logger.info("post = $post")
                    writeUTF(post)
                    flush()
                }
                DataInputStream(socket.getInputStream()).apply {
                    logger.info("客户端接收数据")
                    val packet = Json.decodeFromString<Packet>(readUTF())
                    logger.info("packet = $packet")
                    return packet
                }
            } catch (e: EOFException) {
                logger.error("未收到服务器端发送的数据文件，或服务器端未发送了纯空文件EOF")
                return postPacket
            }
        }
    }
}

