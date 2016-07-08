/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.battered;

import com.tealcube.minecraft.bukkit.config.VersionedConfiguration;
import com.tealcube.minecraft.bukkit.config.VersionedSmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;

public class BatteredPlugin extends FacePlugin implements Listener {
    private VersionedSmartYamlConfiguration configYAML;

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll((Listener) this);
        configYAML = null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        if (configYAML.getStringList("ignored-worlds").contains(event.getPlayer().getWorld().getName())) {
            return;
        }
        final Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] armorContents = inventory.getArmorContents();

        player.sendMessage(ChatColor.YELLOW + "Your equipment lost some durability from dying!");

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                continue;
            }
            ItemStack itemStack = contents[i].clone();
            if (itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!itemStack.getType().name().contains("SWORD") && !itemStack.getType().name().contains("AXE") &&
                    !itemStack.getType().name().contains("SPADE") && !itemStack.getType().name().contains("HOE") &&
                    itemStack.getType() != Material.BOW && itemStack.getType() != Material.SHIELD)  {
                continue;
            }
            ItemMeta newMeta = itemStack.getItemMeta();
            newMeta.spigot().setUnbreakable(false);
            itemStack.setItemMeta(newMeta);
            short dura = (short) ((0.22 * itemStack.getType().getMaxDurability()) + itemStack.getDurability());
            itemStack.setDurability((short) Math.min(dura, itemStack.getType().getMaxDurability()));
            if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                player.sendMessage(ChatColor.RED + "Oh no! One of your tools has dropped below zero durability and " +
                        "was destroyed!");
                contents[i] = null;
                continue;
            }
            if (itemStack.getDurability() > itemStack.getType().getMaxDurability() * 0.75) {
                player.sendMessage(ChatColor.YELLOW + "Watch out! One of your tools is low on durability and is in " +
                        "danger of breaking!");
            }
            contents[i] = itemStack;
        }

        for (int i = 0; i < armorContents.length; i++) {
            if (armorContents[i] == null) {
                continue;
            }
            ItemStack itemStack = armorContents[i].clone();
            if (itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!itemStack.getType().name().contains("BOOTS") && !itemStack.getType().name().contains("LEGGINGS") &&
                    !itemStack.getType().name().contains("CHESTPLATE") && !itemStack.getType().name().contains("HELMET")) {
                continue;
            }
            ItemMeta newMeta = itemStack.getItemMeta();
            newMeta.spigot().setUnbreakable(false);
            itemStack.setItemMeta(newMeta);
            short dura = (short) ((0.17 * itemStack.getType().getMaxDurability()) + itemStack.getDurability());
            itemStack.setDurability((short) Math.min(dura, itemStack.getType().getMaxDurability()));
            if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                player.sendMessage(ChatColor.RED + "Oh no! A piece of your armor has dropped below zero durability " +
                        "and was destroyed!");
                armorContents[i] = null;
                continue;
            }
            if (itemStack.getDurability() > itemStack.getType().getMaxDurability() * 0.75) {
                player.sendMessage(ChatColor.YELLOW + "Watch out! A piece of your armor is low on durability and is " +
                        "in danger of breaking!");
            }
            armorContents[i] = itemStack;
        }

        inventory.setContents(contents);
        inventory.setArmorContents(armorContents);

        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEventLowest(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (configYAML.getStringList("ignored-worlds").contains(event.getEntity().getWorld().getName())) {
            return;
        }
        final Player player = event.getEntity();
        final World world = event.getEntity().getWorld();
        player.updateInventory();

        Inventory inventory = player.getInventory();
        ItemStack[] inventoryContents = inventory.getContents().clone();
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack itemStack = inventoryContents[i];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            ItemStack cloned = itemStack.clone();
            if ((i >= 0 && i <= 8) || (i >= 36 && i <= 40)) {
                if (cloned.getType().getMaxDurability() < 30 || cloned.getType() != Material.BOOK) {
                    continue;
                }
                int dropAmount = Math.min(Math.max(1, (int) (cloned.getAmount() * 0.75)), cloned.getAmount());
                int keepAmount = cloned.getAmount() - dropAmount;
                if (keepAmount > 0) {
                    cloned.setAmount(keepAmount);
                    inventoryContents[i] = cloned.clone();
                } else {
                    inventoryContents[i] = null;
                }
                cloned.setAmount(dropAmount);
                world.dropItemNaturally(event.getEntity().getLocation(), cloned);
            } else {
                world.dropItemNaturally(event.getEntity().getLocation(), cloned);
                inventoryContents[i] = null;
            }
        }
        inventory.setContents(inventoryContents);
        player.updateInventory();
    }

}
