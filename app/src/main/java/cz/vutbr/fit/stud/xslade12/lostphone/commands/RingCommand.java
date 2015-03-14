package cz.vutbr.fit.stud.xslade12.lostphone.commands;

/**
 * Created by Peggy on 28.2.2015.
 */
public class RingCommand extends Command {

    protected long closeAfter = 0;

    public long getCloseAfter() {
        return closeAfter;
    }

    public void setCloseAfter(long closeAfter) {
        this.closeAfter = closeAfter;
    }
}
