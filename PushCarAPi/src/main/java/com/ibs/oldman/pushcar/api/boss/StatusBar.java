package com.ibs.oldman.pushcar.api.boss;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Bedwars Team
 * 状态栏
 *
 */
public interface StatusBar {
	
	/**
	 * @param player
	 */
	public void addPlayer(Player player);
	
	/**
	 * @param player
	 */
	public void removePlayer(Player player);
	
	/**
	 * @param progress
	 * 设置进度
	 */
	public void setProgress(double progress);
	
	/**
	 * @return list of all viewers
	 * 返回所有的观众
	 */
	public List<Player> getViewers();
	
	/**
	 * @return progress of status bar
	 */
	public double getProgress();
	
	/**
	 * @return visibility of status bar
	 */
	public boolean isVisible();
	
	/**
	 * @param visible
	 */
	public void setVisible(boolean visible);
}
