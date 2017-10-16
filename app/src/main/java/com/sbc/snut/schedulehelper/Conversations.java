package com.sbc.snut.schedulehelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Conversations {
    static class Conversation {
        private final int conversationId;
        private final String participantName;
        private final List<String> messages;
        private final long timestamp;

        public Conversation(int conversationId, String participantName, List<String> messages) {
            this.conversationId = conversationId;
            this.participantName = participantName;
            this.messages = messages == null ? Collections.<String>emptyList() : messages;
            this.timestamp = System.currentTimeMillis();
        }

        public int getConversationId() {
            return conversationId;
        }

        public String getParticipantName() {
            return participantName;
        }

        public List<String> getMessages() {
            return messages;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private Conversations() {
    }

    public static Conversation[] getUnreadConversations(int howManyConversations,
                                                        int messagesPerConversation) {
        Conversation[] conversations = new Conversation[howManyConversations];
        for (int i = 0; i < howManyConversations; i++) {
            conversations[i] = new Conversation(
                    ThreadLocalRandom.current().nextInt(),
                    name(), makeMessages(messagesPerConversation));
        }
        return conversations;
    }

    private static List<String> makeMessages(int messagesPerConversation) {
        List<String> messages = new ArrayList<>(messagesPerConversation);
        for (int i = 0; i < messagesPerConversation; i++) {
            messages.add("명령어를 입력하세요!");
        }
        return messages;
    }

    private static String name() {
        return "Notification";
    }
}
