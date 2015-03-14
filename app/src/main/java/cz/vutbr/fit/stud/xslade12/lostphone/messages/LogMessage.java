package cz.vutbr.fit.stud.xslade12.lostphone.messages;

public class LogMessage extends Message {

    public LogMessage() {
        this.type = Message.TYPE_LOG;
    }

    public String callLog;

    public String smsLog;



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
