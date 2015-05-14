package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva s vypisi volani a sms
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LogMessage extends Message {

    public LogMessage() {
        this.type = Message.TYPE_LOG;
    }

    protected String callLog;

    protected String smsLog;


    public String getCallLog() {
        return callLog;
    }

    public void setCallLog(String callLog) {
        this.callLog = callLog;
    }

    public String getSmsLog() {
        return smsLog;
    }

    public void setSmsLog(String smsLog) {
        this.smsLog = smsLog;
    }
}
