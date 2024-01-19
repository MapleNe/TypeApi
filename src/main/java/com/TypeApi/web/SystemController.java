package com.TypeApi.web;

import com.TypeApi.common.*;
import com.TypeApi.entity.Ads;
import com.TypeApi.entity.Apiconfig;
import com.TypeApi.entity.App;
import com.TypeApi.service.PushService;
import com.TypeApi.service.AdsService;
import com.TypeApi.service.ApiconfigService;
import com.TypeApi.service.AppService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 接口系统控制器，负责在线修改配置文件，在线重启RuleAPI接口
 */
@Controller
@RequestMapping(value = "/system")
public class SystemController {

    ResultAll Result = new ResultAll();
    EditFile editFile = new EditFile();
    HttpClient HttpClient = new HttpClient();
    RedisHelp redisHelp = new RedisHelp();


    @Autowired
    private ApiconfigService apiconfigService;

    @Autowired
    private AdsService adsService;

    @Autowired
    private PushService pushService;

    @Autowired
    private AppService appService;

    @Autowired
    private RedisTemplate redisTemplate;
    UserStatus UStatus = new UserStatus();


    @Value("${webinfo.key}")
    private String key;

    @Value("${web.prefix}")
    private String dataprefix;
    /**
     * 密钥配置
     */
    private String webinfoKey;


    /**
     * 缓存配置
     */
    private String usertime;
    private String contentCache;
    private String contentInfoCache;
    private String CommentCache;
    private String userCache;
    /**
     * 邮箱配置
     */
    private String mailHost;
    private String mailUsername;
    private String mailPassword;
    /**
     * Mysql配置
     */
    private String dataUrl;
    private String dataUsername;
    private String dataPassword;
    private String dataPrefix;



    /***
     * 缓存配置
     */
    @RequestMapping(value = "/setupCache")
    @ResponseBody
    public String setupCache(@RequestParam(value = "webkey", required = false) String webkey, @RequestParam(value = "params", required = false) String params) {
        if (webkey.length() < 1) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        if (!webkey.equals(this.key)) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        Map jsonToMap = new HashMap();
        try {
            //读取参数，开始写入
            if (StringUtils.isNotBlank(params)) {
                jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
                //新的配置

            }
            String new_usertime = "";
            String new_contentCache = "";
            String new_contentInfoCache = "";
            String new_CommentCache = "";
            String new_userCache = "";

            String usertime = "webinfo.usertime=";
            String contentCache = "webinfo.contentCache=";
            String contentInfoCache = "webinfo.contentInfoCache=";
            String CommentCache = "webinfo.CommentCache=";
            String userCache = "webinfo.userCache=";
            //老的配置
            String old_usertime = usertime + this.usertime;
            String old_contentCache = contentCache + this.contentCache;
            String old_contentInfoCache = contentInfoCache + this.contentInfoCache;
            String old_CommentCache = CommentCache + this.CommentCache;
            String old_userCache = userCache + this.userCache;
            //新的配置

            if (jsonToMap.get("usertime") != null) {
                new_usertime = usertime + jsonToMap.get("usertime").toString();
            } else {
                new_usertime = usertime;
            }
            editFile.replacTextContent(old_usertime, new_usertime);
            if (jsonToMap.get("contentCache") != null) {
                new_contentCache = contentCache + jsonToMap.get("contentCache").toString();
            } else {
                new_contentCache = contentCache;
            }
            editFile.replacTextContent(old_contentCache, new_contentCache);
            if (jsonToMap.get("contentInfoCache") != null) {
                new_contentInfoCache = contentInfoCache + jsonToMap.get("contentInfoCache").toString();
            } else {
                new_contentInfoCache = contentInfoCache;
            }
            editFile.replacTextContent(old_contentInfoCache, new_contentInfoCache);
            if (jsonToMap.get("CommentCache") != null) {
                new_CommentCache = CommentCache + jsonToMap.get("CommentCache").toString();
            } else {
                new_CommentCache = CommentCache;
            }
            editFile.replacTextContent(old_CommentCache, new_CommentCache);

            if (jsonToMap.get("userCache") != null) {
                new_userCache = userCache + jsonToMap.get("userCache").toString();
            } else {
                new_userCache = userCache;
            }
            editFile.replacTextContent(old_userCache, new_userCache);
            return Result.getResultJson(1, "修改成功，手动重启后生效", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(1, "修改失败，请确认参数是否正确", null);
        }
    }


    /***
     * 获取数据库中的配置
     */
    @RequestMapping(value = "/getApiConfig")
    @ResponseBody
    public String getApiConfig(@RequestParam(value = "webkey", required = false) String webkey) {
        if (webkey.length() < 1) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        if (!webkey.equals(this.key)) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        Apiconfig apiconfig = apiconfigService.selectByKey(1);
        Map json = JSONObject.parseObject(JSONObject.toJSONString(apiconfig), Map.class);
        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("msg", "");
        response.put("data", json);
        return response.toString();
    }

    /***
     * 配置修改
     */
    @RequestMapping(value = "/apiConfigUpdate")
    @ResponseBody
    public String apiConfigUpdate(@RequestParam(value = "params", required = false) String params, @RequestParam(value = "webkey", required = false) String webkey) {
        Apiconfig update = null;
        if (webkey.isEmpty()) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        if (!webkey.equals(this.key)) {
            return Result.getResultJson(0, "请输入正确的访问key", null);
        }
        if (StringUtils.isNotBlank(params)) {
            JSONObject object = JSON.parseObject(params);
            update = object.toJavaObject(Apiconfig.class);
        }
        update.setId(1);
        int rows = apiconfigService.update(update);
        //更新Redis缓存
        Apiconfig apiconfig = apiconfigService.selectByKey(1);
        Map configJson = JSONObject.parseObject(JSONObject.toJSONString(apiconfig), Map.class);
        redisHelp.delete(dataprefix + "_" + "config", redisTemplate);
        redisHelp.setKey(dataprefix + "_" + "config", configJson, 6000, redisTemplate);
        JSONObject response = new JSONObject();
        response.put("code", rows);
        response.put("msg", rows > 0 ? "修改成功，当前配置已生效！" : "修改失败");
        return response.toString();
    }


    /***
     * 初始化APP
     */
    @RequestMapping(value = "/initApp")
    @ResponseBody
    public String initApp(@RequestParam(value = "webkey", required = false, defaultValue = "") String webkey) {

        try {
            if (!webkey.equals(this.key)) {
                return Result.getResultJson(201, "Key错误", null);
            }
            App app = new App();
            Integer total = appService.total(app);
            if (total < 1) {
                app.setName("应用名称");
                app.setCurrencyName("积分");
                appService.insert(app);
            } else {
                return Result.getResultJson(201, "无需初始化", null);
            }
            return Result.getResultJson(200, "已初始化完成", null);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(400, "接口错误", null);
        }
    }

    /***
     * 修改应用
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public String updateApp(@RequestParam(value = "webkey", required = false, defaultValue = "") String webkey,
                            @RequestParam(value = "params", required = false) String params) {

        App update = null;
        if (!webkey.equals(this.key)) {
            return Result.getResultJson(201, "Key错误", null);
        }
        try {
            if (StringUtils.isNotBlank(params)) {
                JSONObject object = JSON.parseObject(params);
                update = object.toJavaObject(App.class);
                update.setId(1);
            }
            int rows = appService.update(update);
            if (rows > 0) {
                redisHelp.delete(this.dataprefix + "_" + "appList", redisTemplate);
                return Result.getResultJson(200, "修改完成", null);
            } else {
                return Result.getResultJson(402, "修改失败", null);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(400, "接口请求异常，请联系管理员", null);
        }
    }


    /***
     * 查询APP详情
     */
    @RequestMapping(value = "/app")
    @ResponseBody
    public String app() {
        try {
            Map appJson = new HashMap<String, String>();
            Map cacheInfo = redisHelp.getMapValue(this.dataprefix + "_" + "appJson_1", redisTemplate);

            if (cacheInfo.size() > 0) {
                appJson = cacheInfo;
            } else {
                App app = appService.selectByKey(1);
                if (app == null) {
                    return Result.getResultJson(401, "应用不存在或密钥错误", null);
                }
                appJson = JSONObject.parseObject(JSONObject.toJSONString(app), Map.class);
                redisHelp.delete(this.dataprefix + "_" + "appJson_1", redisTemplate);
                redisHelp.setKey(this.dataprefix + "_" + "appJson_1", appJson, 10, redisTemplate);
            }
            return Result.getResultJson(200, "获取成功", appJson);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(400, "接口错误", null);
        }

    }

    @RequestMapping(value = "/vip")
    @ResponseBody
    public String vip() {
        try {
            Apiconfig apiconfig = UStatus.getConfig(dataprefix, apiconfigService, redisTemplate);
            Map<String, Object> data = new HashMap<>();
            data.put("vipPrice", apiconfig.getVipPrice());
            data.put("vipDiscount", apiconfig.getVipDiscount());
            data.put("vipDay", apiconfig.getVipDay());
            data.put("ratio", apiconfig.getScale());

            return Result.getResultJson(200, "获取成功", data);


        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(400, "接口异常", null);
        }
    }

}
