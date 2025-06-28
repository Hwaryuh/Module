package semicolon.murinn.module.menu.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public interface Interactable {
    default int[] getAllowedPlacementSlots() { return new int[0];}

    boolean canPlaceItem(int slot, ItemStack item);

    void onItemPlaced(int slot, ItemStack item);

    default boolean onExtraClick(InventoryClickEvent event, int slot) {
        return false;
    }

    default void onInventoryClose(InventoryCloseEvent event, Player player) {
    }

    default boolean preventShiftClick() {
        return false;
    }

    default int[] getCancelClickSlots() {
        return new int[0];
    }

    default void onItemPlacementFailed(int slot, ItemStack item, Player player) {
    }

    default int getMaxPlaceAmount(int slot, ItemStack item) {
        return item.getAmount();
    }
}