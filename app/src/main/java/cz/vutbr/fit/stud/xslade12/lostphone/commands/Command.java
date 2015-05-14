package cz.vutbr.fit.stud.xslade12.lostphone.commands;

import android.os.Bundle;


/**
 * Abstraktní předek všech příkazů
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public abstract class Command {


    // Typy prikazu
    static final int TYPE_PING              = 0x0000;
    static final int TYPE_RING              = 0x0001;
    static final int TYPE_LOCK              = 0x0002;
    static final int TYPE_LOCATE            = 0x0003;
    static final int TYPE_GETLOG            = 0x0004;
    static final int TYPE_ENCRYPTSTORAGE    = 0x0005;
    static final int TYPE_WIPEDATA          = 0x0006;

    /**
     * Unikatni ID
     */
    protected int id;


    /**
     * Vrat Unikatni ID
     * @return int
     */
    public int getId() {
        return id;
    }


    /**
     * Vytvori instanci daneho prikazu z prichotiho Bundlu z GCM
     * @param data
     * @return
     */
    public static Command createFromBundle(Bundle data) {

        int type = Integer.valueOf(data.getString("type"));
        int id = Integer.valueOf(data.getString("id"));
        switch(type) {
            case TYPE_RING:
                RingCommand cmdRing = new RingCommand();
                cmdRing.id = id;
                cmdRing.closeAfter = Long.valueOf(data.getString("closeAfter"));
                return cmdRing;

            case TYPE_LOCK:
                LockCommand cmdLock = new LockCommand();
                cmdLock.id = id;
                cmdLock.password = data.getString("password");
                cmdLock.displayText = data.getString("displayText");
                cmdLock.ownerPhoneNumber = data.getString("ownerPhoneNumber");
                return cmdLock;

            case TYPE_LOCATE:
                LocateCommand cmdLocate = new LocateCommand();
                cmdLocate.id = id;
                return cmdLocate;

            case TYPE_GETLOG:
                GetLogCommand cmdGetLog = new GetLogCommand();
                cmdGetLog.id = id;
                return cmdGetLog;

            case TYPE_ENCRYPTSTORAGE:
                EncryptStorageCommand cmdEncryptStorage = new EncryptStorageCommand();
                cmdEncryptStorage.id = id;
                return cmdEncryptStorage;

            case TYPE_WIPEDATA:
                WipeDataCommand cmdWipeData = new WipeDataCommand();
                cmdWipeData.id = id;
                return cmdWipeData;

            case TYPE_PING:
            default:
                PingCommand cmdPing = new PingCommand();
                cmdPing.id = id;
                return cmdPing;

        }

    }

}
