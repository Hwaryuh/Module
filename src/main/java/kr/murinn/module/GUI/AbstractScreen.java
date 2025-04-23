package kr.murinn.module.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractScreen implements InventoryHolder {
    private final Inventory inventory;

    public AbstractScreen(int size, Component title) {
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

    protected void setItems(int[] slots, ItemStack item) {
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    protected ItemStack createItem(Material material, Integer modelData, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(name);

            if (modelData != null) {
                meta.setCustomModelData(modelData);
            }

            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                Collections.addAll(loreList, lore);
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}