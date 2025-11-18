package org.decaywood.analysis;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 描述每日收盘后资金全景的仪表盘模型。
 */
public class CapitalFlowDashboard {

    private final DashboardGauge netCapitalGauge;
    private final List<DashboardGauge> structureGauges;
    private final List<DashboardGauge> industryStrengthGauges;

    public CapitalFlowDashboard(DashboardGauge netCapitalGauge,
                                List<DashboardGauge> structureGauges,
                                List<DashboardGauge> industryStrengthGauges) {
        this.netCapitalGauge = Objects.requireNonNull(netCapitalGauge, "netCapitalGauge");
        this.structureGauges = Collections.unmodifiableList(structureGauges);
        this.industryStrengthGauges = Collections.unmodifiableList(industryStrengthGauges);
    }

    public DashboardGauge getNetCapitalGauge() {
        return netCapitalGauge;
    }

    public List<DashboardGauge> getStructureGauges() {
        return structureGauges;
    }

    public List<DashboardGauge> getIndustryStrengthGauges() {
        return industryStrengthGauges;
    }
}
