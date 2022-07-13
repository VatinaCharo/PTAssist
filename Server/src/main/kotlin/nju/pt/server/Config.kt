package nju.pt.server

import nju.pt.R
import nju.pt.databaseassist.JsonHelper
import nju.pt.kotlin.ext.loadConfigFromExcel
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.notExists


/**
 * 配置信息
 */
object Config {

    private val logger = LoggerFactory.getLogger(Config::class.java)

    private val configData by lazy {
        if (Path(R.CONFIG_JSON_PATH).notExists()) {
            WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadConfigFromExcel().also {
                JsonHelper.toJson(it, R.CONFIG_JSON_PATH)
            }

        } else {
            JsonHelper.fromJson<ConfigData>(R.CONFIG_JSON_PATH)
        }
    }

    val port by lazy { configData.port }
    val judgeCount by lazy { configData.judgeCount}
    val roomCount by lazy { configData.roomCount }
    val turns by lazy { configData.turns }

    // TODO: 2022/7/13 默认的系数在哪用上?
    val rWeight by lazy { configData.rWeight }
    val orWeight by lazy { configData.oWeight }
    val vWeight by lazy { configData.vWeight }


}
@kotlinx.serialization.Serializable
data class ConfigData(
    var port: Int,
    var judgeCount: Int ,
    var roomCount: Int,
    var turns: Int,
    var rWeight: Double,
    var oWeight: Double,
    var vWeight: Double,
)