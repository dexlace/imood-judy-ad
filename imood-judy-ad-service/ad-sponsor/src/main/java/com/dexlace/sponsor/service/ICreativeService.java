package com.dexlace.sponsor.service;


import com.dexlace.sponsor.vo.creative.CreativeRequest;
import com.dexlace.sponsor.vo.creative.CreativeResponse;

public interface ICreativeService {

    CreativeResponse createCreative(CreativeRequest request);
}
