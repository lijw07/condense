package io.condense.web;

import io.condense.auth.SubscriberSession;
import io.condense.config.EmailProperties;
import io.condense.subscriber.SubscriberView;
import io.condense.subscriber.SubscriberViewFactory;
import io.condense.web.data.DashboardData;
import io.condense.web.data.SubscriberPreferences;
import io.condense.web.view.PreferencesView;
import io.condense.web.view.StockDetailView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final SubscriberSession subscriberSession;
    private final SubscriberViewFactory views;
    private final DashboardData dashboardData;
    private final SubscriberPreferences preferences;
    private final boolean devInboxEnabled;

    public PageController(SubscriberSession subscriberSession,
                          SubscriberViewFactory views,
                          DashboardData dashboardData,
                          SubscriberPreferences preferences,
                          EmailProperties emailProperties) {
        this.subscriberSession = subscriberSession;
        this.views = views;
        this.dashboardData = dashboardData;
        this.preferences = preferences;
        this.devInboxEnabled = emailProperties.isDevInbox();
    }

    @GetMapping("/")
    public String landing(Model model, HttpServletRequest request) {
        if (signedIn(request)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("theme", "dark");
        model.addAttribute("devInboxEnabled", devInboxEnabled);
        return "index";
    }

    @GetMapping("/access")
    public String access(Model model, HttpServletRequest request) {
        if (signedIn(request)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("theme", "dark");
        return "access";
    }

    @GetMapping("/link-sent")
    public String linkSent(@RequestParam(defaultValue = "") String email, Model model) {
        model.addAttribute("theme", "dark");
        model.addAttribute("email", email);
        model.addAttribute("devInboxEnabled", devInboxEnabled);
        return "link-sent";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        Optional<UUID> subscriberId = subscriberSession.currentSubscriberId(request);
        if (subscriberId.isEmpty()) {
            return "redirect:/access";
        }
        SubscriberView subscriber = views.forId(subscriberId.get());
        applyShell(model, subscriberId.get(), subscriber, "dashboard");
        model.addAttribute("stocks", dashboardData.stocksFor(subscriberId.get(), subscriber.tickers()));
        return "dashboard";
    }

    @GetMapping("/stocks/{ticker}")
    public String stockDetail(@PathVariable String ticker, Model model, HttpServletRequest request) {
        Optional<UUID> subscriberId = subscriberSession.currentSubscriberId(request);
        if (subscriberId.isEmpty()) {
            return "redirect:/access";
        }
        SubscriberView subscriber = views.forId(subscriberId.get());
        Optional<StockDetailView> detail = dashboardData.stockDetail(subscriberId.get(), ticker);
        if (detail.isEmpty()) {
            return "redirect:/dashboard";
        }
        applyShell(model, subscriberId.get(), subscriber, "dashboard");
        model.addAttribute("stock", detail.get());
        return "stock-detail";
    }

    @GetMapping("/calendar")
    public String calendar(Model model, HttpServletRequest request) {
        Optional<UUID> subscriberId = subscriberSession.currentSubscriberId(request);
        if (subscriberId.isEmpty()) {
            return "redirect:/access";
        }
        SubscriberView subscriber = views.forId(subscriberId.get());
        applyShell(model, subscriberId.get(), subscriber, "calendar");
        model.addAttribute("months", dashboardData.calendarFor(subscriberId.get(), subscriber.tickers()));
        return "calendar";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpServletRequest request) {
        Optional<UUID> subscriberId = subscriberSession.currentSubscriberId(request);
        if (subscriberId.isEmpty()) {
            return "redirect:/access";
        }
        SubscriberView subscriber = views.forId(subscriberId.get());
        applyShell(model, subscriberId.get(), subscriber, "settings");
        return "settings";
    }

    private void applyShell(Model model, UUID subscriberId, SubscriberView subscriber, String active) {
        PreferencesView current = preferences.forSubscriber(subscriberId);
        model.addAttribute("subscriber", subscriber);
        model.addAttribute("preferences", current);
        model.addAttribute("theme", current.themeAttribute());
        model.addAttribute("active", active);
        model.addAttribute("devInboxEnabled", devInboxEnabled);
    }

    private boolean signedIn(HttpServletRequest request) {
        return subscriberSession.currentSubscriberId(request).isPresent();
    }
}
