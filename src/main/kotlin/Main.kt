import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import com.jessecorbett.diskord.api.common.UserStatus
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.events
import io.github.cdimascio.dotenv.Dotenv
import mu.KotlinLogging

suspend fun main() {
    val dotenv = Dotenv.load()
    val logger = KotlinLogging.logger { }

    val token = dotenv.get("TOKEN") ?: throw Exception("No token found")

    logger.debug { "Getting Firebase app" }
    val firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("automod-378203")
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()
    logger.debug { "Getting Firestore instance" }
    val db = firestoreOptions.service
    logger.debug { "Loading Firestore collection" }
    val triggers = db.collection("triggers")

    bot(token) {
        lateinit var botId: String
        events {
            onReady {
                botId = it.user.id
                setStatus("Fucking your mom", UserStatus.DO_NOT_DISTURB)
                logger.info { "Starting app" }
            }

            onMessageCreate { message ->
                if (message.author.id == botId) {
                    return@onMessageCreate
                }

                val documents = triggers.get().get().documents
                for (document in documents) {
                    val on = document.getString("on") ?: continue
                    val re = Regex(on)
                    val match = re.find(message.content) ?: continue
                    val response = document.getString("response") ?: continue
                    message.reply(response.replace("\$val", match.value))
                }
            }
        }
    }
}
