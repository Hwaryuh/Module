package kr.murinn.module.GUI.impl;

import kr.murinn.module.GUI.AbstractScreen;
import kr.murinn.module.util.Direction;
import kr.murinn.module.util.StructureUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TestMenuScreen extends AbstractScreen {

    public TestMenuScreen() {
        super(27, Component.text("테스트"));
    }

    @Override
    public void setupItems() {
        ItemStack var1 = createItem(Material.PAPER, null, Component.text("N"), (Component) null);
        ItemStack var2 = createItem(Material.PAPER, null, Component.text("W"), (Component) null);
        ItemStack var3 = createItem(Material.PAPER, null, Component.text("E"), (Component) null);
        ItemStack var4 = createItem(Material.PAPER, null, Component.text("S"), (Component) null);

        setItem(1, var1);
        setItem(9, var2);
        setItem(11, var3);
        setItem(19, var4);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        switch(slot) {
            case 2:
                player.closeInventory();
                StructureUtil.placeStructureAtCorner(player, "test2", Direction.NORTH);
                break;
            case 9:
                player.closeInventory();
                StructureUtil.placeStructureAtCorner(player, "test2", Direction.WEST);
                break;
            case 11:
                player.closeInventory();
                StructureUtil.placeStructureAtCorner(player, "test2", Direction.EAST);
                break;
            case 19:
                player.closeInventory();
                StructureUtil.placeStructureAtCorner(player, "test2", Direction.SOUTH);
                break;
        }
    }
}