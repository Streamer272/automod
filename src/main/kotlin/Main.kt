import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.jessecorbett.diskord.api.common.UserStatus
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.events
import io.github.cdimascio.dotenv.Dotenv
import mu.KotlinLogging

suspend fun main() {
    val dotenv = Dotenv.load()
    val logger = KotlinLogging.logger("main")

    val token = dotenv.get("TOKEN") ?: throw Exception("Token not found")
    val serviceAccount = dotenv.get("SERVICE_ACCOUNT") ?: throw Exception("Service account not found")

    logger.debug { "Getting Firebase app" }
    val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount.byteInputStream()))
        .build()
    FirebaseApp.initializeApp(options)
    logger.debug { "Getting Firestore instance" }
    val db = FirestoreClient.getFirestore()
    logger.debug { "Loading Firestore collection" }

    val fluids = db.collection("fluids")
    lateinit var documents: List<QueryDocumentSnapshot>

    fluids.orderBy("zIndex", Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
        if (exception != null) {
            logger.error { "Listening on snapshot failed ($exception)" }
            return@addSnapshotListener
        }

        if (snapshot == null) {
            logger.error { "Snapshot empty" }
            return@addSnapshotListener
        }

        logger.debug { "Snapshot received" }
        documents = snapshot.documents
    }

    bot(token) {
        lateinit var botId: String

        events {
            onReady {
                botId = it.user.id
                setStatus("Fucking your mom", UserStatus.DO_NOT_DISTURB)
                logger.info { "Starting app as ${it.user.username}#${it.user.discriminator}" }
            }

            onMessageCreate { message ->
                if (message.author.id == botId) {
                    return@onMessageCreate
                }

                for (document in documents) {
                    val serverId = document.getString("serverId") ?: continue
                    if (serverId != message.guildId) {
                        continue
                    }

                    val cause = document.getString("cause") ?: continue
                    val echo = document.getString("echo") ?: continue
                    val re = Regex(cause)
                    val match = re.find(message.content) ?: continue
                    message.reply(echo.replace("\$val", match.value))
                    break
                }
            }
        }
    }
}
