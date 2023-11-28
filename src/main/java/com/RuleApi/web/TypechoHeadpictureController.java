package com.RuleApi.web;

import com.RuleApi.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.RuleApi.entity.*;
import com.RuleApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
@Controller
@RequestMapping(value = "/TypechoHeadpicture")
public class TypechoHeadpictureController {
    @Autowired
    private TypechoUsersService usersService;

    @Autowired
    private TypechoHeadpictureService service;

    @Autowired
    private TypechoApiconfigService apiconfigService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${web.prefix}")
    private String dataprefix;

    @Value("${mybatis.configuration.variables.prefix}")
    private String prefix;


    @Autowired
    private JdbcTemplate jdbcTemplate;
    RedisHelp redisHelp = new RedisHelp();
    ResultAll Result = new ResultAll();
    baseFull baseFull = new baseFull();
    UserStatus UStatus = new UserStatus();
    HttpClient HttpClient = new HttpClient();
    EditFile editFile = new EditFile();

    @RequestMapping(value = "/headAdd")
    @ResponseBody
    public String headAdd(@RequestParam(value = "params", required = false) String params,
                          @RequestParam(value = "token", required = true) String token,
                          HttpServletRequest request) {

        try {
            Integer userStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);

            // 未登录
            if (userStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }

            // 如果已登录
            Map<Object, Object> userInfo = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);

            if (userInfo == null) {
                return Result.getResultJson(0, "用户信息为空", null);
            }

            // 判断用户权限
            if (!hasPermission(userInfo)) {
                return Result.getResultJson(0, "无权限", null);
            }

            // 处理头像信息
            TypechoHeadpicture insert = handleHeadPicture(params, userInfo);

            insert.setCreator(Integer.parseInt(userInfo.get("uid").toString()));

            int code = service.insert(insert);

            return Result.getResultJson(code, code > 0 ? "添加成功" : "添加失败", null);

        } catch (NumberFormatException e) {
            return Result.getResultJson(0, "数字格式异常：" + e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口请求异常，请联系管理员", null);
        }
    }

    // 检查用户权限
    private boolean hasPermission(Map<Object, Object> userInfo) {
        Integer viptime = Integer.parseInt(userInfo.get("vip").toString());
        String permission = userInfo.get("group").toString();
        Long date = System.currentTimeMillis();
        String curTime = String.valueOf(date).substring(0, 10);

        return viptime > Integer.parseInt(curTime) || viptime.equals(1) || permission.equals("administrator") || permission.equals("editor");
    }

    // 处理头像信息
    private TypechoHeadpicture handleHeadPicture(String params, Map<Object, Object> userInfo) {
        Map<String, Object> jsonInfo = JSONObject.parseObject(JSON.parseObject(params).toString());

        // 如果不是 Administrator 或者 editor，只能为私人，不能公开
        if (!isAdministratorOrEditor(userInfo)) {
            jsonInfo.put("type", 0);
            jsonInfo.put("permission", 0);
        }

        // 检查 name 和 link 是否为空
        if (isNullOrEmpty(jsonInfo.get("name")) || isNullOrEmpty(jsonInfo.get("link"))) {
            throw new IllegalArgumentException("name或link为空");
        }

        // ... 其他处理逻辑，如果需要的话 ...

        return JSON.parseObject(JSON.toJSONString(jsonInfo), TypechoHeadpicture.class);
    }

    // 检查是否是 Administrator 或者 editor
    private boolean isAdministratorOrEditor(Map<Object, Object> userInfo) {
        String permission = userInfo.get("group").toString();
        return permission.equals("administrator") || permission.equals("editor");
    }

    // 检查字符串是否为 null 或为空
    private boolean isNullOrEmpty(Object value) {
        return value == null || value.toString().isEmpty();
    }
}
