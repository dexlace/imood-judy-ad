package com.dexlace.sponsor.service;


import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.vo.unit.AdUnitRequest;
import com.dexlace.sponsor.vo.unit.AdUnitResponse;
import com.dexlace.sponsor.vo.unit.condition.*;

public interface IAdUnitService {

    AdUnitResponse createUnit(AdUnitRequest request) throws AdException;

    AdUnitKeywordResponse createUnitKeyword(AdUnitKeywordRequest request)
        throws AdException;

    AdUnitInterestResponse createUnitInterest(AdUnitInterestRequest request)
        throws AdException;

    AdUnitDistrictResponse createUnitDistrict(AdUnitDistrictRequest request)
        throws AdException;

    CreativeUnitResponse createCreativeUnit(CreativeUnitRequest request)
        throws AdException;
}
