package com.babytracker.constants;

import java.util.Set;

public final class FeedbackTypes {
    public static final String LIKE = "like";
    public static final String NEUTRAL = "neutral";
    public static final String DISLIKE = "dislike";
    public static final Set<String> ALL = Set.of(LIKE, NEUTRAL, DISLIKE);
    private FeedbackTypes() {}
}
