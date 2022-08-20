package com.terminal;

import java.util.ArrayList;

public class Cell {

    private String containers;
    private String name;

    public Cell(String containers, String name) {
        this.containers = containers;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cell() {
    }

    public Cell(String containers) {
        this.containers = containers;
    }

    public String getContainers() {
        return containers;
    }
}
