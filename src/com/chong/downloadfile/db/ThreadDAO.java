/*
 * @Title ThreadDAO.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-8 ����10:55:21
 * @version 1.0
 */
package com.chong.downloadfile.db;

import java.util.List;

import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.entities.ThreadInfo;

/** 
 * ���ݷ��ʽӿ�
 * @author Yann
 * @date 2015-8-8 ����10:55:21
 */
public interface ThreadDAO
{
	/** 
	 * �����߳���Ϣ
	 * @param threadInfo
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 ����10:56:18
	 */ 
	public void insertThread(ThreadInfo threadInfo);
	/** 
	 * ɾ���߳���Ϣ
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 ����10:56:57
	 */ 
	public void deleteThread(String url);
	/** 
	 * �����߳����ؽ���
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author Yann
	 * @date 2015-8-8 ����10:57:37
	 */ 
	public void updateThread(String url, int thread_id, int finished);
	/** 
	 * ��ѯ�ļ����߳���Ϣ
	 * @param url
	 * @return
	 * @return List<ThreadInfo>
	 * @author Yann
	 * @date 2015-8-8 ����10:58:48
	 */ 
	public List<ThreadInfo> getThreads(String url);
	/** 
	 * �߳���Ϣ�Ƿ����
	 * @param url
	 * @param thread_id
	 * @return
	 * @return boolean
	 * @author Yann
	 * @date 2015-8-8 ����10:59:41
	 */ 
	public boolean isExists(String url, int thread_id);
	
	/*
	 * ��ȡ�ļ���Ϣ�Ĵ�С
	 * ��Ҫ������ʾҳ��ʱ����ʾ�ļ��Ƿ�������״̬�Լ������ض���
	 */
	public  List<FileInfo> getFileInfo();
	
	public boolean isExistFileInfo(String url);
	
	/*
	 * �洢FileInfo��Ϣ���������ع��������ȡFileInfo��Ϣ
	 * 
	 */
	public void insertFileInfo(FileInfo fileInfo);
	
	public void deleteFielInfo(String id);
	
	public List<FileInfo> getFileInfosByFileInfo();
}