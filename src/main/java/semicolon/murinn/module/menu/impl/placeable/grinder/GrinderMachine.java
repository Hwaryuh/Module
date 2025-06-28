package semicolon.murinn.module.menu.impl.placeable.grinder;

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
public class GrinderMachine extends AbstractStateMenu { // 분쇄기
    private static GrinderMachine instance;

    private static final int ORE_SLOT_1 = 3;
    private static final int ORE_SLOT_2 = 5; // 용량 확장 후 사용 가능
    private static final int RESULT_SLOT_1 = 48;
    private static final int RESULT_SLOT_2 = 50; // 용량 확장 후 사용 가능
    private static final int[] LEVER_BUTTON = {15, 16, 24, 25};

    private static final int UPGRADE_1_SLOT = 27; // 용량 확장
    private static final int UPGRADE_2_SLOT = 36; // 기어
    private static final int UPGRADE_3_SLOT = 45; // 드릴
    private static final int[] BLOCKED_SLOTS = {5, 50}; // 배리어 슬롯들

    private static final String MENU_TITLE_IDLE = "§f\u340F\u3471";
    private static final String MENU_TITLE_PROCESSING = "§f\u340F\u3472";

    private static final int GRINDER_EXTENSION_CMD = Ingredients.GRINDER_EXTENSION.getCustomModelData();
    private static final int GEAR_CMD = Ingredients.GEAR.getCustomModelData();
    private static final int DRILL_CMD = Ingredients.DRILL.getCustomModelData();

    // 배열: {기본, 기어, 드릴} 순서
    private static final Map<Ingredients, int[]> GRIND_TIMES = Map.of(
            Ingredients.MG, new int[]{5, 4, 2},     // 5초, 4초, 2초
            Ingredients.AL, new int[]{5, 4, 2},     // 5초, 4초, 2초
            Ingredients.FE, new int[]{7, 6, 4},     // 7초, 6초, 4초
            Ingredients.CU, new int[]{7, 6, 4},     // 7초, 6초, 4초
            Ingredients.LI, new int[]{7, 6, 4},     // 7초, 6초, 4초
            Ingredients.AU, new int[]{9, 8, 6},     // 9초, 8초, 6초
            Ingredients.PT, new int[]{-1, 8, 6},    // 불가능, 8초, 6초
            Ingredients.NI, new int[]{-1, 10, 6},   // 불가능, 10초, 6초
            Ingredients.TI, new int[]{-1, 10, 6}    // 불가능, 10초, 6초
    );

    // 광석과 파우더 매핑
    private static final Map<Ingredients, Ingredients> ORE_TO_POWDER_MAP = Map.of(
            Ingredients.MG, Ingredients.MG_POWDER,
            Ingredients.AL, Ingredients.AL_POWDER,
            Ingredients.FE, Ingredients.FE_POWDER,
            Ingredients.CU, Ingredients.CU_POWDER,
            Ingredients.LI, Ingredients.LI_POWDER,
            Ingredients.AU, Ingredients.AU_POWDER,
            Ingredients.PT, Ingredients.PT_POWDER,
            Ingredients.NI, Ingredients.NI_POWDER,
            Ingredients.TI, Ingredients.TI_POWDER
    );

    public static GrinderMachine getInstance() {
        if (instance == null) instance = new GrinderMachine();
        return instance;
    }

    private GrinderMachine() {
        super(54, Component.text("\u340F\u3471", NamedTextColor.WHITE));
    }

    @Override
    public void setupItems() {
        // 업그레이드 레벨에 따라 배리어 설정
        int upgradeLevel = getUpgradeLevel();

        // IDLE 상태일 때 가이드 아이템 배치
        if (getCurrentState() == MenuState.IDLE) {
            setItem(ORE_SLOT_1, guideItem("분쇄할 광석을 놓아주세요"));

            // 용량 확장 후에는 5번 슬롯에도 가이드 아이템
            if (upgradeLevel >= 1) {
                setItem(ORE_SLOT_2, guideItem("분쇄할 광석을 놓아주세요"));
            }

            // 레버 버튼들에 가이드 아이템 배치
            for (int leverSlot : LEVER_BUTTON) {
                setItem(leverSlot, guideItem("분쇄 시작"));
            }

            // 업그레이드 슬롯 가이드 아이템 배치
            setupUpgradeGuides(upgradeLevel);
        }

        if (upgradeLevel == 0) {
            // 업그레이드 전에는 배리어 표시
            ItemStack blockedSlotItem = createItem(Material.BARRIER, null,
                    Component.text("업그레이드 전에는 놓을 수 없습니다.", NamedTextColor.RED));

            for (int slot : BLOCKED_SLOTS) {
                setItem(slot, blockedSlotItem);
            }
        }
        // 용량 확장 후에는 배리어 제거 (아이템을 설정하지 않음)
    }

    private void setupUpgradeGuides(int upgradeLevel) {
        // 1단계: 분쇄기 용량 확장
        if (upgradeLevel < 1) {
            setItem(UPGRADE_1_SLOT, guideItem("<분쇄기 용량 확장>",
                    "분쇄할 수 있는 광석의 수가 증가합니다.",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }

        // 2단계: Al-Cu 합금 기어
        if (upgradeLevel < 2) {
            setItem(UPGRADE_2_SLOT, guideItem("<Al-Cu 합금 기어>",
                    "분쇄 시간이 소폭 단축되며, 상위 자원을 분쇄할 수 있습니다.",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }

        // 3단계: Ti-Pt-Au 합금 드릴
        if (upgradeLevel < 3) {
            setItem(UPGRADE_3_SLOT, guideItem("<Ti-Pt-Au 합금 드릴>",
                    "분쇄 시간이 대폭 단축됩니다.",
                    Component.text("설치되지 않음", NamedTextColor.RED)));
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();

        // 공통 타이머 클릭 처리 (48번, 50번 슬롯 모두)
        if (getCurrentState() == MenuState.PROCESSING) {
            if (onTimerClick(e, RESULT_SLOT_1, player, "minecraft:semicolon.click")) {
                return;
            }
            // 용량 확장 시 50번 슬롯 타이머도 처리
            if (getUpgradeLevel() >= 1 && onTimerClick(e, RESULT_SLOT_2, player, "minecraft:semicolon.click")) {
                return;
            }
        }

        // IDLE 상태에서만 YES 버튼 처리
        if (getCurrentState() == MenuState.IDLE) {
            onYesButtonClick(e, slot, player);
        }
    }

    private void onYesButtonClick(InventoryClickEvent e, int slot, Player player) {
        for (int yesSlot : LEVER_BUTTON) {
            if (slot == yesSlot) {
                e.setCancelled(true);
                if (startGrindingProcess(player)) {
                    playClickSound(player);
                }
                return;
            }
        }
    }

    private boolean startGrindingProcess(Player player) {
        if (getCurrentState() != MenuState.IDLE) {
            player.sendMessage(Component.text("분쇄기가 이미 작동 중입니다.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        Inventory inv = getInventory();
        ItemStack oreItem1 = inv.getItem(ORE_SLOT_1);
        ItemStack oreItem2 = inv.getItem(ORE_SLOT_2);

        // 용량 확장 여부 확인
        int upgradeLevel = getUpgradeLevel();
        boolean hasCapacityUpgrade = upgradeLevel >= 1;

        // 광석 검증
        boolean hasOre1 = isOre(oreItem1);
        boolean hasOre2 = hasCapacityUpgrade && isOre(oreItem2);

        if (!hasOre1 && !hasOre2) {
            player.sendMessage(Component.text("광석을 넣어주세요.", NamedTextColor.RED));
            playErrorSound(player);
            return false;
        }

        // 첫 번째 슬롯 처리
        Ingredients oreType1 = null;
        if (hasOre1) {
            oreType1 = getOreType(oreItem1);
            if (oreType1 == null || canProcessOre(oreType1)) {
                return false;
            }
        }

        // 두 번째 슬롯 처리 (용량 확장 시)
        Ingredients oreType2 = null;
        if (hasOre2) {
            oreType2 = getOreType(oreItem2);
            if (oreType2 == null || canProcessOre(oreType2)) {
                return false;
            }
        }

        // 분쇄 시간 계산 (두 슬롯 중 더 긴 시간 사용)
        int grindTime1 = hasOre1 ? getGrindTime(oreType1) : 0;
        int grindTime2 = hasOre2 ? getGrindTime(oreType2) : 0;
        int maxGrindTime = Math.max(grindTime1, grindTime2);

        // 광석 제거
        if (hasOre1) setItem(ORE_SLOT_1, null);
        if (hasOre2) setItem(ORE_SLOT_2, null);

        // 분쇄 정보를 PDC에 저장 (두 슬롯 정보 모두)
        String processData = createProcessData(oreType1, oreType2);
        startCrafting(processData, maxGrindTime, player);

        return true;
    }

    private String createProcessData(Ingredients ore1, Ingredients ore2) {
        if (ore1 != null && ore2 != null) {
            return ore1.name() + "," + ore2.name();
        } else if (ore1 != null) {
            return ore1.name() + ",";
        } else if (ore2 != null) {
            return "," + ore2.name();
        }
        return "";
    }

    private boolean canPlaceOre(Ingredients oreType) {
        int upgradeLevel = getUpgradeLevel();
        int[] times = GRIND_TIMES.get(oreType);

        if (times == null) return false;

        // 현재 업그레이드 레벨에서 분쇄 가능한지 확인
        int timeIndex = switch (upgradeLevel) {
            case 0, 1 -> 0; // 기본 또는 용량 확장
            case 2 -> 1;    // 기어
            case 3 -> 2;    // 드릴
            default -> 0;
        };

        return times[timeIndex] != -1;
    }

    private boolean canProcessOre(Ingredients oreType) {
        int upgradeLevel = getUpgradeLevel();
        int[] times = GRIND_TIMES.get(oreType);

        if (times == null) return true;

        // 현재 업그레이드 레벨에서 분쇄 가능한지 확인
        int timeIndex = switch (upgradeLevel) {
            case 0, 1 -> 0; // 기본 또는 용량 확장
            case 2 -> 1;    // 기어
            case 3 -> 2;    // 드릴
            default -> 0;
        };

        return times[timeIndex] == -1;
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

        // 광석 배치 실패 시 기존 로직 (업그레이드 아이템이 아닌 경우만)
        if ((slot == ORE_SLOT_1 || slot == ORE_SLOT_2) && isOre(item)) {
            Ingredients oreType = getOreType(item);
            if (oreType != null && !canPlaceOre(oreType)) {
                if (oreType == Ingredients.PT || oreType == Ingredients.NI || oreType == Ingredients.TI) {
                    player.sendMessage(Component.text("해당 광석을 분쇄하려면 Al-Cu 합금 기어 설치가 필요합니다.", NamedTextColor.RED));
                    playErrorSound(player);
                }
            }
        }
    }
    private int getGrindTime(Ingredients oreType) {
        int upgradeLevel = getUpgradeLevel();
        int[] times = GRIND_TIMES.get(oreType);

        if (times == null) return -1;

        // 배열 인덱스: 0=기본, 1=기어, 2=드릴
        int timeIndex = switch (upgradeLevel) {
            case 0, 1 -> 0; // 기본 또는 용량 확장 (시간 동일)
            case 2 -> 1;    // 기어
            case 3 -> 2;    // 드릴
            default -> 0;
        };

        return times[timeIndex];
    }

    private void startCrafting(String processData, int grindTimeSeconds, Player player) {
        Location location = getModuleLocation();
        if (location != null) {
            long startTime = System.currentTimeMillis();

            // 두 슬롯 정보를 저장
            ModuleStateManager.saveModuleState(
                    location, "PROCESSING",
                    startTime, grindTimeSeconds, processData
            );

            changeState(MenuState.PROCESSING);
            updateMenu(player, MENU_TITLE_PROCESSING);

            // 실제로 광석이 있던 슬롯에만 타이머 표시
            String[] oreTypes = processData.split(",", -1);

            // 첫 번째 슬롯에 광석이 있었다면 48번에 타이머
            if (oreTypes.length > 0 && !oreTypes[0].isEmpty()) {
                timerItem(grindTimeSeconds, RESULT_SLOT_1);
            }

            // 두 번째 슬롯에 광석이 있었다면 50번에 타이머 (용량 확장 시만)
            if (getUpgradeLevel() >= 1 && oreTypes.length > 1 && !oreTypes[1].isEmpty()) {
                timerItem(grindTimeSeconds, RESULT_SLOT_2);
            }

            playSuccessSound(player, "minecraft:semicolon.grinder_module_start");
        }
    }

    @Override
    public void onProcessing(int remainingSeconds) {
        // PDC에서 어떤 슬롯에 광석이 있었는지 확인
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(getModuleLocation());
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            String processData = modulePdc.get(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING);
            String[] oreTypes = processData.split(",", -1);

            // 첫 번째 슬롯에 광석이 있었다면 48번에 타이머
            if (oreTypes.length > 0 && !oreTypes[0].isEmpty()) {
                timerItem(remainingSeconds, RESULT_SLOT_1);
            }

            // 두 번째 슬롯에 광석이 있었다면 50번에 타이머 (용량 확장 시만)
            if (getUpgradeLevel() >= 1 && oreTypes.length > 1 && !oreTypes[1].isEmpty()) {
                timerItem(remainingSeconds, RESULT_SLOT_2);
            }
        } else {
            // PDC 정보가 없으면 기본적으로 첫 번째 슬롯만
            timerItem(remainingSeconds, RESULT_SLOT_1);
        }
    }

    private int getUpgradeLevel() {
        Location location = getModuleLocation();
        if (location == null) return 0;

        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(location);
        if (modulePdc == null) return 0;

        return modulePdc.getOrDefault(PDCUtil.GRINDER_UPGRADE_LEVEL, PersistentDataType.INTEGER, 0);
    }

    private void setUpgradeLevel(int level) {
        Location location = getModuleLocation();
        if (location == null) return;

        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(location);
        if (modulePdc == null) {
            modulePdc = location.getChunk().getPersistentDataContainer()
                    .getAdapterContext().newPersistentDataContainer();
        }

        modulePdc.set(PDCUtil.GRINDER_UPGRADE_LEVEL, PersistentDataType.INTEGER, level);
        PDCUtil.saveModulePdc(location, modulePdc);
    }

    private boolean isUpgradeItem(ItemStack item) {
        return validateCustomModelData(item, Material.SADDLE, GRINDER_EXTENSION_CMD, GEAR_CMD, DRILL_CMD);
    }

    private int getUpgradeItemLevel(ItemStack item) {
        if (!isUpgradeItem(item)) return -1;

        int cmd = item.getItemMeta().getCustomModelData();
        if (cmd == GRINDER_EXTENSION_CMD) return 1;
        if (cmd == GEAR_CMD) return 2;
        if (cmd == DRILL_CMD) return 3;
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

        // 순서대로 업그레이드해야 함 (전 단계를 뛰어넘을 수 없음)
        return itemLevel == currentLevel + 1;
    }

    private void processUpgrade(ItemStack item) {
        int itemLevel = getUpgradeItemLevel(item);
        setUpgradeLevel(itemLevel);

        // 용량 확장(1단계) 시 배리어 제거
        if (itemLevel == 1) {
            for (int blockedSlot : BLOCKED_SLOTS) {
                setItem(blockedSlot, null);
            }
        }
    }

    private boolean isOre(ItemStack item) {
        if (item == null || item.getType() != Material.RED_DYE) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        return cmd >= 1 && cmd <= 9;
    }

    private Ingredients getOreType(ItemStack item) {
        if (!isOre(item)) return null;

        int cmd = item.getItemMeta().getCustomModelData();
        return switch (cmd) {
            case 1 -> Ingredients.MG;
            case 2 -> Ingredients.AL;
            case 3 -> Ingredients.FE;
            case 4 -> Ingredients.CU;
            case 5 -> Ingredients.LI;
            case 6 -> Ingredients.AU;
            case 7 -> Ingredients.PT;
            case 8 -> Ingredients.NI;
            case 9 -> Ingredients.TI;
            default -> null;
        };
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
            // PROCESSING 상태로 변경 시 레버 슬롯의 가이드 아이템 제거
            for (int leverSlot : LEVER_BUTTON) {
                setItem(leverSlot, null);
            }
        } else if (newState == MenuState.IDLE) {
            // IDLE 상태로 복귀 시 가이드 아이템 재배치
            setupItems();
            syncUpgradeItems();
        }
    }

    @Override
    public void onComplete() {
        // ModuleStateManager에서 분쇄 정보 가져오기
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(getModuleLocation());
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            String processData = modulePdc.get(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING);

            // "ore1,ore2" 형태의 데이터 파싱
            String[] oreTypes = processData.split(",", -1);

            // 첫 번째 슬롯 결과물 (3번 → 48번)
            if (oreTypes.length > 0 && !oreTypes[0].isEmpty()) {
                try {
                    Ingredients oreType1 = Ingredients.valueOf(oreTypes[0]);
                    Ingredients powderType1 = ORE_TO_POWDER_MAP.get(oreType1);
                    if (powderType1 != null) {
                        ItemStack powder1 = powderType1.getItemStack(1);
                        setItem(RESULT_SLOT_1, powder1);
                    }
                } catch (IllegalArgumentException ignored) { }
            }

            // 두 번째 슬롯 결과물 (5번 → 50번)
            if (oreTypes.length > 1 && !oreTypes[1].isEmpty()) {
                try {
                    Ingredients oreType2 = Ingredients.valueOf(oreTypes[1]);
                    Ingredients powderType2 = ORE_TO_POWDER_MAP.get(oreType2);
                    if (powderType2 != null) {
                        ItemStack powder2 = powderType2.getItemStack(1);
                        setItem(RESULT_SLOT_2, powder2);
                    }
                } catch (IllegalArgumentException e) {
                    // 잘못된 원료 타입인 경우 무시
                }
            }
        }
    }

    @Override
    protected void updateMenuForCurrentState(Player player) {
        switch (getCurrentState()) {
            case IDLE, COMPLETED -> {
                updateMenu(player, MENU_TITLE_IDLE);
                setupItems();
                syncUpgradeItems();
            }
            case PROCESSING -> {
                updateMenu(player, MENU_TITLE_PROCESSING);
                for (int leverSlot : LEVER_BUTTON) {
                    setItem(leverSlot, null);
                }
            }
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
    public void openForPlayer(Player player, Location moduleLocation) {
        this.moduleLocation = moduleLocation;
        reset();
        open(player);
        syncMenuState();
        updateMenuForCurrentState(player);

        syncUpgradeItems();
    }

    private void syncUpgradeItems() {
        int upgradeLevel = getUpgradeLevel();

        if (upgradeLevel >= 1) {
            ItemStack extension = createUpgradeItem(1);
            setItem(UPGRADE_1_SLOT, extension);
        }
        if (upgradeLevel >= 2) {
            ItemStack gear = createUpgradeItem(2);
            setItem(UPGRADE_2_SLOT, gear);
        }
        if (upgradeLevel >= 3) {
            ItemStack drill = createUpgradeItem(3);
            setItem(UPGRADE_3_SLOT, drill);
        }
    }

    @Override
    protected void reset() {
        super.reset();
    }

    private ItemStack createUpgradeItem(int level) {
        return switch (level) {
            case 1 -> Ingredients.GRINDER_EXTENSION.getItemStack(1);
            case 2 -> Ingredients.GEAR.getItemStack(1);
            case 3 -> Ingredients.DRILL.getItemStack(1);
            default -> null;
        };
    }

    @Override
    public int[] getCancelClickSlots() {
        if (getCurrentState() == MenuState.PROCESSING) {
            // 처리 중일 때는 YES 버튼들과 타이머 슬롯들 모두 클릭 가능
            int upgradeLevel = getUpgradeLevel();
            int[] allSlots;
            if (upgradeLevel >= 1) {
                // 용량 확장 시: 48번, 50번 타이머 모두 클릭 가능
                allSlots = new int[LEVER_BUTTON.length + 2];
                System.arraycopy(LEVER_BUTTON, 0, allSlots, 0, LEVER_BUTTON.length);
                allSlots[LEVER_BUTTON.length] = RESULT_SLOT_1;
                allSlots[LEVER_BUTTON.length + 1] = RESULT_SLOT_2;
            } else {
                // 기본: 48번 타이머만 클릭 가능
                allSlots = new int[LEVER_BUTTON.length + 1];
                System.arraycopy(LEVER_BUTTON, 0, allSlots, 0, LEVER_BUTTON.length);
                allSlots[LEVER_BUTTON.length] = RESULT_SLOT_1;
            }
            return allSlots;
        } else {
            return LEVER_BUTTON;
        }
    }

    @Override
    public int[] getAllowedPlacementSlots() {
        int upgradeLevel = getUpgradeLevel();
        if (upgradeLevel >= 1) {
            // 용량 확장 후: 두 슬롯 모두 사용 가능
            return new int[]{ORE_SLOT_1, ORE_SLOT_2, RESULT_SLOT_1, RESULT_SLOT_2,
                    UPGRADE_1_SLOT, UPGRADE_2_SLOT, UPGRADE_3_SLOT};
        } else {
            // 기본: 첫 번째 슬롯만 사용
            return new int[]{ORE_SLOT_1, RESULT_SLOT_1, UPGRADE_1_SLOT, UPGRADE_2_SLOT, UPGRADE_3_SLOT};
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (getCurrentState() == MenuState.PROCESSING) return false;

        if (slot == ORE_SLOT_1) {
            // 3번 슬롯: 항상 광석 배치 가능
            if (!isOre(item)) return false;

            Ingredients oreType = getOreType(item);
            if (oreType == null) return false;

            return canPlaceOre(oreType);
        } else if (slot == ORE_SLOT_2) {
            // 5번 슬롯: 용량 확장 후에만 광석 배치 가능
            if (getUpgradeLevel() < 1) return false;

            if (!isOre(item)) return false;

            Ingredients oreType = getOreType(item);
            if (oreType == null) return false;

            return canPlaceOre(oreType);
        } else if (slot == RESULT_SLOT_1 || slot == RESULT_SLOT_2) {
            return false; // 결과 슬롯에는 직접 배치 불가
        } else if (slot == UPGRADE_1_SLOT || slot == UPGRADE_2_SLOT || slot == UPGRADE_3_SLOT) {
            return canPlaceUpgrade(slot, item); // 업그레이드 순서 확인
        }
        return false;
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
    public int getMaxPlaceAmount(int slot, ItemStack item) {
        return 1;
    }

    @Override
    public boolean onExtraClick(InventoryClickEvent e, int slot) {
        Player player = (Player) e.getWhoClicked();

        ItemStack clickedItem = e.getCurrentItem();
        if (isGuideItem(clickedItem)) {
            e.setCancelled(true);
            return true;
        }

        // === 배리어 아이템 제거 방지 ===
        if (slot == ORE_SLOT_2 || slot == RESULT_SLOT_2) {
            if (clickedItem != null && clickedItem.getType() == Material.BARRIER) {
                e.setCancelled(true);
                player.sendMessage(Component.text("업그레이드 전에는 사용할 수 없습니다.", NamedTextColor.RED));
                playErrorSound(player);
                return true;
            }
        }

        // 🆕 결과 슬롯 클릭 공통 처리 (48번, 50번 모두)
        if (onResultSlotClick(e, RESULT_SLOT_1, player)) {
            return true;
        }
        if (getUpgradeLevel() >= 1 && onResultSlotClick(e, RESULT_SLOT_2, player)) {
            return true;
        }

        // === 업그레이드 아이템 제거 방지 ===
        if (slot == UPGRADE_1_SLOT || slot == UPGRADE_2_SLOT || slot == UPGRADE_3_SLOT) {
            if (isUpgradeItem(clickedItem)) {
                // 업그레이드 아이템 제거 시도를 차단
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
        // 광석 슬롯의 아이템은 플레이어에게 반환 (처리 중이 아닐 때만)
        if (getCurrentState() != MenuState.PROCESSING) {
            // 3번 슬롯 광석 반환
            ItemStack oreItem1 = event.getView().getTopInventory().getItem(ORE_SLOT_1);
            if (!isGuideItem(oreItem1)) { // 가이드 아이템이 아닐 때만 반환
                returnItemToPlayer(player, oreItem1);
            }

            // 5번 슬롯 광석 반환 (용량 확장 시)
            if (getUpgradeLevel() >= 1) {
                ItemStack oreItem2 = event.getView().getTopInventory().getItem(ORE_SLOT_2);
                if (!isGuideItem(oreItem2)) { // 가이드 아이템이 아닐 때만 반환
                    returnItemToPlayer(player, oreItem2);
                }
            }
        }

        // 완료된 아이템 자동 처리 (48번, 50번 슬롯 모두)
        if (getCurrentState() == MenuState.COMPLETED) {
            // 48번 슬롯 결과물 처리
            onCompletedItemOnClose(event, player, RESULT_SLOT_1);

            // 50번 슬롯 결과물 처리 (용량 확장 시)
            if (getUpgradeLevel() >= 1) {
                ItemStack completedItem2 = event.getView().getTopInventory().getItem(RESULT_SLOT_2);
                if (completedItem2 != null && !completedItem2.getType().isAir()) {
                    returnItemToPlayer(player, completedItem2);
                    setItem(RESULT_SLOT_2, null);
                }
            }

            // 상태를 IDLE로 변경 (한 번만)
            Location location = getModuleLocation();
            if (location != null) {
                ModuleStateManager.updateModuleState(location, "IDLE");
            }
        }
    }

    @Override
    protected void onCompletedItemPickup(Player player, int resultSlot) {
        if (getCurrentState() == MenuState.COMPLETED &&
                (resultSlot == RESULT_SLOT_1 || resultSlot == RESULT_SLOT_2)) {
            Location location = getModuleLocation();
            if (location != null) {
                ModuleStateManager.updateModuleState(location, "IDLE");
                changeState(MenuState.IDLE);
                updateMenuTitle(player);

                // 클릭한 슬롯만 초기화
                setItem(resultSlot, null);
            }
        }
    }
}