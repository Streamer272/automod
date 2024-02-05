import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Query
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

    val tokenPath = dotenv["DISCORD_TOKEN"] ?: throw Exception("Token not found")
    val token = File(tokenPath).readText()
    val serviceAccountPath = dotenv["SERVICE_ACCOUNT"] ?: throw Exception("Service account not found")
    val serviceAccount = File(serviceAccountPath).inputStream()
    val pingInterval = dotenv.get("PING_INTERVAL", "10").toInt()
    val iterationCount = dotenv.get("ITERATION_COUNT", "60").toInt()
    val enableBussy = dotenv.get("ENABLE_BUSSY", "false") == "true"

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
                val message = document.getString("message") ?: continue
                val channelId = document.getString("channelId") ?: continue
                val serverId = document.getString("serverId") ?: continue

                bussies += Bussy(message, channelId, serverId)
            }
        }

    bot(token) {
        lateinit var botId: String
        var bussyIteration = -1

        events {
            onReady {
                botId = it.user.id
                setStatus("Fucking your mom", UserStatus.DO_NOT_DISTURB)
                logger.info { "Starting app as ${it.user.username}#${it.user.discriminator}" }

                if (enableBussy) {
                    CoroutineScope(Dispatchers.Default).launch {
                        val picked: MutableList<GuildMember> = mutableListOf()

                        while (isActive) {
                            if (bussies.size == 0) {
                                delay(pingInterval.seconds)
                                continue
                            }

                            if (bussyIteration == iterationCount || bussyIteration == 0) {
                                logger.debug { "Refreshing users" }
                                picked.clear()

                                bussies.forEach { bussy ->
                                    val members = guild(bussy.serverId).getMembers(100)
                                    picked += members[members.indices.random()]
                                }

                                bussyIteration = 0
                            }

                            bussies.forEachIndexed { index, bussy ->
                                val target = picked.getOrNull(index)
                                target
                                    ?.user
                                    ?.id
                                    ?.let { id ->
                                        channel(bussy.channelId).sendMessage(bussy.message.replace("@author", "<@$id>"))
                                    }
                            }

                            delay(pingInterval.seconds)
                            bussyIteration++
                        }
                    }
                }
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
        }
    }
}
