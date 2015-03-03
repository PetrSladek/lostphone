package cz.vutbr.fit.stud.xslade12.lostphone.commands;

/**
 * Created by Peggy on 28.2.2015.
 */
public class LockCommand extends Command {

    protected String password;

    protected String ownerPhoneNumber;

    protected String displayText;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
