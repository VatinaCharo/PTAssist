package nju.pt.databaseassist

import kotlinx.serialization.json.Json
import java.io.File

object JsonInterface {
    fun toJson(teamDataList: TeamDataList){
        val format = Json { prettyPrint = true }
        File("./toJson.json").writeText(format.encodeToString(teamDataList))
    }
}