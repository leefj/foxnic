package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;

public class PreBuildResult {
    public Collection<? extends Entity> getBuilds() {
        return builds;
    }

    public void setBuilds(Collection<? extends Entity> builds) {
        this.builds = builds;
    }

    public Collection<? extends Entity> getTargets() {
        return targets;
    }

    public void setTargets(Collection<? extends Entity> targets) {
        this.targets = targets;
    }

    /**
     * 已经从缓存构建的部分，无需再查询
     * */
    private Collection<? extends Entity> builds = new ArrayList<>();

    /**
     * 已经从缓存构建的部分，无需再查询
     * */
    private Collection<? extends Entity> targets =new ArrayList<>();
}
