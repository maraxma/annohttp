package org.mosin.annohttp.httpservice;

import org.mosin.annohttp.annotation.AnnoHttpService;
import org.mosin.annohttp.annotation.Query;
import org.mosin.annohttp.annotation.Request;

@AnnoHttpService(baseUrl = "http://localhost:8081/")
public interface TestClient {
    @Request(url = "/item")
    String getItemName(@Query("ItemNo") String itemNo);
}
