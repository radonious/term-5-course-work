package com.course.term5cw.Model;

import java.io.Serializable;

public class Adapter implements Serializable {

    public Adapter() {
        count = 0;
    }

    public Adapter(Integer num) {
        count = num;
    }

    // Number of occurrences of a word in all versions of the file
    public Integer count;
}
