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
 * カテゴリ提出タスクメインテーブル
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_audit_task")
@ApiModel(value="CategoryAuditTaskEntity对象", description="カテゴリ提出タスクメインテーブル")
public class CategoryAuditTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主キー")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "自動生成ユニークタスク番号")
    private String taskSn;

    @ApiModelProperty(value = "ソースシステム SPMP/GPC")
    private String sourceSystem;

    @ApiModelProperty(value = "OA タスクID")
    private Long requestId;

    @ApiModelProperty(value = "バインドされたトップカテゴリID")
    private Long topCategoryId;

    @ApiModelProperty(value = "状態 0-受信済み提出待ち、1-提出済み審査待ち、2-審査済み更新成功、3-審査済み却下")
    private Integer taskState;

    @ApiModelProperty(value = "審査時間")
    private Date auditTime;

    @ApiModelProperty(value = "提出時間")
    private Date requestTime;

    @ApiModelProperty(value = "申請時間")
    private Date applicantTime;

    @ApiModelProperty(value = "有効化時間")
    private Date enableTime;

    @ApiModelProperty(value = "提出者")
    private String requestName;

    @ApiModelProperty(value = "審査者")
    private String auditName;

    @ApiModelProperty(value = "申請者")
    private String applicantName;

    @ApiModelProperty(value = "提出理由説明")
    private String requestRemark;

    @ApiModelProperty(value = "審査備考説明")
    private String auditRemark;

    @ApiModelProperty(value = "申請説明")
    private String applicantRemark;

    @ApiModelProperty(value = "レコード挿入時間")
    private Date insertTime;

    @ApiModelProperty(value = "レコード更新時間")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "削除フラグ(0:未削除,1:削除済み)")
    private Integer isDel;

    @ApiModelProperty(value = "審査バージョン")
    private Integer auditVersion;
}
