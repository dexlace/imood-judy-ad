package com.dexlace.search.find;

import com.alibaba.fastjson.JSON;
import com.dexlace.search.SearchTestApplication;
import com.dexlace.search.search.ISearch;
import com.dexlace.search.search.vo.SearchRequest;
import com.dexlace.search.search.vo.feature.DistrictFeature;
import com.dexlace.search.search.vo.feature.FeatureRelation;
import com.dexlace.search.search.vo.feature.InterestFeature;
import com.dexlace.search.search.vo.feature.KeywordFeature;
import com.dexlace.search.search.vo.media.AdSlot;
import com.dexlace.search.search.vo.media.App;
import com.dexlace.search.search.vo.media.Device;
import com.dexlace.search.search.vo.media.Geo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/6/5
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SearchTestApplication.class},
webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Slf4j
public class SearchTest {

    @Autowired
    private ISearch search;


    @Test
    public void testFetchAds() {

        SearchRequest request=new SearchRequest();
        request.setMediaId("judy-ad");
        request.setRequestInfo(

                new SearchRequest.RequestInfo(
                        "ad-a", //请求的id
                        // 广告位id
                        Collections.singletonList(new AdSlot("aaa",1,
                                1080,720,
                                Arrays.asList(1,2),
                                1000)),
                        // app信息
                        buildExampleApp(),
                        // 地理信息
                        buildExampleGeo(),
                        // 设备信息
                        buildExampleDevice()
                )
        );

        request.setFeatureInfo(buildExampleFeatureInfo(
                Arrays.asList("宝马","大众"),
                Collections.singletonList(
                        new DistrictFeature.ProvinceAndCity(
                                "安徽省", "合肥市")),
                Arrays.asList("台球", "游泳"),
                FeatureRelation.OR

        ));

        log.info(JSON.toJSONString(request));
        log.info(JSON.toJSONString(search.fetchAds(request)));



    }


    private App buildExampleApp() {
        return new App("imood", "judy",
                "com.dexlace", "video");
    }

    private Geo buildExampleGeo() {
        return new Geo((float) 100.28, (float) 88.61,
                "北京市", "北京市");
    }

    private Device buildExampleDevice() {

        return new Device(
                "iphone",
                "0xxxxx",
                "127.0.0.1",
                "x",
                "1080 720",
                "1080 720",
                "123456789"

        );
    }


    private SearchRequest.FeatureInfo buildExampleFeatureInfo(
            List<String> keywords,
            List<DistrictFeature.ProvinceAndCity> provinceAndCities,
            List<String> its,
            FeatureRelation relation
    ) {
        return new SearchRequest.FeatureInfo(
                new KeywordFeature(keywords),
                new DistrictFeature(provinceAndCities),
                new InterestFeature(its),
                relation
        );
    }
}
