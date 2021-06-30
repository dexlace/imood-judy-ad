package com.dexlace.search.search.vo.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 地理信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Geo {


    private Float latitude;
    private Float longitude;

    private String city;
    private String province;
}
