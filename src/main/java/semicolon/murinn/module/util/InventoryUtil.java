package semicolon.murinn.module.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
    public static boolean hasSpace(Inventory inventory, int size) {
        int emptySlots = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            }
        }
        return emptySlots >= size;
    }

    public static boolean hasSpace(Inventory inventory, ItemStack itemToAdd) {
        if (itemToAdd == null || itemToAdd.getType().isAir()) {
            return true;
        }

        int amountToAdd = itemToAdd.getAmount();
        int maxStackSize = itemToAdd.getMaxStackSize();

        for (ItemStack item : inventory.getStorageContents()) {
            if (item != null && !item.getType().isAir() &&
                    item.isSimilar(itemToAdd) &&
                    item.getAmount() < maxStackSize) {

                // 해당 슬롯에 추가 가능한 수량
                int canAddAmount = maxStackSize - item.getAmount();
                amountToAdd -= canAddAmount;

                if (amountToAdd <= 0) {
                    return true;
                }
            }
        }

        int requiredSlots = (int) Math.ceil((double) amountToAdd / maxStackSize);
        int emptySlots = 0;

        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
                if (emptySlots >= requiredSlots) {
                    return true;
                }
            }
        }

        return false;
    }
}