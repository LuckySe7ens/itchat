package com.leospring.itchat4j;

import com.leospring.itchat4j.face.IMsgHandlerFace;
import com.leospring.itchat4j.face.MsgDefaultHandler;

/**
 * Created by Administrator on 2019/3/25.
 */
public class Startup {
    public static void main(String[] args) throws Exception {
        String qrPath = "D://itchat4j"; // 保存登陆二维码图片的路径
        IMsgHandlerFace msgHandler = new MsgDefaultHandler(); // 实现IMsgHandlerFace接口的类
        Wechat wechat = new Wechat(msgHandler, qrPath); // 【注入】
        wechat.start(); // 启动服务，会在qrPath下生成一张二维码图片，扫描即可登陆，注意，二维码图片如果超过一定时间未扫描会过期，过期时会自动更新，所以你可能需要重新打开图片
    }

}



