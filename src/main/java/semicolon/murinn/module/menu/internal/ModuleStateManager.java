package semicolon.murinn.module.menu.internal;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.util.PDCUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ModuleStateManager {
    public static void updateModuleState(Location moduleLocation, String state) {
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(moduleLocation);
        if (modulePdc == null) return;

        long startTime = 0;
        int duration = 0;
        String resultData = null;

        if (modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            resultData = modulePdc.get(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING);
        }

        saveModuleState(moduleLocation, state, startTime, duration, resultData);
    }

    public static String getModuleState(Location moduleLocation) {
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(moduleLocation);
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_STATE, PersistentDataType.STRING)) {
            return modulePdc.get(PDCUtil.MODULE_STATE, PersistentDataType.STRING);
        }
        return "IDLE";
    }


    public static void saveModuleState(Location moduleLocation, String state, long startTime, int durationSeconds, String resultData) {
        long completionTime = 0;
        if ("PROCESSING".equals(state)) {
            long var1 = getCompletionTime(moduleLocation);

            if (var1 == 0) completionTime = startTime + (durationSeconds * 1000L);
            else completionTime = var1;
        }

        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(moduleLocation);
        if (modulePdc == null) return;

        modulePdc.set(PDCUtil.MODULE_STATE, PersistentDataType.STRING, state);
        modulePdc.set(PDCUtil.MODULE_COMPLETION_TIME, PersistentDataType.LONG, completionTime);

        if (resultData != null) {
            modulePdc.set(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING, resultData);
        } else if (modulePdc.has(PDCUtil.MODULE_PROCESS_RESULT, PersistentDataType.STRING)) {
            modulePdc.remove(PDCUtil.MODULE_PROCESS_RESULT);
        }

        PDCUtil.saveModulePdc(moduleLocation, modulePdc);
    }

    public static long getCompletionTime(Location moduleLocation) {
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(moduleLocation);
        if (modulePdc != null && modulePdc.has(PDCUtil.MODULE_COMPLETION_TIME, PersistentDataType.LONG)) {
            return modulePdc.get(PDCUtil.MODULE_COMPLETION_TIME, PersistentDataType.LONG);
        }

        return 0;
    }

    public static void restoreAllModuleTimers() {
        try {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    try {
                        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
                        if (!chunkPdc.has(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER)) continue;

                        PersistentDataContainer moduleMap = chunkPdc.get(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER);
                        if (moduleMap == null) continue;

                        for (NamespacedKey key : moduleMap.getKeys()) {
                            try {
                                if (!key.getKey().startsWith("loc_") ||
                                        !moduleMap.has(key, PersistentDataType.TAG_CONTAINER)) {
                                    continue;
                                }

                                PersistentDataContainer modulePdc = moduleMap.get(key, PersistentDataType.TAG_CONTAINER);
                                if (modulePdc == null ||
                                        !modulePdc.has(PDCUtil.MODULE_STATE, PersistentDataType.STRING) ||
                                        !modulePdc.has(PDCUtil.MODULE_TYPE, PersistentDataType.STRING)) {
                                    continue;
                                }

                                String state = modulePdc.get(PDCUtil.MODULE_STATE, PersistentDataType.STRING);
                                if (!"PROCESSING".equals(state)) continue;

                                // 모듈 위치 추출
                                String locKey = key.getKey().substring(4);
                                Location location = PDCUtil.parseLocationKey(locKey);
                                if (location == null) continue;

                                String moduleType = modulePdc.get(PDCUtil.MODULE_TYPE, PersistentDataType.STRING);
                                long completionTime = getCompletionTime(location);
                                long currentTime = System.currentTimeMillis();

                                // 이미 완료됐어야 하는 경우 상태 업데이트
                                if (currentTime >= completionTime) {
                                    Main.plugin().getLogger().info("모듈 완료 상태로 설정: " + moduleType);
                                    saveModuleState(location, "COMPLETED", 0, 0, null);
                                }
                            } catch (Exception e) {
                                Main.plugin().getLogger().warning("모듈 데이터 처리 오류: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        Main.plugin().getLogger().warning("[ModuleStateManager] 청크 처리 오류: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Main.plugin().getLogger().severe("[ModuleStateManager] 모듈 상태 확인 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}