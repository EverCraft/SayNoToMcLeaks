package fr.evercraft.saynotomcleaks;

import com.comphenix.packetwrapper.WrapperLoginClientEncryptionBegin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class Listeners {
	private final SayNoToMcLeaks plugin;
	
	public Listeners(SayNoToMcLeaks plugin) {
		this.plugin = plugin;
		
		this.plugin.getProtocolManager().addPacketListener(new PacketAdapter(
				this.plugin,
		        ListenerPriority.HIGH, 
		        PacketType.Login.Client.ENCRYPTION_BEGIN) {
		    @Override
		    public void onPacketReceiving(PacketEvent event) {
		        if (event.getPacketType() == PacketType.Login.Client.ENCRYPTION_BEGIN) {
		            WrapperLoginClientEncryptionBegin packet = new WrapperLoginClientEncryptionBegin(event.getPacket());
		            event.getPlayer().sendMessage("SharedSecret : " + packet.getSharedSecret());
		            event.getPlayer().sendMessage("VerifyToken : " + packet.getVerifyToken());
		        }
		    }
		});
	}
}
