/*
 * @Title ThreadDAO.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-8 上午10:55:21
 * @version 1.0
 */
package com.chong.downloadfile.db;

import java.util.List;

import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.entities.ThreadInfo;

/** 
 * 数据访问接口
 * @author Yann
 * @date 2015-8-8 上午10:55:21
 */
public interface ThreadDAO
{
	/** 
	 * 插入线程信息
	 * @param threadInfo
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 上午10:56:18
	 */ 
	public void insertThread(ThreadInfo threadInfo);
	/** 
	 * 删除线程信息
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 上午10:56:57
	 */ 
	public void deleteThread(String url);
	/** 
	 * 更新线程下载进度
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 上午10:57:37
	 */ 
	public void updateThread(String url, int thread_id, int finished);
	/** 
	 * 查询文件的线程信息
	 * @param url
	 * @return
	 * @return List<ThreadInfo>
	 * @author Yann
	 * @date 2015-8-8 上午10:58:48
	 */ 
	public List<ThreadInfo> getThreads(String url);
	/** 
	 * 线程信息是否存在
	 * @param url
	 * @param thread_id
	 * @return
	 * @return boolean
	 * @author Yann
	 * @date 2015-8-8 上午10:59:41
	 */ 
	public boolean isExists(String url, int thread_id);
	
	/*
	 * 获取文件信息的大小
	 * 主要用于显示页面时，显示文件是否处于下载状态以及已下载多少
	 */
	public  List<FileInfo> getFileInfo();
	
	public boolean isExistFileInfo(String url);
	
	/*
	 * 存储FileInfo信息，辅助下载管理界面获取FileInfo信息
	 * 
	 */
	public void insertFileInfo(FileInfo fileInfo);
	
	public void deleteFielInfo(String id);
	
	public List<FileInfo> getFileInfosByFileInfo();
}
