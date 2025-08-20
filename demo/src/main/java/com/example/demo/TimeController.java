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
            case 10,13:
                if(min==55){
                    return "「巡回時刻は5分前です。次の巡回場所は2階の食品テナントです。」";//
                    }
                    if(min==0){
                    return "「巡回時刻になりました。巡回場所は1階の食品テナントです。」";
                }
                    else return "" ;
            case 11,14:
                if(min==20){
                    return "「巡回時刻は5分前です。次の巡回場所は3階の駐車場です。」";//
                    }
                    if(min==0){
                    return "「巡回時刻になりました。巡回場所は2階の食品テナントです。」";
                }
                    else return "" ;
            case 12,15:
            if(min==20){
                    return "「巡回時刻は5分前です。次の巡回場所はすべての階層です。」";//
                    }
                    if(min==0){
                    return "「巡回時刻になりました。巡回場所は3階の食品テナントです。」";
                }
                    else return "" ;
            case 16:
                    if(min==0){
                    return "「巡回時刻になりました。巡回場所はすべての階層です。」";
                }
                    else return "" ;
            default:
            return "対応時間外です";
        }
    }

    @GetMapping("/recode")
    public String getrecode() {
        

        return new String();
    }

    
}

