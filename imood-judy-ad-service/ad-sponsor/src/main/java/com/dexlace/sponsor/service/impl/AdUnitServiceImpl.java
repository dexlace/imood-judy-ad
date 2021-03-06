package com.dexlace.sponsor.service.impl;


import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.constant.Constants;
import com.dexlace.sponsor.dao.AdPlanRepository;
import com.dexlace.sponsor.dao.AdUnitRepository;
import com.dexlace.sponsor.dao.CreativeRepository;
import com.dexlace.sponsor.dao.condition.AdUnitDistrictRepository;
import com.dexlace.sponsor.dao.condition.AdUnitItRepository;
import com.dexlace.sponsor.dao.condition.AdUnitKeywordRepository;
import com.dexlace.sponsor.dao.condition.CreativeUnitRepository;
import com.dexlace.sponsor.entity.AdPlan;
import com.dexlace.sponsor.entity.AdUnit;
import com.dexlace.sponsor.entity.condition.AdUnitDistrict;
import com.dexlace.sponsor.entity.condition.AdUnitInterest;
import com.dexlace.sponsor.entity.condition.AdUnitKeyword;
import com.dexlace.sponsor.entity.condition.CreativeUnit;
import com.dexlace.sponsor.service.IAdUnitService;
import com.dexlace.sponsor.vo.unit.AdUnitRequest;
import com.dexlace.sponsor.vo.unit.AdUnitResponse;
import com.dexlace.sponsor.vo.unit.condition.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdUnitServiceImpl implements IAdUnitService {

    private final AdPlanRepository planRepository;
    private final AdUnitRepository unitRepository;

    private final AdUnitKeywordRepository unitKeywordRepository;
    private final AdUnitItRepository unitItRepository;
    private final AdUnitDistrictRepository unitDistrictRepository;

    private final CreativeRepository creativeRepository;
    private final CreativeUnitRepository creativeUnitRepository;

    @Autowired
    public AdUnitServiceImpl(AdPlanRepository planRepository,
                             AdUnitRepository unitRepository,
                             AdUnitKeywordRepository unitKeywordRepository,
                             AdUnitItRepository unitItRepository,
                             AdUnitDistrictRepository unitDistrictRepository, CreativeRepository creativeRepository, CreativeUnitRepository creativeUnitRepository) {
        this.planRepository = planRepository;
        this.unitRepository = unitRepository;
        this.unitKeywordRepository = unitKeywordRepository;
        this.unitItRepository = unitItRepository;
        this.unitDistrictRepository = unitDistrictRepository;
        this.creativeRepository = creativeRepository;
        this.creativeUnitRepository = creativeUnitRepository;
    }

    @Override
    public AdUnitResponse createUnit(AdUnitRequest request)
            throws AdException {

        if (!request.createValidate()) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        Optional<AdPlan> adPlan =
                planRepository.findById(request.getPlanId());
        if (!adPlan.isPresent()) {
            throw new AdException(Constants.ErrorMsg.CAN_NOT_FIND_RECORD);
        }

        AdUnit oldAdUnit = unitRepository.findByPlanIdAndUnitName(
                request.getPlanId(), request.getUnitName()
        );
        if (oldAdUnit != null) {
            throw new AdException(Constants.ErrorMsg.SAME_NAME_UNIT_ERROR);
        }

        AdUnit newAdUnit = unitRepository.save(
                new AdUnit(request.getPlanId(), request.getUnitName(),
                        request.getPositionType(), request.getBudget())
        );

        return new AdUnitResponse(newAdUnit.getId(),
                newAdUnit.getUnitName());
    }

    /**
     * ??????????????????????????????  ???????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????
     * @param request ????????????????????????????????????????????????????????????id???keyword???list????????????????????????UnitKeyword???
     *                ??????????????????vo?????????
     * @return ???????????????id
     * @throws AdException
     */
    @Override
    public AdUnitKeywordResponse createUnitKeyword(
            AdUnitKeywordRequest request) throws AdException {

        // stream  java8?????????
        // ???????????????????????????id  ?????????????????????????????????????????????????????????????????????id????????????????????????
        List<Long> unitIds = request.getUnitKeywords().stream()
                .map(AdUnitKeywordRequest.UnitKeyword::getUnitId)  // map???????????????
                .collect(Collectors.toList()); // collect???????????????
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        // ??? ?????????????????????????????????ids
        // ?????????????????????list ?????????????????????????????????id list???????????????????????????????????????????????????
        List<Long> ids = Collections.emptyList();

        List<AdUnitKeyword> unitKeywords = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.getUnitKeywords())) {

            // forEach????????????list?????????????????????forEach????????????????????????????????????????????????
            // AdUnitKeyword???list,??????????????????id????????????keyword?????????
            request.getUnitKeywords().forEach(i -> unitKeywords.add(
                    new AdUnitKeyword(i.getUnitId(), i.getKeyword())
            ));
            // ???????????????????????????????????????????????????????????????????????????????????????????????????id?????????
            ids = unitKeywordRepository.saveAll(unitKeywords).stream()
                    .map(AdUnitKeyword::getId)
                    .collect(Collectors.toList());
        }

        return new AdUnitKeywordResponse(ids);
    }


    /**
     * ???????????????????????????  ????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????
     * @param request ?????????????????????????????????????????????????????????id???interest???list????????????????????????UnitInterest???
     *                ??????????????????vo?????????
     * @return ???????????????id
     * @throws AdException
     */
    @Override
    public AdUnitInterestResponse createUnitInterest(
            AdUnitInterestRequest request) throws AdException {


        // ???????????????????????????????????????????????????id
        List<Long> unitIds = request.getUnitInterests().stream()
                .map(AdUnitInterestRequest.UnitInterest::getUnitId)
                .collect(Collectors.toList());
        // ???????????? ???????????????????????????????????????id
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        // ???????????????????????????????????????????????????VO???????????????????????????????????????
        List<AdUnitInterest> unitIts = new ArrayList<>();
        request.getUnitInterests().forEach(i -> unitIts.add(
                new AdUnitInterest(i.getUnitId(), i.getItTag())
        ));
        // ????????????????????????????????????????????????????????????????????????id?????????????????????vo???
        List<Long> ids = unitItRepository.saveAll(unitIts).stream()
                .map(AdUnitInterest::getId)
                .collect(Collectors.toList());

        return new AdUnitInterestResponse(ids);
    }

    /**
     * ????????????
     * @param request
     * @return
     * @throws AdException
     */
    @Override
    public AdUnitDistrictResponse createUnitDistrict(
            AdUnitDistrictRequest request) throws AdException {

        // ?????????????????????????????????????????????????????????id
        List<Long> unitIds = request.getUnitDistricts().stream()
                .map(AdUnitDistrictRequest.UnitDistrict::getUnitId)
                .collect(Collectors.toList());
        // ???????????? ??????????????????????????????
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }


        // ???????????????????????????????????????VO???????????????????????????entity???
        List<AdUnitDistrict> unitDistricts = new ArrayList<>();
        request.getUnitDistricts().forEach(d -> unitDistricts.add(
                new AdUnitDistrict(d.getUnitId(), d.getProvince(),
                        d.getCity())
        ));

        // ??????????????????????????????  ???????????????id????????????????????????vo???
        List<Long> ids = unitDistrictRepository.saveAll(unitDistricts)
                .stream().map(AdUnitDistrict::getId)
                .collect(Collectors.toList());

        return new AdUnitDistrictResponse(ids);
    }

    /**
     * ??????
     * @param request
     * @return
     * @throws AdException
     */
    @Override
    public CreativeUnitResponse createCreativeUnit(
            CreativeUnitRequest request) throws AdException {

        List<Long> unitIds = request.getUnitItems().stream()
                .map(CreativeUnitRequest.CreativeUnitItem::getUnitId)
                .collect(Collectors.toList());
        List<Long> creativeIds = request.getUnitItems().stream()
                .map(CreativeUnitRequest.CreativeUnitItem::getCreativeId)
                .collect(Collectors.toList());

        if (!(isRelatedUnitExist(unitIds) && isRelatedCreativeExist(creativeIds))) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        List<CreativeUnit> creativeUnits = new ArrayList<>();
        request.getUnitItems().forEach(i -> creativeUnits.add(
                new CreativeUnit(i.getCreativeId(), i.getUnitId())
        ));

        List<Long> ids = creativeUnitRepository.saveAll(creativeUnits)
                .stream()
                .map(CreativeUnit::getId)
                .collect(Collectors.toList());

        return new CreativeUnitResponse(ids);
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * @param unitIds
     * @return
     */
    private boolean isRelatedUnitExist(List<Long> unitIds) {

        if (CollectionUtils.isEmpty(unitIds)) {
            return false;
        }

        // ???????????????????????????????????????????????????set????????????
        return unitRepository.findAllById(unitIds).size() ==
                new HashSet<>(unitIds).size();
    }

    /**
     * ??????????????????????????????????????????
     * @param creativeIds
     * @return
     */
    private boolean isRelatedCreativeExist(List<Long> creativeIds) {

        if (CollectionUtils.isEmpty(creativeIds)) {
            return false;
        }

        return creativeRepository.findAllById(creativeIds).size() ==
                new HashSet<>(creativeIds).size();
    }
}
