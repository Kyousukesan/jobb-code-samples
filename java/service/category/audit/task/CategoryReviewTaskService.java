package cn.dotfashion.soa.pim.service.category.audit.task;

import cn.dotfashion.soa.pim.entity.CategoryAuditTaskEntity;
import cn.dotfashion.soa.pim.enums.category.audit.TaskStateEnum;
import cn.dotfashion.soa.pim.vo.categorytask.response.TopCategoryInfoResp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 分类提报任务主表 服务类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
public interface CategoryAuditTaskService extends IService<CategoryAuditTaskEntity> {

    /**
     * 创建提报任务
     * @param applicantName
     * @param topCategoryId
     * @param sourceSystem
     * @return
     */
    CategoryAuditTaskEntity createAppCategoryAuditTask(String applicantName, Long topCategoryId, String sourceSystem);


    /**
     * 检查是否有锁
     * @param topCategoryId
     * @return
     */
    CategoryAuditTaskEntity checkTopCategoryLock(Long topCategoryId);


    CategoryAuditTaskEntity getTopCategoryNotLock(Long topCategoryId);

    /**
     * 根据id获取任务
     * @param id
     * @return
     */
    CategoryAuditTaskEntity getById(Long id);

    /**
     * 根据id获取任务
     * @param topCategoryId
     * @return
     */
    TopCategoryInfoResp getTopCategoryInfo(Long topCategoryId);

    /**
     * 检查任务是否可以编辑版本
     * @param taskEntity
     */
    void checkTaskToEditVersion(CategoryAuditTaskEntity taskEntity);


    /**
     * 获取分类信息
     * @param topCategoryId
     * @return
     */
    List<TopCategoryInfoResp> getTopCategoryInfoList(List<Long> topCategoryId);

    void updateTaskStatus(Long taskId, TaskStateEnum taskStateEnum);
}
