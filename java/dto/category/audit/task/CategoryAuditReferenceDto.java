package cn.dotfashion.soa.module.dto.category.audit.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryAuditReferenceDto {

    //分类标识
    private String categoryKey;

    //审批意见列表
    private List<ReferenceInfo> infoList = new ArrayList<>();

    @Data
    public static class ReferenceInfo {

        //审批意见
        private String reference;

        //发表人
        private String operator;
    }

}
