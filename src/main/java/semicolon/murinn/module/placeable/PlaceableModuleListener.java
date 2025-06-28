package semicolon.murinn.module.placeable;

import semicolon.murinn.module.menu.impl.placeable.coffee.CoffeeMachine;
import semicolon.murinn.module.menu.internal.ModuleStateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlaceableModuleListener implements Listener {
    @EventHandler
    public void onModuleInteraction(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        Block block = e.getClickedBlock();
        Player player = e.getPlayer();

        if (player.isSneaking() && e.getAction() == Action.LEFT_CLICK_BLOCK && block.getType() == Material.BARRIER) {
            Location moduleLoc = PlaceableModuleManager.getModuleLocation(block);
            if (moduleLoc != null) {
                if (canRemoveModule(moduleLoc, player)) {
                    e.setCancelled(true);
                    return;
                }

                PlaceableModule.removeModule(moduleLoc);
                e.setCancelled(true);
                return;
            }
        }

        if (!player.isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.BARRIER && e.getHand() == EquipmentSlot.HAND) {
            PlaceableModule module = PlaceableModuleManager.getModuleFromBarrier(block);
            if (module != null) {
                player.swingMainHand();
                module.interact(player, block);
                e.setCancelled(true);
                return;
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getBlockFace() == BlockFace.UP && e.getHand() == EquipmentSlot.HAND) {
            if (!player.isSneaking()) {
                switch (block.getType()) {
                    case CHEST, BARREL, TRAPPED_CHEST, FURNACE, BLAST_FURNACE, SMOKER, CRAFTING_TABLE, ANVIL, ENCHANTING_TABLE, BREWING_STAND -> {
                        return;
                    }
                }
            }

            ItemStack var1 = player.getInventory().getItemInMainHand();

            if (PlaceableModule.isPlaceableModule(var1) && var1.hasItemMeta()) {
                ItemMeta meta = var1.getItemMeta();

                if (meta.hasCustomModelData()) {
                    PlaceableModule module = switch (meta.getCustomModelData()) {
                        case 1 -> PlaceableModule.COMMUNICATION;
                        case 2 -> PlaceableModule.GRINDER;
                        case 3 -> PlaceableModule.PRINT;
                        case 4 -> PlaceableModule.COFFEE_MACHINE;
                        case 5 -> PlaceableModule.BATTERY;
                        case 6 -> PlaceableModule.FURNACE_OPEN;
                        case 7 -> PlaceableModule.FURNACE_CLOSE;
                        case 8 -> PlaceableModule.SOLAR;
                        default -> null;
                    };

                    if (module != null) {
                        if (module.place(block.getLocation(), player)) {
                            if (var1.getAmount() > 1) {
                                var1.setAmount(var1.getAmount() - 1);
                            } else {
                                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            }

                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCreativePlayerBreakModule(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) return;
        if (e.getBlock().getType() != Material.BARRIER) return;

        Location moduleLoc = PlaceableModuleManager.getModuleLocation(e.getBlock());
        if (moduleLoc != null) {
            if (canRemoveModule(moduleLoc, e.getPlayer())) {
                e.setCancelled(true);
                return;
            }

            PlaceableModule.removeModule(moduleLoc);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (CoffeeMachine.getInstance().isCoffee(e.getItem())) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1, false, true, false));
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 60, 1, false, true, true));
        }
    }

    private boolean canRemoveModule(Location moduleLocation, Player player) {
        String state = ModuleStateManager.getModuleState(moduleLocation);

        if (!"IDLE".equals(state)) {
            if ("PROCESSING".equals(state)) {
                player.sendMessage(Component.text("모듈이 작동 중입니다.", NamedTextColor.RED));
            } else if ("COMPLETED".equals(state)) {
                player.sendMessage(Component.text("모듈에 결과물이 있습니다. 결과물을 회수한 후 철거해주세요.", NamedTextColor.RED));
            }
            return true;
        }

        return false;
    }
}