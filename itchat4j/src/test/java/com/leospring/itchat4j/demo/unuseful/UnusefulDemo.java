package com.leospring.itchat4j.demo.unuseful;

import java.io.FileWriter;
import java.io.IOException;

import com.leospring.itchat4j.Wechat;
import com.leospring.itchat4j.api.AssistTools;
import com.leospring.itchat4j.beans.BaseMsg;
import com.leospring.itchat4j.face.IMsgHandlerFace;

/**
 * 自用的测试类，请无视
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年5月22日 下午10:41:44
 * @version 1.0
 *
 */
public class UnusefulDemo implements IMsgHandlerFace {

	@Override
	public String textMsgHandle(BaseMsg msg) {
		if (!msg.isGroupMsg()) { // 群消息不处理
			String text = msg.getText(); // 发送文本消息，也可调用MessageTools.sendFileMsgByUserId(userId,text);
			if (text.equals("111")) {
				String username = "yaphone";
				String password = "123456";
				String localPath = "D://itchat4j/pic/1.jpg";
				String uploadUrl = "http://127.0.0.1/file/put";
				try {
					AssistTools.sendQrPicToServer(username, password, uploadUrl, localPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return text;
		}
		return null;
	}

	@Override
	public String picMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String voiceMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String viedoMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String nameCardMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Exception {
//		IMsgHandlerFace msgHandler = new UnusefulDemo();
//		Wechat wechat = new Wechat(msgHandler, "D://itchat4j/login");
//		wechat.start();
		FileWriter fw = new FileWriter("d:/union_index.sql");
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<1024;i++) {
			sb.append("ALTER TABLE `wb_widget_"+ i + "` ADD INDEX union_pk_index ( `user_id`, `widget_id`); \r\n");
		}
		fw.write(sb.toString());
		fw.close();
	}

	@Override
	public void sysMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public String verifyAddFriendMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mediaMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
