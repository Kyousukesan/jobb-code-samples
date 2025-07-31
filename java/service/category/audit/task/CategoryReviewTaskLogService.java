package cn.company.soa.module.service.category.audit.task;

import cn.company.soa.module.enums.category.audit.TaskStateEnum;

import java.util.Date;
import java.util.List;

public interface CategoryAuditTaskLogService {


    //记录任务单号内容版本
    void saveVersionLog(Long taskId, Integer version, String operator);


    //任务状态变更
    void saveTaskStateLog(Long taskId, TaskStateEnum taskStateEnum, String operator);

    //任务单号展示时间变更
    void saveTaskShowTimeLog(Long taskId, List<String> categoryKeyList, Date showTime, String operator);

    void saveTaskShowTimeSuccessLog(Long taskId, List<String> categoryKeyList, String operator);
}
