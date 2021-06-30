package com.dexlace.sponsor.service;


import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.DumpApplication;
import com.dexlace.sponsor.vo.creative.CreativeRequest;
import com.dexlace.sponsor.vo.plan.AdPlanGetRequest;
import com.dexlace.sponsor.vo.unit.AdUnitRequest;
import com.dexlace.sponsor.vo.unit.condition.AdUnitDistrictRequest;
import com.dexlace.sponsor.vo.unit.condition.AdUnitInterestRequest;
import com.dexlace.sponsor.vo.unit.condition.AdUnitKeywordRequest;
import com.dexlace.sponsor.vo.unit.condition.CreativeUnitRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DumpApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AdUnitServiceTest {

    @Autowired
    private IAdUnitService unitService;

    @Test
    public void testCreateAdUnit() throws AdException {

        System.out.println(
                unitService.createUnit(
                        new AdUnitRequest(10L,
                                "第六个推广单元",
                                1, 190000000L))
        );

        System.out.println(
                unitService.createUnit(
                        new AdUnitRequest(10L,
                                "第四个推广单元",
                                0, 180000000L))
        );
        System.out.println(
                unitService.createUnit(
                        new AdUnitRequest(10L,
                                "第五个推广单元",
                                0, 55555500L))
        );
    }


    @Test
    public void testCreateAdUnitDistrict() throws AdException {

        System.out.println(
                unitService.createUnitDistrict(new AdUnitDistrictRequest(
                        Arrays.asList(new AdUnitDistrictRequest.UnitDistrict(10L, "江西省", "吉安市"),
                                new AdUnitDistrictRequest.UnitDistrict(12L, "江西省", "吉安市"),
                                new AdUnitDistrictRequest.UnitDistrict(13L, "江西省", "吉安市"),
                                new AdUnitDistrictRequest.UnitDistrict(15L, "江西省", "吉安市"),
                                new AdUnitDistrictRequest.UnitDistrict(10L, "广东省", "深圳市"),
                                new AdUnitDistrictRequest.UnitDistrict(12L, "广东省", "深圳市")
                        )
                ))
        );
    }

    @Test
    public void testCreateAdUnitInterest() throws AdException {

        System.out.println(
                unitService.createUnitInterest(
                        new AdUnitInterestRequest(
                                Arrays.asList(
                                        new AdUnitInterestRequest.UnitInterest(12L, "游泳"),
                                        new AdUnitInterestRequest.UnitInterest(13L, "游泳"),
                                        new AdUnitInterestRequest.UnitInterest(14L, "滑冰"),
                                        new AdUnitInterestRequest.UnitInterest(15L, "台球")
                                )
                        )
                )
        );
    }



    @Test
    public void testCreateAdUnitKeyword() throws AdException {

        System.out.println(
                unitService.createUnitKeyword(
                        new AdUnitKeywordRequest(
                                Arrays.asList(
                                        new AdUnitKeywordRequest.UnitKeyword(12L, "红旗"),
                                        new AdUnitKeywordRequest.UnitKeyword(10L, "保时捷"),
                                        new AdUnitKeywordRequest.UnitKeyword(10L, "丰田")
                                )
                        )
                )
        );
    }


    @Test
    public void testCreateAdCreativeUnit() throws AdException {

        System.out.println(
                unitService.createCreativeUnit(
                        new CreativeUnitRequest(
                                Arrays.asList(
                                        new CreativeUnitRequest.CreativeUnitItem(10L, 16L),
                                        new CreativeUnitRequest.CreativeUnitItem(10L, 12L),
                                        new CreativeUnitRequest.CreativeUnitItem(10L, 13L)

                                )
                        )
                )
        );
    }


}
