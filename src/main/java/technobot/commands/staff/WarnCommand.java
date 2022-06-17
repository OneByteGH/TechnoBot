package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.awt.*;
import java.util.Date;

/**
 * Command that adds a warning to user's account.
 *
 * @author TechnoVision
 */
public class WarnCommand extends Command {

    public WarnCommand(TechnoBot bot) {
        super(bot);
        this.name = "warn";
        this.description = "Adds a warning to a user's profile.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to warn", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the warning"));
        this.permission = Permission.MODERATE_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getGuild().getMemberById(user.getIdLong());
        if (target == null) {
            event.replyEmbeds(EmbedUtils.createError("That user is not in this server!")).setEphemeral(true).queue();
            return;
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError("Why would I warn myself... silly human!")).setEphemeral(true).queue();
            return;
        }
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";

        // Check that target is not the same as author
        if (target.getIdLong() == event.getUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError("You cannot warn yourself!")).setEphemeral(true).queue();
            return;
        }

        // Add warning for user
        GuildData data = GuildData.get(event.getGuild());
        data.moderationHandler.addWarning(reason, target.getIdLong(), event.getUser().getIdLong());

        // Private message user with reason for warn
        user.openPrivateChannel().queue(privateChannel -> {
            MessageEmbed msg = data.moderationHandler.createCaseMessage(event.getUser().getIdLong(), "Warn", reason, Color.yellow.getRGB());
            privateChannel.sendMessageEmbeds(msg).queue();
        }, fail -> { });

        // Send confirmation message
        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been warned", null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
