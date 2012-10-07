package org.cniska.foosball;

import java.util.List;

public interface PlayerDataSource {

	/**
	 * Creates a new player.
	 * @param name Player name.
	 * @return The record.
	 */
	public Player createPlayer(String name);

	/**
	 * Updates the given player.
	 * @param player The player.
	 * @return Number of affected rows.
	 */
	public int updatePlayer(Player player);

	/**
	 * Finds a single player by its name.
	 * @param name Player name.
	 * @return The player.
	 */
	public Player findPlayerByName(String name);

	/**
	 * Finds all players and returns them.
	 * @return The players.
	 */
	public List<Player> findAllPlayers();
}
