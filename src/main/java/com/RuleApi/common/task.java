package com.RuleApi.common;
import com.RuleApi.entity.*;
import com.RuleApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/**
 * &#064;每日任务
 * &#064; 鸽鸽鸽
 */
@Controller
public class task {

    @Autowired
    private TypechoApiconfigService apiconfigService;

    @Autowired
    private TypechoUsersService usersService;

    @Autowired
    private TypechoTaskService taskService;

    @Autowired
    private TypechoPaylogService paylogService;
    public String sign(Integer uid, String type) {
        String result = "";
        try {
            TypechoApiconfig config = apiconfigService.selectByKey(1);
            int quantity = 0;
            int refsExp = 0;
            int refsScore = 0;
            switch (type) {
                case "comment":
                    quantity = config.getComment();
                    refsExp = config.getCommentExp();
                    refsScore = config.getCommentScore();
                    break;
                case "contents":
                    quantity = config.getContents();
                    refsExp = config.getContentsExp();
                    refsScore = config.getContentsScore();
                    break;
                case "follow":
                    quantity = config.getFollow();
                    refsExp = config.getFollowExp();
                    refsScore = config.getFollowScore();
                    break;
                case "reward":
                    quantity = config.getReward();
                    refsExp = config.getRewardExp();
                    refsScore = config.getRewardScore();
                    break;
            }

            long curStamp = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Integer curtime = Integer.valueOf(sdf.format(new Date(curStamp)));

            TypechoTask task = new TypechoTask();
            task.setUid(uid);
            List<TypechoTask> taskList = taskService.selectList(task);

            if (!taskList.isEmpty()) {
                result = handleExistingTask(uid, type, quantity, refsExp, refsScore, curtime, taskList.get(0));
            } else if(quantity!=0) {
                result = handleNewTask(uid, type, quantity, refsExp, refsScore, curtime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String handleExistingTask(Integer uid, String type, int quantity, int refsExp, int refsScore, Integer curtime, TypechoTask userTask) {
        int time = userTask.getTime();
        String result = "";
        int current = 0;
        switch (type) {
            case "comment":
                current = userTask.getComment();
                break;
            case "contents":
                current = userTask.getContents();
                break;
            case "follow":
                current = userTask.getFollow();
                break;
            case "reward":
                current = userTask.getReward();
                break;
        }
        int taskId = userTask.getId();

        if (time >= curtime) {
            if (current < quantity) {
                result = updateUserTaskAndExperience(uid, type, refsExp, refsScore, current, taskId);
            }
        } else if(quantity!=0) {
            result = createNewUserTaskAndExperience(uid, type, refsExp, refsScore, curtime);
        }
        return result;
    }
    private String handleNewTask(Integer uid, String type, int quantity, int refsExp, int refsScore, Integer curtime) {

        return createNewUserTaskAndExperience(uid, type, refsExp, refsScore, curtime);
    }
    private String updateUserTaskAndExperience(Integer uid, String type, int refsExp, int refsScore, int current, Integer taskId) {
        TypechoUsers user = usersService.selectByKey(uid);
        TypechoUsers newUser = new TypechoUsers();
        Integer oldExperience = user.getExperience();
        Integer newExperience = oldExperience + refsExp;
        Integer newScore = user.getAssets() + refsScore;
        newUser.setAssets(newScore);
        newUser.setExperience(newExperience);
        newUser.setUid(uid);
        usersService.update(newUser);

        TypechoTask log = new TypechoTask();
        log.setId(taskId);

        switch (type) {
            case "comment":
                log.setComment(current + 1);
                break;
            case "contents":
                log.setContents(current + 1);
                break;
            case "follow":
                log.setFollow(current + 1);
                break;
            case "reward":
                log.setReward(current + 1);
                break;
        }

        taskService.update(log);
        if (refsScore > 0){
            Rewardnotification(uid,refsScore);
            return "赠送您" + refsExp + "经验值和" + refsScore + "积分";
        }
        return "赠送您" + refsExp + "经验值";
    }

    private String createNewUserTaskAndExperience(Integer uid, String type, int refsExp, int refsScore, Integer curTime) {
        TypechoUsers user = usersService.selectByKey(uid);
        TypechoUsers newUser = new TypechoUsers();
        Integer oldExperience = user.getExperience();
        Integer newExperience = oldExperience + refsExp;
        Integer newScore = user.getAssets() + refsScore;
        newUser.setAssets(newScore);
        newUser.setExperience(newExperience);
        newUser.setUid(uid);
        usersService.update(newUser);

        TypechoTask log = new TypechoTask();
        log.setUid(uid);
        log.setTime(curTime);

        switch (type) {
            case "comment":
                log.setComment(1);
                break;
            case "contents":
                log.setContents(1);
                break;
            case "follow":
                log.setFollow(1);
                break;
            case "reward":
                log.setReward(1);
                break;
        }

        log.setUid(uid);
        log.setTime(curTime);
        taskService.insert(log);
        if (refsScore > 0){
            Rewardnotification(uid,refsScore);
            return "赠送您" + refsExp + "经验值和" + refsScore + "积分";
        }
        return "赠送您" + refsExp + "经验值";
    }

    private void Rewardnotification(Integer uid, Integer assets) {
        Long date = System.currentTimeMillis();
        String userTime = String.valueOf(date).substring(0,10);
        TypechoPaylog paylog = new TypechoPaylog();
        paylog.setStatus(1);
        paylog.setCreated(Integer.parseInt(userTime));
        paylog.setUid(uid);
        paylog.setOutTradeNo(userTime+"adsGift");
        paylog.setTotalAmount(String.valueOf(assets));
        paylog.setPaytype("Task");
        paylog.setSubject("任务奖励");
        paylogService.insert(paylog);
    }
}
