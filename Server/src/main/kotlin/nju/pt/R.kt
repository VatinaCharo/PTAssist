package nju.pt

import java.io.File

object R {
    const val VERSION = "v0.1.0a"

    val DEFAULT_CSS_PATH = R::class.java.getResource("assets/Element.css")?.toExternalForm()
    val SPECIAL_CSS_PATH = R::class.java.getResource("assets/Special.css")?.toExternalForm()
    val LOGO_PATH = R::class.java.getResource("assets/logo.png")?.toExternalForm()

    private const val CONFIG_EXCEL_NAME = "server_config.xlsx"

    val CONFIG_EXCEL_FILE = File(CONFIG_EXCEL_NAME)
    const val CONFIG_SHEET_NAME = "软件配置"
    const val QUESTIONS_SHEET_NAME = "赛题信息"
    const val TEAM_SHEET_NAME = "队伍信息"
    const val JUDGE_SHEET_NAME = "裁判信息"

    const val DATA_JSON_PATH = "data.json"
    const val COUNTERPART_TABLE_JSON_PATH = "counterpartTable.json"
    const val COUNTERPART_TABLE_EXCEL_PATH = "counterpartTable.xlsx"
}