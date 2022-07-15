package nju.pt

import java.io.File
import java.util.concurrent.ArrayBlockingQueue


object R {
    const val VERSION = "v0.1.0a"

    val DEFAULT_CSS_PATH = R::class.java.getResource("assets/Element.css")?.toExternalForm()
    val SPECIAL_CSS_PATH = R::class.java.getResource("assets/Special.css")?.toExternalForm()
    val LOGO_PATH = R::class.java.getResource("assets/logo.png")?.toExternalForm()
    val START_IMAGE_PATH = R::class.java.getResource("assets/beidalou.png")?.toExternalForm()


    const val SERVER_DATA_DIR_PATH ="ServerData"
    const val SERVER_CACHE_DIR_PATH ="ServerCache"

    const val CONFIG_EXCEL_PATH = "$SERVER_DATA_DIR_PATH/server_config.xlsx"
    const val CONFIG_JSON_PATH = "$SERVER_CACHE_DIR_PATH/config_data.json"

    val CONFIG_EXCEL_FILE:File by lazy { File(CONFIG_EXCEL_PATH) }
    const val CONFIG_SHEET_NAME = "软件配置"
    const val QUESTIONS_SHEET_NAME = "赛题信息"
    const val TEAM_SHEET_NAME = "队伍信息"
    const val JUDGE_SHEET_NAME = "裁判信息"

    const val DATA_JSON_PATH = "$SERVER_DATA_DIR_PATH/data.json"
    val DATA_JSON_EXAMPLE_PATH: String = R::class.java.getResource("assets/data_example.json")!!.toExternalForm()

    const val COUNTERPART_TABLE_JSON_PATH = "$SERVER_CACHE_DIR_PATH/counterpartTable.json"
    const val COUNTERPART_TABLE_EXCEL_PATH = "$SERVER_DATA_DIR_PATH/counterpartTable.xlsx"

    //文件传输
    const val SERVER_SEND_FILE_DIR_PATH = "$SERVER_DATA_DIR_PATH/match"
    const val SERVER_BACKUP_FILE_DIR_PATH = "$SERVER_DATA_DIR_PATH/backup"
    const val SERVER_ACCEPT_FILE_TEMP_DIR_PATH = "$SERVER_DATA_DIR_PATH/.temp"
//    val ROOM_DATA_UPDATE_QUEUE = ArrayBlockingQueue<Int>(100, true)

//    init {
//        nju.pt.net.R.initialize(SERVER_SEND_FILE_DIR_PATH,SERVER_ACCEPT_FILE_TEMP_DIR_PATH)
//    }
}