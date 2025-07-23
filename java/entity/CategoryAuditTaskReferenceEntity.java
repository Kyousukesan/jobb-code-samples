package cn.dotfashion.soa.pim.entity;

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
 * 分类提报任务版本数据表
 * </p>
 *
 * @author zhangdongdong
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pim_category_audit_task_reference")
@ApiModel(value="CategoryAuditTaskReferenceEntity对象", description="分类提报任务版本数据表")
public class CategoryAuditTaskReferenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务主表")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "关联审批参考数据")
    private String referenceData;

    @ApiModelProperty(value = "记录插入时间")
    private Date insertTime;

    @ApiModelProperty(value = "记录更新时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "是否删除(0:未删除,1:已删除)")
    private Integer isDel;


}
