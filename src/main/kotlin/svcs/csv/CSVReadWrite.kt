package svcs.csv

import java.nio.file.Path
import java.time.LocalDateTime

interface CSVReadWrite<T : CSVRecord> {
    fun read(): List<T>
    fun write(records: List<T>)
}

abstract class AbstractCSVReader<T : CSVRecord>(val path: Path, val dataValidator: AbstractCSVDataValidator) :
    CSVReadWrite<T> {

    private val lineSep = System.lineSeparator()

    override fun read(): List<T> {
        val lines = path.toFile().readLines()
        if (lines.size in 0..1) return emptyList()
        dataValidator.validateHeader()
        return lines.drop(1).mapIndexed { index, s -> s.mapToCSVRecord(index) }
    }

    protected abstract fun String.mapToCSVRecord(recordIndex: Int): T

    override fun write(records: List<T>) {
        val result = StringBuilder("${dataValidator.headerFields.joinToString(",")}$lineSep")
        records.forEach {
            result.append("${csvRecord(it)}$lineSep")
        }
        path.toFile().writeText(result.toString())
    }

    protected abstract fun csvRecord(record: T): String
}

class IndexCSVReadWrite(indexPath: Path) :
    AbstractCSVReader<IndexCSVRecord>(indexPath, CSVIndexDataValidator(indexPath)) {

    override fun String.mapToCSVRecord(recordIndex: Int): IndexCSVRecord {
        val recordValues = this.split(',').toTypedArray()
        dataValidator.validateRecordSize(recordIndex, *recordValues)
        val (path, hash, indexDate, updateDate, edited) = recordValues
        dataValidator.validateRecordValues(recordIndex, path, hash, indexDate, updateDate, edited)
        return IndexCSVRecord(
            path,
            hash.lowercase(),
            LocalDateTime.parse(indexDate),
            LocalDateTime.parse(updateDate),
            edited.toBooleanStrict()
        )
    }

    override fun csvRecord(record: IndexCSVRecord) = with(record) {
        "$path,$hash,$indexDate,$updateDate,$new"
    }
}

class LogCSVReadWrite(logPath: Path) :
    AbstractCSVReader<LogCSVRecord>(logPath, CSVLogDataValidator(logPath)) {

    override fun String.mapToCSVRecord(recordIndex: Int): LogCSVRecord {
        val recordValues = this.split(',').toTypedArray()
        dataValidator.validateRecordSize(recordIndex, *recordValues)
        val (hash, message, authorName, commitDate) = recordValues
        dataValidator.validateRecordValues(recordIndex, hash, message, message, commitDate)
        return LogCSVRecord(
            hash.lowercase(),
            message,
            authorName,
            LocalDateTime.parse(commitDate)
        )
    }

    override fun csvRecord(record: LogCSVRecord) = with(record) {
        "$hashValue,$message,$authorName,$commitDate"
    }
}