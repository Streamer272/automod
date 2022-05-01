import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.channel.EmbedFooter

val NoEmbed = Embed.new(null, description = "no", null)

fun Embed.Companion.new(title: String?, description: String?, fields: MutableList<EmbedField>?): Embed {
    return Embed(
        title = title,
        description = description,
        color = 0xff1493,
        footer = EmbedFooter(
            text = "This action was automatically performed by a bot, dont argue with me idiots",
        ),
        fields = fields ?: ArrayList()
    )
}
