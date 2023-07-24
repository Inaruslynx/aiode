package net.robinfriedli.aiode.discord.property.properties;

import java.util.Objects;

import net.dv8tion.jda.api.entities.Guild;
import net.robinfriedli.aiode.Aiode;
import net.robinfriedli.aiode.boot.SpringPropertiesConfig;
import net.robinfriedli.aiode.concurrent.ExecutionContext;
import net.robinfriedli.aiode.discord.GuildContext;
import net.robinfriedli.aiode.discord.property.AbstractGuildProperty;
import net.robinfriedli.aiode.discord.property.GuildPropertyManager;
import net.robinfriedli.aiode.entities.GuildSpecification;
import net.robinfriedli.aiode.entities.xml.GuildPropertyContribution;
import net.robinfriedli.aiode.exceptions.InvalidCommandException;
import org.hibernate.Session;

/**
 * Property that defines the custom command prefix
 */
public class PrefixProperty extends AbstractGuildProperty {

    public static final String DEFAULT_FALLBACK = "$aiode";

    public PrefixProperty(GuildPropertyContribution contribution) {
        super(contribution);
    }

    /**
     * @return the prefix for a command based on the current context. Simply returns the prefix if set, else returns the
     * bot name plus a trailing whitespace if present or else "$aiode " / the default fallback. This is meant to be used to format example commands.
     */
    public static String getEffectiveCommandStartForCurrentContext() {
        SpringPropertiesConfig springPropertiesConfig = Aiode.get().getSpringPropertiesConfig();
        boolean messageContentEnabled = Objects.requireNonNullElse(springPropertiesConfig.getApplicationProperty(Boolean.class, "aiode.preferences.enable_message_content"), true);

        if (!messageContentEnabled) {
            Guild guild = ExecutionContext.Current.require().getGuild();
            return guild.getSelfMember().getAsMention() + " ";
        }

        GuildPropertyManager guildPropertyManager = Aiode.get().getGuildPropertyManager();
        return guildPropertyManager.getPropertyOptional("prefix")
            .flatMap(property -> property.getSetValue(String.class))
            .or(() -> guildPropertyManager.getPropertyOptional("botName")
                .flatMap(property -> property.getSetValue(String.class))
                .map(s -> s + " "))
            .orElse(DEFAULT_FALLBACK + " ");
    }

    public static String getForContext(GuildContext guildContext, Session session) {
        GuildSpecification specification = guildContext.getSpecification(session);
        GuildPropertyManager guildPropertyManager = Aiode.get().getGuildPropertyManager();
        return guildPropertyManager
            .getPropertyValueOptional("prefix", String.class, specification)
            .orElse(DEFAULT_FALLBACK);
    }

    @Override
    public void validate(Object state) {
        String input = (String) state;
        if (input.length() < 1 || input.length() > 5) {
            throw new InvalidCommandException("Length should be 1 - 5 characters");
        }
    }

    @Override
    public Object process(String input) {
        return input;
    }

    @Override
    public void setValue(String value, GuildSpecification guildSpecification) {
        guildSpecification.setPrefix(value);
    }

    @Override
    public Object extractPersistedValue(GuildSpecification guildSpecification) {
        return guildSpecification.getPrefix();
    }

}
