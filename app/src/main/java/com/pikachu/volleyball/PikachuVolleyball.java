package com.pikachu.volleyball;

import com.kilobolt.framework.Screen;
import com.kilobolt.framework.implementation.AndroidGame;

/**
 * Created by wanlin on 15/6/4.
 */

/**
 * when we start the game, the SampleGame class will be instantiated,
 * and the methods from the Activity Lifecycle will be called
 * (starting with the onCreate). These methods are all implemented
 * in the AndroidGame superclass that SampleGame extends.
 */

public class PikachuVolleyball extends AndroidGame {

    public PikachuVolleyball() {

    }

    @Override
    public Screen getInitScreen() {
        return new LoadingScreen(this);
    }

    @Override
    public void onBackPressed() {
        getCurrentScreen().backButton();
    }


}
