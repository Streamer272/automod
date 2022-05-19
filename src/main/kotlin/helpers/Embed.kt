package helpers

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.channel.EmbedFooter

val NoEmbed = Embed.customEmbed(null, "no", null)
val YesEmbed = Embed.customEmbed(null, "yes", null)

fun Embed.Companion.customEmbed(title: String?, description: String?, fields: MutableList<EmbedField>?): Embed {
    return Embed(
        title = title,
        description = description,
        color = 0xff1493,
        footer = EmbedFooter(
            text = "This action was performed automatically by a bot, dont argue with me you fucking moron",
        ),
        fields = fields ?: ArrayList()
    )
}
