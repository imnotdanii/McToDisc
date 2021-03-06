package me.imnotdani.mctodisc;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.List;

public class DiscordListener extends ListenerAdapter {

    private final Mctodisc mctodisc;
    public DiscordListener(Mctodisc mctodisc){
        this.mctodisc = mctodisc;
    }

    /**
     * Waits for message to be sent in a specific text channel.
     *
     * @param event - for any guild messages read by bot
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        boolean isBot = event.getAuthor().isBot();
        TextChannel textChannel = event.getTextChannel();
        String user = event.getAuthor().getName(), rawMsg = event.getMessage().getContentRaw(), channelID = textChannel.getId();

        if(!isBot){
            try{
                if(channelID.equals(mctodisc.getMinecraftServerChatChannelID())){
                    mctodisc.sendToServer(user, rawMsg);
                }
                if(channelID.equals(mctodisc.getBotChannelID())){
                    if(rawMsg.startsWith("!")){
                        String []msgSplit = rawMsg.split(" ");
                        int size = msgSplit.length;
                        switch(msgSplit[0]){
                            case "!mhelp": listCommands(textChannel); break;
                            case "!mstats": if(size!= 2){
                                textChannel.sendMessage("Wrong parameters given. Please try again.").queue();
                                break;
                            } else { listStats(msgSplit[1], textChannel); } break;
                            case "!mlistOn": listOnlinePlayers(textChannel); break;
                            default: break;
                        }
                    }
                }
            } catch(InsufficientPermissionException ipe){
                ipe.printStackTrace();
            }
        }
    }

    /**
     * Waits for a reaction to be added in a specific text channel. Only mods should be able to add reaction.
     *
     * @param event - sees when there's a reaction
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event){
        boolean isBot = event.getUser().isBot();
        TextChannel channel = event.getTextChannel();
        String channelID  = channel.getId(), rawMsg = event.retrieveMessage().complete().getContentRaw();
        int sizeOfMsg = rawMsg.split(" ").length; //Checks that message is ONLY a username.
        try{
            if(channelID.equals(mctodisc.getWhitelistChannelID())) {
                if (sizeOfMsg > 1) {
                    channel.sendMessage("Please make sure that you only enter your in-game name and nothing else.").queue();
                    return;
                }
                if(!isBot && sizeOfMsg == 1){
                    mctodisc.whitelistUser(rawMsg);
                }
            }
        } catch(InsufficientPermissionException | NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private void listCommands(TextChannel channel){
        try{
            channel.sendMessage("**List of available commands:**\n\n" +
                    "`!mlistOn` - lists the names of players online right now\n" +
                    "`!mstats <username>` - lists statistics of a given player (eg. `!mstats artemercy`)\n" +
                    "`!mhelp` - to bring up this menu").queue();
        } catch(InsufficientPermissionException ipe){
            ipe.printStackTrace();
        }

    }

    private void listStats(String user, TextChannel channel){
        try{
            OfflinePlayer player = mctodisc.getPlayer(user);
            channel.sendMessage("**Player:** " + user +
                    "\n\n:skull_crossbones:  **Deaths:** " + player.getStatistic(Statistic.DEATHS) +
                    "\n\n:bell:  **Bells rung:** " + player.getStatistic(Statistic.BELL_RING) +
                    "\n\n:potted_plant:  **Flowers potted:** " + player.getStatistic(Statistic.FLOWER_POTTED) +
                    "\n\n:magic_wand:  **Items enchanted:** " + player.getStatistic(Statistic.ITEM_ENCHANTED) +
                    "\n\n:cake:  **Cake slices eaten: ** " + player.getStatistic(Statistic.CAKE_SLICES_EATEN)).queue();
        } catch(NullPointerException npe){
            channel.sendMessage("Player not found.").queue();
            npe.printStackTrace();
        }
    }

    private void listOnlinePlayers(TextChannel channel){
        List<String> playerNames = mctodisc.getOnlinePlayers();
        int size = playerNames.size();
        StringBuilder toPrint;

        if(size == 0){
            toPrint = new StringBuilder("There are currently no players online right now.");
        } else if (size == 1) {
            toPrint = new StringBuilder("There is only 1 player online: " + playerNames.get(0));
        } else{
            toPrint = new StringBuilder("There are currently **" + size + " players** online right now:\n");
            for(String s : playerNames){
                toPrint.append(s).append("\n");
            }
        }
        channel.sendMessage(toPrint.toString()).queue();
    }
}