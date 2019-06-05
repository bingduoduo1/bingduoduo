package com.bingduoduo.editor.view;

import androidx.test.rule.ActivityTestRule;

import com.termux.R;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSwitch()
    {
//        onView(withId(R.id.menu2)).check(matches(isDisplayed()));
        onView(withId(R.id.menu2)).check(matches(isDisplayed()));
//        onView(withId(R.id.))


    }
}
