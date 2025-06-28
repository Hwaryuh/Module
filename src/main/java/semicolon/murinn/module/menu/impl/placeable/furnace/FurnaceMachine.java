package semicolon.murinn.module.menu.impl.placeable.furnace;

import semicolon.murinn.module.item.Ingredients;
import semicolon.murinn.module.menu.internal.AbstractStateMenu;
import semicolon.murinn.module.menu.internal.ModuleStateManager;
import semicolon.murinn.module.util.PDCUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class FurnaceMachine extends AbstractStateMenu {
    private static FurnaceMachine instance;

    private static final int FUEL_SLOT = 3;           // 마그네슘 파우더 (연료)
    private static final int POWDER_SLOT_1 = 38;      // 파우더 슬롯 1
    private static final int POWDER_SLOT_2 = 39;      // 파우더 슬롯 2 (1단계 업그레이드 후)
    private static final int POWDER_SLOT_3 = 40;      // 파우더 슬롯 3 (2단계 업그레이드 후)
    private static final int RECIPE_SLOT = 48;        // 레시피 가이드
    private static final int START_BUTTON = 46;       // 시작 버튼
    private static final int RESULT_SLOT = 43;        // 결과물

    // 업그레이드 슬롯들
    private static final int UPGRADE_1_SLOT = 8;      // 용해 슬롯 확장
    private static final int UPGRADE_2_SLOT = 17;     // 냉각 몰드
    private static final int UPGRADE_3_SLOT = 26;     // 토치
    private static final int[] BLOCKED_SLOTS = {39, 40}; // 배리어 슬롯들

    private static final String MENU_TITLE_IDLE = "§f\u340F\u3475";
    private static final String MENU_TITLE_PROCESSING = "§f\u340F\u3476";

    // 업그레이드 아이템 상수들
    private static final int FURNACE_EXTENSION_CMD = Ingredients.FURNACE_EXTENSION.getCustomModelData();
    private static final int ICE_MOLD_CMD = Ingredients.ICE_MOLD.getCustomModelData();
    private static final int TORCH_CMD = Ingredients.TORCH.getCustomModelData();

    // 연료 상수
    private static final int MG_POWDER_CMD = Ingredients.MG_POWDER.getCustomModelData();
    private static final int POWDER_REQUIRED = 4; // 파우더 필요량

    // 레시피 매핑 - 레시피 가이드 CMD → 필요 파우더 및 결과물
    private static final Map<Integer, FurnaceRecipe> RECIPE_MAP = createRecipeMap();

    private static Map<Integer, FurnaceRecipe> createRecipeMap() {
        Map<Integer, FurnaceRecipe> map = new java.util.HashMap<>();

        map.put(23, new FurnaceRecipe(new Ingredients[]{Ingredients.MG_POWDER}, Ingredients.MG_INGOT, 1));
        map.put(24, new FurnaceRecipe(new Ingredients[]{Ingredients.AL_POWDER}, Ingredients.AL_INGOT, 1));
        map.put(25, new FurnaceRecipe(new Ingredients[]{Ingredients.FE_POWDER}, Ingredients.FE_INGOT, 1));
        map.put(26, new FurnaceRecipe(new Ingredients[]{Ingredients.CU_POWDER}, Ingredients.CU_INGOT, 1));
        map.put(27, new FurnaceRecipe(new Ingredients[]{Ingredients.LI_POWDER}, Ingredients.LI_INGOT, 1));
        map.put(28, new FurnaceRecipe(new Ingredients[]{Ingredients.AU_POWDER}, Ingredients.AU_INGOT, 1));
        map.put(29, new FurnaceRecipe(new Ingredients[]{Ingredients.PT_POWDER}, Ingredients.PT_INGOT, 1));
        map.put(30, new FurnaceRecipe(new Ingredients[]{Ingredients.NI_POWDER}, Ingredients.NI_INGOT, 1));
        map.put(31, new FurnaceRecipe(new Ingredients[]{Ingredients.TI_POWDER}, Ingredients.TI_INGOT, 1));

        map.put(32, new FurnaceRecipe(new Ingredients[]{Ingredients.AL_POWDER, Ingredients.MG_POWDER}, Ingredients.AL_MG_INGOT, 2));
        map.put(33, new FurnaceRecipe(new Ingredients[]{Ingredients.AL_POWDER, Ingredients.CU_POWDER}, Ingredients.AL_CU_INGOT, 2));
        map.put(34, new FurnaceRecipe(new Ingredients[]{Ingredients.AL_POWDER, Ingredients.LI_POWDER}, Ingredients.AL_LI_INGOT, 2));
        map.put(35, new FurnaceRecipe(new Ingredients[]{Ingredients.CU_POWDER, Ingredients.AU_POWDER}, Ingredients.CU_AU_INGOT, 2));
        map.put(36, new FurnaceRecipe(new Ingredients[]{Ingredients.NI_POWDER, Ingredients.FE_POWDER}, Ingredients.NI_FE_INGOT, 2));
        map.put(37, new FurnaceRecipe(new Ingredients[]{Ingredients.TI_POWDER, Ingredients.PT_POWDER, Ingredients.AU_POWDER}, Ingredients.TI_PT_AU_INGOT, 3));

        return map;
    }

    private record FurnaceRecipe(Ingredients[] requiredPowders, Ingredients result, int requiredSlots) { }

    public static FurnaceMachine getInstance() {
        if (instance == null) instance = new FurnaceMachine();
        return instance;
    }

    private FurnaceMachine() {
        super(54, Component.text("용광로", NamedTextColor.WHITE));
    }

    @Override
    public void setupItems() {
        int upgradeLevel = getUpgradeLevel();

        // IDLE 상태일 때 가이드 아이템 배치
        if (getCurrentState() == MenuState.IDLE) {
            setItem(FUEL_SLOT, guideItem("마그네슘 파우더", "모듈을 작동시키려면 마그네슘 파우더가 필요합니다."));
            setItem(POWDER_SLOT_1, guideItem("파우더", "용해시킬 파우더를 놓아주세요. (4개 필요)"));
            setItem(RECIPE_SLOT, guideItem("레시피", "만들 주괴의 레시피를 놓아주세요."));
            setItem(START_BUTTON, guideItem("용해 시작", "재료를 배치 후 클릭하기"));

            if (upgradeLevel >= 1) {
                setItem(POWDER_SLOT_2, guideItem("파우더", "용해시킬 파우더를 놓아주세요. (4개 필요)"));
            }
            if (upgradeLevel >= 2) {
                setItem(POWDER_SLOT_3, guideItem("파우더", "용해시킬 파우더를 놓아주세요. (4개 필요)"));
            }

            setupUpgradeGuides(upgradeLevel);
        }

        if (upgradeLevel == 0) {
            // 업그레이드 전에는 39, 40번 슬롯에 배리어 표시
            ItemStack blockedSlotItem = createItem(Material.BARRIER, null,
                    Component.text("업그레이드 전에는 사용할 수 없습니다.", NamedTextColor.RED));

            for (int slot : BLOCKED_SLOTS) {
                setItem(slot, blockedSlotItem);
            }
        } else if (upgradeLevel == 1) {
            // 1단계 업그레이드 시 40번 슬롯만 배리어
            ItemStack blockedSlotItem = createItem(Material.BARRIER, null,
                    Component.text("2단계 업그레이드가 필요합니다.", NamedTextColor.RED));
            setItem(40, blockedSlotItem);
        }
    }

    private void setupUpgradeGuides(int upgradeLevel) {
        if (upgradeLevel < 1) {
            setItem(UPGRADE_1_SLOT, guideItem("용광로 용량 확장",
                    "용해할 수 있는 파우더 수가 증가합니다. (1 → 2)",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }

        if (upgradeLevel < 2) {
            setItem(UPGRADE_2_SLOT, guideItem("Cu-Au 합금 냉각 몰드",
                    "용해할 수 있는 파우더 수가 증가합니다. (2 → 3)",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }

        if (upgradeLevel < 3) {
            setItem(UPGRADE_3_SLOT, guideItem("Ni-Fe 합금 토치",
                    "용해 시간이 단축됩니다.",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();

        // 타이머 클릭 처리
        if (getCurrentState() == MenuState.PROCESSING && onTimerClick(e, RESULT_SLOT, player, "minecraft:semicolon.click")) {
            return;
        }

        // IDLE 상태에서만 시작 버튼 처리
        if (getCurrentState() == MenuState.IDLE && slot == START_BUTTON) {
            onStartButtonClick(e, player);
        }
    }

    @Override
    public boolean preventShiftClick() {
        return false;
    }


    @Override
    public int getMaxPlaceAmount(int slot, ItemStack item) {
        if (slot == FUEL_SLOT) {
            return 1;
        } else if (slot == POWDER_SLOT_1 || slot == POWDER_SLOT_2 || slot == POWDER_SLOT_3) {
            return Math.min(4, item.getAmount());
        }
        return 1;
    }

    @Override
    public void onItemPlacementFailed(int slot, ItemStack item, Player player) {
        if (isUpgradeItem(item)) {
            int currentLevel = getUpgradeLevel();
            int itemLevel = getUpgradeItemLevel(item);

            if (itemLevel == -1) {
                player.sendMessage(Component.text("알 수 없는 아이템입니다.", NamedTextColor.RED));
                playErrorSound(player);
                return;
            }

            if (itemLevel > currentLevel + 1) {
                player.sendMessage(Component.text("이전 업그레이드가 진행되지 않아 설치할 수 없습니다.", NamedTextColor.RED));
                playErrorSound(player);
                return;
            }

            if (itemLevel <= currentLevel) {
                player.sendMessage(Component.text("이미 진행된 업그레이드입니다.", NamedTextColor.RED));
                playErrorSound(player);
                return;
            }
        }

        // 업그레이드 아이템이 아닌 경우 기존 로직 실행
        super.onItemPlacementFailed(slot, item, player);
    }

    private void onStartButtonClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        if (startSmeltingProcess(player)) {
            playClickSound(player);
        }
    }

    private int getSmeltingTime(FurnaceRecipe recipe) {
        int baseTime = switch (recipe.requiredSlots()) {
            case 2 -> 12;
            case 3 -> 15;
            default -> 7;
        };

        int upgradeLevel = getUpgradeLevel();
        if (upgradeLevel >= 3) {
            baseTime = (int) Math.ceil(baseTime * 0.7);
        }

        return baseTime;
    }

    private boolean startSmeltingProcess(Player p) {
        if (getCurrentState() != MenuState.IDLE) {
            p.sendMessage(Component.text("용광로가 이미 작동 중입니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        Inventory inv = getInventory();

        // 연료 확인 (마그네슘 파우더)
        ItemStack fuelItem = inv.getItem(FUEL_SLOT);
        if (!isFuel(fuelItem)) {
            p.sendMessage(Component.text("연료(마그네슘 파우더)가 필요합니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        // 레시피 가이드 확인
        ItemStack recipeItem = inv.getItem(RECIPE_SLOT);
        if (!isRecipeGuide(recipeItem)) {
            p.sendMessage(Component.text("레시피 가이드가 필요합니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        // 레시피 정보 가져오기
        FurnaceRecipe recipe = getRecipe(recipeItem);
        if (recipe == null) {
            p.sendMessage(Component.text("알 수 없는 레시피입니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        // 업그레이드 레벨 확인
        int upgradeLevel = getUpgradeLevel();
        if (recipe.requiredSlots() > upgradeLevel + 1) {
            p.sendMessage(Component.text("이 레시피를 사용하려면 업그레이드가 필요합니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        // 필요한 파우더 확인
        if (!hasRequiredPowders(inv, recipe)) {
            p.sendMessage(Component.text("필요한 파우더가 부족합니다.", NamedTextColor.RED));
            playErrorSound(p);
            return false;
        }

        // 여기서부터는 성공 로직
        ItemStack recipeToReturn = recipeItem.clone();
        setItem(RECIPE_SLOT, null);
        returnItemToPlayer(p, recipeToReturn);

        // 재료 소모
        consumeMaterials(inv, recipe);

        // 제련 시작
        int smeltingTime = getSmeltingTime(recipe);
        startCrafting(recipe.result().name(), smeltingTime, p);

        return true;
    }

    private boolean hasRequiredPowders(Inventory inv, FurnaceRecipe recipe) {
        Ingredients[] requiredPowders = recipe.requiredPowders();
        int[] powderSlots = {POWDER_SLOT_1, POWDER_SLOT_2, POWDER_SLOT_3};

        for (int i = 0; i < requiredPowders.length; i++) {
            ItemStack powderItem = inv.getItem(powderSlots[i]);
            if (powderItem != null && (!isPowder(powderItem, requiredPowders[i]) || powderItem.getAmount() < POWDER_REQUIRED)) {
                return false;
            }
        }

        return true;
    }

    private void consumeMaterials(Inventory inv, FurnaceRecipe recipe) {
        // 연료 소모 (마그네슘 파우더 1개)
        ItemStack fuelItem = inv.getItem(FUEL_SLOT);
        if (fuelItem != null) {
            fuelItem.setAmount(fuelItem.getAmount() - 1);  // 직접 1개 차감
        }

        // 파우더 소모 (각각 4개씩)
        Ingredients[] requiredPowders = recipe.requiredPowders();
        int[] powderSlots = {POWDER_SLOT_1, POWDER_SLOT_2, POWDER_SLOT_3};

        for (int i = 0; i < requiredPowders.length; i++) {
            ItemStack powderItem = inv.getItem(powderSlots[i]);
            if (powderItem != null) {
                powderItem.setAmount(powderItem.getAmount() - POWDER_REQUIRED);  // 직접 4개 차감
            }
        }
    }

    private void startCrafting(String resultName, int smeltTimeSeconds, Player player) {
        Location location = getModuleLocation();
        if (location != null) {
            long startTime = System.currentTimeMillis();

            ModuleStateManager.saveModuleState(
                    location, "PROCESSING",
                    startTime, smeltTimeSeconds, resultName
            );

            changeState(MenuState.PROCESSING);
            updateMenu(player, MENU_TITLE_PROCESSING);
            timerItem(smeltTimeSeconds, RESULT_SLOT);
            playSuccessSound(player, "minecraft:semicolon.furnace_module_start");
        }
    }

    // === 아이템 검증 메서드들 ===

    private boolean isFuel(ItemStack item) {
        return validateCustomModelData(item, Material.RED_DYE, MG_POWDER_CMD);
    }

    private boolean isRecipeGuide(ItemStack item) {
        if (item == null || item.getType() != Material.SADDLE) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        return cmd >= 23 && cmd <= 37; // 레시피 가이드 CMD 범위
    }

    private boolean isPowder(ItemStack item, Ingredients expectedPowder) {
        return validateCustomModelData(item, expectedPowder.getMaterial(), expectedPowder.getCustomModelData());
    }

    private FurnaceRecipe getRecipe(ItemStack recipeItem) {
        if (!isRecipeGuide(recipeItem)) return null;

        int cmd = recipeItem.getItemMeta().getCustomModelData();
        return RECIPE_MAP.get(cmd);
    }

    // === 업그레이드 관련 메서드들 ===

    private int getUpgradeLevel() {
        Location location = getModuleLocation();
        if (location == null) return 0;

        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(location);
        if (modulePdc == null) return 0;

        return modulePdc.getOrDefault(PDCUtil.FURNACE_UPGRADE_LEVEL, PersistentDataType.INTEGER, 0);
    }

    private void setUpgradeLevel(int level) {
        Location location = getModuleLocation();
        if (location == null) return;

        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(location);
        if (modulePdc == null) {
            modulePdc = location.getChunk().getPersistentDataContainer()
                    .getAdapterContext().newPersistentDataContainer();
        }

        modulePdc.set(PDCUtil.FURNACE_UPGRADE_LEVEL, PersistentDataType.INTEGER, level);
        PDCUtil.saveModulePdc(location, modulePdc);
    }

    private boolean isUpgradeItem(ItemStack item) {
        return validateCustomModelData(item, Material.SADDLE,
                FURNACE_EXTENSION_CMD, ICE_MOLD_CMD, TORCH_CMD);
    }

    private int getUpgradeItemLevel(ItemStack item) {
        if (!isUpgradeItem(item)) return -1;

        int cmd = item.getItemMeta().getCustomModelData();
        if (cmd == FURNACE_EXTENSION_CMD) return 1;
        if (cmd == ICE_MOLD_CMD) return 2;
        if (cmd == TORCH_CMD) return 3;
        return -1;
    }

    private boolean canPlaceUpgrade(int slot, ItemStack item) {
        if (!isUpgradeItem(item)) return false;

        int currentLevel = getUpgradeLevel();
        int itemLevel = getUpgradeItemLevel(item);
        int requiredSlot = switch (itemLevel) {
            case 1 -> UPGRADE_1_SLOT;
            case 2 -> UPGRADE_2_SLOT;
            case 3 -> UPGRADE_3_SLOT;
            default -> -1;
        };

        // 올바른 슬롯인지 확인
        if (slot != requiredSlot) return false;

        // 순서대로 업그레이드해야 함
        return itemLevel == currentLevel + 1;
    }

    private void processUpgrade(ItemStack item) {
        int itemLevel = getUpgradeItemLevel(item);
        setUpgradeLevel(itemLevel);

        // 업그레이드에 따른 슬롯 개방
        if (itemLevel == 1) {
            // 1단계: 39번 슬롯 개방
            setItem(39, null);
        } else if (itemLevel == 2) {
            // 2단계: 40번 슬롯도 개방
            setItem(40, null);
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
        } else if (newState == MenuState.PROCESSING) {
            // 제조 시작 시 배리어 제거
            clearBarriers();
        } else if (newState == MenuState.IDLE) {
            // IDLE 상태 복귀 시 배리어 재설정
            setupItems();
            syncUpgradeItems();
        }
    }

    @Override
    public void onProcessing(int remainingSeconds) {
        timerItem(remainingSeconds, RESULT_SLOT);
    }

    @Override
    public void onComplete() {
        setItem(RESULT_SLOT, null);

        // ModuleStateManager에서 제련 결과 아이템 정보 가져오기
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(getModuleLocation());
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            String resultName = modulePdc.get(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING);

            try {
                Ingredients resultIngredient = Ingredients.valueOf(resultName);
                ItemStack result = resultIngredient.getItemStack(1);
                setItem(RESULT_SLOT, result);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    @Override
    protected void updateMenuForCurrentState(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> {
                updateMenu(player, MENU_TITLE_IDLE);
                // IDLE 상태에서는 업그레이드에 따른 배리어 재설정
                setupItems();
                syncUpgradeItems();
            }
            case PROCESSING -> {
                updateMenu(player, MENU_TITLE_PROCESSING);
                clearBarriers();
            }
        }
    }

    /**
     * 배리어 아이템들을 제거하는 메서드 (제조 중 UI 개선)
     */
    private void clearBarriers() {
        setItem(39, null);
        setItem(40, null);
    }

    @Override
    protected void updateMenuTitle(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> updateMenu(player, MENU_TITLE_IDLE);
            case PROCESSING -> updateMenu(player, MENU_TITLE_PROCESSING);
        }
    }

    @Override
    public void openForPlayer(Player player, Location moduleLocation) {
        this.moduleLocation = moduleLocation;
        reset();
        open(player);
        syncMenuState();
        updateMenuForCurrentState(player);

        // 업그레이드 상태 로드 및 업그레이드 아이템 표시
        syncUpgradeItems();
    }

    private void syncUpgradeItems() {
        int upgradeLevel = getUpgradeLevel();

        if (upgradeLevel >= 1) {
            ItemStack extension = Ingredients.FURNACE_EXTENSION.getItemStack(1);
            setItem(UPGRADE_1_SLOT, extension);
        }
        if (upgradeLevel >= 2) {
            ItemStack iceMold = Ingredients.ICE_MOLD.getItemStack(1);
            setItem(UPGRADE_2_SLOT, iceMold);
        }
        if (upgradeLevel >= 3) {
            ItemStack torch = Ingredients.TORCH.getItemStack(1);
            setItem(UPGRADE_3_SLOT, torch);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        setupItems();
    }

    @Override
    public int[] getCancelClickSlots() {
        if (getCurrentState() == MenuState.PROCESSING) {
            return new int[]{START_BUTTON, RESULT_SLOT};
        } else {
            return new int[]{START_BUTTON};
        }
    }

    @Override
    public int[] getAllowedPlacementSlots() {
        int upgradeLevel = getUpgradeLevel();
        return switch (upgradeLevel) {
            case 0 -> new int[]{FUEL_SLOT, POWDER_SLOT_1, RECIPE_SLOT, RESULT_SLOT, UPGRADE_1_SLOT, UPGRADE_2_SLOT, UPGRADE_3_SLOT};
            case 1 -> new int[]{FUEL_SLOT, POWDER_SLOT_1, POWDER_SLOT_2, RECIPE_SLOT, RESULT_SLOT, UPGRADE_1_SLOT, UPGRADE_2_SLOT, UPGRADE_3_SLOT};
            default -> new int[]{FUEL_SLOT, POWDER_SLOT_1, POWDER_SLOT_2, POWDER_SLOT_3, RECIPE_SLOT, RESULT_SLOT, UPGRADE_1_SLOT, UPGRADE_2_SLOT, UPGRADE_3_SLOT};
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (getCurrentState() == MenuState.PROCESSING) return false;

        if (slot == FUEL_SLOT) return isFuel(item);
        else if (slot == POWDER_SLOT_1) return isPowder(item);
        else if (slot == POWDER_SLOT_2) return getUpgradeLevel() >= 1 && isPowder(item);
        else if (slot == POWDER_SLOT_3) return getUpgradeLevel() >= 2 && isPowder(item);
        else if (slot == RECIPE_SLOT) return isRecipeGuide(item);
        else if (slot == RESULT_SLOT) return false;
        else if (slot == UPGRADE_1_SLOT || slot == UPGRADE_2_SLOT || slot == UPGRADE_3_SLOT) return canPlaceUpgrade(slot, item);
        return false;
    }

    private boolean isPowder(ItemStack item) {
        if (item == null || item.getType() != Material.RED_DYE) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        return cmd >= 10 && cmd <= 18;
    }

    @Override
    public void onItemPlaced(int slot, ItemStack item) {
        if (slot == UPGRADE_1_SLOT || slot == UPGRADE_2_SLOT || slot == UPGRADE_3_SLOT) {
            if (isUpgradeItem(item)) {
                processUpgrade(item);
                playUpgradeSound();
            }
        }
    }

    @Override
    public boolean onExtraClick(InventoryClickEvent e, int slot) {
        Player player = (Player) e.getWhoClicked();

        ItemStack clickedItem = e.getCurrentItem();
        if (isGuideItem(clickedItem)) {
            e.setCancelled(true);
            return true;
        }

        // 배리어 아이템 제거 방지
        if ((slot == POWDER_SLOT_2 && getUpgradeLevel() < 1) ||
                (slot == POWDER_SLOT_3 && getUpgradeLevel() < 2)) {
            if (clickedItem != null && clickedItem.getType() == Material.BARRIER) {
                e.setCancelled(true);
                player.sendMessage(Component.text("업그레이드가 필요합니다.", NamedTextColor.RED));
                playErrorSound(player);
                return true;
            }
        }

        // 결과 슬롯 클릭 처리
        if (onResultSlotClick(e, RESULT_SLOT, player)) {
            return true;
        }

        // 업그레이드 아이템 제거 방지
        if (slot == UPGRADE_1_SLOT || slot == UPGRADE_2_SLOT || slot == UPGRADE_3_SLOT) {
            if (isUpgradeItem(clickedItem)) {
                e.setCancelled(true);
                player.sendMessage(Component.text("업그레이드 아이템은 제거할 수 없습니다.", NamedTextColor.RED));
                playErrorSound(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event, Player player) {
        // 처리 중이 아닐 때 재료 슬롯의 아이템들 반환
        if (getCurrentState() != MenuState.PROCESSING) {
            ItemStack fuelItem = event.getView().getTopInventory().getItem(FUEL_SLOT);
            if (!isGuideItem(fuelItem)) {
                returnItemToPlayer(player, fuelItem);
            }

            ItemStack powderItem1 = event.getView().getTopInventory().getItem(POWDER_SLOT_1);
            if (!isGuideItem(powderItem1)) {
                returnItemToPlayer(player, powderItem1);
            }

            if (getUpgradeLevel() >= 1) {
                ItemStack powderItem2 = event.getView().getTopInventory().getItem(POWDER_SLOT_2);
                if (!isGuideItem(powderItem2)) {
                    returnItemToPlayer(player, powderItem2);
                }
            }
            if (getUpgradeLevel() >= 2) {
                ItemStack powderItem3 = event.getView().getTopInventory().getItem(POWDER_SLOT_3);
                if (!isGuideItem(powderItem3)) {
                    returnItemToPlayer(player, powderItem3);
                }
            }

            // 레시피 가이드도 반환 (재사용 가능하므로)
            ItemStack recipeItem = event.getView().getTopInventory().getItem(RECIPE_SLOT);
            if (!isGuideItem(recipeItem)) {
                returnItemToPlayer(player, recipeItem);
            }
        }

        if (getCurrentState() == MenuState.COMPLETED) {
            onCompletedItemOnClose(event, player, RESULT_SLOT);
        }
    }
}