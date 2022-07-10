package nju.pt.databaseassist

import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

object JsonHelper {
    inline fun <reified T> toJson(data: T, savePath: String) {
        val logger = LoggerFactory.getLogger("JSON Saver")
        logger.info("===================== SavingJsonFile =====================")
        logger.info("save json file to: ${File(savePath).absolutePath}")
        val format = Json { prettyPrint = true }
        File(savePath).writeText(format.encodeToString(data))
        logger.info("JsonFileSavedSuccessfully!")

    }

    inline fun <reified T> fromJson(readPath: String): T {
        val logger = LoggerFactory.getLogger("JSON Reader")
        logger.info("===================== ReadingJsonFile =====================")
        logger.info("read json form ${File(readPath).absolutePath}")
        val data = Json.decodeFromString<T>(File(readPath).readText())
        logger.info("JsonFileReadSuccessfully!")
        return data
    }
}