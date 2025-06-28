package semicolon.murinn.module.menu.internal;

import semicolon.murinn.module.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {
    @EventHandler
    public void onClickScreen(InventoryClickEvent e) {
        InventoryView view = e.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();

        if (holder == null) return;
        if (!(holder instanceof AbstractMenu)) return;

        // AbstractStateMenu 타입이고 버튼 클릭인 경우 특별 처리
        if (holder instanceof AbstractStateMenu stateMenu &&
                stateMenu.getCurrentState() == AbstractStateMenu.MenuState.PROCESSING) {
            // 취소 슬롯 확인
            int slot = e.getRawSlot(); // 원시 슬롯 사용
            for (int cancelSlot : stateMenu.getCancelClickSlots()) {
                if (slot == cancelSlot) {
                    ((AbstractMenu) holder).onClick(e);
                    return;
                }
            }
        }

        ((AbstractMenu) holder).onClick(e);

        if (holder instanceof Interactable interactable) {
            int slot = e.getSlot();

            // 메뉴 특화 로직을 해당 메뉴에 위임
            if (e.getClickedInventory() == view.getTopInventory()) {
                // 커스텀 클릭 처리 시도 (각 메뉴가 자신의 특별한 케이스를 처리)
                if (interactable.onExtraClick(e, slot)) return;

                if (interactable.getCancelClickSlots() != null) {
                    for (int cancelSlot : interactable.getCancelClickSlots()) {
                        if (slot == cancelSlot) {
                            e.setCancelled(true);
                            break;
                        }
                    }
                }

                // 일반적인 아이템 배치 처리 (모든 메뉴에 공통)
                if (e.getAction() == InventoryAction.PLACE_ALL ||
                        e.getAction() == InventoryAction.PLACE_SOME ||
                        e.getAction() == InventoryAction.PLACE_ONE ||
                        e.getAction() == InventoryAction.SWAP_WITH_CURSOR) {

                    boolean isAllowedSlot = false;
                    for (int allowedSlot : interactable.getAllowedPlacementSlots()) {
                        if (slot == allowedSlot) {
                            isAllowedSlot = true;
                            break;
                        }
                    }

                    if (!isAllowedSlot || !interactable.canPlaceItem(slot, e.getCursor())) {
                        e.setCancelled(true);
                        return;
                    }

                    Bukkit.getScheduler().runTask(Main.plugin(), () -> interactable.onItemPlaced(slot, e.getClickedInventory().getItem(slot)));
                } else if (e.getAction() == InventoryAction.PICKUP_ALL ||
                        e.getAction() == InventoryAction.PICKUP_SOME ||
                        e.getAction() == InventoryAction.PICKUP_HALF ||
                        e.getAction() == InventoryAction.PICKUP_ONE) {
                    ItemStack clickedItem = e.getCurrentItem();
                    if (interactable instanceof AbstractStateMenu stateMenu &&
                            stateMenu.isGuideItem(clickedItem)) {
                        e.setCancelled(true);
                    }
                } else if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    e.setCancelled(true);
                }
            } else if (e.getClick().isShiftClick() && e.getClickedInventory() == view.getBottomInventory()) {
                onShiftClick(e, view, interactable);
            }
        }
    }

    private void onShiftClick(InventoryClickEvent e, InventoryView view, Interactable interactable) {
        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem != null && !clickedItem.getType().isAir()) {
            boolean callBack = false;

            for (int allowedSlot : interactable.getAllowedPlacementSlots()) {
                ItemStack slotItem = view.getTopInventory().getItem(allowedSlot);
                boolean isSlotAvailable = slotItem == null || slotItem.getType().isAir();

                if (interactable instanceof AbstractStateMenu stateMenu) {
                    isSlotAvailable = isSlotAvailable || stateMenu.isGuideItem(slotItem);
                }

                if (isSlotAvailable) {
                    if (interactable.canPlaceItem(allowedSlot, clickedItem)) {
                        int maxPlaceAmount = interactable.getMaxPlaceAmount(allowedSlot, clickedItem);
                        int maxAmount = Math.min(maxPlaceAmount, clickedItem.getAmount());

                        if (maxAmount > 0) {
                            // 배치할 아이템 생성
                            ItemStack itemToPlace = clickedItem.clone();
                            itemToPlace.setAmount(maxAmount);

                            // 원본 아이템에서 배치한 개수만큼 차감
                            if (clickedItem.getAmount() > maxAmount) {
                                clickedItem.setAmount(clickedItem.getAmount() - maxAmount);
                            } else {
                                view.getBottomInventory().setItem(e.getSlot(), null);
                            }

                            // 슬롯에 아이템 배치
                            view.getTopInventory().setItem(allowedSlot, itemToPlace);

                            Bukkit.getScheduler().runTask(Main.plugin(), () ->
                                    interactable.onItemPlaced(allowedSlot, itemToPlace));
                        }

                        return;
                    } else if (!callBack) {
                        interactable.onItemPlacementFailed(allowedSlot, clickedItem, (Player) e.getWhoClicked());
                        callBack = true;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDragScreen(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder == null) return;
        if (!(holder instanceof AbstractMenu)) return;
        if (holder instanceof Interactable) event.setCancelled(true);
    }

    @EventHandler
    public void onCloseScreen(InventoryCloseEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder == null) return;
        if (!(holder instanceof AbstractMenu)) return;

        if (holder instanceof Interactable interactable) {
            interactable.onInventoryClose(event, (Player) event.getPlayer());
        }
    }
}