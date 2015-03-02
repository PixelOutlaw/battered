/*
 * This file is part of Battered, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.battered;

import com.kill3rtaco.tacoserialization.SingleItemSerialization;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.shade.config.SmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.facecore.shade.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatteredPlugin extends FacePlugin implements Listener {

    private Map<UUID, List<String>> inventoryMap;
    private SmartYamlConfiguration dataFile;

    @Override
    public void enable() {
        inventoryMap = new HashMap<UUID, List<String>>();
        dataFile = new SmartYamlConfiguration(new File(getDataFolder(), "data.yml"));
        dataFile.load();
        for (String s : dataFile.getKeys(false)) {
            UUID uuid = UUID.fromString(s);
            List<String> value = dataFile.getStringList(s);
            inventoryMap.put(uuid, value);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void disable() {
        for (String key : dataFile.getKeys(true)) {
            dataFile.set(key, null);
        }
        for (Map.Entry<UUID, List<String>> entry : inventoryMap.entrySet()) {
            dataFile.set(entry.getKey().toString(), entry.getValue());
        }
        dataFile.save();
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<String> items;
        if (!inventoryMap.containsKey(player.getUniqueId())) {
            items = dataFile.getStringList(player.getUniqueId().toString());
        } else {
            items = inventoryMap.get(player.getUniqueId());
        }
        List<ItemStack> itemStacks = new ArrayList<ItemStack>();
        for (String s : items) {
            ItemStack is = SingleItemSerialization.getItem(s);
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            itemStacks.add(is);
        }
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && itemStack.getType() != null) {
                player.getInventory().addItem(itemStack);
            }
        }
        inventoryMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory playerInventory = player.getInventory();
        int nullChecker = 0;
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack doubleDeathChecker = playerInventory.getItem(i);
            if (doubleDeathChecker == null || doubleDeathChecker.getType() == Material.AIR) {
                nullChecker++;
            }
        }
        if (nullChecker >= playerInventory.getSize()) {
            return;
        }
        List<ItemStack> drops = new ArrayList<ItemStack>();
        List<ItemStack> keeps = new ArrayList<ItemStack>();
        event.getDrops().clear();

        for (int i = 0; i < playerInventory.getSize(); i++) {
            if (playerInventory.getItem(i) == null || playerInventory.getItem(i).getType() == Material.AIR) {
                continue;
            }
            HiltItemStack itemStack = new HiltItemStack(playerInventory.getItem(i));
            if (i >= 9) {
                drops.add(itemStack);
                continue;
            }

            short maxDurability = itemStack.getType().getMaxDurability();
            short curDurability = itemStack.getDurability();
            short newDurability = (short) (curDurability + 0.2 * maxDurability);

            if (maxDurability > 1 && newDurability >= maxDurability) {
                MessageUtils.sendMessage(player, "<red>Dang! Your item, " + itemStack.getName() + "<red>, has broken!");
                continue;
            }

            int amount = itemStack.getAmount();
            int newAmount = (int) (0.25 * amount);
            int droppedAmount = amount - newAmount;
            if (newAmount <= 0 && amount > 1) {
                continue;
            }
            if (itemStack.getType() == Material.WOOD_AXE || itemStack.getType() == Material.STONE_AXE ||
                    itemStack.getType() == Material.IRON_AXE || itemStack.getType() == Material.GOLD_AXE ||
                    itemStack.getType() == Material.DIAMOND_AXE || itemStack.getType() == Material.DIAMOND_SWORD ||
                    itemStack.getType() == Material.GOLD_SWORD || itemStack.getType() == Material.IRON_SWORD ||
                    itemStack.getType() == Material.STONE_SWORD || itemStack.getType() == Material.WOOD_SWORD ||
                    itemStack.getType() == Material.BOW) {
                itemStack.setDurability(newDurability);
                keeps.add(itemStack);
            } else {
                itemStack.setAmount(droppedAmount);
                HiltItemStack dropItemStack = new HiltItemStack(itemStack);
                dropItemStack.setItemMeta(itemStack.getItemMeta());
                dropItemStack.setAmount(Math.max(droppedAmount, 1));
                drops.add(dropItemStack);
                itemStack.setAmount(newAmount);
                keeps.add(itemStack);
            }
        }

        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            HiltItemStack his = new HiltItemStack(itemStack);
            short maxDurability = his.getType().getMaxDurability();
            short curDurability = his.getDurability();
            short newDurability = (short) (curDurability + 0.2 * maxDurability);
            if (maxDurability > 1 && newDurability < maxDurability) {
                itemStack.setDurability(newDurability);
                keeps.add(itemStack);
            } else {
                MessageUtils.sendMessage(player, "<gray>Ay yo nigga, yo item broke: " + his.getName());
            }
        }

        List<String> keepStrings = new ArrayList<String>();
        for (ItemStack keep : keeps) {
            if (keep == null || keep.getType() == Material.AIR) {
                continue;
            }
            keepStrings.add(SingleItemSerialization.serializeItemAsString(keep));
        }
        inventoryMap.put(player.getUniqueId(), keepStrings);

        for (ItemStack drop : drops) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    // SO INEFFICIENT ITS PAINFUL YO!
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() == null) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            PlayerInventory inv = ((Player) event.getEntity()).getInventory();
            final String name = event.getEntity().getName();

            final Short arr[] = new Short[4];
            if (inv.getHelmet() != null) {
                arr[0] = inv.getHelmet().getDurability();
            }
            if (inv.getChestplate() != null) {
                arr[1] = inv.getChestplate().getDurability();
            }
            if (inv.getLeggings() != null) {
                arr[2] = inv.getLeggings().getDurability();
            }
            if (inv.getBoots() != null) {
                arr[3] = inv.getBoots().getDurability();
            }

            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    Player pa = getServer().getPlayerExact(name);
                    if (pa != null) {
                        PlayerInventory inv2 = pa.getInventory();
                        if (inv2 != null) {
                            if (inv2.getHelmet() != null) {
                                inv2.getHelmet().setDurability(arr[0]);
                            }
                            if (inv2.getChestplate() != null) {
                                inv2.getChestplate().setDurability(arr[1]);
                            }
                            if (inv2.getLeggings() != null) {
                                inv2.getLeggings().setDurability(arr[2]);
                            }
                            if (inv2.getBoots() != null) {
                                inv2.getBoots().setDurability(arr[3]);
                            }
                        }
                    }
                }
            }, 1L);
        }

        if (event.getDamager() == null) {
            return;
        }

        if (event.getDamager() instanceof Player) {
            Player p = (Player) event.getDamager();
            if (p.getItemInHand() != null) {
                ItemStack stack = p.getItemInHand();
                if (stack.getType() == Material.WOOD_AXE || stack.getType() == Material.STONE_AXE ||
                        stack.getType() == Material.IRON_AXE || stack.getType() == Material.GOLD_AXE ||
                        stack.getType() == Material.DIAMOND_AXE || stack.getType() == Material.DIAMOND_SWORD ||
                        stack.getType() == Material.GOLD_SWORD || stack.getType() == Material.IRON_SWORD ||
                        stack.getType() == Material.STONE_SWORD || stack.getType() == Material.WOOD_SWORD ||
                        stack.getType() == Material.BOW || stack.getType() == Material.FISHING_ROD) {
                    final String name = p.getName();
                    final short prevDura = stack.getDurability();
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        public void run() {
                            Player pa = getServer().getPlayerExact(name);
                            if (pa != null) {
                                PlayerInventory inv2 = pa.getInventory();
                                if (inv2.getItemInHand() != null) {
                                    inv2.getItemInHand().setDurability(prevDura);
                                }
                            }
                        }
                    }, 1L);
                }
            }
        }
    }

}
