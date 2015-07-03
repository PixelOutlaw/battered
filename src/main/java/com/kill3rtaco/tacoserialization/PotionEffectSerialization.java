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

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * A class to help with the serialization of PotionEffects. The serialization technique is similar to that
 * of serializing enchantments. The process can be as explained as such:
 * <br/><br/>
 * <pre>
 * String serializedEffects = "";
 * for(PotionEffect e : effects){
 *     serializedEffects += e.getType().getId() + ":" + e.getDuration() + ":" + e.getAmplifier() + ";";
 * }
 * </pre>
 * <br/>
 * So that it would follow this pattern:<br/>
 * <pre>id:duration:amplifier;...</pre>
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class PotionEffectSerialization {
	
	protected PotionEffectSerialization() {
	}
	
	/**
	 * Serialize a Collection of PotionEffects into a string that follows the regex
	 * <pre>([0-9]+:[0-9]+:[0-9]+;)+</pre>
	 * @param effects The PotionEffects to serialize
	 * @return The serialized PotionEffects
	 */
	public static String serializeEffects(Collection<PotionEffect> effects) {
		String serialized = "";
		for(PotionEffect e : effects) {
			serialized += e.getType().getId() + ":" + e.getDuration() + ":" + e.getAmplifier() + ";";
		}
		return serialized;
	}
	
	/**
	 * Get a Collection of PotionEffects from the given potion effect code
	 * @param serializedEffects The potion effect code to decode from
	 * @return A Collection of PotionEffects from the given potion effect code
	 */
	public static Collection<PotionEffect> getPotionEffects(String serializedEffects) {
		ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		if(serializedEffects.isEmpty())
			return effects;
		String[] effs = serializedEffects.split(";");
		for(int i = 0; i < effs.length; i++) {
			String[] effect = effs[i].split(":");
			if(effect.length < 3)
				throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): split must at least have a length of 3");
			if(!Util.isNum(effect[0]))
				throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): id is not an integer");
			if(!Util.isNum(effect[1]))
				throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): duration is not an integer");
			if(!Util.isNum(effect[2]))
				throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): amplifier is not an integer");
			int id = Integer.parseInt(effect[0]);
			int duration = Integer.parseInt(effect[1]);
			int amplifier = Integer.parseInt(effect[2]);
			PotionEffectType effectType = PotionEffectType.getById(id);
			if(effectType == null)
				throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): no PotionEffectType with id of " + id);
			PotionEffect e = new PotionEffect(effectType, duration, amplifier);
			effects.add(e);
		}
		return effects;
	}
	
	/**
	 * Add the given PotionEffects to a LivingEntity
	 * @param code The PotionEffects to add
	 * @param entity The entity to add the PotionEffects
	 */
	public static void addPotionEffects(String code, LivingEntity entity) {
		entity.addPotionEffects(getPotionEffects(code));
	}
	
	/**
	 * Remove any current PotionEffects from a LivingEntity then add the given effects
	 * @param code The PotionEffects to add
	 * @param entity The entity to set the PotionEffects
	 */
	public static void setPotionEffects(String code, LivingEntity entity) {
		for(PotionEffect effect : entity.getActivePotionEffects()) {
			entity.removePotionEffect(effect.getType());
		}
		addPotionEffects(code, entity);
	}
	
}
