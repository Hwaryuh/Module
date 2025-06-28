package semicolon.murinn.module.menu.impl.placeable.coffee;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.item.Ingredients;
import semicolon.murinn.module.menu.internal.AbstractStateMenu;
import semicolon.murinn.module.menu.internal.ModuleStateManager;
import semicolon.murinn.module.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.item.ItemUseAnimation;
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

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class CoffeeMachine extends AbstractStateMenu { //커피 머신
    private static CoffeeMachine instance;

    private static final int[] ALLOWED_SLOTS = {39, 52};
    private static final int BREW_BUTTON_SLOT = 12;
    private static final int MUG_SLOT = 39;
    private static final int BEAN_SLOT = 52;

    private static final String MENU_TITLE_IDLE = "§f\u340F\u3473";
    private static final String MENU_TITLE_PROCESSING = "§f\u340F\u3474";

    private static final int COFFEE_BEAN_AG = Ingredients.COFFEE_AG.getCustomModelData();
    private static final int COFFEE_BEAN_AU = Ingredients.COFFEE_AU.getCustomModelData();
    private static final int COFFEE_BEAN_TI = Ingredients.COFFEE_TI.getCustomModelData();
    private static final int COFFEE_MUG_CMD = Ingredients.MUG.getCustomModelData();
    private static final int COFFEE_CMD = Ingredients.COFFEE.getCustomModelData();

    // 제작 시간 상수들
    private static final int AG_TIME = 120;
    private static final int AU_TIME = 90;
    private static final int TI_TIME = 30;

    public static CoffeeMachine getInstance() {
        if (instance == null) instance = new CoffeeMachine();
        return instance;
    }

    private CoffeeMachine() {
        super(54, Component.text("\u340F\u3473", NamedTextColor.WHITE));
    }

    @Override
    public void setupItems() {
        if (getCurrentState() == MenuState.IDLE) {
            setItem(BREW_BUTTON_SLOT, guideItem("제조 시작"));
            setItem(MUG_SLOT, guideItem("머그잔을 놓아주세요."));
            setItem(BEAN_SLOT, guideItem("커피콩을 놓아주세요."));
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (e.getRawSlot() == BREW_BUTTON_SLOT) {
            onBrewButtonClick(e, player);
        }
    }

    @Override
    public Location getModuleLocation() {
        return this.moduleLocation;
    }

    @Override
    public void onStateChanged(MenuState newState) {
        if (newState == MenuState.COMPLETED) {
            onComplete();
        }
    }

    @Override
    public void onProcessing(int remainingSeconds) {
        timerItem(remainingSeconds, BREW_BUTTON_SLOT);
    }

    @Override
    public void onComplete() {
        setItem(BREW_BUTTON_SLOT, null);

        ItemStack coffee = Ingredients.COFFEE.getItemStack(1);
        ItemUtil.applyConsumable(coffee, ItemUseAnimation.DRINK, true);
        ItemUtil.setMaxStack(coffee, 1);

        setItem(MUG_SLOT, coffee);
    }

    @Override
    protected void updateMenuForCurrentState(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> updateMenu(player, MENU_TITLE_IDLE);
            case PROCESSING -> updateMenu(player, MENU_TITLE_PROCESSING);
        }
    }

    @Override
    protected void updateMenuTitle(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> updateMenu(player, MENU_TITLE_IDLE);
            case PROCESSING -> updateMenu(player, MENU_TITLE_PROCESSING);
        }
    }

    @Override
    public int[] getCancelClickSlots() {
        return new int[]{BREW_BUTTON_SLOT};
    }

    @Override
    public int[] getAllowedPlacementSlots() {
        return ALLOWED_SLOTS;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (getCurrentState() == MenuState.PROCESSING) {
            return false;
        }

        if (slot == MUG_SLOT) return isMug(item);
        else if (slot == BEAN_SLOT) return isCoffeeBean(item);

        return false;
    }

    @Override
    public void onItemPlaced(int slot, ItemStack item) {
        if (getCurrentState() == MenuState.IDLE) {
            if (slot == MUG_SLOT && isMug(item)) {
                setItem(MUG_SLOT, item);
            } else if (slot == BEAN_SLOT && isCoffeeBean(item)) {
                setItem(BEAN_SLOT, item);
            }
        }
    }

    @Override
    public int getMaxPlaceAmount(int slot, ItemStack item) {
        return 1;
    }

    @Override
    public boolean onExtraClick(InventoryClickEvent e, int slot) {
        if (slot == MUG_SLOT) {
            ItemStack clickedItem = e.getCurrentItem();
            if (isCoffee(clickedItem) && getCurrentState() == MenuState.COMPLETED) {
                if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        e.getAction() == InventoryAction.PICKUP_ALL ||
                        e.getAction() == InventoryAction.PICKUP_SOME ||
                        e.getAction() == InventoryAction.PICKUP_HALF ||
                        e.getAction() == InventoryAction.PICKUP_ONE) {
                    Bukkit.getScheduler().runTask(Main.plugin(), () ->
                            onCompletedItemPickup((Player) e.getWhoClicked(), slot));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event, Player player) {
        if (getCurrentState() == MenuState.COMPLETED) {
            onCompletedItemOnClose(event, player, MUG_SLOT);
        }

        for (int slot : getAllowedPlacementSlots()) {
            if (slot == MUG_SLOT) continue;

            ItemStack item = event.getView().getTopInventory().getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            if (isGuideItem(item)) continue;

            returnItemToPlayer(player, item);
        }
    }

    @Override
    protected void onCompletedItemPickup(Player player, int resultSlot) {
        if (getCurrentState() == MenuState.COMPLETED && resultSlot == MUG_SLOT) {
            super.onCompletedItemPickup(player, resultSlot);
        }
    }

    private void onBrewButtonClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);

        if (getCurrentState() == MenuState.PROCESSING) {
            onTimerClick(e, BREW_BUTTON_SLOT, player, null);
        } else if (getCurrentState() == MenuState.IDLE) {
            startBrewingProcess(player);
        }
    }

    private void startBrewingProcess(Player player) {
        if (getCurrentState() != MenuState.IDLE) return;

        Inventory inv = getInventory();
        ItemStack mug = inv.getItem(MUG_SLOT);
        ItemStack beans = inv.getItem(BEAN_SLOT);

        if (isMug(mug) && isCoffeeBean(beans)) {
            inv.setItem(MUG_SLOT, null);
            inv.setItem(BEAN_SLOT, null);

            startCrafting(getCoffeeBrewTime(beans), player);

        } else {
            player.sendMessage(Component.text("재료가 부족합니다. 머그잔과 커피콩이 필요합니다.", NamedTextColor.RED));
            playErrorSound(player);
        }
    }

    private void startCrafting(int brewTime, Player player) {
        Location location = getModuleLocation();
        if (location != null) {
            long startTime = System.currentTimeMillis();

            ModuleStateManager.saveModuleState(
                    location, "PROCESSING",
                    startTime, brewTime, null
            );

            changeState(MenuState.PROCESSING);
            updateMenu(player, MENU_TITLE_PROCESSING);
            timerItem(brewTime, BREW_BUTTON_SLOT);
            playSuccessSound(player, "minecraft:semicolon.coffee_module_start");
        }
    }

    private int getCoffeeBrewTime(ItemStack coffeeBean) {
        Ingredients beanType = getCoffeeBeanType(coffeeBean);
        if (beanType == null) return AG_TIME;

        return switch (beanType) {
            case COFFEE_AU -> AU_TIME;
            case COFFEE_TI -> TI_TIME;
            default -> AG_TIME;
        };
    }

    private Ingredients getCoffeeBeanType(ItemStack item) {
        if (item == null || item.getType() != Material.BLACK_DYE) return null;
        if (!item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return null;

        int cmd = meta.getCustomModelData();
        if (cmd == Ingredients.COFFEE_AG.getCustomModelData()) return Ingredients.COFFEE_AG;
        if (cmd == Ingredients.COFFEE_AU.getCustomModelData()) return Ingredients.COFFEE_AU;
        if (cmd == Ingredients.COFFEE_TI.getCustomModelData()) return Ingredients.COFFEE_TI;

        return null;
    }

    private boolean isMug(ItemStack item) {
        return validateCustomModelData(item, Material.BOWL, COFFEE_MUG_CMD);
    }

    private boolean isCoffeeBean(ItemStack item) {
        return validateCustomModelData(item, Material.BLACK_DYE, COFFEE_BEAN_AG, COFFEE_BEAN_AU, COFFEE_BEAN_TI);
    }

    public boolean isCoffee(ItemStack item) {
        return validateCustomModelData(item, Material.BOWL, COFFEE_CMD);
    }
}