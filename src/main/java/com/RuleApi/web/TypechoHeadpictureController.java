package com.RuleApi.web;

import com.RuleApi.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.RuleApi.entity.*;
import com.RuleApi.service.*;
import org.apache.commons.lang3.StringUtils;
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

            insert = JSON.parseObject(JSON.toJSONString(insert), TypechoHeadpicture.class);
            Integer code = service.insert(insert);
            // 给用户添加头像框ID
            Integer id = insert.getId();
            Map<String, Object> result = new HashMap();
            if (id != 0 && id != null) {
                JSONArray headList = new JSONArray();
                TypechoUsers user = usersService.selectByKey(userInfo.get("uid"));
                if (user != null) {
                    if (user.getHead_picture() != null && !user.getHead_picture().isEmpty()) {
                        headList = JSONArray.parseArray(user.getHead_picture());
                    }
                }
                result.put("id", id);
                headList.add(id);
                user.setHead_picture(headList.toString());
                usersService.update(user);
            }

            return Result.getResultJson(code, code > 0 ? "添加成功" : "添加失败", result);

        } catch (NumberFormatException e) {
            return Result.getResultJson(0, "数字格式异常：" + e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口请求异常，请联系管理员", null);
        }
    }


    /***
     * 获取头像框
     */

    @RequestMapping(value = "/headList")
    @ResponseBody
    public String headpictureList(
            @RequestParam(value = "searchParams", required = false) String searchParams,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
            @RequestParam(value = "order", required = false, defaultValue = "permission desc") String order,
            @RequestParam(value = "token", required = false) String token
    ) {
        // 限制 limit 的范围
        limit = (limit > 50) ? 50 : limit;

        Integer total = 0;
        List<Map<String, Object>> jsonList = new ArrayList<>();

        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            TypechoHeadpicture query = new TypechoHeadpicture();
            query.setStatus(object.getInteger("status"));
            query.setType(object.getInteger("type"));
            // 如果传入了token就查自己的头像框
            if (!token.isEmpty() && token != null) {
                Map<Object, Object> userInfo = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                if (!userInfo.isEmpty() && userInfo != null) {
                    query.setId(Integer.parseInt(userInfo.get("uid").toString()));
                }
            }
            total = service.total(query);
            PageList<TypechoHeadpicture> Pagelist = service.selectPage(query, page, limit, order);
            List<TypechoHeadpicture> list = Pagelist.getList();

            for (TypechoHeadpicture headpicture : list) {
                Map<String, Object> json = new HashMap<>();
                json.put("id", headpicture.getId());
                json.put("name", headpicture.getName());
                json.put("link", headpicture.getLink());
                json.put("type", headpicture.getType());
                json.put("permission", headpicture.getPermission());
                // 添加其他属性...
                Integer isActive = 0;
                if (!token.isEmpty() && token != null) {
                    Map<Object, Object> user = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                    if (user.get("uid") != null) {
                        // 获取用户拥有的头像框
                        TypechoUsers userInfo = usersService.selectByKey(user.get("uid"));
                        if (userInfo != null && StringUtils.isNotBlank(userInfo.getHead_picture())) {
                            List<String> headList = JSONArray.parseArray(userInfo.getHead_picture(), String.class);
                            if (headList.contains(headpicture.getId().toString())) {
                                isActive = 1;
                            }
                        }
                    }
                }

                json.put("isActive", isActive);
                jsonList.add(json);
            }
        }

        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("msg", "");
        response.put("data", jsonList);
        response.put("count", jsonList.size());
        response.put("total", total);
        return response.toString();
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
        Map<String, Object> jsonInfo = JSONObject.parseObject(params);
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
        Object groupValue = userInfo.get("group");
        if (groupValue != null) {
            String permission = groupValue.toString();
            return permission.equals("administrator") || permission.equals("editor");
        } else {
            // 处理空值的情况，这里可以根据实际需求做相应的处理，例如返回false或抛出异常。
            return false;
        }
    }

    // 检查字符串是否为 null 或为空
    private boolean isNullOrEmpty(Object value) {
        return value == null || value.toString().isEmpty();
    }
}
