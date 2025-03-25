import com.chrissytopher.source.GetSourceDataLambda
import com.chrissytopher.source.NotificationSender
import com.chrissytopher.source.SourceData
import com.chrissytopher.source.doBackgroundSync
import com.chrissytopher.source.getCurrentQuarter
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.Test

class BackgroundSyncTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testBackgroundSync() = runTest {
        val t = Terminal()
        t.println(TextColors.brightBlue("Running background sync test"))
        val json = Json {
            allowTrailingComma = true
            ignoreUnknownKeys = true
        }
        val sourceData = json.decodeFromString<SourceData>(MockSourceData.exampleNoAssignments)
        val newSourceData = json.decodeFromString<SourceData>(MockSourceData.exampleThreeAssignments)
        val getSourceData: GetSourceDataLambda = { username, password, quarter, pfp ->
            Result.success(newSourceData)
        }
        val notificationSender = object : NotificationSender() {
            override fun sendNotification(title: String, body: String) {
                t.println("-".repeat(20))
                t.println((TextColors.yellow + TextStyles.bold)("\uD83D\uDD14$title"))
                t.println(TextStyles.bold("  - $body"))
                t.println("-".repeat(20))
            }

        }
        val res = doBackgroundSync(
            "",
            "",
            hashMapOf(getCurrentQuarter() to sourceData),
            hashMapOf(),
            true,
            17f,
            false,
            true,
            true,
            notificationSender,
            getSourceData,
        )
        if (res != null) {
            t.println(TextColors.brightGreen("Finished background sync successfully"))
        } else {
            t.println(TextColors.red("Background sync failed"))
        }
    }
}