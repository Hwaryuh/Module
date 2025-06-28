package semicolon.murinn.module.command;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import semicolon.murinn.module.item.AdminMenu;
import semicolon.murinn.module.menu.impl.placeable.ModuleItemMenu;
import semicolon.murinn.module.menu.impl.build.BuildPanelMenu;
import semicolon.murinn.module.util.BuildUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import semicolon.murinn.module.util.ItemUtil;

public class ModuleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) return true;

        if (args[0].equalsIgnoreCase("build")) {
            new BuildPanelMenu().open(player);
        } else if (args[0].equalsIgnoreCase("place")) {
            new ModuleItemMenu().open(player);
        } else if (args[0].equalsIgnoreCase("reset")) {
            BuildUtil.resetPlayerChunk(player);
        } else if (args[0].equalsIgnoreCase("admin")) {
            new AdminMenu().open(player);
        } else if (args[0].equalsIgnoreCase("test")) {
            ItemStack test = new ItemStack(Material.SHEARS);
            ItemUtil.setMaxDurability(test, 10);

            player.getInventory().addItem(test);
        } else if (args[0].equalsIgnoreCase("test2")) {
            ItemStack held = player.getInventory().getItemInMainHand();
            player.damageItemStack(held, 1);
        }

        return false;
    }
}