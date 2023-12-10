package com.course.term5cw.Common;

import java.io.File;
import java.util.TimerTask;

public abstract class FileWatcher extends TimerTask {
    private long timeStamp;
    private final File file;

    public FileWatcher(File file) {
        this.file = file;
        this.timeStamp = file.lastModified();
    }

    @Override
    public final void run() {
        long timeStamp = file.lastModified();

        if( this.timeStamp != timeStamp ) {
            this.timeStamp = timeStamp;
            onChange(file);
        }
    }

    protected abstract void onChange( File f );
}
