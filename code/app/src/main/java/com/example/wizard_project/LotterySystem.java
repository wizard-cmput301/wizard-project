package com.example.wizard_project;

import android.util.Log;

import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * LotterySystem provides functionality to randomly draw entrants for an event.
 */
public class LotterySystem {
    private final EventController eventController = new EventController();

    /**
     * Performs a lottery draw of entrants for an event.
     * @param event The event to perform the lottery draw on.
     * @param entrants A list of the event's entrants.
     * @param drawCount The number of entrants to draw.
     */
    public void drawEntrants(Event event, List<Entrant> entrants, int drawCount) {
        // Randomly shuffle the list of entrants.
        Collections.shuffle(entrants);

        // After shuffling, all entrants up to the max draw amount are selected; the remaining are not.
        for (int i = 0; i < entrants.size(); i++) {
            Entrant entrant = entrants.get(i);
            if (i < drawCount) {
                entrant.setStatus("Selected");
            }
            else {
                entrant.setStatus("Not Selected");
            }
            eventController.updateEntrantStatus(event, entrant);
        }
    }
}
