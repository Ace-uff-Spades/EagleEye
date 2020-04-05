package com.example.droneapplication;

import android.util.Log;

import java.io.File;

public class Testing {

    public Testing(){
        File one = new File("./");
        System.out.println(one.isDirectory());
    }
}
