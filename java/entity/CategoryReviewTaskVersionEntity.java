package cn.dotfashion.soa.pim.entity;

import cn.dotfashion.soa.pim.dto.category.audit.task.CategoryTreeVersionDto;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.ArrayList;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

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
@TableName("pim_category_audit_task_version")
@ApiModel(value="CategoryAuditTaskVersionEntity对象", description="分类提报任务版本数据表")
public class CategoryAuditTaskVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务主表")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "申请人")
    private String applicantName;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "变更数据")
    private String changeData;

    @ApiModelProperty(value = "版本数据")
    private String versionData;

    @ApiModelProperty(value = "记录插入时间")
    private Date insertTime;

    @ApiModelProperty(value = "记录更新时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "是否删除(0:未删除,1:已删除)")
    private Integer isDel;

    @ApiModelProperty(value = "当前步骤 初始0 层级调整1 信息调整确认2")
    private Integer step;

}
