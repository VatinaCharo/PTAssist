package nju.pt.server

import nju.pt.databaseassist.TeamDataList
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists


class ExportExcel(private val teamDataList: TeamDataList, saveDirPath :String) {

    init {
        val logger = LoggerFactory.getLogger("Judge Directory Path:$saveDirPath")
        logger.info("=====================Judging Directory Path =====================")
        if (Path(saveDirPath).isDirectory())
        {
            logger.info("导出excel文件夹路径正确")
        }
        else{
            logger.error("导出excel文件夹路径错误！")
            throw Exception("导出excel文件夹路径错误！")
        }
    }

    //获知该数据中有多少轮，以此来命名输出文件名
    private val roundName =  teamDataList.teamDataList[0].recordDataList?.map {it.round}?.toSortedSet().let{ it ->
        var string = ""
        it?.forEach{
            string  += "$it&"
        }
        string.dropLast(1)
    }

    //导出文件路径
    val savePath = when(saveDirPath.endsWith("/")){
        true -> saveDirPath + "第${roundName}轮成绩.xlsx"
        else -> saveDirPath + "/第${roundName}轮成绩.xlsx"
    }


    fun exportTeamScore(){
        val logger = LoggerFactory.getLogger("Export Team Score")
        logger.info("===================== ExportTeamScore =====================")
        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if(Path(savePath).notExists()){
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(savePath))
            logger.info("New excel file created successfully!")
        }else{
            logger.info("File already exists, reading...")
        }


        val teamScoreWorkbook = WorkbookFactory.create(FileInputStream(savePath)).apply {
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try{
                this.removeSheetAt(this.getSheetIndex("队伍总得分"))
                logger.info("Exists, deleting and updating...")
            }catch (e:java.lang.Exception){
                logger.info("Not exists, creating...")
            }

            createSheet("队伍总得分").apply {
                this.createRow(0).apply {
                    createCell(0).setCellValue("学校名")
                    createCell(1).setCellValue("队伍名")
                    createCell(2).setCellValue("总得分")

                }
                //由于有标题行，生成的行序号是index+1
                teamDataList.getTeamScore().forEachIndexed{index,data ->
                    logger.info("写入数据：${data}")
                    this.createRow(index+1).apply {
                        createCell(0).setCellValue(data.first)
                        createCell(1).setCellValue(data.second)
                        createCell(2).setCellValue(data.third)

                    }
                }
            }

        }
        try {
            val fileOutputStream = FileOutputStream(savePath)
            teamScoreWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export team score successfully to $savePath !")
        }catch (e: FileNotFoundException){
            logger.error(e.message)
            throw Exception("文件 $savePath 正被另一个程序占用，无法访问，请关闭！")
        }
    }


    fun exportReveiwTable(){
        val logger = LoggerFactory.getLogger("Export Review Table")
        logger.info("===================== ExportReviewTable =====================")
        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if(Path(savePath).notExists()){
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(savePath))
            logger.info("New excel file created successfully!")
        }else{
            logger.info("File already exists, reading...")
        }


        val teamScoreWorkbook = WorkbookFactory.create(FileInputStream(savePath)).apply {
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try{
                this.removeSheetAt(this.getSheetIndex("回顾表"))
                logger.info("Exists, deleting and updating...")
            }catch (e:java.lang.Exception){
                logger.info("Not exists, creating...")
            }


            createSheet("回顾表").apply {
                //标题行
                this.createRow(0).apply {
                    createCell(0).setCellValue("学校名")
                    createCell(1).setCellValue("队伍名")

                    teamDataList.questionMap.forEach { (qId, qName) ->
                        createCell(qId+1).setCellValue("${qId}${qName}")

                    }

                }
                //由于有标题行，生成的行序号是index+1
                teamDataList.getReviewTable().forEachIndexed { index, triple ->
                    logger.info("写入数据：${triple}")
                    this.createRow(index+1).apply {
                        createCell(0).setCellValue(triple.first)
                        createCell(1).setCellValue(triple.second)
                        triple.third.forEach{(qId,role) ->
                            createCell(qId+1).setCellValue(role)
                        }

                    }
                }
            }

        }
        try {
            val fileOutputStream = FileOutputStream(savePath)
            teamScoreWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export review table successfully to $savePath !")
        }catch (e: FileNotFoundException){
            logger.error(e.message)
            throw Exception("文件 $savePath 正被另一个程序占用，无法访问，请关闭！")
        }
    }


}

