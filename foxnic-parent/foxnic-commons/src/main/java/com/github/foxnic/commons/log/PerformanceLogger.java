package com.github.foxnic.commons.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 性能日志工具
 */
public class PerformanceLogger {

    private static class Anchor {
        private String name;
        private Long time;
        private Integer load;
        private Integer memory;
        private int index;
    }

    public static class Item {
        private String from;
        private String to;
        private Long cost;
    }

    private boolean enable=true;
    public  PerformanceLogger(boolean enable) {
        this.enable=enable;
    }

    public  PerformanceLogger() {
        this.enable=true;
    }


    private List<Anchor> anchors = new ArrayList<>();
    private Set<String> names=new HashSet<>();

    public void collect(String name) {
        if(!this.enable) return;
        if(names.contains(name)) {
            throw new RuntimeException(name+" have been used");
        }
        Anchor anchor = new Anchor();
        anchor.index = anchors.size();
        anchor.name = name;
        anchor.time = System.currentTimeMillis();
        anchors.add(anchor);
        names.add(name);
    }

    public List<Item> getResult() {
        if(!this.enable) return new ArrayList<>();
        List<Item> items = new ArrayList<>();
        for (Anchor from : anchors) {
            for (Anchor to : anchors) {
                if (from.equals(to)) continue;
                if (from.index >= to.index) continue;
                Item item = new Item();
                item.from = from.name;
                item.to = to.name;
                item.cost = to.time - from.time;
                items.add(item);
            }
        }
        return items;
    }

    public void info(String tag) {
        if(!this.enable) return;
        if(anchors.size()<2) {
            throw new RuntimeException("collect point is not enough");
        }

        Long total=anchors.get(anchors.size()-1).time-anchors.get(0).time;

        StringBuilder builder = new StringBuilder("\n┏━━━ PERFORMANCE [ "+tag+" , total = "+total+" ] ━━━ \n");
        Anchor next = null;

        for (int i = 0; i < anchors.size(); i++) {
            Anchor anchor = anchors.get(i);
            if(i<anchors.size()-1) {
                next = anchors.get(i+1);
                builder.append("┣ point : "+anchor.name + "\n");
                builder.append("┣━ cost : "+ (next.time- anchor.time) + "\n");

            } else {
                builder.append("┣ point : "+anchor.name + "\n");
            }

        }

        builder.append("┗━━━ PERFORMANCE [ "+tag+" , total = "+total+" ] ━━━");

        Logger.info(builder);
    }


}
