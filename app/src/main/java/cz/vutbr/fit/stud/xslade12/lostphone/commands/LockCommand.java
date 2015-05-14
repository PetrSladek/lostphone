package cz.vutbr.fit.stud.xslade12.lostphone.commands;

/**
 * Příkaz pro uzamceni zarizeni
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LockCommand extends Command {

    /**
     * Heslo pro odemknuti
     */
    protected String password;

    /**
     * Telefonni cislo majitele
     */
    protected String ownerPhoneNumber;

    /**
     * Text, ktery se zobrazi na displeji
     */
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
