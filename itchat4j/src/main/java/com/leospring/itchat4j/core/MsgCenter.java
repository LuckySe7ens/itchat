package com.leospring.itchat4j.core;

import com.leospring.itchat4j.api.MessageTools;
import com.leospring.itchat4j.beans.BaseMsg;
import com.leospring.itchat4j.face.IMsgHandlerFace;
import com.leospring.itchat4j.utils.HttpUtils;
import com.leospring.itchat4j.utils.enums.MsgCodeEnum;
import com.leospring.itchat4j.utils.enums.MsgTypeEnum;
import com.leospring.itchat4j.utils.tools.CommonTools;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * 消息处理中心
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年5月14日 下午12:47:50
 * @version 1.0
 *
 */
public class MsgCenter {
	private static Logger LOG = LoggerFactory.getLogger(MsgCenter.class);

	private static Core core = Core.getInstance();

	private static String language="zhs";
	private static String memberId	= "3622231";
	private static String qzId = "5417";
	private static int accountId = 5048647;
	private static String imei = "9F9884D6-F298-4A28-9FD9-412A351FA6FE";
	private static String clientV= "2-5.13.0-1-1";
	private static String deviceModel = "iPhone X";
	private static String deviceName = "苹果";
	private static String address = "用友产业园";
	private static String wifiName = "yonyou";
	private static String wifiMac = "88:df:9e:31:34:01";

	public static String getEncryptedAttentance() {
		JSONObject json = new JSONObject();
		try {
			json.put("longitude", "116.2364238823785");
			json.put("latitude", "40.06774468315972");
			json.put("address", address);
			json.put("wifiMac", wifiMac);
			json.put("wifiName", wifiName);
			json.put("accountId", accountId);
			json.put("szId", 5417);
			json.put("signTime", System.currentTimeMillis());
			json.put("imei", imei);
			json.put("deviceModel", deviceModel);
			json.put("deviceName", deviceName);
			json.put("isRoot", 0);

			Map<String, String> params = new HashMap<>();
			params.put("data", json.toString());
			params.put("type", "aes");
			params.put("arg", "m=ecb_pad=pkcs5_block=128_p=light-app-123456_i=255_o=1_s=utf-8_t=0");
			String response = HttpUtils.submitPostData("http://tool.chacuo.net/cryptaes", params, "UTF-8");
			JSONObject obj = JSONObject.parseObject(response);
			String token = obj.getJSONArray("data").get(0).toString();
			return token;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getToken(){
		String url = "https://ezone.yonyoucloud.com/signin/index/webLogin?clientV="+clientV;
		Map<String, String> param = new HashMap<>();
		param.put("qzId",qzId);
		param.put("memberId",memberId);
		String data = HttpUtils.submitPostData(url, param, "UTF-8");
		String token = JSONObject.parseObject(data).getString("data");
		return token;
	}

	public static String sign() {
		try {
				String url = "https://ezone.yonyoucloud.com/signin/attentance/encryptSignIn?token=" + getToken() + "&clientV=" +clientV;
				Map<String,String> params = new HashMap<>();
				params.put("encryptedAttentance",getEncryptedAttentance());
				String postData = HttpUtils.submitPostData(url, params, "UTF-8");
				return postData;
		} catch (Throwable e) {
			return "打卡失败" + e.getLocalizedMessage();
		} finally {

		}
	}

	/**
	 * 接收消息，放入队列
	 * 
	 * @author https://github.com/yaphone
	 * @date 2017年4月23日 下午2:30:48
	 * @param msgList
	 * @return
	 */
	public static JSONArray produceMsg(JSONArray msgList) {
		JSONArray result = new JSONArray();
		for (int i = 0; i < msgList.size(); i++) {
			JSONObject msg = new JSONObject();
			JSONObject m = msgList.getJSONObject(i);
			m.put("groupMsg", false);// 是否是群消息
			if (m.getString("FromUserName").contains("@@") || m.getString("ToUserName").contains("@@")) { // 群聊消息
				if (m.getString("FromUserName").contains("@@")
						&& !core.getGroupIdList().contains(m.getString("FromUserName"))) {
					core.getGroupIdList().add((m.getString("FromUserName")));
				} else if (m.getString("ToUserName").contains("@@")
						&& !core.getGroupIdList().contains(m.getString("ToUserName"))) {
					core.getGroupIdList().add((m.getString("ToUserName")));
				}
				// 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，这里需要处理一下，去掉多余信息，只保留消息内容
				if (m.getString("Content").contains("<br/>")) {
					String content = m.getString("Content").substring(m.getString("Content").indexOf("<br/>") + 5);
					m.put("Content", content);
					m.put("groupMsg", true);
				}
			} else {
				CommonTools.msgFormatter(m, "Content");
			}
			if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_TEXT.getCode())) { // words
																						// 文本消息
				if (m.getString("Url").length() != 0) {
					String regEx = "(.+?\\(.+?\\))";
					Matcher matcher = CommonTools.getMatcher(regEx, m.getString("Content"));
					String data = "Map";
					if (matcher.find()) {
						data = matcher.group(1);
					}
					msg.put("Type", "Map");
					msg.put("Text", data);
				} else {
					msg.put("Type", MsgTypeEnum.TEXT.getType());
					msg.put("Text", m.getString("Content"));
				}
				m.put("Type", msg.getString("Type"));
				m.put("Text", msg.getString("Text"));
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_IMAGE.getCode())
					|| m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_EMOTICON.getCode())) { // 图片消息
				m.put("Type", MsgTypeEnum.PIC.getType());
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_VOICE.getCode())) { // 语音消息
				m.put("Type", MsgTypeEnum.VOICE.getType());
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_VERIFYMSG.getCode())) {// friends
				// 好友确认消息
				// MessageTools.addFriend(core, userName, 3, ticket); // 确认添加好友
				m.put("Type", MsgTypeEnum.VERIFYMSG.getType());

			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_SHARECARD.getCode())) { // 共享名片
				m.put("Type", MsgTypeEnum.NAMECARD.getType());

			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_VIDEO.getCode())
					|| m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_MICROVIDEO.getCode())) {// viedo
				m.put("Type", MsgTypeEnum.VIEDO.getType());
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_MEDIA.getCode())) { // 多媒体消息
				m.put("Type", MsgTypeEnum.MEDIA.getType());
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_STATUSNOTIFY.getCode())) {// phone
				// init
				// 微信初始化消息

			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_SYS.getCode())) {// 系统消息
				m.put("Type", MsgTypeEnum.SYS.getType());
			} else if (m.getInteger("MsgType").equals(MsgCodeEnum.MSGTYPE_RECALLED.getCode())) { // 撤回消息

			} else {
				LOG.info("Useless msg");
			}
			LOG.info("收到消息一条，来自: " + m.getString("FromUserName") + ":" + m.getString("Content"));
			result.add(m);
		}
		return result;
	}

	/**
	 * 消息处理
	 * 
	 * @author https://github.com/yaphone
	 * @date 2017年5月14日 上午10:52:34
	 * @param msgHandler
	 */
	public static void handleMsg(IMsgHandlerFace msgHandler) {
		while (true) {
			if (core.getMsgList().size() > 0 && core.getMsgList().get(0).getContent() != null) {
				if (core.getMsgList().get(0).getContent().length() > 0) {
					BaseMsg msg = core.getMsgList().get(0);
					if (msg.getType() != null) {
						try {
							if (msg.getType().equals(MsgTypeEnum.TEXT.getType()) && !msg.isGroupMsg()) {
								String result = null;

								if(core.getOpenRobot().get(msg.getFromUserName()) == null) {
									if(msg.getContent().equals("呼叫小七")) {
										result = "您好，小七出现啦，您可以输入问题向小七询问 天气、菜谱、快递、列车、日期、附近酒店、股票、讲笑话、故事、成语接龙、新闻、星座、脑筋急转弯、歇后语、绕口令、顺口溜... 哎哟，好多好多呢！开始吧";
										core.getOpenRobot().put(msg.getFromUserName(),1);
										MessageTools.sendMsgById(result + "\r\n           ---消息来自机器人小七", msg.getFromUserName());

									}else {
										result = "您好，我是主人的微信小助手 小七，我将尽快通知到主人回复消息，您也可以 输入 呼叫小七  与我进行对话，输入 小七再见 结束与我的对话 \r\n       ---消息来自机器人小七";
										MessageTools.sendMsgById(result, msg.getFromUserName());
										core.getOpenRobot().put(msg.getFromUserName(),-1);
									}

								} else if(1 == core.getOpenRobot().get(msg.getFromUserName())) {
									if(msg.getContent().equals("小七再见")) {
										core.getOpenRobot().put(msg.getFromUserName(),0);
										result = "小七真的走咯，您要是想念小七可以输入 呼叫小七 再次将我召唤出来哈";
									} else {
										result = msgHandler.textMsgHandle(msg);
									}
									MessageTools.sendMsgById(result + "\r\n           ---消息来自机器人小七", msg.getFromUserName());
								} else if(1 != core.getOpenRobot().get(msg.getFromUserName()) && msg.getContent().equals("呼叫小七")) {
									result = "您好，小七出现啦，您可以输入问题向小七询问 天气、菜谱、快递、列车、日期、附近酒店、股票、讲笑话、故事、成语接龙、新闻、星座、脑筋急转弯、歇后语、绕口令、顺口溜... 哎哟，好多好多呢！开始吧";
									core.getOpenRobot().put(msg.getFromUserName(),1);
									MessageTools.sendMsgById(result + "\r\n           ---消息来自机器人小七", msg.getFromUserName());
								}
//								if(msg.getContent().equals("打卡")) {
//									String s = sign();
//									MessageTools.sendMsgById(s, core.getUserName());
//								}
//								MessageTools.sendMsgById(result + "\r           ---消息来自机器人小七", msg.getFromUserName());
							} else if (msg.getType().equals(MsgTypeEnum.PIC.getType())) {

								String result = msgHandler.picMsgHandle(msg);
//								MessageTools.sendMsgById(result, core.getMsgList().get(0).getFromUserName());
							} else if (msg.getType().equals(MsgTypeEnum.VOICE.getType())) {
								String result = msgHandler.voiceMsgHandle(msg);
//								MessageTools.sendMsgById(result, core.getMsgList().get(0).getFromUserName());
							} else if (msg.getType().equals(MsgTypeEnum.VIEDO.getType())) {
								String result = msgHandler.viedoMsgHandle(msg);
//								MessageTools.sendMsgById(result, core.getMsgList().get(0).getFromUserName());
							} else if (msg.getType().equals(MsgTypeEnum.NAMECARD.getType())) {
								String result = msgHandler.nameCardMsgHandle(msg);
//								MessageTools.sendMsgById(result, core.getMsgList().get(0).getFromUserName());
							} else if (msg.getType().equals(MsgTypeEnum.SYS.getType())) { // 系统消息
								msgHandler.sysMsgHandle(msg);
							} else if (msg.getType().equals(MsgTypeEnum.VERIFYMSG.getType())) { // 确认添加好友消息
								String result = msgHandler.verifyAddFriendMsgHandle(msg);
//								MessageTools.sendMsgById(result,
//										core.getMsgList().get(0).getRecommendInfo().getUserName());
							} else if (msg.getType().equals(MsgTypeEnum.MEDIA.getType())) { // 多媒体消息
								String result = msgHandler.mediaMsgHandle(msg);
//								MessageTools.sendMsgById(result, core.getMsgList().get(0).getFromUserName());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				core.getMsgList().remove(0);
			}
			try {
				TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public static String inputStream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n;(n = in.read(b)) != -1;)   {
			out.append(new String(b,0, n));
		}
		return out.toString();
	}
}
