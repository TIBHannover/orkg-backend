package eu.tib.orkg.prototype.statements.domain.model

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

    fun registerDoi(DOIData: String, credentials: String, url: String): String {
        var httpConnection = prepareHttpCall(url, credentials, DOIData)
            return doiRegisterRequest(DOIData, httpConnection)
    }

    fun prepareHttpCall(url: String, credentials: String, DOIData: String): HttpURLConnection {
        val url = URL(url)
        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/vnd.api+json; utf-8")
            con.setRequestProperty("Authorization", "Basic $credentials")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true
            return con
        } catch (e: Exception) {
            throw IOException("Error Creating DOI")
        }
    }

    fun doiRegisterRequest(DOIData: String, httpConnection: HttpURLConnection): String {
        try {
            httpConnection.outputStream.write(DOIData.toByteArray(charset("utf-8")))

            return BufferedReader(
            InputStreamReader(httpConnection.inputStream, "utf-8")
        ).readLines().map(String::trim).joinToString("\n")
    } catch (e: Exception) {
        throw IOException("Error creating DOI")
    }
    }
}
