package com.dexlace.search.search.impl;


import com.alibaba.fastjson.JSON;
import com.dexlace.search.index.CommonStatus;
import com.dexlace.search.index.DataTable;
import com.dexlace.search.index.adunit.AdUnitIndex;
import com.dexlace.search.index.adunit.AdUnitObject;
import com.dexlace.search.index.creative.CreativeIndex;
import com.dexlace.search.index.creative.CreativeObject;
import com.dexlace.search.index.creativeunit.CreativeUnitIndex;
import com.dexlace.search.index.district.UnitDistrictIndex;
import com.dexlace.search.index.interest.UnitItIndex;
import com.dexlace.search.index.keyword.UnitKeywordIndex;
import com.dexlace.search.search.ISearch;
import com.dexlace.search.search.vo.SearchRequest;
import com.dexlace.search.search.vo.SearchResponse;
import com.dexlace.search.search.vo.feature.DistrictFeature;
import com.dexlace.search.search.vo.feature.FeatureRelation;
import com.dexlace.search.search.vo.feature.InterestFeature;
import com.dexlace.search.search.vo.feature.KeywordFeature;
import com.dexlace.search.search.vo.media.AdSlot;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SearchImpl implements ISearch {

    public SearchResponse fallback(SearchRequest request, Throwable e) {
        return null;
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallback")
    public SearchResponse fetchAds(SearchRequest request) {

        // 请求的广告位信息
        List<AdSlot> adSlots = request.getRequestInfo().getAdSlots();

        // 三个 Feature
        KeywordFeature keywordFeature =
                request.getFeatureInfo().getKeywordFeature();
        DistrictFeature districtFeature =
                request.getFeatureInfo().getDistrictFeature();
        InterestFeature itFeature =
                request.getFeatureInfo().getItFeature();

        FeatureRelation relation = request.getFeatureInfo().getRelation();

        // 构造响应对象
        SearchResponse response = new SearchResponse();
        // 广告位编码：对应的广告创意
        Map<String, List<SearchResponse.Creative>> adSlot2Ads =
                response.getAdSlot2Ads();

        for (AdSlot adSlot : adSlots) {

            // 得到目标的推广单元的set集合
            Set<Long> targetUnitIdSet;

            // 一、拿到AdUnitIndex的索引类，根据流量类型（开屏、贴片之类的）获取初始 AdUnit
            Set<Long> adUnitIdSet = DataTable.of(
                    AdUnitIndex.class
            ).match(adSlot.getPositionType());



            // 二、匹配，根据关键词、地域、兴趣进行匹配
            if (relation == FeatureRelation.AND) {
                // 三项都过滤 and关系类型过滤
                filterKeywordFeature(adUnitIdSet, keywordFeature);
                filterDistrictFeature(adUnitIdSet, districtFeature);
                filterItTagFeature(adUnitIdSet, itFeature);
                // 得到目标推广单元的集合
                targetUnitIdSet = adUnitIdSet;

            } else {
                // or关系类型过滤
                targetUnitIdSet = getORRelationUnitIds(
                        adUnitIdSet,
                        keywordFeature,
                        districtFeature,
                        itFeature
                );
            }
            // 由推广单元的id得到推广单元的索引对象
            List<AdUnitObject> unitObjects =
                    DataTable.of(AdUnitIndex.class).fetch(targetUnitIdSet);

            // 四、过滤推广单元索引对象的状态：包括推广计划的状态、包括推广单元的状态
            filterAdUnitAndPlanStatus(unitObjects, CommonStatus.VALID);




            // 五：创意对象获取
            // 匹配相应的创意，得到创意id
            List<Long> adIds = DataTable.of(CreativeUnitIndex.class)
                    .selectAds(unitObjects);
            // 根据创意id获取创意对象
            List<CreativeObject> creatives = DataTable.of(CreativeIndex.class)
                    .fetch(adIds);

            // 通过 AdSlot 实现对 CreativeObject 的过滤
            filterCreativeByAdSlot(
                    creatives,
                    adSlot.getWidth(),
                    adSlot.getHeight(),
                    adSlot.getType()
            );

            // 六、得到最终的广告信息
            adSlot2Ads.put(
                    adSlot.getAdSlotCode(), buildCreativeResponse(creatives)
            );
        }

        log.info("fetchAds: {}-{}",
                JSON.toJSONString(request),
                JSON.toJSONString(response));

        return response;
    }

    /**
     * or关系类型的过滤
     * @param adUnitIdSet 推广单元id的set
     * @param keywordFeature 关键词特征
     * @param districtFeature 地域特征
     * @param itFeature 兴趣特征
     * @return 返回or关系类型过滤结果
     */
    private Set<Long> getORRelationUnitIds(Set<Long> adUnitIdSet,
                                           KeywordFeature keywordFeature,
                                           DistrictFeature districtFeature,
                                           InterestFeature itFeature) {

        if (CollectionUtils.isEmpty(adUnitIdSet)) {
            return Collections.emptySet();
        }

        /**
         * 先拿到推广单元id 集合副本
         */
        Set<Long> keywordUnitIdSet = new HashSet<>(adUnitIdSet);
        Set<Long> districtUnitIdSet = new HashSet<>(adUnitIdSet);
        Set<Long> itUnitIdSet = new HashSet<>(adUnitIdSet);

        /**
         * 用副本过滤
         */
        filterKeywordFeature(keywordUnitIdSet, keywordFeature);
        filterDistrictFeature(districtUnitIdSet, districtFeature);
        filterItTagFeature(itUnitIdSet, itFeature);

        // 返回三者过滤后的并集
        return new HashSet<>(
                CollectionUtils.union(
                        CollectionUtils.union(keywordUnitIdSet, districtUnitIdSet),
                        itUnitIdSet
                )
        );
    }


    /**
     * 对匹配了流量信息的推广单元进行关键词过滤
     * @param adUnitIds 推广单元的id
     * @param keywordFeature 关键词
     */
    private void filterKeywordFeature(
            Collection<Long> adUnitIds, KeywordFeature keywordFeature) {

        if (CollectionUtils.isEmpty(adUnitIds)) {
            return;
        }

        // 关键词非空
        if (CollectionUtils.isNotEmpty(keywordFeature.getKeywords())) {

            CollectionUtils.filter(
                    adUnitIds, //过滤的对象
                    adUnitId ->  // 对其中每一项进行过滤
                            DataTable.of(UnitKeywordIndex.class)
                                    .match(adUnitId,
                                            keywordFeature.getKeywords())
            );
        }
    }

    /**
     * 对匹配了流量信息的推广单元进行地域过滤
     * @param adUnitIds 推广单元id
     * @param districtFeature 地域限制特征
     */
    private void filterDistrictFeature(
            Collection<Long> adUnitIds, DistrictFeature districtFeature
    ) {
        /**
         * 判断是否为空
         */
        if (CollectionUtils.isEmpty(adUnitIds)) {
            return;
        }

        /**
         * 地域限制特征是否为空
         */
        if (CollectionUtils.isNotEmpty(districtFeature.getDistricts())) {

            CollectionUtils.filter(
                    adUnitIds, // 过滤的对象
                    adUnitId -> // 过滤的每一项
                            DataTable.of(UnitDistrictIndex.class)
                                    .match(adUnitId,
                                            districtFeature.getDistricts())
            );
        }
    }

    /**
     * 对推广单元的兴趣限制进行过滤
     * 同样的逻辑
     * @param adUnitIds 推广单元id
     * @param itFeature 兴趣限制的特征
     */
    private void filterItTagFeature(Collection<Long> adUnitIds,
                                    InterestFeature itFeature) {

        if (CollectionUtils.isEmpty(adUnitIds)) {
            return;
        }

        if (CollectionUtils.isNotEmpty(itFeature.getIts())) {

            CollectionUtils.filter(
                    adUnitIds,
                    adUnitId ->
                            DataTable.of(UnitItIndex.class)
                                    .match(adUnitId,
                                            itFeature.getIts())
            );
        }
    }

    /**
     * 推广单元索引对象的状态匹配
     * @param unitObjects 推广单元索引对象
     * @param status 状态
     */
    private void filterAdUnitAndPlanStatus(List<AdUnitObject> unitObjects,
                                           CommonStatus status) {

        /**
         * 判断是否为空
         */
        if (CollectionUtils.isEmpty(unitObjects)) {
            return;
        }

        // 根据推广单元的状态和推广计划的状态进行过滤
        CollectionUtils.filter(
                unitObjects,
                object -> object.getUnitStatus().equals(status.getStatus())
                && object.getAdPlanObject().getPlanStatus().equals(status.getStatus())
        );
    }

    /**
     * 创意的过滤
     * @param creatives 创意
     * @param width 宽
     * @param height 高
     * @param type 类型
     */
    private void filterCreativeByAdSlot(List<CreativeObject> creatives,
                                        Integer width,
                                        Integer height,
                                        List<Integer> type) {

        if (CollectionUtils.isEmpty(creatives)) {
            return;
        }

        CollectionUtils.filter(
                creatives,
                creative ->
                        creative.getAuditStatus().equals(CommonStatus.VALID.getStatus())
                && creative.getWidth().equals(width)
                && creative.getHeight().equals(height)
                && type.contains(creative.getType())
        );
    }

    /**
     * 获取到多个，需要随机获取一个并转换成我们所需要展示的SearchResponse.Creative对象
     * @param creatives  List<CreativeObject> 对象
     * @return 随机的需要展示的SearchResponse.Creative
     */
    private List<SearchResponse.Creative> buildCreativeResponse(
            List<CreativeObject> creatives
    ) {

        if (CollectionUtils.isEmpty(creatives)) {
            return Collections.emptyList();
        }

        // 随机获取一个创意对象
        CreativeObject randomObject = creatives.get(
                Math.abs(new Random().nextInt()) % creatives.size()
        );

        return Collections.singletonList(   // 单例的list
                SearchResponse.convert(randomObject)  // 转换成给用户展示的对象
        );
    }
}
