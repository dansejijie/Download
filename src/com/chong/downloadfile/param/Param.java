package com.chong.downloadfile.param;

import android.R.integer;
import android.os.Environment;
/**
 * �������صı���
 * ��Ҫ�޸Ŀ����������޸�
 * @author zhengqiang
 *
 */
public class Param {

	//�����ļ����·��
	public static final String DOWNLOAD_PATH=Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
	//�����ļ�Ĭ�Ͽ����߳���
	public static final int THREAD_COUNT=1;
	
	//���õ�������ɺ��Ƿ�򿪰�װ����
	public static final boolean IS_INSTALL=true;
	
	

}
