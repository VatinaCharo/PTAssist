package nju.pt.server

import nju.pt.R
import nju.pt.kotlin.ext.loadConfigFromExcel
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory


/**
 * 配置信息
 */
object Config {

    private val logger = LoggerFactory.getLogger(Config::class.java)

    private val configDataList by lazy { WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadConfigFromExcel() }

    val port  by lazy { configDataList[0] }
    val judgeCount by lazy { configDataList[1] }
    val roomCount by lazy { configDataList[2] }
}
