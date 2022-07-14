import java.util.concurrent.ArrayBlockingQueue

object R {
    val ROOM_DATA_UPDATE_QUEUE = ArrayBlockingQueue<Int>(100, true)
}