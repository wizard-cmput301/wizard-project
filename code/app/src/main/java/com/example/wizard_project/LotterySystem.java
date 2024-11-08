package com.example.wizard_project;

import com.example.wizard_project.Classes.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LotterySystem {

    public List<User> drawEntrants(List<User> entrants, int selectCount, int listSize) {
        List<User> selections = new ArrayList<>();

        Collections.shuffle(entrants);
        selections = entrants.subList(0, selectCount);

        return selections;
    }

}
