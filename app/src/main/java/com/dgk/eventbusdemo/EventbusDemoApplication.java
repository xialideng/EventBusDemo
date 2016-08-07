package com.dgk.eventbusdemo;

import android.app.Application;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by denglixia on 16-8-7.
 */
public class EventbusDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }
}
