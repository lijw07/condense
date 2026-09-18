package io.condense.web.view;

import java.util.List;

public record BriefView(String number,
                        String ticker,
                        String filingType,
                        String filedOn,
                        String periodLabel,
                        String headline,
                        List<KeyPointView> keyPoints,
                        List<MetricView> metrics,
                        String edgarUrl,
                        int sourcePageCount) {
}
