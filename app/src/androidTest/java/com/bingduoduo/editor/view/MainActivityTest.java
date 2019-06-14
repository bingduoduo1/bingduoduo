package com.bingduoduo.editor.view;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import com.termux.R;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
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
        onView(isRoot()).perform(swipeRight());
//        onView(withId(R.id.menu_helper)).check(matches(isDisplayed()));
//        onView(withId(android.R.id.home)).check(matches(isDisplayed()));
//        Espresso.pressBack();
        onView(withContentDescription(containsString("navigation drawer"))).check(matches(isDisplayed()));
        onView(withContentDescription(containsString("navigation drawer"))).perform(click());


    }
}
