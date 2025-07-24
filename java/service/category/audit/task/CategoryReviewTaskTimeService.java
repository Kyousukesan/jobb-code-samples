package cn.dotfashion.soa.module.service.category.audit.task;

import cn.dotfashion.soa.module.dto.category.audit.task.CategoryTreeVersionDto;
import cn.dotfashion.soa.module.entity.CategoryAuditTaskTimeEntity;
import cn.dotfashion.soa.module.vo.categorytask.request.SetUpCategoryShowTimeReq;
import cn.dotfashion.soa.module.vo.categorytask.response.SetUpCategoryShowTimeResp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Category Submission Task启用任务 服务类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
public interface CategoryAuditTaskTimeService extends IService<CategoryAuditTaskTimeEntity> {

    /**
     * 绑定分类任务启用任务
     * @param taskId
     * @param categoryIdList
     */
    void startCategoryAuditTaskTime(Long taskId, List<Long> categoryIdList);

    /**
     * 创建分类任务启用任务
     * @param taskId
     * @param categoryKeyList
     */
    void createCategoryAuditTaskTime(Long taskId, List<CategoryTreeVersionDto.CategoryNode> categoryKeyList, Date showTime);

    /**
     * 获取分类任务启用任务
     * @param req
     * @return
     */
    List<SetUpCategoryShowTimeResp> getSetUpCategoryShowTime(SetUpCategoryShowTimeReq req);
}
