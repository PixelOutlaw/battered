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
package com.kill3rtaco.tacoserialization;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Wolf;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of Wolves.
 * <br/><br/>
 * This serialization class supports optional serialization.<br/>
 * TacoSerialization will create a folder in your server plugins directory (wherever that may be) called
 * 'TacoSerialization'. Inside the folder will be a config.yml file. Various values can be turned off to
 * prevent some keys from being generated.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class WolfSerialization {
	
	protected WolfSerialization() {
	}
	
	/**
	 * Serialize a Wolf
	 * @param wolf The Wolf to serialize
	 * @return The serialized Wolf
	 */
	public static JSONObject serializeWolf(Wolf wolf) {
		try {
			JSONObject root = LivingEntitySerialization.serializeEntity(wolf);
			if(shouldSerialize("collar-color"))
				root.put("collar-color", ColorSerialization.serializeColor(wolf.getCollarColor().getColor()));
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize a wolf as a string
	 * @param wolf The wolf to serialize
	 * @return The serialization string
	 */
	public static String serializeWolfAsString(Wolf wolf) {
		return serializeWolfAsString(wolf, false);
	}
	
	/**
	 * Serialize a wolf as a string
	 * @param wolf The wolf to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeWolfAsString(Wolf wolf, boolean pretty) {
		return serializeWolfAsString(wolf, pretty, 5);
	}
	
	/**
	 * Serialize a wolf as a string
	 * @param wolf The wolf to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeWolfAsString(Wolf wolf, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeWolf(wolf).toString(indentFactor);
			} else {
				return serializeWolf(wolf).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a wolf in a desired location with desired stats
	 * @param location Where to spawn the wolf
	 * @param stats The desired stats
	 * @return The wolf spawned
	 */
	public static Wolf spawnWolf(Location location, String stats) {
		try {
			return spawnWolf(location, new JSONObject(stats));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a wolf in a desired location with desired stats
	 * @param location Where to spawn the wolf
	 * @param stats The desired stats
	 * @return The wolf spawned
	 */
	public static Wolf spawnWolf(Location location, JSONObject stats) {
		try {
			Wolf wolf = (Wolf) LivingEntitySerialization.spawnEntity(location, stats);
			if(stats.has("collar-color"))
				wolf.setCollarColor(DyeColor.getByColor(ColorSerialization.getColor(stats.getString("collar-color"))));
			return wolf;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Test if a certain key should be serialized
	 * @param key The key to test
	 * @return Whether the key should be serilaized or not
	 */
	public static boolean shouldSerialize(String key) {
		return SerializationConfig.getShouldSerialize("wolf." + key);
	}
	
}
