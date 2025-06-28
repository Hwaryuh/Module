package semicolon.murinn.module.menu.impl.placeable.print;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.menu.internal.AbstractStateMenu;
import semicolon.murinn.module.menu.internal.ModuleStateManager;
import semicolon.murinn.module.util.InventoryUtil;
import semicolon.murinn.module.util.PDCUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class PrintMachine extends AbstractStateMenu {
    private static PrintMachine instance;
    private boolean isPage2 = false;

    private static final int[] PRINT_ITEM_LIST = {18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39};
    private static final int DISPLAY_SLOT = 19;
    private static final int[] YES_BUTTON_SLOTS = {27, 28};
    private static final int[] NO_BUTTON_SLOTS = {29, 30};
    private static final int RESULT_SLOT = 33;

    private static final String MENU_TITLE_PAGE1 = "§f\u340F\u3478";
    private static final String MENU_TITLE_PAGE2 = "§f\u340F\u3479";
    private static final String MENU_TITLE_PROCESSING = "§f\u340F\u3480";

    public static PrintMachine getInstance() {
        if (instance == null) instance = new PrintMachine();
        return instance;
    }

    public PrintMachine() {
        super(54, Component.text("\u340F\u3478", NamedTextColor.WHITE));
    }

    @Override
    public void setupItems() {
        if (getCurrentState() == MenuState.PROCESSING) return;

        if (isPage2) {
            setupPage2GuideItems();
        } else {
            setupPage1Items();
        }
    }

    private void setupPage1Items() {
        PrintResult[] ingredients = PrintResult.values();

        for (int i = 0; i < PRINT_ITEM_LIST.length && i < ingredients.length; i++) {
            ItemStack ingredientItem = ingredients[i].getItemStack(1);
            setItem(PRINT_ITEM_LIST[i], ingredientItem);
        }
    }

    private void setupPage2GuideItems() {
        for (int yesSlot : YES_BUTTON_SLOTS) {
            setItem(yesSlot, guideItem("제작 시작",
                    "선택된 아이템을 제작합니다.",
                    Component.text("재료를 확인하고 클릭하세요", NamedTextColor.GREEN)));
        }

        for (int noSlot : NO_BUTTON_SLOTS) {
            setItem(noSlot, guideItem("취소",
                    "아이템 선택을 취소합니다.",
                    Component.text("이전 화면으로 돌아갑니다", NamedTextColor.RED)));
        }
    }

    private void switchToPage2(Player player, ItemStack selectedItem) {
        // 모든 슬롯 초기화
        for (int slot : PRINT_ITEM_LIST) {
            setItem(slot, null);
        }

        // 선택된 아이템을 디스플레이 슬롯에 배치
        setItem(DISPLAY_SLOT, addRecipeLore(selectedItem.clone()));

        isPage2 = true;
        setupPage2GuideItems(); // 가이드 아이템 배치
        updateMenu(player, MENU_TITLE_PAGE2);
    }

    private void switchToPage1(Player player) {
        reset();
        isPage2 = false;
        setupPage1Items(); // 페이지1 아이템들 배치
        updateMenu(player, MENU_TITLE_PAGE1);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();

        if (onTimerClick(e, RESULT_SLOT, player, "minecraft:semicolon.click")) {
            return;
        }

        // 페이지2 상태이고 IDLE 상태일 때만 YES/NO 버튼 처리
        if (isPage2 && getCurrentState() == MenuState.IDLE) {
            if (onYesNoButtons(e, slot, player)) {
                return;
            }
        }

        // 페이지1 상태에서만 프린트 아이템 선택 처리
        if (!isPage2) {
            onPrintItemSelection(e, slot, player);
        }
    }

    private boolean onYesNoButtons(InventoryClickEvent e, int slot, Player p) {
        for (int yes : YES_BUTTON_SLOTS) {
            if (slot == yes) {
                e.setCancelled(true);
                if (onClickYesSlots(p)) {
                    p.playSound(p, "minecraft:semicolon.click", 1.3f, 1.0f);
                }
                return true;
            }
        }

        for (int no : NO_BUTTON_SLOTS) {
            if (slot == no) {
                e.setCancelled(true);
                switchToPage1(p);
                p.playSound(p, "minecraft:semicolon.click", 1.3f, 1.0f);
                return true;
            }
        }

        return false;
    }

    private void onPrintItemSelection(InventoryClickEvent e, int slot, Player player) {
        for (int displaySlot : PRINT_ITEM_LIST) {
            if (slot == displaySlot) {
                e.setCancelled(true);

                ItemStack item = e.getCurrentItem();
                if (item != null && !item.getType().isAir()) {
                    switchToPage2(player, item);
                    player.playSound(player, "minecraft:semicolon.click", 1.3f, 1.0f);
                }
                break;
            }
        }
    }

    private ItemStack addRecipeLore(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return item;

        int modelData = item.getItemMeta().getCustomModelData();
        PrintResult printResult = Arrays.stream(PrintResult.values())
                .filter(result -> result.getCustomModelData() == modelData)
                .findFirst().orElse(null);

        if (printResult == null) {
            return item;
        }

        item.editMeta(meta -> {
            List<Component> lore = printResult.getRecipeLore();
            meta.lore(lore);
        });

        return item;
    }

    private boolean onClickYesSlots(Player player) {
        ItemStack displayItem = getInventory().getItem(DISPLAY_SLOT);
        if (displayItem == null || displayItem.getType().isAir()) {
            player.sendMessage(Component.text("제작할 아이템을 선택해주세요.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        // 현재 상태가 IDLE이 아니면 제작 불가
        if (getCurrentState() != MenuState.IDLE) {
            player.sendMessage(Component.text("프린터가 이미 작동 중입니다.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        // 어떤 PrintResult인지 확인
        PrintResult selectedResult = getSelectedPrintResult(displayItem);
        if (selectedResult == null) {
            player.sendMessage(Component.text("잘못된 아이템입니다.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        // 재료 확인
        if (!selectedResult.hasRequiredMaterials(player.getInventory())) {
            player.sendMessage(Component.text("재료가 부족합니다.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        selectedResult.consumeMaterials(player.getInventory());

        // 제작 시작 시 DISPLAY_SLOT 아이템 제거
        setItem(DISPLAY_SLOT, null);

        startCrafting(selectedResult, player);
        return true;
    }

    private void startCrafting(PrintResult selectedResult, Player player) {
        Location location = getModuleLocation();
        if (location != null) {
            long startTime = System.currentTimeMillis();
            int craftingTime = 10;

            ModuleStateManager.saveModuleState(
                    location, "PROCESSING",
                    startTime, craftingTime, selectedResult.name()
            );

            changeState(MenuState.PROCESSING);

            // 제작 시작 시 모든 슬롯 클리어하고 페이지 상태 리셋
            clearAllSlots();
            isPage2 = false;

            updateMenu(player, MENU_TITLE_PROCESSING);
            timerItem(craftingTime, RESULT_SLOT);
            player.playSound(player, "minecraft:semicolon.print_module_start", 1.3f, 1.0f);
        }
    }

    private PrintResult getSelectedPrintResult(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return null;

        int modelData = item.getItemMeta().getCustomModelData();

        for (PrintResult result : PrintResult.values()) {
            if (result.getCustomModelData() == modelData) {
                return result;
            }
        }

        return null;
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
        timerItem(remainingSeconds, RESULT_SLOT);
    }

    @Override
    public void onComplete() {
        if (getModuleLocation() == null) return;

        setItem(RESULT_SLOT, null);

        // ModuleStateManager에서 제작 결과 아이템 정보 가져오기
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(getModuleLocation());
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            String resultName = modulePdc.get(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING);

            ItemStack result = PrintResult.valueOf(resultName).getItemStack(1);
            setItem(RESULT_SLOT, result);
        }

        // 완료 시 페이지1로 리셋
        isPage2 = false;
    }

    @Override
    protected void updateMenuForCurrentState(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> {
                // IDLE이나 COMPLETED 상태에서는 항상 페이지1로 리셋
                if (getCurrentState() == MenuState.IDLE) {
                    isPage2 = false;
                }

                if (isPage2) {
                    // COMPLETED 상태에서 페이지2에 있다면 페이지1으로 전환하지 않고 유지
                    updateMenu(player, MENU_TITLE_PAGE2);
                    setupPage2GuideItems();
                } else {
                    updateMenu(player, MENU_TITLE_PAGE1);
                    setupPage1Items();
                }
            }
            case PROCESSING -> {
                updateMenu(player, MENU_TITLE_PROCESSING);
                // 제작 중에는 모든 슬롯 클리어
                clearAllSlots();
                isPage2 = false;
            }
        }
    }

    @Override
    protected void updateMenuTitle(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> updateMenu(player, MENU_TITLE_PAGE1);
            case PROCESSING -> updateMenu(player, MENU_TITLE_PROCESSING);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        isPage2 = false;
    }

    @Override
    public int[] getCancelClickSlots() {
        if (getCurrentState() == MenuState.PROCESSING) {
            return new int[]{RESULT_SLOT};
        } else {
            int[] allSlots = new int[PRINT_ITEM_LIST.length + YES_BUTTON_SLOTS.length + NO_BUTTON_SLOTS.length];
            int index = 0;

            for (int slot : PRINT_ITEM_LIST) {
                allSlots[index++] = slot;
            }
            for (int slot : YES_BUTTON_SLOTS) {
                allSlots[index++] = slot;
            }
            for (int slot : NO_BUTTON_SLOTS) {
                allSlots[index++] = slot;
            }

            return allSlots;
        }
    }

    @Override
    public int[] getAllowedPlacementSlots() {
        return new int[]{RESULT_SLOT};
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        return false;
    }

    @Override
    public boolean onExtraClick(InventoryClickEvent e, int slot) {
        ItemStack clickedItem = e.getCurrentItem();
        if (isGuideItem(clickedItem)) {
            e.setCancelled(true);
            return true;
        }

        if (slot == RESULT_SLOT) {
            if ((e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                    e.getAction().name().contains("PICKUP")) &&
                    clickedItem != null && !clickedItem.getType().isAir() &&
                    getCurrentState() == MenuState.COMPLETED) {

                Bukkit.getScheduler().runTask(Main.plugin(), () ->
                        onCompletedItemPickup((Player) e.getWhoClicked(), slot));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCompletedItemPickup(Player player, int resultSlot) {
        if (getCurrentState() == MenuState.COMPLETED && resultSlot == RESULT_SLOT) {
            Location location = getModuleLocation();
            if (location != null) {
                ModuleStateManager.updateModuleState(location, "IDLE");
                changeState(MenuState.IDLE);

                // 아이템 수령 후 페이지1로 완전히 리셋
                isPage2 = false;
                setItem(resultSlot, null);

                // 페이지1 UI로 업데이트
                updateMenu(player, MENU_TITLE_PAGE1);
                setupPage1Items();
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event, Player player) {
        if (getCurrentState() == MenuState.COMPLETED) {
            ItemStack completedItem = event.getView().getTopInventory().getItem(RESULT_SLOT);

            if (completedItem != null && !completedItem.getType().isAir()) {
                if (InventoryUtil.hasSpace(player.getInventory(), completedItem)) {
                    player.getInventory().addItem(completedItem);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), completedItem);
                }

                // 상태를 IDLE로 변경하고 페이지1로 리셋
                if (getModuleLocation() != null) {
                    ModuleStateManager.updateModuleState(getModuleLocation(), "IDLE");
                    isPage2 = false;
                }
            }
        }
    }

    private void clearAllSlots() {
        for (int slot : PRINT_ITEM_LIST) {
            setItem(slot, null);
        }
        for (int slot : YES_BUTTON_SLOTS) {
            setItem(slot, null);
        }
        for (int slot : NO_BUTTON_SLOTS) {
            setItem(slot, null);
        }
        setItem(DISPLAY_SLOT, null);
    }
}