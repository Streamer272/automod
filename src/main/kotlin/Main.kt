import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.jessecorbett.diskord.api.common.GuildMember
import com.jessecorbett.diskord.api.common.UserStatus
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.File
import kotlin.time.Duration.Companion.seconds

val REGEX_CASE_INSENSITIVE = setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
val REGEX_CASE_SENSITIVE = setOf(RegexOption.MULTILINE)

suspend fun main() {
    val dotenv = Dotenv.configure().ignoreIfMissing().load()
    val logger = KotlinLogging.logger("main")

    val token = dotenv.get("TOKEN") ?: throw Exception("Token not found")
    val serviceAccountPath = dotenv.get("SERVICE_ACCOUNT") ?: throw Exception("Service account not found")
    val serviceAccount = File(serviceAccountPath).inputStream()
    val pingInterval = dotenv.get("PING_INTERVAL").toIntOrNull() ?: 10
    val iterationLength = dotenv.get("ITERATION_LENGTH").toIntOrNull() ?: 60

    logger.debug { "Getting Firebase app" }
    val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()
    FirebaseApp.initializeApp(options)
    logger.debug { "Getting Firestore instance" }
    val db = FirestoreClient.getFirestore()
    logger.debug { "Loading Firestore collection" }

    val fluids: MutableList<Fluid> = mutableListOf()
    val bussies: MutableList<Bussy> = mutableListOf()

    db
        .collection("fluids")
        .orderBy("rank", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                logger.error { "Listening on fluids snapshot failed ($exception)" }
                return@addSnapshotListener
            }

            if (snapshot == null) {
                logger.error { "Fluids snapshot empty" }
                return@addSnapshotListener
            }

            logger.debug { "Fluids snapshot received" }
            fluids.clear()

            for (document in snapshot.documents) {
                val cause = document.getString("cause") ?: continue
                val echo = document.getString("echo") ?: continue
                val caseSensitive = document.getBoolean("caseSensitive") ?: true
                val rank = document.getLong("rank")?.toInt() ?: continue
                val solid = document.getBoolean("solid") ?: continue
                val serverId = document.getString("serverId") ?: continue
                val uid = document.getString("uid") ?: continue

                fluids += Fluid(cause, echo, caseSensitive, rank, solid, serverId, uid)
            }
        }

    db
        .collection("bussies")
        .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                logger.error { "Listening on bussies snapshot failed ($exception)" }
                return@addSnapshotListener
            }

            if (snapshot == null) {
                logger.error { "Bussies snapshot empty" }
                return@addSnapshotListener
            }

            logger.debug { "Bussies snapshot received" }
            bussies.clear()

            for (document in snapshot.documents) {
                val channelId = document.getString("channelId") ?: continue
                val serverId = document.getString("serverId") ?: continue

                bussies += Bussy(channelId, serverId)
            }
        }


    bot(token) {
lateinit var botId: String
        var bussyIteration = 0

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

                for (fluid in fluids) {
                    if (fluid.serverId != message.guildId) {
                        continue
                    }

                    val re =
                        Regex(fluid.cause, if (fluid.caseSensitive) REGEX_CASE_SENSITIVE else REGEX_CASE_INSENSITIVE)
                    val match = re.find(message.content) ?: continue
                    message.reply(
                        fluid.echo
                            .replace("@value", match.value)
                            .replace("@author", "<@${message.author.id}>")
                    )
                    break
                }
            }

            CoroutineScope(Dispatchers.Default).launch {
                val picked: MutableList<GuildMember> = mutableListOf()
                while (isActive) {
                    logger.debug { "Bussies size ${bussies.size}" }
                    if (bussies.size == 0) {
                        delay(pingInterval.seconds)
                        continue
                    }

                    logger.debug { "Refreshing users ${bussyIteration == iterationLength}" }
                    if (bussyIteration == iterationLength || bussyIteration == 0) {
                        for (bussy in bussies) {
                            logger.debug { "Fetching the members of ${bussy.serverId}" }
                            val guild = guild(bussy.serverId)
                            channel(bussy.channelId).getThreadMembers()
                            val members = guild(bussy.serverId).getMembers(100)
                            picked += members[(0..99).random()]
                        }
                        bussyIteration = 1
                    }

                    logger.debug { "Pinging" }
                    bussies.forEachIndexed { index, bussy ->
                        logger.info { "ayo ${bussy.channelId} - ${bussy.serverId}" }
                        val target = picked.getOrNull(index)
                        target?.user?.id?.let {
                            channel(bussy.channelId).sendMessage("<@${it}> yo what the fuck")
                        }
                    }

                    delay(pingInterval.seconds)
                    bussyIteration++
                }
            }
        }
    }
}
