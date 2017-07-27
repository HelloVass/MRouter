package com.chiclaim.modularization.user;

import java.io.Serializable;

/**
 * Description：
 * <br/>
 * Created by kumu on 2017/7/27.
 */

public class User implements Serializable {

    private String name;

    private int age;

    private int gender;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
