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

    var configData =
        if (Path(R.CONFIG_JSON_PATH).notExists()) {
            WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadConfigFromExcel().also {
                JsonHelper.toJson(it, R.CONFIG_JSON_PATH)
            }

        } else {
            JsonHelper.fromJson<ConfigData>(R.CONFIG_JSON_PATH)
        }


    var port: Int
    var judgeCount: Int
    var roomCount: Int
    var turns: Int

    // TODO: 2022/7/13 默认的系数在哪用上?
    var rWeight: Double
    var oWeight: Double
    var vWeight: Double

    init {
        port = configData.port
        judgeCount = configData.judgeCount
        roomCount = configData.roomCount
        turns = configData.turns
        rWeight = configData.rWeight
        oWeight = configData.oWeight
        vWeight = configData.vWeight
    }


    fun writeIntoConfig(newConfigData: ConfigData) {
        logger.info("Save Config")
        logger.info("port: ${newConfigData.port}")
        logger.info("judgeCount: ${newConfigData.judgeCount}")
        logger.info("turns: ${newConfigData.turns}")
        logger.info("rWeight: ${newConfigData.rWeight}")
        logger.info("oWeight: ${newConfigData.oWeight}")
        logger.info("vWeight: ${newConfigData.vWeight}")


        JsonHelper.toJson(configData, R.CONFIG_JSON_PATH)
        refreshData(newConfigData)
        logger.info("Saved successfully!")
    }

    fun refreshData(
        newConfigData: ConfigData = if (Path(R.CONFIG_JSON_PATH).notExists()) {
            WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadConfigFromExcel().also {
                JsonHelper.toJson(it, R.CONFIG_JSON_PATH)
            }

        } else {
            JsonHelper.fromJson<ConfigData>(R.CONFIG_JSON_PATH)
        }

    ) {
        this.configData = newConfigData
        port = configData.port
        judgeCount = configData.judgeCount
        roomCount = configData.roomCount
        turns = configData.turns
        rWeight = configData.rWeight
        oWeight = configData.oWeight
        vWeight = configData.vWeight
    }


}

@kotlinx.serialization.Serializable
data class ConfigData(
    var port: Int,
    var judgeCount: Int,
    var roomCount: Int,
    var turns: Int,
    var rWeight: Double,
    var oWeight: Double,
    var vWeight: Double,
)