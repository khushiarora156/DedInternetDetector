package com.detector.filter;

import com.detector.model.AnalysisResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Combines multiple Predicate<AnalysisResult> filters using AND or OR composition logic.
 * Demonstrates the Composite and Specification design patterns.
 */
public class CompositeResultFilter implements Predicate<AnalysisResult> {
    public enum Operator {
        AND,
        OR
    }

    private final Operator operator;
    private final List<Predicate<AnalysisResult>> filters;

    public CompositeResultFilter(Operator operator, List<Predicate<AnalysisResult>> filters) {
        this.operator = (operator != null) ? operator : Operator.AND;
        this.filters = (filters != null) ? new ArrayList<>(filters) : new ArrayList<>();
    }

    @SafeVarargs
    public static CompositeResultFilter and(Predicate<AnalysisResult>... filters) {
        List<Predicate<AnalysisResult>> list = new ArrayList<>();
        if (filters != null) {
            Collections.addAll(list, filters);
        }
        return new CompositeResultFilter(Operator.AND, list);
    }

    @SafeVarargs
    public static CompositeResultFilter or(Predicate<AnalysisResult>... filters) {
        List<Predicate<AnalysisResult>> list = new ArrayList<>();
        if (filters != null) {
            Collections.addAll(list, filters);
        }
        return new CompositeResultFilter(Operator.OR, list);
    }

    public CompositeResultFilter addFilter(Predicate<AnalysisResult> filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return this;
    }

    @Override
    public boolean test(AnalysisResult result) {
        if (result == null || filters.isEmpty()) {
            return true;
        }

        if (operator == Operator.AND) {
            for (Predicate<AnalysisResult> filter : filters) {
                if (!filter.test(result)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Predicate<AnalysisResult> filter : filters) {
                if (filter.test(result)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Filters a given collection of AnalysisResult according to this composite predicate.
     *
     * @param results collection to filter
     * @return filtered list
     */
    public List<AnalysisResult> filterList(List<AnalysisResult> results) {
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().filter(this).collect(Collectors.toList());
    }

    public Operator getOperator() {
        return operator;
    }

    public List<Predicate<AnalysisResult>> getFilters() {
        return Collections.unmodifiableList(filters);
    }
}
