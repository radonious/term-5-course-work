package com.course.term5cw.Common;

import com.course.term5cw.Model.Adapter;
import com.course.term5cw.Model.Dictionary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

@JsonSerialize(using = Dictionary.CustomOperationUnitSerializer.class)
public class OperationUnit implements Serializable {

    private int sourcePosition;
    private int targetPosition;
    private Operation type;
    private ArrayList<Adapter> adapters;

    @JsonIgnore
    private LinkedList<String> adapters_words;


    public OperationUnit(int v0, int v1, Operation v2, ArrayList<Adapter> v3) {
        sourcePosition = v0;
        targetPosition = v1;
        type = v2;
        adapters = v3;
    }

    public void setAdapters_words(LinkedList<String> adapters_words) {
        this.adapters_words = adapters_words;
    }

    public LinkedList<String> getAdapters_words() {
        return adapters_words;
    }

    public int getSourcePosition() {
        return sourcePosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public ArrayList<Adapter> getAdapters() {
        return adapters;
    }

    public Operation getType() {
        return type;
    }
}
