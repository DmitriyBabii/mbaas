package com.mbaas.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OpenFile {
    private final String name;
    private final String content;
}
