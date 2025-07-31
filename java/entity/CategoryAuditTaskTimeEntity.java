package cn.company.soa.module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Category Submission Task启用任务
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_audit_task_time")
@ApiModel(value="CategoryAuditTaskTimeEntity对象", description="Category Submission Task启用任务")
public class CategoryAuditTaskTimeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务主表")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "分类唯一标识 父id-备注名")
    private String categoryKey;

    @ApiModelProperty(value = "启用的分类id")
    private Long categoryId;

    @ApiModelProperty(value = "0未启用1待执行2执行完毕")
    private Integer taskState;

    @ApiModelProperty(value = "启用时间")
    private Date enableTime;

    @ApiModelProperty(value = "记录插入时间")
    private Date insertTime;

    @ApiModelProperty(value = "记录更新时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "是否删除(0:未删除,1:已删除)")
    private Integer isDel;


}
