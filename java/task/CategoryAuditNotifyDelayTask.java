package cn.dotfashion.soa.pim.task;

import cn.dotfashion.soa.api.vo.Response;
import cn.dotfashion.soa.framework.util.DateFormatTools;
import cn.dotfashion.soa.framework.util.JsonTools;
import cn.dotfashion.soa.log.LtLogFormat;
import cn.dotfashion.soa.pim.client.PdsCategoryBackClient;
import cn.dotfashion.soa.pim.client.PdsFrontCategoryClient;
import cn.dotfashion.soa.pim.client.vo.req.BatchChangeCategoryControlShowReq;
import cn.dotfashion.soa.pim.client.vo.req.BatchChangeCategoryControlStatueReq;
import cn.dotfashion.soa.pim.dto.CategoryAuditTaskWechatDto;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskTimeEntity;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskLogService;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskTimeService;
import cn.dotfashion.soa.pim.util.UserContextUtil;
import cn.dotfashion.soa.task.embedded.core.SchedulableTask;
import cn.dotfashion.soa.task.embedded.core.Task;
import cn.dotfashion.soa.task.embedded.core.TaskParams;
import com.shein.common.combine.CombineManager;
import com.shein.common.enums.ModuleTypeEnum;
import com.shein.common.exception.BusinessAssert;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Component
public class CategoryAuditNotifyDelayTask implements SchedulableTask {

    @Resource
    private CategoryAuditTaskLogService categoryAuditTaskLogService;
    @Resource
    private PdsFrontCategoryClient pdsFrontCategoryClient;
    @Resource
    private CategoryAuditTaskTimeService categoryAuditTaskTimeService;
    @Resource
    private CombineManager combineManager;

    @Override
    public String exec(Task task, TaskParams param) throws Exception {
        log.info("开始执行分类管控定时展示分类任务,任务参数:{},任务信息:{}", JsonTools.defaultMapper().toJson(param), JsonTools.defaultMapper().toJson(task));
        String taskId = param.getParam("TIME_TASK_ID");
        String operator = param.getParam("OPERATOR");
        BusinessAssert.isTextNotBlank(taskId, ModuleTypeEnum.BASIC, "分类管控定时展示没有对应任务数据id");
        Optional<CategoryAuditTaskTimeEntity> taskTime = categoryAuditTaskTimeService.lambdaQuery()
                .eq(CategoryAuditTaskTimeEntity::getId, Long.valueOf(taskId))
                .eq(CategoryAuditTaskTimeEntity::getTaskState, 1)
                .eq(CategoryAuditTaskTimeEntity::getIsDel, 0).oneOpt();
        BusinessAssert.isTrue(taskTime.isPresent(), ModuleTypeEnum.BASIC, "分类管控定时展示没有对应任务数据");
        CategoryAuditTaskTimeEntity categoryAuditTaskTimeEntity = taskTime.get();
//        BatchChangeCategoryControlStatueReq batchChangeCategoryControlStatueReq = new BatchChangeCategoryControlStatueReq();
//        batchChangeCategoryControlStatueReq.setCategoryIdList(Collections.singletonList(categoryAuditTaskTimeEntity.getCategoryId()));
//        batchChangeCategoryControlStatueReq.setIsEnabled((byte) 1);
//        batchChangeCategoryControlStatueReq.setOperator(operator);
//        log.info("开始执行分类管控定时展示分类任务,更改状态req:{}", JsonTools.defaultMapper().toJson(batchChangeCategoryControlStatueReq));
//        pdsFrontCategoryClient.batchChangeStatus(batchChangeCategoryControlStatueReq);
        BatchChangeCategoryControlShowReq batchChangeCategoryControlShowReq = new BatchChangeCategoryControlShowReq();
        batchChangeCategoryControlShowReq.setCategoryIdList(Collections.singletonList(categoryAuditTaskTimeEntity.getCategoryId()));
        batchChangeCategoryControlShowReq.setIsShow((byte) 1);
        batchChangeCategoryControlShowReq.setShowChangeMode(0);
        batchChangeCategoryControlShowReq.setOperator(operator);
        log.info("开始执行分类管控定时展示分类任务,更改展示状态req:{}", JsonTools.defaultMapper().toJson(batchChangeCategoryControlShowReq));
        Response response = pdsFrontCategoryClient.batchChangeShow(batchChangeCategoryControlShowReq);
        if (!response.isSuccessResponse()) {
            categoryAuditTaskTimeService.lambdaUpdate().set(CategoryAuditTaskTimeEntity::getTaskState, 3).eq(CategoryAuditTaskTimeEntity::getId, Long.valueOf(taskId)).update();
            log.warn("分类展示时间到了，执行失败了，分类id:{}，执行失败原因:{}",
                    categoryAuditTaskTimeEntity.getCategoryId(),
                    JsonTools.defaultMapper().toJson(response),
                    LtLogFormat.builder().logTag("category.audit.detail.status.change.error").build());
            return "执行完成";
        }
        categoryAuditTaskTimeService.lambdaUpdate().set(CategoryAuditTaskTimeEntity::getTaskState, 2).eq(CategoryAuditTaskTimeEntity::getId, Long.valueOf(taskId)).update();
        //记录日志
        categoryAuditTaskLogService.saveTaskShowTimeSuccessLog(categoryAuditTaskTimeEntity.getId(), Collections.singletonList(categoryAuditTaskTimeEntity.getCategoryKey()), operator);
        //调用pds保存成功 发到企微群
        CategoryAuditTaskWechatDto categoryAuditTaskWechatDto = new CategoryAuditTaskWechatDto();
        categoryAuditTaskWechatDto.setAuditTaskId(categoryAuditTaskTimeEntity.getCategoryAuditTaskId());
        categoryAuditTaskWechatDto.setCategoryId(categoryAuditTaskTimeEntity.getCategoryId());
        categoryAuditTaskWechatDto.setTime(DateFormatTools.formatDate(DateFormatTools.PATTERN_DEFAULT_ON_SECOND, new Date()));
        combineManager.combineModel(categoryAuditTaskWechatDto, false);
        log.warn("分类展示时间到了，分类备注名:{}，分类任务标签:{}，分类展示时间:{}",
                categoryAuditTaskWechatDto.getCategoryName(),
                categoryAuditTaskWechatDto.getCategoryTag(),
                categoryAuditTaskWechatDto.getTime(),
                LtLogFormat.builder().logTag("category.audit.detail.status.change").build());
        return "执行完成";
    }
}
