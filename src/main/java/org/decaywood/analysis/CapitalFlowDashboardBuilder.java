package org.decaywood.analysis;

import org.decaywood.entity.CapitalFlow;
import org.decaywood.entity.Entry;
import org.decaywood.entity.Industry;
import org.decaywood.entity.Stock;
import org.decaywood.utils.EmptyObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 根据每日收盘后的资金数据生成仪表盘视图，覆盖资金流量大小、结构与板块强弱。
 */
public class CapitalFlowDashboardBuilder {

    private static final int INDUSTRY_LIMIT = 5;

    private CapitalFlowDashboardBuilder() {
    }

    public static CapitalFlowDashboard build(List<Entry<Stock, CapitalFlow>> entries) {
        Objects.requireNonNull(entries, "entries");

        List<Entry<Stock, CapitalFlow>> validEntries = entries.stream()
                .filter(entry -> entry != null && entry.getKey() != EmptyObject.emptyStock && entry.getValue() != EmptyObject.emptyCapitalFlow)
                .collect(Collectors.toList());

        DashboardGauge netGauge = buildNetGauge(validEntries);
        List<DashboardGauge> structureGauges = buildStructureGauges(validEntries);
        List<DashboardGauge> industryGauges = buildIndustryGauges(validEntries);

        return new CapitalFlowDashboard(netGauge, structureGauges, industryGauges);
    }

    private static DashboardGauge buildNetGauge(List<Entry<Stock, CapitalFlow>> entries) {
        double total = entries.stream()
                .mapToDouble(entry -> parseDouble(entry.getValue().getCapitalInflow()))
                .sum();

        double maxAbs = entries.stream()
                .mapToDouble(entry -> Math.abs(parseDouble(entry.getValue().getCapitalInflow())))
                .max()
                .orElse(0D);

        double gaugeRange = maxAbs == 0 ? 1D : maxAbs;
        double min = -gaugeRange;
        double max = gaugeRange;
        String desc = "资金净流入(万)，正值代表当日资金流入，负值代表流出";
        return new DashboardGauge("市场总体净流", total, min, max, desc);
    }

    private static List<DashboardGauge> buildStructureGauges(List<Entry<Stock, CapitalFlow>> entries) {
        double large = entries.stream().mapToDouble(e -> parseDouble(e.getValue().getLargeQuantity())).sum();
        double mid = entries.stream().mapToDouble(e -> parseDouble(e.getValue().getMidQuantity())).sum();
        double small = entries.stream().mapToDouble(e -> parseDouble(e.getValue().getSmallQuantity())).sum();

        double totalAbs = Math.abs(large) + Math.abs(mid) + Math.abs(small);
        if (totalAbs == 0) {
            totalAbs = 1D;
        }

        List<DashboardGauge> gauges = new ArrayList<>();
        gauges.add(new DashboardGauge("大单贡献度", Math.abs(large) / totalAbs * 100, 0, 100, "机构/游资大单净额占比"));
        gauges.add(new DashboardGauge("中单活跃度", Math.abs(mid) / totalAbs * 100, 0, 100, "中单净额占比"));
        gauges.add(new DashboardGauge("散户情绪", Math.abs(small) / totalAbs * 100, 0, 100, "小单净额占比"));
        return gauges;
    }

    private static List<DashboardGauge> buildIndustryGauges(List<Entry<Stock, CapitalFlow>> entries) {
        Map<String, Double> industryFlow = new HashMap<>();
        for (Entry<Stock, CapitalFlow> entry : entries) {
            Stock stock = entry.getKey();
            Industry industry = stock.getIndustry();
            String name = industry == null ? "未分组板块" : industry.getIndustryName();
            double flow = parseDouble(entry.getValue().getCapitalInflow());
            industryFlow.merge(name, flow, Double::sum);
        }

        return industryFlow.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(INDUSTRY_LIMIT)
                .map(e -> buildIndustryGauge(e.getKey(), e.getValue(), industryFlow))
                .collect(Collectors.toList());
    }

    private static DashboardGauge buildIndustryGauge(String industryName, double value, Map<String, Double> industryFlow) {
        double maxAbs = industryFlow.values().stream().mapToDouble(Math::abs).max().orElse(1D);
        double normalized = value / maxAbs * 100;
        return new DashboardGauge(industryName, normalized, -100, 100, "板块资金净流强弱(归一化%)");
    }

    private static double parseDouble(String raw) {
        if (raw == null) return 0D;
        String normalized = raw.replace(",", "").replaceAll("[^0-9+\-.]", "");
        if (normalized.isEmpty() || "-".equals(normalized) || "+".equals(normalized)) return 0D;
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0D;
        }
    }
}
