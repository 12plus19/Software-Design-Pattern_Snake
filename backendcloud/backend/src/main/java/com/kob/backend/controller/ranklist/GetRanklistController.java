package com.kob.backend.controller.ranklist;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.ranklist.GetRanklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetRanklistController {
    @Autowired
    private GetRanklistService getRanklistService;
    @GetMapping("/ranklist/getlist/")
    public JSONObject getRanklist(@RequestParam Map<String, String> data){
        Integer pageNo = Integer.parseInt(data.get("page_no"));
        return getRanklistService.getRanklist(pageNo);
    }
}