package com.RuleApi.web;

import com.RuleApi.common.*;
import com.RuleApi.entity.TypechoPaylog;
import com.RuleApi.entity.TypechoUsers;
import com.RuleApi.service.TypechoPaylogService;
import com.RuleApi.service.TypechoUsersService;
import com.RuleApi.service.WxPayService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/pay")
public class PayController {
    @Value("${webinfo.scale}")
    private Integer scale;


    /**支付宝**/

    /** 创建的应用ID */
    @Value("${fastboot.pay.alipay.app-id}")
    private String appId;
    /** 自己生成的应用私钥 */
    @Value("${fastboot.pay.alipay.private-key}")
    private String privateKey;
    /** 支付宝提供的支付宝公钥 */
    @Value("${fastboot.pay.alipay.alipay-public-key}")
    private String alipayPublicKey;
    /** 回调地址，需要公网且链接不需要重定向的，如：https://fastboot.shaines.cn/api/myalipay/trade-notify */
    @Value("${fastboot.pay.alipay.notify-url}")
    private String notifyUrl;

    /**微信支付**/
    @Value("${gzh.appid}")
    private String wxappid;
    @Value("${wxPay.key}")
    private String wxkey;
    @Value("${wxPay.mchId}")
    private String mchId;
    @Value("${wxPay.notifyUrl}")
    private String wxnotifyUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TypechoUsersService usersService;


    @Autowired
    private TypechoPaylogService paylogService;

    @Autowired(required = false)
    private WxPayService wxpayService;

    @Value("${web.prefix}")
    private String dataprefix;

    RedisHelp redisHelp =new RedisHelp();
    ResultAll Result = new ResultAll();
    UserStatus UStatus = new UserStatus();


    /**
     * 支付宝扫码支付
     * @return 支付宝生成的订单信息
     */
    @RequestMapping(value = "/scancodePay")
    @ResponseBody
    public String scancodepay(@RequestParam(value = "num", required = false) String num, @RequestParam(value = "token", required = false) String  token) throws AlipayApiException {

        Integer uStatus = UStatus.getStatus(token,this.dataprefix,redisTemplate);
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        if(!pattern.matcher(num).matches()){
            return Result.getResultJson(0,"充值金额必须为正整数",null);
        }
        if(Integer.parseInt(num) <= 0){
            return Result.getResultJson(0,"充值金额不正确",null);
        }

        final String APPID = this.appId;
        String RSA2_PRIVATE = this.privateKey;
        String ALIPAY_PUBLIC_KEY = this.alipayPublicKey;

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
        String timeID = dateFormat.format(now);
        String order_no=timeID+"scancodealipay";
        String body = "";


        String total_fee=num;  //真实金钱

        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APPID, RSA2_PRIVATE, "json",
                "UTF-8", ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();//创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + order_no + "\"," +
                "    \"total_amount\":\"" + total_fee + "\"," +
                "    \"body\":\"" + body + "\"," +
                "    \"subject\":\"扫码支付\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        request.setNotifyUrl(this.notifyUrl);
        AlipayTradePrecreateResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
        System.out.print(response.getBody());

        //根据response中的结果继续业务逻辑处理
        if (response.getMsg().equals("Success")) {
            //先生成订单
            Map map =redisHelp.getMapValue(this.dataprefix+"_"+"userInfo"+token,redisTemplate);
            Integer uid  = Integer.parseInt(map.get("uid").toString());
            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0,10);
            TypechoPaylog paylog = new TypechoPaylog();
            paylog.setStatus(0);
            paylog.setCreated(Integer.parseInt(created));
            paylog.setUid(uid);
            paylog.setOutTradeNo(order_no);
            paylog.setTotalAmount(total_fee);
            paylog.setPaytype("scancodePay");
            paylog.setSubject("扫码支付");
            paylogService.insert(paylog);
            //再返回二维码
            String qrcode = response.getQrCode();
            JSONObject toResponse = new JSONObject();
            toResponse.put("code" ,1);
            toResponse.put("data" , qrcode);
            toResponse.put("msg"  , "获取成功");
            return toResponse.toString();
        } else{
            JSONObject toResponse = new JSONObject();
            toResponse.put("code" ,0);
            toResponse.put("data" , "");
            toResponse.put("msg"  , "请求失败");
            return toResponse.toString();
        }

    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    @ResponseBody
    public String notify(HttpServletRequest request,
                         HttpServletResponse response) throws AlipayApiException {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        System.err.println(params);
        String CHARSET = "UTF-8";
        //支付宝公钥
        String ALIPAY_PUBLIC_KEY = this.alipayPublicKey;

        String tradeStatus = request.getParameter("trade_status");
        boolean flag = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, "RSA2");

        if (flag) {//验证成功

            if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                //支付完成后，写入充值日志
                String trade_no = params.get("trade_no");
                String out_trade_no = params.get("out_trade_no");
                String total_amount = params.get("total_amount");
                Integer scale = this.scale;
                Integer integral = Double.valueOf(total_amount).intValue() * scale;

                Long date = System.currentTimeMillis();
                String created = String.valueOf(date).substring(0,10);
                TypechoPaylog paylog = new TypechoPaylog();
                //根据订单和发起人，是否有数据库对应，来是否充值成功
                paylog.setOutTradeNo(out_trade_no);
                List<TypechoPaylog> logList= paylogService.selectList(paylog);
                if(logList.size() > 0){
                    Integer pid = logList.get(0).getPid();
                    Integer uid = logList.get(0).getUid();
                    paylog.setStatus(1);
                    paylog.setTradeNo(trade_no);
                    paylog.setPid(pid);
                    paylog.setCreated(Integer.parseInt(created));
                    paylogService.update(paylog);
                    //订单修改后，插入用户表
                    TypechoUsers users = usersService.selectByKey(uid);
                    Integer oldAssets = users.getAssets();
                    Integer assets = oldAssets + integral;
                    users.setAssets(assets);
                    usersService.update(users);
                }else{
                    System.out.println("数据库不存在订单");
                    return "fail";
                }
            }
            return "success";
        } else {//验证失败
            return "fail";
        }
    }
    /**
     * 二维码生成
     * */
    @RequestMapping(value = "/qrCode")
    @ResponseBody
    public void getQRCode(String codeContent,@RequestParam(value = "token", required = false) String  token, HttpServletResponse response) {
        Integer uStatus = UStatus.getStatus(token,this.dataprefix,redisTemplate);
        if(uStatus==0){
            System.out.println("用户未的登陆");
        }
        System.out.println("codeContent=" + codeContent);
        try {
            /*
             * 调用工具类生成二维码并输出到输出流中
             */
            QRCodeUtil.createCodeToOutputStream(codeContent, response.getOutputStream());
            System.out.println("成功生成二维码!");
        } catch (IOException e) {
            System.out.println("发生错误");
        }
    }
    /**
     * 充值记录
     * */
    @RequestMapping(value = "/payLogList")
    @ResponseBody
    public String orderSellList (@RequestParam(value = "token", required = false) String  token) {

        String page = "1";
        String limit = "30";
        Integer uStatus = UStatus.getStatus(token,this.dataprefix,redisTemplate);
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        Map map =redisHelp.getMapValue(this.dataprefix+"_"+"userInfo"+token,redisTemplate);
        Integer uid =Integer.parseInt(map.get("uid").toString());

        TypechoPaylog query = new TypechoPaylog();
        query.setUid(uid);

        List<TypechoPaylog> list = new ArrayList();
        List cacheList = redisHelp.getList(this.dataprefix+"_"+"payLogList_"+page+"_"+limit+"_"+uid,redisTemplate);
        try{
            if(cacheList.size()>0){
                list = cacheList;
            }else {
                PageList<TypechoPaylog> pageList = paylogService.selectPage(query, Integer.parseInt(page), Integer.parseInt(limit));
                list = pageList.getList();
                redisHelp.delete(this.dataprefix+"_"+"payLogList_"+page+"_"+limit+"_"+uid, redisTemplate);
                redisHelp.setList(this.dataprefix+"_"+"payLogList_"+page+"_"+limit+"_"+uid, list, 5, redisTemplate);
            }
        }catch (Exception e){
            if(cacheList.size()>0){
                list = cacheList;
            }
        }
        JSONObject response = new JSONObject();
        response.put("code" , 1);
        response.put("msg"  , "");
        response.put("data" , null != list ? list : new JSONArray());
        response.put("count", list.size());
        return response.toString();
    }
    /**
     * 微信支付
     * */
    @RequestMapping(value = "/WxPay")
    @ResponseBody
    public String wxAdd(HttpServletRequest request,@RequestParam(value = "price", required = false) Integer price,@RequestParam(value = "token", required = false) String  token) throws Exception {
        Integer uStatus = UStatus.getStatus(token,this.dataprefix,redisTemplate);
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        if(!pattern.matcher(price.toString()).matches()){
            return Result.getResultJson(0,"充值金额必须为正整数",null);
        }
        if(price <= 0){
            return Result.getResultJson(0,"充值金额不正确",null);
        }
        WXConfigUtil config = new WXConfigUtil();
        WXPay wxpay = new WXPay(config);
        Map<String, String> data = new HashMap<>();
        //生成商户订单号，不可重复
        data.put("appid", this.wxappid);
        data.put("mch_id", this.mchId);
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        String body = "订单支付";
        data.put("body", body);
        data.put("out_trade_no", System.currentTimeMillis()+ "");
        data.put("total_fee", String.valueOf((int)(price)));
        //自己的服务器IP地址
        data.put("spbill_create_ip", request.getRemoteAddr());
        //异步通知地址（请注意必须是外网）
        data.put("notify_url", wxnotifyUrl);
        //交易类型
        data.put("trade_type", "APP");
        //附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
        data.put("attach", "");
        data.put("sign", WXPayUtil.generateSignature(data, this.wxkey,
                WXPayConstants.SignType.MD5));
        //使用官方API请求预付订单
        Map<String, String> response = wxpay.unifiedOrder(data);
        System.out.println(response);
        if ("SUCCESS".equals(response.get("return_code"))) {
            //主要返回以下5个参数
            Map<String, String> param = new HashMap<>();
            param.put("appid",this.wxappid);
            param.put("partnerid", response.get("mch_id"));
            param.put("prepayid", response.get("prepay_id"));
            param.put("package", "Sign=WXPay");
            param.put("noncestr", WXPayUtil.generateNonceStr());
            param.put("timestamp", System.currentTimeMillis() / 1000 + "");
            param.put("sign", WXPayUtil.generateSignature(param, config.getKey(),
                    WXPayConstants.SignType.MD5));
            System.out.println(param);
            //先生成订单
            Map map =redisHelp.getMapValue(this.dataprefix+"_"+"userInfo"+token,redisTemplate);
            Integer uid  = Integer.parseInt(map.get("uid").toString());
            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0,10);
            TypechoPaylog paylog = new TypechoPaylog();
            paylog.setStatus(0);
            paylog.setCreated(Integer.parseInt(created));
            paylog.setUid(uid);
            paylog.setOutTradeNo(response.get("out_trade_no"));
            paylog.setTotalAmount(price.toString());
            paylog.setPaytype("WXPay");
            paylog.setSubject("微信APP支付");
            paylogService.insert(paylog);


            JSONObject res = new JSONObject();
            res.put("code" , 1);
            res.put("msg"  , "");
            res.put("data" , param);
            return response.toString();
        }else {
            return Result.getResultJson(0,"支付失败",null);
        }
    }
    /**
     * 微信回调
     * */
    @RequestMapping(value = "/wxPayNotify")
    @ResponseBody
    public String wxPayNotify(HttpServletRequest request) {

        String resXml = "";
        try {

            InputStream inputStream = request.getInputStream();
            //将InputStream转换成xmlString
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {

                while ((line = reader.readLine()) != null) {

                    sb.append(line + "\n");
                }
            } catch (IOException e) {

                System.out.println(e.getMessage());
            } finally {

                try {

                    inputStream.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
            resXml = sb.toString();

            //支付完成后，写入充值日志
            String out_trade_no = request.getParameter("out_trade_no");
            String total_amount = request.getParameter("total_fee");
            Integer scale = this.scale;
            Integer integral = Double.valueOf(total_amount).intValue() * scale;

            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0,10);
            TypechoPaylog paylog = new TypechoPaylog();
            //根据订单和发起人，是否有数据库对应，来是否充值成功
            paylog.setOutTradeNo(out_trade_no);
            List<TypechoPaylog> logList= paylogService.selectList(paylog);
            if(logList.size() > 0){
                Integer pid = logList.get(0).getPid();
                Integer uid = logList.get(0).getUid();
                paylog.setStatus(1);
                paylog.setTradeNo(out_trade_no);
                paylog.setPid(pid);
                paylog.setCreated(Integer.parseInt(created));
                paylogService.update(paylog);
                //订单修改后，插入用户表
                TypechoUsers users = usersService.selectByKey(uid);
                Integer oldAssets = users.getAssets();
                Integer assets = oldAssets + integral;
                users.setAssets(assets);
                usersService.update(users);
            }else{
                System.out.println("数据库不存在订单");
                String result = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
                return result;
            }

            //写入本地方法
            String result = wxpayService.payBack(resXml);
            return result;
        } catch (Exception e) {

            System.out.println("微信手机支付失败:" + e.getMessage());
            String result = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            return result;
        }
    }
}