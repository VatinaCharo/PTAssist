package nju.pt.databaseassist

import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

object JsonHelper {
    inline fun <reified T> toJson(data: T, savePath: String) {
        val logger = LoggerFactory.getLogger("Save json file to: $savePath")
        logger.info("===================== SavingJsonFile =====================")
        val format = Json { prettyPrint = true }
        File(savePath).writeText(format.encodeToString(data))
        logger.info("===================== JsonFileSavedSuccessfully =====================")

    }

    inline fun <reified T> fromJson(readPath: String): T {
        val logger = LoggerFactory.getLogger("Read json file from: $readPath")
        logger.info("===================== ReadingJsonFile =====================")
        try {
            val data = Json.decodeFromString<T>(File(readPath).readText())
            logger.info("JsonFileReadSuccessfully!")
            return data
        } catch (e: FileNotFoundException) {
            logger.error("未找到文件: ${e.message}")
            throw Exception("未找到文件: ${e.message}")
        }
    }
}