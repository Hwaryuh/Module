package kr.murinn.module.GUI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

public class ScreenListener implements Listener {

    @EventHandler
    public void onClickCustomInventory(InventoryClickEvent event) {
        InventoryView inventoryView = event.getView();

        InventoryHolder holder = inventoryView.getTopInventory().getHolder();
        if (holder == null) return;

        if (holder instanceof AbstractScreen) {
            ((AbstractScreen) holder).onClick(event);
        }
    }
}