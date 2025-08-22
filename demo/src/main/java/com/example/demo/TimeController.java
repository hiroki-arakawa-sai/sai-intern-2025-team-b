package com.example.demo;

import java.time.ZonedDateTime;

public class TimeController {
    public static void main(String[] args) throws Exception{
        while (true) {
            try {
                Thread.sleep(10000);
                getCurrentTime();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public static void getCurrentTime() throws Exception{
        BuddyBotCallApplication bbca = new BuddyBotCallApplication();
        ZonedDateTime now = ZonedDateTime.now();
        int hour = now.getHour();
        int min = now.getMinute();
        switch (min){
            case 55:
                if(hour==9 || hour==12){
                    bbca.call("「巡回時刻は5分前です。次の巡回場所は1階の食品売り場です。」");
                }
                else if(hour==10 || hour==13){
                    bbca.call("「巡回時刻は5分前です。次の巡回場所は2階のテナントです。」");
                }
                else if(hour==11 || hour==14){
                    bbca.call("「巡回時刻は5分前です。次の巡回場所は3階の駐車場です。」");
                }
                else if(hour==15){
                    bbca.call("「巡回時刻は5分前です。次の巡回場所は全階です。」");
                }
            case 0:
                if(hour==10 || hour==13){
                    bbca.call("「巡回時刻です。巡回場所は1階の食品売り場です。」");
                }
                else if(hour==11 || hour==14){
                    bbca.call("「巡回時刻です。巡回場所は2階のテナントです。」");
                }
                else if(hour==12 || hour==15){
                    bbca.call("「巡回時刻です。巡回場所は3階の駐車場です。」");
                }
                else if(hour==16){
                    bbca.call("「巡回時刻です。巡回場所は全階です。」");
                }
            default: bbca.call("「対応時間外です」");
        }
    }

    public String getrecode() {
        

        return new String();
    }
}

