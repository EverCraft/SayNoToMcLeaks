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
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class WrapperPlayServerChat extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.CHAT;

	public WrapperPlayServerChat() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerChat(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve the chat message.
	 * <p>
	 * Limited to 32767 bytes
	 * 
	 * @return The current message
	 */
	public WrappedChatComponent getMessage() {
		return handle.getChatComponents().read(0);
	}

	/**
	 * @deprecated Renamed to {@link #getMessage()}
	 */
	@Deprecated
	public WrappedChatComponent getJsonData() {
		return getMessage();
	}

	/**
	 * Set the message.
	 * 
	 * @param value - new value.
	 */
	public void setMessage(WrappedChatComponent value) {
		handle.getChatComponents().write(0, value);
	}

	/**
	 * @deprecated Renamed to {@link #setMessage(WrappedChatComponent)}
	 */
	@Deprecated
	public void setJsonData(WrappedChatComponent value) {
		setMessage(value);
	}

	/**
	 * Retrieve Position.
	 * <p>
	 * Notes: 0 - Chat (chat box) ,1 - System Message (chat box), 2 - Above
	 * action bar
	 * 
	 * @return The current Position
	 */
	public byte getPosition() {
		return handle.getBytes().read(0);
	}

	/**
	 * Set Position.
	 * 
	 * @param value - new value.
	 */
	public void setPosition(byte value) {
		handle.getBytes().write(0, value);
	}

}
