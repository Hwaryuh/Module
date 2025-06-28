package semicolon.murinn.module.menu.impl.placeable;

import semicolon.murinn.module.menu.internal.AbstractMenu;
import semicolon.murinn.module.placeable.PlaceableModule;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModuleItemMenu extends AbstractMenu {
    public ModuleItemMenu() {
        super(9, Component.text("설치형 모듈 아이템"));
    }

    @Override
    public void setupItems() {
        setItem(0, PlaceableModule.COMMUNICATION.getItemStack(1));
        setItem(1, PlaceableModule.GRINDER.getItemStack(1));
        setItem(2, PlaceableModule.PRINT.getItemStack(1));
        setItem(3, PlaceableModule.COFFEE_MACHINE.getItemStack(1));
        setItem(4, PlaceableModule.BATTERY.getItemStack(1));
        setItem(5, PlaceableModule.FURNACE_OPEN.getItemStack(1));
        setItem(6, PlaceableModule.FURNACE_CLOSE.getItemStack(1));
        setItem(7, PlaceableModule.SOLAR.getItemStack(1));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        switch(slot) {
            case 0 -> player.getInventory().addItem(PlaceableModule.COMMUNICATION.getItemStack(1));
            case 1 -> player.getInventory().addItem(PlaceableModule.GRINDER.getItemStack(1));
            case 2 -> player.getInventory().addItem(PlaceableModule.PRINT.getItemStack(1));
            case 3 -> player.getInventory().addItem(PlaceableModule.COFFEE_MACHINE.getItemStack(1));
            case 4 -> player.getInventory().addItem(PlaceableModule.BATTERY.getItemStack(1));
            case 5 -> player.getInventory().addItem(PlaceableModule.FURNACE_OPEN.getItemStack(1));
            case 6 -> player.getInventory().addItem(PlaceableModule.FURNACE_CLOSE.getItemStack(1));
            case 7 -> player.getInventory().addItem(PlaceableModule.SOLAR.getItemStack(1));
        }
    }
}