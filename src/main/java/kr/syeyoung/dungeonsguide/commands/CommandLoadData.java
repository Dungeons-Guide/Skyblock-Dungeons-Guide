package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CommandLoadData extends CommandBase {
    @Override
    public String getCommandName() {
        return "loadrooms";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "loadrooms";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            DungeonRoomInfoRegistry.loadAll();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
