package com.mbaas.models;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Statistic {
    private Integer onlineUsersCount = 0;

    public Integer getOnlineUsersCount() {
        return onlineUsersCount;
    }

    public void setOnlineUsersCount(Integer onlineUsersCount) {
        this.onlineUsersCount = onlineUsersCount;
    }
}
