package org.decaywood.analysis;

import java.util.Objects;

/**
 * 仪表盘指标，描述资金相关的单个量化视图。
 */
public class DashboardGauge {

    private final String name;
    private final double value;
    private final double min;
    private final double max;
    private final String description;

    public DashboardGauge(String name, double value, double min, double max, String description) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = value;
        this.min = min;
        this.max = max;
        this.description = description == null ? "" : description;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getDescription() {
        return description;
    }
}
