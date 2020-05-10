package com.xxxzz.gmall;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 返回结果实体类
 */
@Data
public class SkuLsResult implements Serializable {

    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;
}
