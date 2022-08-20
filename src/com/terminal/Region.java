package com.terminal;

import java.util.ArrayList;

public class Region {

    private ArrayList<Cell> cells;
    private String name;

    public Region() {
    }


    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Region(ArrayList<Cell> cells, String name) {
        this.cells = cells;
        this.name = name;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

}
