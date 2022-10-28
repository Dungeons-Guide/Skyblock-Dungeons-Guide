package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.utils.ScoreBoardUtils;
import kr.syeyoung.dungeonsguide.utils.TitleRender;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CommandDgDebug extends CommandBase {
    @Override
    public String getCommandName() {
        return "dgdebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dgdebug";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 0) return;
        String arg = args[0];



        switch (arg){
            case "scoreboard":
                ScoreBoardUtils.forEachLine(l -> {
                    ChatTransmitter.addToReciveChatQueue("LINE: " + l, false);
                });


                break;
            case "scoreboardClean":
                ScoreBoardUtils.forEachLineClean(l -> {
                    ChatTransmitter.addToReciveChatQueue("LINE: " + l, false);
                });


                break;

            case "title":
                if(args.length == 2){
                    System.out.println("Displayuing title:" + args[1]);
                    TitleRender.displayTitle(args[1], "", 10, 40, 20);
                }
                break;
            case "mockDungeonStart":
                if(!Minecraft.getMinecraft().isSingleplayer()){
                    ChatTransmitter.addToReciveChatQueue("This only works in singlepauer", false);
                    return;
                }

                if(args.length == 2){
                    int time = Integer.parseInt(args[1]);
                    ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in "+time+" seconds.§r", false);
                    return;
                }


                (new Thread(() -> {
                    try {
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 15 seconds.§r", false);
                        Thread.sleep(6000);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 10 seconds.§r", false);
                        Thread.sleep(700);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 5 seconds.§r", false);
                        Thread.sleep(1000);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 4 seconds.§r", false);
                        Thread.sleep(1000);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 3 seconds.§r", false);
                        Thread.sleep(1000);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 2 seconds.§r", false);
                        Thread.sleep(1000);
                        ChatTransmitter.addToReciveChatQueue("§r§aDungeon starts in 1 seconds.§r", false);
                    } catch (InterruptedException ignored) {}
                })).start();

//                §r§aDungeon starts in 15 seconds.§r
//                §r§aDungeon starts in 10 seconds.§r
//                §r§aDungeon starts in 5 seconds.§r
//                §r§aDungeon starts in 4 seconds.§r
//                §r§aDungeon starts in 3 seconds.§r
//                §r§aDungeon starts in 2 seconds.§r
//                §r§aDungeon starts in 1 second.§r

                break;
        }

    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
