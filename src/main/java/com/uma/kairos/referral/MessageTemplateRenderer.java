package com.umasankar.applytrack.referral;

import com.umasankar.applytrack.application.JobApplication;
import com.umasankar.applytrack.auth.User;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageTemplateRenderer {
    public static final String DEFAULT_TEMPLATE =
        "Hi {{name}},\n\nI’m applying for the {{jobTitle}} role at {{company}}. Would you be open to referring me?\n\nThanks,\n{{senderName}}";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([A-Za-z_]+)}}");

    public String render(String template, Referral referral, JobApplication application, User sender) {
        String source = template == null || template.isBlank() ? DEFAULT_TEMPLATE : template;
        Map<String, String> values = Map.of(
            "name", safe(referral.getName()), "company", safe(application.getCompanyName()),
            "jobTitle", safe(application.getJobTitle()), "role", safe(application.getJobTitle()),
            "jobId", safe(application.getJobId()), "job_ID", safe(application.getJobId()),
            "jobUrl", safe(application.getJobUrl()), "senderName", safe(sender.getName())
        );
        return java.util.Arrays.stream(source.split("\\n", -1))
            .filter(line -> !containsKnownEmptyValue(line, values))
            .map(line -> replace(line, values))
            .collect(java.util.stream.Collectors.joining("\n")).trim();
    }

    private boolean containsKnownEmptyValue(String line, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(line);
        while (matcher.find()) {
            if (values.containsKey(matcher.group(1)) && values.get(matcher.group(1)).isBlank()) return true;
        }
        return false;
    }

    private String replace(String line, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(line);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = values.get(matcher.group(1));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String safe(String value) { return value == null ? "" : value; }
}
