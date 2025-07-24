package cn.dotfashion.soa.pim.service.category.audit.task.impl;

import cn.dotfashion.soa.framework.util.DateFormatTools;
import cn.dotfashion.soa.framework.util.ExceptionTools;
import cn.dotfashion.soa.pim.constant.LoggerConstants;
import cn.dotfashion.soa.pim.enums.category.audit.TaskStateEnum;
import cn.dotfashion.soa.pim.mq.log.LogCommonUtilService;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskLogService;
import com.shein.common.enums.LogEnum;
import com.shein.common.enums.OperationMethodEnum;
import com.shein.common.logger.entity.OperationLogger;
import com.shein.common.logger.service.OperationLoggerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.shein.common.enums.OperationMethodEnum.UPDATE;


@Service
public class CategoryAuditTaskLogServiceImpl implements CategoryAuditTaskLogService {

    @Resource
    private OperationLoggerService operationLoggerService;

    @Override
    public void saveVersionLog(Long taskId, Integer version, String operator) {
        StringBuilder after = new StringBuilder();
        after.append(version);
        OperationLogger operationLogger = OperationLogger.buildDefaultLoggerWithDto(LoggerConstants.CATEGORY_TASK_VALUE_CHANGE,
                UPDATE, null, StringUtils.EMPTY, after.toString(), String.valueOf(taskId), operator);
        operationLoggerService.log(operationLogger);
    }

    @Override
    public void saveTaskStateLog(Long taskId, TaskStateEnum taskStateEnum, String operator) {
        StringBuilder after = new StringBuilder();
        after.append(taskStateEnum.getDescription());
        OperationLogger operationLogger = OperationLogger.buildDefaultLoggerWithDto(LoggerConstants.CATEGORY_TASK_STATE_CHANGE,
                UPDATE, null, StringUtils.EMPTY, after.toString(), String.valueOf(taskId), operator);
        operationLoggerService.log(operationLogger);
    }

    @Override
    public void saveTaskShowTimeLog(Long taskId, List<String> categoryKeyList, Date showTime, String operator) {
        StringBuilder after = new StringBuilder();
        after.append("设置展示时间为：").append(DateFormatTools.formatDate(DateFormatTools.PATTERN_DEFAULT_ON_SECOND, showTime)).append("，分类信息：");
        after.append(categoryKeyList);
        OperationLogger operationLogger = OperationLogger.buildDefaultLoggerWithDto(LoggerConstants.CATEGORY_TASK_SHOW_TIME_CHANGE,
                UPDATE, null, StringUtils.EMPTY, after.toString(), String.valueOf(taskId), operator);
        operationLoggerService.log(operationLogger);
    }

    @Override
    public void saveTaskShowTimeSuccessLog(Long taskId, List<String> categoryKeyList, String operator) {
        StringBuilder after = new StringBuilder();
        after.append("设置展示时间成功").append("，分类信息：");
        after.append(categoryKeyList);
        OperationLogger operationLogger = OperationLogger.buildDefaultLoggerWithDto(LoggerConstants.CATEGORY_TASK_SHOW_TIME_CHANGE,
                UPDATE, null, StringUtils.EMPTY, after.toString(), String.valueOf(taskId), operator);
        operationLoggerService.log(operationLogger);
    }
}
