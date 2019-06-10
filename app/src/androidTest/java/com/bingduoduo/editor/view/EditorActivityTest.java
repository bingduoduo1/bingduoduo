package com.bingduoduo.editor.view;

import com.termux.R;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

public class EditorActivityTest {

    @Test
    public void initData() {
        onView(withId(R.id.menu2)).check(matches(isDisplayed()));

    }
}
