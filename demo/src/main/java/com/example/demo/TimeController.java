package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
public class TimeController {

    @GetMapping("/Apoint")
    public String getCurrentTime() {
        ZonedDateTime now = ZonedDateTime.now();
        int hour = now.getHour();
        int min = now.getMinute();
        switch (hour){
            case 11,12,13,14,15,16:
                if(min==55){
                    return "「巡回時刻は5分前です。次の巡回場所は○○です。」";//
                    }
                    if(min==0){
                    return "「巡回時刻になりました。巡回場所は○○です。」";
                }
                    else return "成功" ;
            default:
            return "対応時間外です";
        }
    }
}

