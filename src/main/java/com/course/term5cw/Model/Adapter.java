package com.course.term5cw.Model;

import java.io.Serializable;

public class Adapter implements Serializable {

    // Number of occurrences of a word in all versions of the file
    private Integer count;

    public Adapter() {
        count = 0;
    }

    public Adapter(Integer num) {
        count = num;
    }

    public void plus() {
        count++;
    }

    public void minus() {
        count--;
    }

    public Integer getCount() {
        return count;
    }
}
