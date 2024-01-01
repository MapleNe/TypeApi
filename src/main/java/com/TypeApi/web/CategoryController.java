package com.TypeApi.web;

import com.TypeApi.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.TypeApi.entity.*;
import com.TypeApi.service.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制层
 * TypechoMetasController
 *
 * @author buxia97
 * @date 2021/11/29
 */
@Component
@Controller
@RequestMapping(value = "/category")
public class CategoryController {

    @Autowired
    CategoryService service;

    @Autowired
    private RelationshipsService relationshipsService;

    @Autowired
    private ArticleService contentsService;

    @Autowired
    private FieldsService fieldsService;

    @Autowired
    private FanService fanService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private ApiconfigService apiconfigService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${webinfo.contentCache}")
    private Integer contentCache;

    @Value("${web.prefix}")
    private String dataprefix;


    RedisHelp redisHelp = new RedisHelp();
    ResultAll Result = new ResultAll();
    baseFull baseFull = new baseFull();
    UserStatus UStatus = new UserStatus();
    EditFile editFile = new EditFile();

    /***
     * 查询分类或标签下的文章
     *
     */
    @RequestMapping(value = "/selectContents")
    @ResponseBody
    public String selectContents(@RequestParam(value = "searchParams", required = false) String searchParams,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                 @RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
                                 @RequestParam(value = "order", required = false, defaultValue = "") String order,
                                 @RequestParam(value = "token", required = false, defaultValue = "") String token) {

        Relationships query = new Relationships();
        if (limit > 50) {
            limit = 50;
        }

        Integer uid = null;
        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        Apiconfig userStatus = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
        if (userStatus.getIsLogin().equals(1)) {
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            } else {
                if (token != null && !token.isEmpty()) {
                    Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                    uid = Integer.parseInt(map.get("uid").toString());
                }
            }
        }
        String sqlParams = "null";
        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            Integer mid = 0;
            if (object.get("mid") != null) {
                mid = Integer.parseInt(object.get("mid").toString());
            }

            query.setMid(mid);
            Article contents = new Article();
            contents.setStatus("publish");
            query.setContents(contents);
            Map paramsJson = JSONObject.parseObject(JSONObject.toJSONString(query), Map.class);
            sqlParams = paramsJson.toString();
        }
        List jsonList = new ArrayList();
        List cacheList = redisHelp.getList(this.dataprefix + "_" + "selectContents_" + page + "_" + limit + "_" + sqlParams, redisTemplate);

        try {
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            } else {
                Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
                //首先查询typechoRelationships获取映射关系
                PageList<Relationships> pageList = relationshipsService.selectPage(query, page, limit);
                List<Relationships> list = pageList.getList();
                if (list.size() < 1) {
                    JSONObject noData = new JSONObject();
                    noData.put("code", 1);
                    noData.put("msg", "");
                    noData.put("data", new ArrayList());
                    noData.put("count", 0);
                    return noData.toString();
                }
                for (int i = 0; i < list.size(); i++) {
                    Integer cid = list.get(i).getCid();
                    Article article = list.get(i).getContents();
                    Map contentsInfo = JSONObject.parseObject(JSONObject.toJSONString(article), Map.class);
                    //写入作者详细信息
                    Integer authorId = article.getAuthorId();
                    if (authorId > 0) {
                        Users author = usersService.selectByKey(authorId);
                        Map authorInfo = new HashMap();
                        if (author != null) {
                            String name = author.getName();
                            if (author.getScreenName() != "" && author.getScreenName() != null) {
                                name = author.getScreenName();
                            }
                            String avatar = apiconfig.getWebinfoAvatar() + "null";
                            if (author.getAvatar() != "" && author.getAvatar() != null) {
                                avatar = author.getAvatar();
                            } else {
                                if (author.getMail() != "" && author.getMail() != null) {
                                    String mail = author.getMail();

                                    if (mail.indexOf("@qq.com") != -1) {
                                        String qq = mail.replace("@qq.com", "");
                                        avatar = "https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";
                                    } else {
                                        avatar = baseFull.getAvatar(apiconfig.getWebinfoAvatar(), author.getMail());
                                    }
                                    //avatar = baseFull.getAvatar(apiconfig.getWebinfoAvatar(), author.getMail());
                                }
                            }
                            // 格式化用户opt设置
                            JSONObject opt = JSONObject.parseObject(author.getOpt());
                            if (opt instanceof Object) {
                                opt = JSONObject.parseObject(author.getOpt());
                            } else {
                                opt = null;
                            }
                            // 再查询是否关注了用户
                            Integer isfollow = 0;
                            if (uid != null && uid > 0) {
                                Fan fan = new Fan();
                                fan.setUid(uid);
                                fan.setTouid(authorId);
                                isfollow = fanService.total(fan);
                            }

                            authorInfo.put("name", name);
                            authorInfo.put("avatar", avatar);
                            authorInfo.put("customize", author.getCustomize());
                            authorInfo.put("opt", opt);
                            authorInfo.put("isfollow", isfollow);
                            //判断是否为VIP
                            authorInfo.put("isvip", 0);
                            Long date = System.currentTimeMillis();
                            String curTime = String.valueOf(date).substring(0, 10);
                            Integer viptime = author.getVip();

                            if (viptime > Integer.parseInt(curTime) || viptime.equals(1)) {
                                authorInfo.put("isvip", 1);
                            }
                            if (viptime.equals(1)) {
                                //永久VIP
                                authorInfo.put("isvip", 2);
                            }
                        } else {
                            authorInfo.put("name", "用户已注销");
                            authorInfo.put("avatar", apiconfig.getWebinfoAvatar() + "null");
                        }
                        contentsInfo.put("authorInfo", authorInfo);
                    }

                    // 格式化文章opt
                    JSONObject opt = null;
                    Object optValue = contentsInfo.get("opt");

                    if (optValue != null) {
                        String optString = optValue.toString();

                        if (!optString.isEmpty()) {
                            opt = JSONObject.parseObject(optString);
                        }
                    }
                    //处理文章内容为简介

                    String text = contentsInfo.get("text").toString();
                    boolean status = text.contains("<!--markdown-->");
                    if (status) {
                        contentsInfo.put("markdown", 1);
                    } else {
                        contentsInfo.put("markdown", 0);
                    }
                    List imgList = baseFull.getImageSrc(text);
                    text = baseFull.toStrByChinese(text);
                    contentsInfo.put("text", text.length() > 400 ? text.substring(0, 400) : text);

                    contentsInfo.put("images", imgList);
                    //加入自定义字段，分类和标签
                    //加入自定义字段信息，这里取消注释即可开启，但是数据库查询会消耗性能
                    Fields f = new Fields();
                    f.setCid(cid);
                    List<Fields> fields = fieldsService.selectList(f);
                    contentsInfo.put("fields", fields);

                    Relationships rs = new Relationships();
                    rs.setCid(cid);
                    List<Relationships> relationships = relationshipsService.selectList(rs);

                    List metas = new ArrayList();
                    List tags = new ArrayList();
                    for (int j = 0; j < relationships.size(); j++) {
                        Map info = JSONObject.parseObject(JSONObject.toJSONString(relationships.get(j)), Map.class);
                        if (info != null) {
                            String mid = info.get("mid").toString();

                            Category metasList = service.selectByKey(mid);
                            Map metasInfo = JSONObject.parseObject(JSONObject.toJSONString(metasList), Map.class);
                            String type = metasInfo.get("type").toString();
                            if (type.equals("category")) {
                                metas.add(metasInfo);
                            }
                            if (type.equals("tag")) {
                                tags.add(metasInfo);
                            }
                        }

                    }

                    contentsInfo.remove("password");
                    contentsInfo.put("category", metas);
                    contentsInfo.put("opt", opt);
                    contentsInfo.put("tag", tags);

                    jsonList.add(contentsInfo);


                    //存入redis

                }
                redisHelp.delete(this.dataprefix + "_" + "selectContents_" + page + "_" + limit + "_" + sqlParams, redisTemplate);
                redisHelp.setList(this.dataprefix + "_" + "selectContents_" + page + "_" + limit + "_" + sqlParams, jsonList, this.contentCache, redisTemplate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            }
        }
        if (order != null && order != "") {
            jsonList = sortJsonList(jsonList, order);
        }

        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("msg", "");
        response.put("data", null != jsonList ? jsonList : new JSONArray());
        response.put("count", jsonList.size());
        return response.toString();
    }

    private List<Map<Object, Integer>> sortJsonList(List<Map<Object, Integer>> jsonList, String order) {
        switch (order) {
            case "hot":
                jsonList.sort(
                        Comparator.comparingInt(o -> {
                                    Map<Object, Integer> map = (Map<Object, Integer>) o;
                                    return ((Number) map.getOrDefault("likes", 0)).intValue();
                                })
                                .thenComparing(o -> {
                                    Map<Object, Long> map = (Map<Object, Long>) o;
                                    return ((Number) map.getOrDefault("replyTime", 0L)).longValue();

                                })
                                .thenComparing(o -> {
                                    Map<Object, String> map = (Map<Object, String>) o;
                                    return ((String) map.getOrDefault("text", ""));
                                })

                                .thenComparing(o -> {
                                    Map<Object, Long> map = (Map<Object, Long>) o;
                                    return ((Number) map.getOrDefault("created", 0L)).longValue();

                                }).thenComparingInt(o -> {
                                    Map<Object, Integer> map = (Map<Object, Integer>) o;
                                    return ((Number) map.getOrDefault("views", 0)).intValue();
                                })
                );
                break;
            case "new":
                jsonList.sort((Comparator<? super Map<Object, Integer>>) Comparator
                        .comparingInt((Map<Object, Integer> o) -> o.getOrDefault("created", 0))
                        .reversed()
                );
                break;
            // Add more cases for other order types if needed
        }

        // Convert List<Map<Object, Integer>> to List<Map<String, Object>> if needed
        // Assuming your data structure allows this conversion
        // List<Map<String, Object>> result = new ArrayList<>(jsonList);

        // Return the sorted list
        return jsonList;
    }


    /***
     * 查询分类和标签
     * @param searchParams Bean对象JSON字符串
     * @param page         页码
     * @param limit        每页显示数量
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public String categoryList(@RequestParam(value = "searchParams", required = false) String searchParams,
                               @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                               @RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
                               @RequestParam(value = "searchKey", required = false, defaultValue = "") String searchKey,
                               @RequestParam(value = "order", required = false, defaultValue = "") String order) {
        Category query = new Category();
        String sqlParams = "null";
        if (limit > 50) {
            limit = 50;
        }
        Integer total = 0;
        List jsonList = new ArrayList();

        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            query = object.toJavaObject(Category.class);
            Map paramsJson = JSONObject.parseObject(JSONObject.toJSONString(query), Map.class);
            sqlParams = paramsJson.toString();
        }
        List cacheList = redisHelp.getList(this.dataprefix + "_" + "metasList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, redisTemplate);

        total = service.total(query);
        try {
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            } else {
                PageList<Category> pageList = service.selectPage(query, page, limit, searchKey, order);
                List<Category> list = pageList.getList();


                if (list.size() < 1) {
                    JSONObject noData = new JSONObject();
                    noData.put("code", 1);
                    noData.put("msg", "");
                    noData.put("data", new ArrayList());
                    noData.put("count", 0);
                    return noData.toString();
                } else {
                    for (int i = 0; i < list.size(); i++) {

                        Map json = JSONObject.parseObject(JSONObject.toJSONString(list.get(i)), Map.class);
                        Object optObject = json.get("opt");

                        if (optObject != null && !optObject.toString().isEmpty() && optObject.toString() != "") {
                            JSONObject opt = JSONObject.parseObject(optObject.toString());
                            json.put("opt", opt);
                        }
                        // 获取二级分类
//                        Category subCategorySearch = new Category();
//                        subCategorySearch.setParent(Integer.parseInt(json.get("mid").toString()));
//                        List<Category> subCategory  = service.selectList(subCategorySearch);
//                        System.out.println("打印子分类查询"+subCategorySearch+subCategory);
//                        json.put("subCategory", subCategory);

                        // 根据具体需求决定是否将 json 添加到 jsonList
                        jsonList.add(json);
                    }
                }
                redisHelp.delete(this.dataprefix + "_" + "metasList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, redisTemplate);
                redisHelp.setList(this.dataprefix + "_" + "metasList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, jsonList, 10, redisTemplate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cacheList.size() > 0) {
                jsonList = cacheList;
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

    /***
     * 查询分类详情
     */
    @RequestMapping(value = "/info")
    @ResponseBody
    public String metaInfo(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "slug", required = false) String slug) {
        try {
            Map metaInfoJson = new HashMap<String, String>();
            Map cacheInfo = redisHelp.getMapValue(this.dataprefix + "_" + "metaInfo_" + key + "_" + slug, redisTemplate);

            if (cacheInfo.isEmpty()) {
                Category metas = (slug != null) ? service.selectBySlug(slug) : service.selectByKey(key);
                Map<String, Object> opt = JSONObject.parseObject(metas.getOpt());
                metaInfoJson = new HashMap<>(JSONObject.parseObject(JSONObject.toJSONString(metas), Map.class));
                metaInfoJson.put("opt", opt);
                redisHelp.delete(this.dataprefix + "_" + "metaInfo_" + key + "_" + slug, redisTemplate);
                redisHelp.setKey(this.dataprefix + "_" + "metaInfo_" + key + "_" + slug, metaInfoJson, 20, redisTemplate);
            } else {
                metaInfoJson = cacheInfo;
            }

            JSONObject response = new JSONObject();
            response.put("code", 1);
            response.put("msg", "");
            response.put("data", metaInfoJson);

            return response.toString();
        } catch (Exception e) {
            JSONObject response = new JSONObject();
            response.put("code", 1);
            response.put("msg", "An error occurred while processing metaInfo");
            response.put("data", null);

            return response.toString();
        }
    }

    /***
     * 修改分类和标签
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public String editMeta(@RequestParam(value = "params", required = false) String params, @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            String group = map.get("group").toString();
            if (!group.equals("administrator")) {
                return Result.getResultJson(0, "你没有操作权限", null);
            }
            String logUid = map.get("uid").toString();
            Category update = new Category();
            Map jsonToMap = null;
            if (StringUtils.isNotBlank(params)) {
                jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
                //为了数据稳定性考虑，禁止修改类型
                jsonToMap.remove("type");
                update = JSON.parseObject(JSON.toJSONString(jsonToMap), Category.class);
            }

            int rows = service.update(update);
            editFile.setLog("管理员" + logUid + "请求修改分类" + jsonToMap.get("mid").toString());
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", 0);
            response.put("msg", "操作失败");
            return response.toString();
        }

    }

    /***
     * 修改分类和标签
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public String addMeta(@RequestParam(value = "params", required = false) String params, @RequestParam(value = "token", required = false) String token) {
        try {
            System.out.println(params);
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            String group = map.get("group").toString();
            if (!group.equals("administrator")) {
                return Result.getResultJson(0, "你没有操作权限", null);
            }
            String logUid = map.get("uid").toString();
            Category insert = new Category();
            Map jsonToMap = null;
            if (StringUtils.isNotBlank(params)) {
                jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
                String type = jsonToMap.get("type").toString();
                if (!type.equals("category") && !type.equals("tag")) {
                    return Result.getResultJson(0, "类型参数不正确", null);
                }
                //为了数据稳定性考虑，禁止修改类型
                insert = JSON.parseObject(JSON.toJSONString(jsonToMap), Category.class);
            }
            //判断是否存在相同的分类或标签名称
            Category oldMeta = new Category();
            oldMeta.setName(insert.getName());
            oldMeta.setType(insert.getType());
            Integer isHave = service.total(oldMeta);
            if (isHave > 0) {
                return Result.getResultJson(0, "已存在同名数据", null);
            }
            int rows = service.insert(insert);
            editFile.setLog("管理员" + logUid + "请求添加分类");
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            JSONObject response = new JSONObject();
            response.put("code", 0);
            response.put("msg", "操作失败");
            return response.toString();
        }

    }

    /***
     * 删除分类和标签
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public String deleteMeta(@RequestParam(value = "id", required = false) String id,
                             @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            String group = map.get("group").toString();
            String logUid = map.get("uid").toString();
            if (!group.equals("administrator")) {
                return Result.getResultJson(0, "你没有操作权限", null);
            }
            Category meta = service.selectByKey(id);
            if (meta == null) {
                return Result.getResultJson(0, "数据不存在", null);
            }
            int rows = service.delete(id);
            editFile.setLog("管理员" + logUid + "请求删除分类" + id);
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", 0);
            response.put("msg", "操作失败");
            return response.toString();
        }

    }

}