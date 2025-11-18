package analysis;

import org.decaywood.analysis.CapitalFlowDashboard;
import org.decaywood.analysis.CapitalFlowDashboardBuilder;
import org.decaywood.analysis.DashboardGauge;
import org.decaywood.entity.CapitalFlow;
import org.decaywood.entity.Entry;
import org.decaywood.entity.Industry;
import org.decaywood.entity.Stock;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CapitalFlowDashboardBuilderTest {

    @Test
    public void shouldBuildDashboardWithNormalizedGauges() {
        Stock stockA = new Stock("StockA", "SH000001");
        stockA.setIndustry(new Industry("半导体", "8010"));
        Stock stockB = new Stock("StockB", "SZ000002");
        stockB.setIndustry(new Industry("白酒", "8810"));

        CapitalFlow flowA = new CapitalFlow("100", "60", "30", "10", "20", "10", "55", "120",
                Collections.singletonList(10D));
        CapitalFlow flowB = new CapitalFlow("-50", "-40", "-5", "-5", "10", "50", "45", "-80",
                Collections.singletonList(-10D));

        CapitalFlowDashboard dashboard = CapitalFlowDashboardBuilder.build(Arrays.asList(
                new Entry<>(stockA, flowA),
                new Entry<>(stockB, flowB)
        ));

        DashboardGauge netGauge = dashboard.getNetCapitalGauge();
        assertEquals(50D, netGauge.getValue(), 0.001);
        assertEquals(-100D, netGauge.getMin(), 0.001);
        assertEquals(100D, netGauge.getMax(), 0.001);

        assertEquals(3, dashboard.getStructureGauges().size());
        assertEquals(40D, dashboard.getStructureGauges().get(0).getValue(), 0.001);
        assertEquals(50D, dashboard.getStructureGauges().get(1).getValue(), 0.001);
        assertEquals(10D, dashboard.getStructureGauges().get(2).getValue(), 0.001);

        assertFalse(dashboard.getIndustryStrengthGauges().isEmpty());
        assertEquals("半导体", dashboard.getIndustryStrengthGauges().get(0).getName());
        assertEquals(100D, dashboard.getIndustryStrengthGauges().get(0).getValue(), 0.001);
        assertTrue(dashboard.getIndustryStrengthGauges().stream().anyMatch(g -> g.getName().equals("白酒")));
    }
}
