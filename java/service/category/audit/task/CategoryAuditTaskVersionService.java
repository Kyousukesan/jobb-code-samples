package cn.dotfashion.soa.pim.service.category.audit.task;

import cn.dotfashion.soa.pim.client.vo.resp.BatchSaveCategoryControlDetailResp;
import cn.dotfashion.soa.pim.dto.category.audit.task.CategoryDiffChangeDto;
import cn.dotfashion.soa.pim.dto.category.audit.task.CategoryTreeVersionDto;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskEntity;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskVersionEntity;
import cn.dotfashion.soa.pim.enums.category.audit.CategoryAuditTaskStepEnum;
import cn.dotfashion.soa.pim.enums.category.audit.ChangeTagEnum;
import cn.dotfashion.soa.pim.vo.categorytask.request.CategoryLevelTaskSubReq;
import cn.dotfashion.soa.pim.vo.categorytask.request.SubmitCategoryInfoDataReq;
import cn.dotfashion.soa.pim.vo.categorytask.response.CategoryLevelTaskGetResp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 分类提报任务版本数据表 服务类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
public interface CategoryAuditTaskVersionService extends IService<CategoryAuditTaskVersionEntity> {


    /**
     * 根据任务id获取任务版本
     * @param taskId
     * @return
     */
    CategoryAuditTaskVersionEntity getByTaskId(Long taskId);

    /**
     * 获取最新版本号
     * @param taskId
     * @return
     */
    Integer latestVersion(Long taskId);

    /**
     * 获取版本列表
     * @param taskId
     * @return
     */
    List<Integer> getVersionList(Long taskId);


    /**
     * 获取版本树
     * @param taskId
     * @param version
     * @return
     */
    CategoryTreeVersionDto getCategoryTreeVersion(Long taskId, Integer version);

    /**
     * 获取当前生产使用的版本树
     * @param taskId
     * @return
     */
    CategoryTreeVersionDto getNowCategoryTreeVersion(Long taskId);

    /**
     * 转换成树形结构对象类型
     * @param categoryNode
     * @return
     */
    CategoryLevelTaskGetResp.CategoryNodeInfo convertToCategoryNodeInfo(CategoryTreeVersionDto.CategoryNode categoryNode, List<Object> combineList);


    /**
     * 创建版本
     * @param taskId
     * @param applicantName
     * @return
     */
    CategoryAuditTaskVersionEntity createVersion(Long taskId, String applicantName);

    /**
     * 根据id获取版本
     * @param id
     * @return
     */
    CategoryAuditTaskVersionEntity getById(Long id);


    CategoryTreeVersionDto.CategoryNode convertToCategoryNodeReq(CategoryLevelTaskSubReq.CategoryNodeInfo categoryNodeInfo, String pKey, Map<String, CategoryTreeVersionDto.CategoryNode> oldNodeMap);

    /**
     * 查询是否有正在处理的版本
     * @param taskId
     * @return
     */
    CategoryAuditTaskVersionEntity hasProcessingVersion(Long taskId);

    /**
     * 更新版本
     * @param dto
     */
    void updateLevelEdit(CategoryTreeVersionDto dto, CategoryAuditTaskVersionEntity entity);

    /**
     * 根据任务和版本获取版本数据
     * @param taskId
     * @param version
     * @return
     */
    CategoryAuditTaskVersionEntity getVersionByTaskIdAndVersion(Long taskId, Integer version);

    /**
     * 转换变化数据实体
     * @param versionEntity
     * @return
     */
    List<CategoryTreeVersionDto.ChangeData> convertChangeDataByEntity(CategoryAuditTaskVersionEntity versionEntity);

    /**
     * 转换层级树数据实体
     * @param versionEntity
     * @return
     */
    CategoryTreeVersionDto.CategoryNode convertTreeDtoByEntity(CategoryAuditTaskVersionEntity versionEntity);


    /**
     * 版本数据确认
     * @param taskId
     * @param version
     */
    void verifyVersion(Long taskId, Integer version);

    /**
     * 获取变化标签
     * @param taskId
     * @return
     */
    Map<Long, List<ChangeTagEnum>> getChangeTagMapByTaskId(List<Long> taskId);

    /**
     * 对比变化生成保存数据
     * @param categoryAuditTask
     * @return
     */
    List<CategoryDiffChangeDto> handleDiffChangeDto(CategoryAuditTaskEntity categoryAuditTask);

    /**
     * 转换成树形结构为map
     * @param node
     * @return
     */
    void convertMapByNodeDto(CategoryTreeVersionDto.CategoryNode node, Map<String, CategoryTreeVersionDto.CategoryNode> map);

    /**
     * 删除版本
     * @param versionId
     */
    void deleteById(Long versionId);

    /**
     * 落表后更新审批版本的新分类id
     * @param taskEntity
     * @param detailRespList
     */
    void updateNewCategoryId(CategoryAuditTaskEntity taskEntity, List<BatchSaveCategoryControlDetailResp> detailRespList);


    /**
     * 备注名转换出中文名
     * @param changeDataList
     * @param stepEnum
     */
    void fillRemarkNameToCnName(List<CategoryTreeVersionDto.ChangeData> changeDataList, Map<String, CategoryTreeVersionDto.CategoryNode> newNodeMap);
}
