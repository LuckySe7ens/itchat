package com.leospring.itchat4j.demo.demo2;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.leospring.itchat4j.core.Core;
import com.leospring.itchat4j.utils.MyHttpClient;
import com.leospring.itchat4j.utils.tools.DownloadTools;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.leospring.itchat4j.Wechat;
import com.leospring.itchat4j.beans.BaseMsg;
import com.leospring.itchat4j.face.IMsgHandlerFace;
import com.leospring.itchat4j.utils.enums.MsgTypeEnum;

/**
 * 图灵机器人示例
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月24日 上午12:13:26
 * @version 1.0
 *
 */
public class TulingRobot implements IMsgHandlerFace {
	Logger logger = Logger.getLogger("TulingRobot");
	MyHttpClient myHttpClient = Core.getInstance().getMyHttpClient();
	String url = "http://www.tuling123.com/robot-chat/robot/chat/477129/O7oT?geetest_challenge=&geetest_validate=&geetest_seccode=";
	String apiKey = "597b34bea4ec4c85a775c469c84b6817"; // 这里是我申请的图灵机器人API接口，每天只能5000次调用，建议自己去申请一个，免费的:)

	@Override
	public String textMsgHandle(BaseMsg msg) {
		String result = "";
		String text = msg.getText();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("reqType",0);

		Map<String,Object> perception = new HashMap<>();

		Map<String,String> inputText = new HashMap<>();
		inputText.put("text",text);

		perception.put("inputText",inputText);

		paramMap.put("perception",perception);
		Map<String,String> userInfo = new HashMap<>();
		userInfo.put("userId","123456");

		paramMap.put("userInfo",userInfo);
//		paramMap.put("key", apiKey);
//		paramMap.put("info", text);
//		paramMap.put("userid", "123456");
		String paramStr = JSON.toJSONString(paramMap);
		try {
			HttpEntity entity = myHttpClient.doPost(url, paramStr);
			result = EntityUtils.toString(entity, "UTF-8");
			String values = "";
			JSONObject obj = JSON.parseObject(result);
			if (obj.getString("type").equals("success")) {
				JSONArray results = obj.getJSONObject("data").getJSONArray("results");
				for (Object o : results) {
					JSONObject json = JSONObject.parseObject(o.toString());
					String type = json.getString("resultType");
					if("text".equals(type)) {
						values += json.getJSONObject("values").getString("text") + "\r\n";
					}
					if("url".equals(type)) {
						values += json.getJSONObject("values").getString("url") + "\r\n";
					}
					if("news".equals(type)) {
						JSONArray news = json.getJSONObject("values").getJSONArray("news");
						for (Object o1 : news) {
							JSONObject new1 = JSONObject.parseObject(o1.toString());
							values += "\r\n" + new1.getString("name") + "\r\n" + new1.getString("info")+"\r\n" + new1.getString("detailurl") + "\r\n";

						}
					}
				}
				result = values;
			} else {
				result = "处理有误";
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return result;
	}

	@Override
	public String picMsgHandle(BaseMsg msg) {
		String fileName = String.valueOf(new Date().getTime());
		String picPath = "D://itchat4j/pic" + File.separator + fileName + ".jpg";
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.PIC.getType(), picPath);
		return "收到图片";
	}

	@Override
	public String voiceMsgHandle(BaseMsg msg) {
		String fileName = String.valueOf(new Date().getTime());
		String voicePath = "D://itchat4j/voice" + File.separator + fileName + ".mp3";
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VOICE.getType(), voicePath);
		return "收到语音";
	}

	@Override
	public String viedoMsgHandle(BaseMsg msg) {
		String fileName = String.valueOf(new Date().getTime());
		String viedoPath = "D://itchat4j/video" + File.separator + fileName + ".mp4";
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VIEDO.getType(), viedoPath);
		return "收到视频";
	}

	public static void main(String[] args) {
		IMsgHandlerFace msgHandler = new TulingRobot();
		Wechat wechat = new Wechat(msgHandler, "D://itchat4j");
		wechat.start();
	}

	@Override
	public String nameCardMsgHandle(BaseMsg msg) {
		// TODO Auto-generated method stub
		return null;
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
