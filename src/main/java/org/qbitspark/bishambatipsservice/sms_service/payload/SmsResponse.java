package org.qbitspark.bishambatipsservice.sms_service.payload;

import lombok.Data;
import java.util.List;

@Data
public class SmsResponse {
    private List<SmsMessage> messages;

    @Data
    public static class SmsMessage {
        private String to;
        private SmsStatus status;
        private Long messageId;
        private Integer smsCount;
        private String message;
    }

    @Data
    public static class SmsStatus {
        private Integer groupId;
        private String groupName;
        private Integer id;
        private String name;
        private String description;
    }

    public boolean isSuccessful() {
        return messages != null && !messages.isEmpty() &&
                messages.stream().anyMatch(msg ->
                        "PENDING_ENROUTE".equals(msg.getStatus().getName()));
    }
}