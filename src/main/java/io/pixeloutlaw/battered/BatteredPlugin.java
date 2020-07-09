/*
 * The MIT License
 * Copyright © 2015 Pixel Outlaw
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
package io.pixeloutlaw.battered;

import io.pixeloutlaw.battered.listeners.DeathListener;
import io.pixeloutlaw.battered.listeners.ItemDamageListener;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;

import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.io.File;

public class BatteredPlugin extends FacePlugin {

  private VersionedSmartYamlConfiguration configYAML;

  @Override
  public void enable() {
    configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
        getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);

    Bukkit.getPluginManager().registerEvents(new DeathListener(this, configYAML), this);
    Bukkit.getPluginManager().registerEvents(new ItemDamageListener(), this);
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);
    configYAML = null;
  }
}
