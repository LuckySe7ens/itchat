package com.leospring.itchat4j.face;

import com.leospring.itchat4j.beans.BaseMsg;
import com.leospring.itchat4j.utils.enums.MsgTypeEnum;
import com.leospring.itchat4j.utils.tools.DownloadTools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2019/3/25.
 */
public class MsgDefaultHandler implements IMsgHandlerFace {
    @Override
    public String textMsgHandle(BaseMsg msg) {
        return msg.getContent();
    }

    @Override
    public String picMsgHandle(BaseMsg msg) {
        try {
            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".jpg"; // 这里使用收到图片的时间作为文件名
            String picPath = "D://itchat4j/pic" + File.separator + fileName; // 保存图片的路径
            DownloadTools.getDownloadFn(msg, MsgTypeEnum.PIC.getType(), picPath); // 调用此方法来保存图片
            return "图片保存成功";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String voiceMsgHandle(BaseMsg msg) {
        try {
            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".mp3"; // 这里使用收到语音的时间作为文件名
            String voicePath = "D://itchat4j/voice" + File.separator + fileName; // 保存语音的路径
            DownloadTools.getDownloadFn(msg, MsgTypeEnum.VOICE.getType(), voicePath); // 调用此方法来保存语音
            return "声音保存成功";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String viedoMsgHandle(BaseMsg msg) {
        try {
            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".mp4"; // 这里使用收到小视频的时间作为文件名
            String viedoPath = "D://itchat4j/viedo" + File.separator + fileName;// 保存小视频的路径
            DownloadTools.getDownloadFn(msg, MsgTypeEnum.VIEDO.getType(), viedoPath);// 调用此方法来保存小视频
            return "视频保存成功";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String nameCardMsgHandle(BaseMsg msg) {
        return "收到名片消息";
    }

    @Override
    public void sysMsgHandle(BaseMsg msg) {

    }

    @Override
    public String verifyAddFriendMsgHandle(BaseMsg msg) {
        return null;
    }

    @Override
    public String mediaMsgHandle(BaseMsg msg) {
        return null;
    }
}
