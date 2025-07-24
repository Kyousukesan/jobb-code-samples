package cn.dotfashion.soa.module.service.category.audit.task.impl;

import cn.dotfashion.soa.module.dto.category.audit.task.CategoryTreeVersionDto;
import cn.dotfashion.soa.module.entity.CategoryAuditTaskTimeEntity;
import cn.dotfashion.soa.module.mapper.CategoryAuditTaskTimeMapper;
import cn.dotfashion.soa.module.service.category.audit.task.CategoryAuditTaskTimeService;
import cn.dotfashion.soa.module.util.EntityOperateUtils;
import cn.dotfashion.soa.module.vo.categorytask.request.SetUpCategoryShowTimeReq;
import cn.dotfashion.soa.module.vo.categorytask.response.SetUpCategoryShowTimeResp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Category Submission Task启用任务 服务实现类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Service
public class CategoryAuditTaskTimeServiceImpl extends ServiceImpl<CategoryAuditTaskTimeMapper, CategoryAuditTaskTimeEntity> implements CategoryAuditTaskTimeService {

    @Override
    public void startCategoryAuditTaskTime(Long taskId, List<Long> categoryIdList) {
        if (taskId == null || categoryIdList == null || categoryIdList.isEmpty()) {
            return;
        }
        List<CategoryAuditTaskTimeEntity> list = lambdaQuery().eq(CategoryAuditTaskTimeEntity::getCategoryAuditTaskId, taskId)
                .eq(CategoryAuditTaskTimeEntity::getIsDel, false).list();


    }

    @Override
    public void createCategoryAuditTaskTime(Long taskId, List<CategoryTreeVersionDto.CategoryNode> categoryNodeList, Date showTime) {
        if (taskId == null || categoryNodeList == null || categoryNodeList.isEmpty()) {
            return;
        }
        List<CategoryAuditTaskTimeEntity> timeEntityList = categoryNodeList.stream().map(node -> {
            CategoryAuditTaskTimeEntity entity = new CategoryAuditTaskTimeEntity();
            entity.setCategoryAuditTaskId(taskId);
            entity.setCategoryKey(node.getKey());
            entity.setCategoryId(node.getId());
            entity.setEnableTime(showTime);
            entity.setTaskState(1);
            return entity;
        }).collect(Collectors.toList());

        List<CategoryAuditTaskTimeEntity> allList = lambdaQuery().eq(CategoryAuditTaskTimeEntity::getCategoryAuditTaskId, taskId).list();

        EntityOperateUtils.saveOrReplaceList(allList, timeEntityList, this, CategoryAuditTaskTimeEntity::getCategoryKey);
    }

    @Override
    public List<SetUpCategoryShowTimeResp> getSetUpCategoryShowTime(SetUpCategoryShowTimeReq req) {
        return lambdaQuery().eq(CategoryAuditTaskTimeEntity::getCategoryAuditTaskId, req.getTaskId())
                .eq(CategoryAuditTaskTimeEntity::getIsDel, false).list()
                .stream().map(v -> {
                    SetUpCategoryShowTimeResp resp = new SetUpCategoryShowTimeResp();
                    resp.setCategoryKey(v.getCategoryKey());
                    resp.setShowTime(v.getEnableTime());
                    resp.setTaskState(v.getTaskState());
                    return resp;
                }).collect(Collectors.toList());
    }
}
