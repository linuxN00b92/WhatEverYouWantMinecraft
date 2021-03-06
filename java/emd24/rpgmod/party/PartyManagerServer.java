package emd24.rpgmod.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import emd24.rpgmod.RPGMod;
import emd24.rpgmod.packets.PartyDataPacket;
import emd24.rpgmod.packets.PartyManaPacket;
import emd24.rpgmod.packets.PlayerDataPacket;

public class PartyManagerServer {
	// Maps player name to party ID
	public static HashMap<String, Integer> playerParty
		= new HashMap<String, Integer>();
	
	// Maps player name to current mana
	public static HashMap<String, Integer[]> playerMana
	= new HashMap<String, Integer[]>();
	public final int CURR_MANA_LOC = 0;
	public final int MAX_MANA_LOC = 1;
	
	protected static int autoincrement = 1;
	
	/**
	 * Gets a list of player names in a specified player's party.
	 * 
	 * @param name String name of player
	 * @return ArrayList of player names in the party
	 */
	public static ArrayList<String> getPlayerParty(String name){
		ArrayList<String> partyPlayerList = new ArrayList<String>();
		// If the player is not in a party, don't pass back everyone in the
		// general party by short circuiting the search.
		for(String playerName : playerParty.keySet()){
			/* Since the leader has a negative partyID, we must do an absolute  
			 * value equality check.
			 */
			if(Math.abs(playerParty.get(playerName)) == Math.abs(playerParty.get(name))){
				partyPlayerList.add(playerName);
			}
		}
		return partyPlayerList;
	}
	
	public static boolean addPlayerToParty(String playerName, int partyID) {
		if(!playerParty.containsKey(playerName) || playerParty.get(playerName) == 0) {
			//no party -> join
			playerParty.put(playerName, partyID);
			
			//Update clients with new party info
			RPGMod.packetPipeline.sendToAll(new PartyDataPacket());
			return true;
		}
		return false;
		
	}
	
	public static void addPlayerToPlayersParty(String playerName, String invitingPlayer) {
		//lookup inviting player's party
		int partyID = Math.abs(playerParty.get(invitingPlayer));
		
		/* For denoting the leader of a party, they will have the a negative PartyID. ALL
		 * CHECKS FOR PARTY EQUALITY SHOULD BE DONE USING ABSOLUTE VALUE. This saves space,
		 * and not use another field
		 */

		if(partyID == 0) {
			partyID = autoincrement++;
			playerParty.put(invitingPlayer, -partyID);
		}
		addPlayerToParty(playerName, partyID);
	}
	
	public static void removePlayerFromParty(String playerName, String kickingPlayer){
		if(playerParty == null)
			return;
		ArrayList<String> party = getPlayerParty(playerName);
		if(party.size() == 2){
			for(String player: party){
				playerParty.remove(player);
				playerParty.put(player,  0);
			}
		} else if(playerParty.get(playerName) < 0){
			boolean done = false;
			//Search for a new party leader
			Iterator<String> itr = party.iterator();
			while(itr.hasNext() && !done){
				String curr = itr.next();
				//If this person isn't the leaving player, promote them.
				if(curr.compareTo(playerName) != 0){
					promotePlayer(curr, playerName);
					done = true;
				}
			}
			playerParty.remove(playerName);
			playerParty.put(playerName, 0);
		} else {
			playerParty.put(playerName, 0);
		}
		RPGMod.packetPipeline.sendToAll(new PartyDataPacket());
	}


	public static void removePlayerFromGame(String playerName){
		if (playerName == null){
			return;
		} else {
		removePlayerFromParty(playerName, playerName);
		playerParty.remove(playerName);
		playerMana.remove(playerName);
		RPGMod.packetPipeline.sendToAll(new PartyDataPacket());
		}
	}

	public static void promotePlayer(String playerName, String oldLeader) {
		int leaderID = playerParty.get(oldLeader);
		if (leaderID < 0){
			playerParty.remove(oldLeader);
			playerParty.remove(playerName);
			playerParty.put(oldLeader, -leaderID);
			playerParty.put(playerName, leaderID);
			RPGMod.packetPipeline.sendToAll(new PartyDataPacket());
		} else {}
	}
	
	public static void handlePlayerMana(String player, int[] mana){
		if (player == null || mana == null){
			throw new NullPointerException();
		}
		//If the player is already in the map, remove it so we get a clean add
		if(playerMana.containsKey(player)){
			playerMana.remove(player);
		}
		//Then add the player to our map
		Integer[] manaArray = ArrayUtils.toObject(mana);
		playerMana.put(player, manaArray);
		RPGMod.packetPipeline.sendToAll(new PartyManaPacket());
	}

}
