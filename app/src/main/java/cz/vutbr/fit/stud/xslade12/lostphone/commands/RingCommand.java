package cz.vutbr.fit.stud.xslade12.lostphone.commands;

/**
 * Příkaz na prozvonění zařízení
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class RingCommand extends Command {

    /**
     * Doba, po ktere se prozvoneni ukončí
     */
    protected long closeAfter = 0;

    public long getCloseAfter() {
        return closeAfter;
    }

    public void setCloseAfter(long closeAfter) {
        this.closeAfter = closeAfter;
    }
}
