package com.concientic.catalog.ingestion;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HtmlSemanticAnalyzer {
    private static final Pattern TITLE = Pattern.compile("(?is)<title\\b[^>]*>(.*?)</title>");
    private static final Pattern DESCRIPTION = Pattern.compile("(?is)<meta\\b[^>]*name=[\\\"']description[\\\"'][^>]*content=[\\\"'](.*?)[\\\"'][^>]*>");
    private static final Pattern CANONICAL = Pattern.compile("(?is)<link\\b[^>]*rel=[\\\"']canonical[\\\"'][^>]*href=[\\\"'](.*?)[\\\"'][^>]*>");

    Analysis analyze(URI requestedUrl, URI finalUrl, String html, String expectedTitle) {
        String normalizedHtml = html == null ? "" : html;
        String visibleText = stripMarkup(normalizedHtml).toLowerCase(Locale.ROOT);
        String title = firstGroup(TITLE, normalizedHtml);
        String description = firstGroup(DESCRIPTION, normalizedHtml);
        URI canonical = resolve(finalUrl, firstGroup(CANONICAL, normalizedHtml));
        boolean removed = containsAny(visibleText, "course is no longer available", "this course has been withdrawn", "page not found", "resource not found", "no longer available");
        boolean requiresPayment = containsAny(visibleText, "buy certificate", "paid certificate", "payment required", "purchase this course");
        boolean requiresAccount = containsAny(visibleText, "sign in to access", "log in to access", "create an account");
        boolean expectedMatches = expectedTitle == null || expectedTitle.isBlank() || visibleText.contains(expectedTitle.toLowerCase(Locale.ROOT));
        boolean hasLearningSignal = containsAny(visibleText, "learn", "course", "free", "digital skills", "cyber security", "cybersecurity");
        boolean semanticMatch = !removed && expectedMatches && hasLearningSignal;
        AccessStatus status = removed ? AccessStatus.REMOVED : semanticMatch ? AccessStatus.ACTIVE : AccessStatus.UNKNOWN;
        return new Analysis(status, clean(title), clean(description), canonical, semanticMatch, requiresAccount, requiresPayment);
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }

    private static String stripMarkup(String html) {
        return html.replaceAll("(?is)<script\\b.*?</script>|<style\\b.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String firstGroup(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String clean(String value) {
        return value == null ? null : stripMarkup(value).trim();
    }

    private static URI resolve(URI base, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return base.resolve(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    record Analysis(AccessStatus status, String title, String description, URI canonicalUrl, boolean semanticMatch, boolean requiresAccount, boolean requiresPayment) {
    }
}
