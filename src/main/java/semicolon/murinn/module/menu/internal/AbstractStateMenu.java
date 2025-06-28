package semicolon.murinn.module.menu.internal;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.util.InventoryUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class AbstractStateMenu extends AbstractMenu implements Interactable {

    public enum MenuState {
        IDLE, PROCESSING, COMPLETED
    }

    private MenuState currentState = MenuState.IDLE;
    protected Location moduleLocation;

    public AbstractStateMenu(int size, Component title) {
        super(size, title);
    }

    public final MenuState getCurrentState() {
        return currentState;
    }

    protected final void setCurrentState(MenuState state) {
        this.currentState = state;
    }

    public abstract Location getModuleLocation();

    public abstract void onStateChanged(MenuState newState);

    public abstract void onComplete();

    public abstract void onProcessing(int remainingSeconds);

    protected final void changeState(MenuState newState) {
        setCurrentState(newState);

        // 모듈 상태를 데이터베이스에 저장
        Location location = getModuleLocation();
        if (location != null) {
            String stateStr = newState.name();
            ModuleStateManager.updateModuleState(location, stateStr);
        }

        // 상태 변경 콜백 호출
        onStateChanged(newState);
    }

    public void syncMenuState() {
        Location location = getModuleLocation();
        if (location == null) return;

        String state = ModuleStateManager.getModuleState(location);

        switch (state) {
            case "IDLE" -> {
                if (getCurrentState() != MenuState.IDLE) {
                    changeState(MenuState.IDLE);
                }
            }
            case "PROCESSING" -> {
                long currentTime = System.currentTimeMillis();
                long completionTime = ModuleStateManager.getCompletionTime(location);

                if (currentTime >= completionTime) {
                    // 이미 완료됐어야 하는 시간이면 완료 상태로 변경
                    ModuleStateManager.updateModuleState(location, "COMPLETED");
                    changeState(MenuState.COMPLETED);
                    onComplete();
                } else {
                    // 아직 처리 중이면 처리 상태로 변경하고 남은 시간 표시
                    if (getCurrentState() != MenuState.PROCESSING) {
                        changeState(MenuState.PROCESSING);
                    }
                    int remainingSeconds = (int) Math.ceil((completionTime - currentTime) / 1000.0);
                    onProcessing(remainingSeconds);
                }
            }
            case "COMPLETED" -> {
                if (getCurrentState() != MenuState.COMPLETED) {
                    changeState(MenuState.COMPLETED);
                    onComplete();
                }
            }
        }
    }

    protected void timerItem(int remainingSeconds, int timerSlot) {
        ItemStack timerItem = createItem(
                Material.GLASS_PANE, 1,
                Component.text("남은 시간: " + remainingSeconds + "초", NamedTextColor.GRAY),
                Component.text("클릭하여 갱신", NamedTextColor.GRAY)
        );
        setItem(timerSlot, timerItem);
    }

    protected ItemStack guideItem(String slotName) {
        return createItem(Material.GLASS_PANE, 2,
                Component.text(slotName, NamedTextColor.GRAY)
        );
    }

    protected ItemStack guideItem(String slotName, String var1) {
        return createItem(Material.GLASS_PANE, 2,
                Component.text(slotName, NamedTextColor.GRAY),
                Component.text(var1, NamedTextColor.GRAY)
        );
    }

    protected ItemStack guideItem(String slotName, String var1, Component var2) {
        return createItem(Material.GLASS_PANE, 2,
                Component.text(slotName, NamedTextColor.GRAY),
                Component.text(var1, NamedTextColor.GRAY),
                var2
        );
    }

    protected boolean isGuideItem(ItemStack item) {
        return validateCustomModelData(item, Material.GLASS_PANE, 2);
    }

    protected boolean onTimerClick(InventoryClickEvent e, int timerSlot, Player player, String clickSound) {
        if (e.getRawSlot() == timerSlot && getCurrentState() == MenuState.PROCESSING) {
            e.setCancelled(true);

            Location location = getModuleLocation();
            if (location != null) {
                long currentTime = System.currentTimeMillis();
                long completionTime = ModuleStateManager.getCompletionTime(location);

                if (currentTime >= completionTime) {
                    ModuleStateManager.updateModuleState(location, "COMPLETED");
                    changeState(MenuState.COMPLETED);
                    onComplete();
                    updateMenuTitle(player);
                } else {
                    int remainingSeconds = (int) Math.ceil((completionTime - currentTime) / 1000.0);
                    timerItem(remainingSeconds, timerSlot);
                }

                if (clickSound != null) {
                    player.playSound(player, clickSound, 1.3f, 1.0f);
                }
            }
            return true;
        }
        return false;
    }

    protected boolean onResultSlotClick(InventoryClickEvent e, int resultSlot, Player player) {
        if (e.getRawSlot() != resultSlot) return false;

        ItemStack clickedItem = e.getCurrentItem();
        if ((e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                e.getAction().name().contains("PICKUP")) &&
                clickedItem != null && !clickedItem.getType().isAir() &&
                getCurrentState() == MenuState.COMPLETED) {

            Bukkit.getScheduler().runTask(Main.plugin(), () -> onCompletedItemPickup(player, resultSlot));
            return true;
        }
        return false;
    }

    protected void returnItemToPlayer(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;

        if (InventoryUtil.hasSpace(player.getInventory(), item)) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    protected void onCompletedItemOnClose(InventoryCloseEvent event, Player player, int resultSlot) {
        if (getCurrentState() != MenuState.COMPLETED) return;

        ItemStack completedItem = event.getView().getTopInventory().getItem(resultSlot);
        if (completedItem != null && !completedItem.getType().isAir()) {
            returnItemToPlayer(player, completedItem);

            // 상태를 IDLE로 변경
            Location location = getModuleLocation();
            if (location != null) {
                ModuleStateManager.updateModuleState(location, "IDLE");
            }
        }
    }

    protected void playErrorSound(Player player) {
        player.playSound(player, "minecraft:semicolon.error", 1.75f, 1.0f);
    }

    protected void playClickSound(Player player) {
        player.playSound(player, "minecraft:semicolon.click", 1.3f, 1.0f);
    }

    protected void playUpgradeSound() {
        getInventory().getViewers().forEach(viewer -> {
            if (viewer instanceof Player player) {
                player.playSound(player, "minecraft:semicolon.upgrade", 1.2f, 1.0f);
            }
        });
    }

    protected void playSuccessSound(Player player, String soundName) {
        player.playSound(player, soundName, 1.5f, 1.0f);
    }

    protected boolean validateCustomModelData(ItemStack item, Material expectedMaterial, int expectedCmd) {
        if (item == null || item.getType() != expectedMaterial) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == expectedCmd;
    }

    protected boolean validateCustomModelData(ItemStack item, Material expectedMaterial, int... expectedCmds) {
        if (item == null || item.getType() != expectedMaterial) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        for (int expectedCmd : expectedCmds) {
            if (cmd == expectedCmd) return true;
        }
        return false;
    }

    protected void updateMenuTitle(Player player) {
    }

    public void openForPlayer(Player player, Location moduleLocation) {
        this.moduleLocation = moduleLocation;
        reset();
        open(player);
        syncMenuState();
        updateMenuForCurrentState(player);
    }

    protected abstract void updateMenuForCurrentState(Player player);

    protected void reset() {
        Inventory inv = getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, null);
        }
        setupItems();
    }

    protected void onCompletedItemPickup(Player player, int resultSlot) {
        if (getCurrentState() == MenuState.COMPLETED) {
            Location location = getModuleLocation();
            if (location != null) {
                ModuleStateManager.updateModuleState(location, "IDLE");
                changeState(MenuState.IDLE);
                updateMenuTitle(player);
                setItem(resultSlot, null);
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        return getCurrentState() != MenuState.PROCESSING;
    }

    @Override
    public void onItemPlaced(int slot, ItemStack item) {
    }

    @Override
    public boolean preventShiftClick() {
        return true;
    }
}