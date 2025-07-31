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
 * Category Submission Task主表
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_audit_task")
@ApiModel(value="CategoryAuditTaskEntity对象", description="Category Submission Task主表")
public class CategoryAuditTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "自生成唯一任务编号")
    private String taskSn;

    @ApiModelProperty(value = "来源系统 SPMP/GPC")
    private String sourceSystem;

    @ApiModelProperty(value = "OA 任务id")
    private Long requestId;

    @ApiModelProperty(value = "绑定的顶级分类ID")
    private Long topCategoryId;

    @ApiModelProperty(value = "状态 0-已接收待提报，1-已提报待审核，2-已审核已更新成功，3-已审核已驳回")
    private Integer taskState;

    @ApiModelProperty(value = "审批时间")
    private Date auditTime;

    @ApiModelProperty(value = "提报时间")
    private Date requestTime;

    @ApiModelProperty(value = "申请时间")
    private Date applicantTime;

    @ApiModelProperty(value = "启用时间")
    private Date enableTime;

    @ApiModelProperty(value = "提报人")
    private String requestName;

    @ApiModelProperty(value = "审批人")
    private String auditName;

    @ApiModelProperty(value = "申请人")
    private String applicantName;

    @ApiModelProperty(value = "提报原因说明")
    private String requestRemark;

    @ApiModelProperty(value = "审批备注说明")
    private String auditRemark;

    @ApiModelProperty(value = "申请说明")
    private String applicantRemark;

    @ApiModelProperty(value = "记录插入时间")
    private Date insertTime;

    @ApiModelProperty(value = "记录更新时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "是否删除(0:未删除,1:已删除)")
    private Integer isDel;

    @ApiModelProperty(value = "审批版本")
    private Integer auditVersion;
}
