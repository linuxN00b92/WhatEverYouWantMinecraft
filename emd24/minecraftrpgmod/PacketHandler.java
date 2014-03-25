package emd24.minecraftrpgmod;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

import com.jcraft.jogg.Packet;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import emd24.minecraftrpgmod.skills.SkillPlayer;
import emd24.minecraftrpgmod.skills.SkillManagerServer;

/**
 * Class that handles creating and sending packets.
 * 
 * @author Evan Dyke
 *
 */
public class PacketHandler implements IPacketHandler, IConnectionHandler {

	/**
	 * Creates a packet to be sent containing updated information on a single skill
	 * for a single player.
	 * 
	 * @param player String name of player to generate packet for
	 * @param skill String name of skill to generate packet for
	 * @return packet of information to send
	 */
	public static Packet250CustomPayload getSkillUpdatePacket(String player, String skill){
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			Packet250CustomPayload packet = new Packet250CustomPayload();
			dos.writeByte(0);
			dos.writeUTF(player);
			SkillPlayer temp = SkillManagerServer.getSkill(player, skill);
			dos.writeUTF(temp.name);
			dos.writeInt(temp.getLevel());
			dos.writeInt(temp.getExperience());
			dos.close();
			packet.channel = "rpgmod";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			packet.isChunkDataPacket = false;
			return packet;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Creates a packet to be sent containing updated information all the skills
	 * for a single player.
	 * 
	 * @param player String name of player to generate packet for
	 * @return packet of information to send
	 */
	public static Packet250CustomPayload getPlayerSkillUpdatePacket(String player){
		try{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		Packet250CustomPayload packet = new Packet250CustomPayload();
		dos.writeByte(1);
		dos.writeUTF(player);
		HashMap<String, SkillPlayer> temp = SkillManagerServer.getPlayerSkillList(player);
		dos.writeInt(temp.size());
		for(SkillPlayer skill : temp.values()){
			dos.writeUTF(skill.name);
			dos.writeInt(skill.getLevel());
			dos.writeInt(skill.getExperience());
		}
		dos.close();
		packet.channel = "rpgmod";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = false;
		return packet;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 *  Creates a packet to be sent containing updated information all the skills
	 * for all the players.
	 * 
	 * @return packet to be sent
	 */
	public static Packet250CustomPayload getAllSkillsUpdatePacket(){
		try{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		Packet250CustomPayload packet = new Packet250CustomPayload();
		dos.writeByte(2);
		dos.writeInt(SkillManagerServer.players.size());

		for(String player : SkillManagerServer.players.keySet()){
			dos.writeUTF(player);
			HashMap<String, SkillPlayer> skills = SkillManagerServer.getPlayerSkillList(player);
			dos.writeInt(skills.size());
			for(SkillPlayer skill : skills.values()){
				dos.writeUTF(skill.name);
				dos.writeInt(skill.getLevel());
				dos.writeInt(skill.getExperience());
			}
		}
		dos.close();
		packet.channel = "rpgmod";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = false;
		return packet;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler,
			INetworkManager manager) {
		PacketDispatcher.sendPacketToPlayer(getAllSkillsUpdatePacket(), player);
		PacketDispatcher.sendPacketToAllPlayers(getPlayerSkillUpdatePacket(netHandler.getPlayer().username)); 
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			INetworkManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, INetworkManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, INetworkManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionClosed(INetworkManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler,
			INetworkManager manager, Packet1Login login) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		// TODO Auto-generated method stub
		
	}

}