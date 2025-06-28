package semicolon.murinn.module.menu.internal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenu implements InventoryHolder {
    private final Inventory inventory;

    public AbstractMenu(int size, Component title) {
        this.inventory = Bukkit.createInventory(this, size, title);
        setupItems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public abstract void setupItems();

    public abstract void onClick(InventoryClickEvent event);

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @SuppressWarnings("deprecation")
    public void updateMenu(Player player, String title) {
        if (player == null || !player.isOnline()) return;

        InventoryView openInventory = player.getOpenInventory();

        if (openInventory.getTopInventory().getHolder() == this) {
            try {
                openInventory.setTitle(title);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    protected ItemStack createItem(Material material, Integer modelData, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.itemName(name.decoration(TextDecoration.ITALIC, false));

            if (modelData != null) {
                meta.setCustomModelData(modelData);
            }

            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (Component loreLine : lore) {
                    loreList.add(loreLine.decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}