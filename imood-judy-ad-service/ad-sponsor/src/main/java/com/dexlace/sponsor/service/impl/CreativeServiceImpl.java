package com.dexlace.sponsor.service.impl;


import com.dexlace.sponsor.dao.CreativeRepository;
import com.dexlace.sponsor.entity.Creative;
import com.dexlace.sponsor.service.ICreativeService;
import com.dexlace.sponsor.vo.creative.CreativeRequest;
import com.dexlace.sponsor.vo.creative.CreativeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreativeServiceImpl implements ICreativeService {

    private final CreativeRepository creativeRepository;

    @Autowired
    public CreativeServiceImpl(CreativeRepository creativeRepository) {
        this.creativeRepository = creativeRepository;
    }

    @Override
    public CreativeResponse createCreative(CreativeRequest request) {

        Creative creative = creativeRepository.save(
                request.convertToEntity()
        );

        return new CreativeResponse(creative.getId(), creative.getName());
    }
}
