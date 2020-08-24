package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.DoiNotCreated
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DoiService {

    fun registerDoi(doiData: String, credentials: String, url: String): String {
        var httpConnection = prepareHttpCall(url, credentials)
            return doiRegisterRequest(doiData, httpConnection)
    }

    fun prepareHttpCall(url: String, credentials: String): HttpURLConnection {
        val dataciteUrl = URL(url)
        try {
            val con = dataciteUrl.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/vnd.api+json; utf-8")
            con.setRequestProperty("Authorization", "Basic $credentials")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true
            return con
        } catch (e: Exception) {
            throw IOException(DoiNotCreated())
        }
    }

    fun doiRegisterRequest(doiData: String, httpConnection: HttpURLConnection): String {
        try {
            httpConnection.outputStream.write(doiData.toByteArray(charset("utf-8")))

            return BufferedReader(
            InputStreamReader(httpConnection.inputStream, "utf-8")
        ).readLines().map(String::trim).joinToString("\n")
    } catch (e: Exception) {
        throw IOException(DoiNotCreated())
    }
    }
}
