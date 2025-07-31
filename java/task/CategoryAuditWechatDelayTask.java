package cn.company.soa.module.task;

import cn.company.soa.framework.util.DateFormatTools;
import cn.company.soa.framework.util.JsonTools;
import cn.company.soa.log.LtLogFormat;
import cn.company.soa.module.dto.CategoryAuditTaskWechatDto;
import cn.company.soa.module.entity.CategoryAuditTaskTimeEntity;
import cn.company.soa.module.service.category.audit.task.CategoryAuditTaskTimeService;
import cn.company.soa.task.embedded.core.SchedulableTask;
import cn.company.soa.task.embedded.core.Task;
import cn.company.soa.task.embedded.core.TaskParams;
import com.shein.common.combine.CombineManager;
import com.shein.common.enums.ModuleTypeEnum;
import com.shein.common.exception.BusinessAssert;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

@Component
public class CategoryAuditWechatDelayTask implements SchedulableTask {

    @Resource
    private CategoryAuditTaskTimeService categoryAuditTaskTimeService;
    @Resource
    private CombineManager combineManager;

    @Override
    public String exec(Task task, TaskParams param) throws Exception {
        log.info("开始执行分类展示时间提前1天提示,任务参数:{},任务信息:{}", JsonTools.defaultMapper().toJson(param), JsonTools.defaultMapper().toJson(task));
        String taskId = param.getParam("TIME_TASK_ID");
        BusinessAssert.isTextNotBlank(taskId, ModuleTypeEnum.BASIC, "分类管控定时展示没有对应任务数据id");
        Optional<CategoryAuditTaskTimeEntity> taskTime = categoryAuditTaskTimeService.lambdaQuery()
                .eq(CategoryAuditTaskTimeEntity::getId, Long.valueOf(taskId))
                .eq(CategoryAuditTaskTimeEntity::getTaskState, 1)
                .eq(CategoryAuditTaskTimeEntity::getIsDel, 0).oneOpt();
        BusinessAssert.isTrue(taskTime.isPresent(), ModuleTypeEnum.BASIC, "分类管控定时展示没有对应任务数据");
        CategoryAuditTaskTimeEntity categoryAuditTaskTimeEntity = taskTime.get();
        //调用pds保存成功 发到企微群
        CategoryAuditTaskWechatDto categoryAuditTaskWechatDto = new CategoryAuditTaskWechatDto();
        categoryAuditTaskWechatDto.setAuditTaskId(categoryAuditTaskTimeEntity.getCategoryAuditTaskId());
        categoryAuditTaskWechatDto.setCategoryId(categoryAuditTaskTimeEntity.getCategoryId());
        categoryAuditTaskWechatDto.setTime(DateFormatTools.formatDate(DateFormatTools.PATTERN_DEFAULT_ON_SECOND, new Date()));
        combineManager.combineModel(categoryAuditTaskWechatDto, false);
        log.warn("分类展示时间提前1天提示，分类备注名:{}，分类任务标签:{}，分类展示时间:{}",
                categoryAuditTaskWechatDto.getCategoryName(),
                categoryAuditTaskWechatDto.getCategoryTag(),
                categoryAuditTaskWechatDto.getTime(),
                LtLogFormat.builder().logTag("category.audit.detail.status.change").build());
        return "执行完成";
    }
}
