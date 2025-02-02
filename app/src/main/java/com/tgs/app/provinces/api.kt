import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

fun fetchProvinces() {
    val url = URL("https://psgc.vercel.app/api/province")

    Thread {
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Using Log.d() to print to Logcat in Android
                Log.d("ProvincesAPI", "Response: $response")
            } else {
                Log.e("ProvincesAPI", "Error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("ProvincesAPI", "Exception: ${e.message}")
        }
    }.start()
}

fun main() {
    fetchProvinces()
}
