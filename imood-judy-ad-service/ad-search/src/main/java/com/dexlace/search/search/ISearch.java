package com.dexlace.search.search;


import com.dexlace.search.search.vo.SearchRequest;
import com.dexlace.search.search.vo.SearchResponse;

public interface ISearch {

    SearchResponse fetchAds(SearchRequest request);
}
