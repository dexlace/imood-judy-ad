package com.dexlace.search.search.vo;


import com.dexlace.search.index.creative.CreativeObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    /**
     * 广告位编码：创意
     */
    public Map<String, List<Creative>> adSlot2Ads = new HashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creative {

        /**
         * 广告id
         */
        private Long adId;
        /**
         * 广告url
         */
        private String adUrl;
        private Integer width;
        private Integer height;
        /**
         * 类型
         */
        private Integer type;
        /**
         * 子类型
         */
        private Integer materialType;

        // 展示监测 url
        private List<String> showMonitorUrl =
                Arrays.asList("judy.imood.com", "judy.imood.com.cn");
        // 点击监测 url
        private List<String> clickMonitorUrl =
                Arrays.asList("judy.imood.com", "judy.imooc.com.cn");
    }

    /**
     * 把索引对象转换为给媒体方的创意数据
     * @param object
     * @return
     */
    public static Creative convert(CreativeObject object) {

        Creative creative = new Creative();
        creative.setAdId(object.getAdId());
        creative.setAdUrl(object.getAdUrl());
        creative.setWidth(object.getWidth());
        creative.setHeight(object.getHeight());
        creative.setType(object.getType());
        creative.setMaterialType(object.getMaterialType());

        return creative;
    }
}
