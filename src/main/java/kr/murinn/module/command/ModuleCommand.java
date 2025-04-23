package kr.murinn.module.command;

import kr.murinn.module.GUI.impl.TestMenuScreen;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ModuleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) return true;

        if (args[0].equalsIgnoreCase("build")) {
            new TestMenuScreen().open(player);
        }

        return true;
    }
}