package org.mosin.annohttp.httpservice;

import org.mosin.annohttp.annotation.Query;
import org.mosin.annohttp.annotation.Request;

public interface TestClientWithoutAnno {
    @Request(url = "http://baidu.com")
    String getItemName(@Query("ItemNo") String itemNo);
}
