package com.github.blindpirate.gogradle.util;

import java.util.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {
    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public static <T> List<T> collectOptional(Optional<T>... optionals) {
        List<T> ret = new ArrayList<>();
        for (Optional<T> optional : optionals) {
            if (optional.isPresent()) {
                ret.add(optional.get());
            }
        }

        return ret;
    }

    public static <T> List<T> flatten(List<T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    public static <T> boolean isEmpty(Collection<T> c) {
        return c == null || c.isEmpty();
    }

    public static <T> T findFirst(Collection<T> c) {
        for (T element : c) {
            return element;
        }
        return null;
    }

}
