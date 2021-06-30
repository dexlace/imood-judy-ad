package com.dexlace.kafka.sender;


import com.dexlace.common.dto.definition.MySqlRowData;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/5/25
 */
public interface ISender {

    void sender(MySqlRowData rowData);
}

