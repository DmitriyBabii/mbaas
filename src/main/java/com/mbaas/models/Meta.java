package com.mbaas.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Meta {
    private final List<String> metas = new ArrayList<>();

    public void addMeta(String meta) {
        metas.add(meta);
    }
}
