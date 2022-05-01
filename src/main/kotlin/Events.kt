import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage

fun BotBase.bindEvents() {
    events {
        onReady {
            channel("806802320565993544").sendMessage("Hey there!")
        }
    }
}
