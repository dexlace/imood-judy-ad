package com.dexlace.search.search.vo;


import com.dexlace.search.search.vo.feature.DistrictFeature;
import com.dexlace.search.search.vo.feature.FeatureRelation;
import com.dexlace.search.search.vo.feature.InterestFeature;
import com.dexlace.search.search.vo.feature.KeywordFeature;
import com.dexlace.search.search.vo.media.AdSlot;
import com.dexlace.search.search.vo.media.App;
import com.dexlace.search.search.vo.media.Device;
import com.dexlace.search.search.vo.media.Geo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    // 媒体方的请求标识
    private String mediaId;
    // 请求基本信息
    private RequestInfo requestInfo;
    // 匹配信息
    private FeatureInfo featureInfo;

    /**
     * 请求基本信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestInfo {

        /**
         * 请求id
         */
        private String requestId;
        /**
         * 广告位信息:
         广告位编码;
         流量类型（也就是开屏广告、贴片广告之类的类型）;
         广告位宽;
         广告位高;
         广告物料类型（图片，视频之类的）;
         最低出价
         */
        private List<AdSlot> adSlots;
        /**
         * 应用信息:
         应用编码;
         应用名称;
         应用包名;
         应用请求页面的名称;
         */
        private App app;
        /**
         * 地理信息:
         经度;
         纬度;
         城市;
         省份;
         */
        private Geo geo;
        /**
         * 设备信息:
         设备id;
         设备mac;
         设备ip;
         机型编码;
         分辨率尺寸;
         屏幕尺寸;
         设备序列号;
         */
        private Device device;
    }

    /**
     * 匹配信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureInfo {

        /**
         * 一系列的关键词list
         */
        private KeywordFeature keywordFeature;
        /**
         * 区域限制的list
         */
        private DistrictFeature districtFeature;
        /**
         * 兴趣限制的list
         */
        private InterestFeature itFeature;

        /**
         * 关系
         */
        private FeatureRelation relation = FeatureRelation.AND;
    }
}
