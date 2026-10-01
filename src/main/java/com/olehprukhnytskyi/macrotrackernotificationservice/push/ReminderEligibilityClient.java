package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.olehprukhnytskyi.util.CustomHeaders;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderEligibilityClient {
    private static final String X_APP_VERSION_CODE = "X-App-Version-Code";
    private final RestClient.Builder restClientBuilder;
    @Value("${services.user-service-url}")
    private String userServiceUrl;
    @Value("${services.intake-service-url}")
    private String intakeServiceUrl;
    @Value("${services.bff-service-url}")
    private String bffServiceUrl;
    @Value("${notifications.app-version-code:62}")
    private String appVersionCode;

    public DailySummary dailySummary(Long userId, LocalDate date) {
        try {
            List<DailySummary> rows = client(intakeServiceUrl)
                    .get().uri(uri -> uri.path("/internal/intakes/daily-summary")
                            .queryParam("from", date).queryParam("to", date).build())
                    .header(CustomHeaders.X_USER_ID, userId.toString())
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            BigDecimal calories = rows == null || rows.isEmpty()
                    ? BigDecimal.ZERO : rows.getFirst().calories();
            List<IntakeRow> intakes = client(intakeServiceUrl)
                    .get().uri(uri -> uri.path("/api/intake")
                            .queryParam("date", date).build())
                    .header(CustomHeaders.X_USER_ID, userId.toString())
                    .header(X_APP_VERSION_CODE, appVersionCode)
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            List<IntakeRow> consumed = intakes == null ? List.of()
                    : intakes.stream()
                    .filter(row -> "CONSUMED".equals(row.status()))
                    .toList();
            int mealCount = (int) consumed.stream().map(IntakeRow::intakePeriod)
                    .filter(Objects::nonNull).distinct().count();
            return new DailySummary(date, calories, mealCount,
                    intakes == null ? 0 : intakes.size());
        } catch (RuntimeException exception) {
            log.warn("Could not load intake summary userId={} date={}",
                    userId, date, exception);
            return null;
        }
    }

    public Integer calorieGoal(Long userId, LocalDate date) {
        try {
            Goal goal = client(userServiceUrl)
                    .get().uri(uri -> uri.path("/api/profile/goal")
                            .queryParam("date", date).build())
                    .header(CustomHeaders.X_USER_ID, userId.toString())
                    .retrieve().body(Goal.class);
            return goal == null ? null : goal.calories();
        } catch (RuntimeException exception) {
            log.warn("Could not load calorie goal userId={} date={}",
                    userId, date, exception);
            return null;
        }
    }

    public boolean wasAiLimitExhausted(Long userId, LocalDate date, String timeZone) {
        try {
            AiExhaustion result = client(userServiceUrl)
                    .get().uri(uri -> uri.path("/internal/users/ai-credits/exhausted")
                            .queryParam("date", date).queryParam("timeZone", timeZone).build())
                    .header(CustomHeaders.X_USER_ID, userId.toString())
                    .retrieve().body(AiExhaustion.class);
            return result != null && result.exhausted();
        } catch (RuntimeException exception) {
            log.warn("Could not load AI exhaustion userId={} date={}",
                    userId, date, exception);
            return false;
        }
    }

    public boolean weeklyReportReady(Long userId, LocalDate weekStart) {
        try {
            WeeklyReport report = client(bffServiceUrl)
                    .get().uri(uri -> uri.path("/api/insights/weekly-report")
                            .queryParam("weekStart", weekStart).build())
                    .header(CustomHeaders.X_USER_ID, userId.toString())
                    .header(X_APP_VERSION_CODE, appVersionCode)
                    .retrieve().body(WeeklyReport.class);
            return report != null && report.recordedDays() > 0;
        } catch (RuntimeException exception) {
            log.info("Weekly report is not ready userId={} weekStart={}",
                    userId, weekStart);
            return false;
        }
    }

    private RestClient client(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return restClientBuilder.clone().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public record DailySummary(LocalDate date, BigDecimal calories,
                               int mealCount, int intakeCount) {
    }

    private record Goal(int calories) {
    }

    private record AiExhaustion(boolean exhausted) {
    }

    private record WeeklyReport(int recordedDays) {
    }

    private record IntakeRow(String intakePeriod, String status) {
    }
}
