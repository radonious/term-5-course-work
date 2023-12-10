package com.course.term5cw;

import com.course.term5cw.View.MainView;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.text.DiffRow;
import javafx.application.Application;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args)  {
        // Show MainView
        Application.launch(MainView.class);
    }
}
