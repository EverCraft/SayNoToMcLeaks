/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.comphenix.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class WrapperPlayClientTabComplete extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.TAB_COMPLETE;

	public WrapperPlayClientTabComplete() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayClientTabComplete(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve Text.
	 * 
	 * @return The current Text
	 */
	public String getText() {
		return handle.getStrings().read(0);
	}

	/**
	 * Set Text.
	 * 
	 * @param value - new value.
	 */
	public void setText(String value) {
		handle.getStrings().write(0, value);
	}

	/**
	 * Retrieve Has Position.
	 * 
	 * @return The current Has Position
	 */
	public BlockPosition getHasPosition() {
		return handle.getBlockPositionModifier().read(0);
	}

	/**
	 * Set Has Position.
	 * 
	 * @param value - new value.
	 */
	public void setHasPosition(BlockPosition value) {
		handle.getBlockPositionModifier().write(0, value);
	}

	/**
	 * Retrieve Looked at block.
	 * <p>
	 * Notes: the position of the block being looked at. Only sent if the
	 * previous field is true
	 * 
	 * @return The current Looked at block
	 */
	public BlockPosition getLookedAtBlock() {
		return handle.getBlockPositionModifier().read(0);
	}

	/**
	 * Set Looked at block.
	 * 
	 * @param value - new value.
	 */
	public void setLookedAtBlock(BlockPosition value) {
		handle.getBlockPositionModifier().write(0, value);
	}

}
