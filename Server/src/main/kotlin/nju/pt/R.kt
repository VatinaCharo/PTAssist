package nju.pt

import java.io.File

object R {
    // 默认的正反评三方的分数权重
    val DEFAULT_WEIGHT_ARRAY = doubleArrayOf(3.0, 2.0, 1.0)

    private const val CONFIG_EXCEL_NAME = "server_config.xlsx"

    val CONFIG_EXCEL_FILE = File("./$CONFIG_EXCEL_NAME")
    const val CONFIG_SHEET_NAME = "软件配置"
    const val QUESTIONS_SHEET_NAME = "赛题信息"
    const val TEAM_SHEET_NAME = "队伍信息"
    const val JUDGE_SHEET_NAME  = "裁判信息"

    private const val DATA_EXCEL_NAME = "server_data.xlsx"
    val DATA_EXCEL_FILE = File("./$DATA_EXCEL_NAME")

    const val DATA_JSON_PATH =  "./data.json"
    const val COUNTERPART_TABLE_JSON_PATH = "./counterpartTable.json"
    const val COUNTERPART_TABLE_EXCEL_PATH= "./counterpartTable.xlsx"
}