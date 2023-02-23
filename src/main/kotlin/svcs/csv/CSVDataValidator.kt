package svcs.csv

import svcs.ShaCalculator
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

open class InvalidCSVException(message: String) : Exception(message)

interface CSVDataValidator {
    fun validateHeader()
    fun validateRecordSize(index: Int, vararg values: String)
    fun validateRecordValues(index: Int, vararg values: String)
}

abstract class AbstractCSVDataValidator(
    val path: Path,
    val headerFields: List<String>
) : CSVDataValidator {
    protected val fieldNumbers = headerFields.size
    protected var hashSize = ShaCalculator.HEX_LENGTH
    protected var hashPattern = "^[A-Fa-f0-9]{$hashSize}$"

    override fun validateHeader() {
        val headerFieldNames = path.toFile().useLines { it.first() }.split(",")
        if (headerFieldNames != headerFields)
            throw InvalidCSVException(
                """
                    The file (${path.toFile().absoluteFile}) header must be equal to 
                    '${headerFields.joinToString(",")}', but
                    equal to '${headerFieldNames.joinToString(",")}' (ignore single quotes)
                """.trimIndent()
            )

    }

    override fun validateRecordSize(index: Int, vararg values: String) {
        if (values.size > fieldNumbers) {
            throw InvalidCSVException(
                """
                    ${invalidLineMessage(index, "has extra values")}
                    The record: ${values.joinToString(",")}
                    Extra values: ${values.copyOfRange(fieldNumbers, values.size).joinToString(",")}
                """.trimIndent()
            )
        }
        if (values.size < fieldNumbers) {
            throw InvalidCSVException(
                """
                    ${invalidLineMessage(index, "does not contain mandatory values!")}
                    The header: ${headerFields.joinToString(",")}
                    The record: ${values.copyOfRange(fieldNumbers, values.size).joinToString(",")}
                """.trimIndent()
            )
        }
    }

    protected fun invalidLineMessage(index: Int, truncated: String) =
        "The file (${path.toFile().absoluteFile}) record on line ${index + 1} $truncated!"

    protected fun validateDate(fieldName: String, date: String, index: Int) {
        try {
            LocalDateTime.parse(date)
        } catch (e: DateTimeParseException) {
            throw InvalidCSVException(
                """
                    ${invalidLineMessage(index, "has invalid the $fieldName value")}
                    The value of $fieldName must be of the ISO-8601 date format. Example: '2011-12-03T10:15:30'!
                """.trimIndent()
            )
        }
    }

    protected fun validateHash(hash: String, index: Int) {
        if (hash.length != hashSize) {
            throw InvalidCSVException(
                """
                    ${invalidLineMessage(index, "has invalid the hash value")}
                    The size of has value must be equal ${hashSize}, but ${hash.length}
                    The hash value: $hash
                """.trimIndent()
            )
        }
        if (!hash.matches(Regex(hashPattern))) {
            throw InvalidCSVException(
                """
                    ${invalidLineMessage(index, "has invalid the hash value")}
                    The hash value doesn't match the following regex pattern: '$hashPattern'
                    The hash value: $hash
                """.trimIndent()
            )
        }
    }
}

class CSVIndexDataValidator(path: Path) :
    AbstractCSVDataValidator(path, listOf("PathFile", "HashValue", "IndexDate", "UpdateDate", "New")) {
    override fun validateRecordValues(index: Int, vararg values: String) {
        val hash = values[1]
        validateHash(hash, index)
        val indexDate = values[2]
        validateDate("index date", indexDate, index)
        val updateDate = values[3]
        validateDate("update date", updateDate, index)
    }
}

class CSVLogDataValidator(path: Path) :
    AbstractCSVDataValidator(path, listOf("HashValue", "Message", "Author", "CommitDate")) {
    override fun validateRecordValues(index: Int, vararg values: String) {
        val hash = values[0]
        validateHash(hash, index)
        val updateDate = values[3]
        validateDate("commit date", updateDate, index)
    }
}