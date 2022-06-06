package nju.pt

import java.io.File

object R {
    const val DEFAULT_PORT = 7890

    private const val CONFIG_EXCEL_NAME = "server_config.xlsx"
    val CONFIG_EXCEL_FILE = File("./$CONFIG_EXCEL_NAME")
    const val CONFIG_SHEET_NAME = "服务端配置"
    const val QUESTIONS_SHEET_NAME = "赛题信息"
    const val SCHOOL_SHEET_NAME = "学校信息"

    private const val DATA_EXCEL_NAME = "server_data.xlsx"
    val DATA_EXCEL_FILE = File("./$DATA_EXCEL_NAME")
}