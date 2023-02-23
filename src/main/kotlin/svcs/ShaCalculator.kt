package svcs

import svcs.vcs.VCSConstants
import java.io.File
import java.security.MessageDigest


class ShaCalculator {
    companion object {
        private val messageDigest = MessageDigest.getInstance(VCSConstants.SHA_ALGORITHM)

        val HEX_LENGTH = hashString("").length

        fun hashFile(file: File): String = hashBytes(file.readBytes())

        fun hashString(string: String): String = hashBytes(string.toByteArray())

        fun hashArrayBytes(bytes: Array<ByteArray>): String {
            bytes.forEach(messageDigest::update)
            return bytesToHex(messageDigest.digest())
        }

        fun hashBytes(bytes: ByteArray): String {
            val encodedHash = messageDigest.digest(bytes)
            return bytesToHex(encodedHash)
        }

        private fun bytesToHex(hash: ByteArray): String {
            val hexString = StringBuilder(2 * hash.size)
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            return hexString.toString()
        }
    }
}