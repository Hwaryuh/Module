package semicolon.murinn.module.item;

import semicolon.murinn.module.menu.internal.AbstractMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminMenu extends AbstractMenu {
    private int currentPage = 0;
    private final int itemsPerPage = 45;

    public AdminMenu() {
        super(54, Component.text(""));
    }

    private int getMaxPages() {
        return (int) Math.ceil((double) Ingredients.values().length / itemsPerPage);
    }

    @Override
    public void setupItems() {
        if (currentPage > 0) {
            ItemStack prevArrow = createItem(Material.ARROW, null,
                    Component.text("이전 페이지", NamedTextColor.YELLOW));
            setItem(45, prevArrow);
        }

        if (currentPage < getMaxPages() - 1) {
            ItemStack nextArrow = createItem(Material.ARROW, null,
                    Component.text("다음 페이지", NamedTextColor.YELLOW));
            setItem(53, nextArrow);
        }

        Ingredients[] ingredients = Ingredients.values();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, ingredients.length);

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            if (slot < 45) {
                setItem(slot, ingredients[i].getItemStack(1));
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        // 이전 페이지
        if (slot == 45 && currentPage > 0) {
            currentPage--;
            refresh();
        }
        // 다음 페이지
        else if (slot == 53 && currentPage < getMaxPages() - 1) {
            currentPage++;
            refresh();
        }
        // 재료 클릭 (상단 45칸)
        else if (slot < 45) {
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                player.getInventory().addItem(clickedItem.clone());
            }
        }
    }

    private void refresh() {
        for (int i = 0; i < 54; i++) {
            setItem(i, null);
        }
        setupItems();
    }
}