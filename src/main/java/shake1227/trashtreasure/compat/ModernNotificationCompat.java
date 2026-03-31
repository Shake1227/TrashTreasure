package shake1227.trashtreasure.compat;

import net.minecraft.network.chat.Component;
import shake1227.modernnotification.core.NotificationCategory;
import shake1227.modernnotification.core.NotificationType;
import shake1227.modernnotification.notification.Notification;
import shake1227.modernnotification.notification.NotificationManager;
import shake1227.modernnotification.util.TextFormattingUtils;

import java.util.List;

public class ModernNotificationCompat {
    public static void showNotification(String categoryStr, Component component) {
        String text = component.getString();
        String formattedText = formatMessage(text);

        NotificationCategory category;
        try {
            category = NotificationCategory.valueOf(categoryStr);
        } catch (Exception e) {
            category = NotificationCategory.SYSTEM;
        }

        List<Component> messageComponents = TextFormattingUtils.parseLegacyText(formattedText);

        Notification notification = new Notification(
                NotificationType.LEFT,
                category,
                null,
                messageComponents,
                5
        );

        NotificationManager.getInstance().getRenderer().calculateDynamicWidth(notification);
        NotificationManager.getInstance().addNotification(notification);
    }

    private static String formatMessage(String text) {
        int rawLength = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '§' || c == '&') && i + 1 < text.length()) {
                i++;
                continue;
            }
            rawLength++;
        }
        if (rawLength <= 25) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int currentLineLen = 0;
        boolean inQuotes = false;
        String lastColor = "";

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if ((c == '§' || c == '&') && i + 1 < text.length()) {
                char nextC = text.charAt(i + 1);
                lastColor = String.valueOf(c) + nextC;
                result.append(c).append(nextC);
                i++;
                continue;
            }

            result.append(c);

            if (c == '「' || c == '『' || c == '[' || c == '<' || c == '【') inQuotes = true;
            if (c == '」' || c == '』' || c == ']' || c == '>' || c == '】') inQuotes = false;

            currentLineLen++;

            if (currentLineLen >= 22 && !inQuotes) {
                int remainingChars = text.length() - i - 1;
                if (remainingChars <= 6) {
                    continue;
                }
                if (i + 1 < text.length()) {
                    char nextC = text.charAt(i + 1);
                    if (nextC == '、' || nextC == '。' || nextC == '！' || nextC == '？' || nextC == '」' || nextC == '』' || nextC == ')' || nextC == ']' || nextC == '）' || nextC == '】' || nextC == '.' || nextC == ' ') {
                        continue;
                    }
                }
                result.append("&u").append(lastColor);
                currentLineLen = 0;
            }
        }
        return result.toString();
    }
}