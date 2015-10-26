package tachos.ru.touch.me.data.db;

import com.orm.SugarRecord;

public class PrivateMessage extends SugarRecord<PrivateMessage> {
    String message;
    String messageId;
    String partnerId;
    boolean incoming;
    boolean viewed;
    long receivedTime;

    public PrivateMessage() {
    }

    public PrivateMessage(String message, String messageId, String partnerId, boolean incoming, long receivedTime) {
        this.message = message;
        this.messageId = messageId;
        this.partnerId = partnerId;
        this.incoming = incoming;
        this.receivedTime = receivedTime;
        viewed = false;
    }

    public String getText() {
        return message;
    }
}
