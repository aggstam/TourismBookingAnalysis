// -------------------------------------------------------------
//
// Each ControlKey enum represents a keyboard key the user can
// submit during search, to pause, resume or stop the execution.
// ControlKey data: Key.
//
// Authors: Giorgos Mourtzounis, Aggelos Stamatiou, August 2020
//
// --------------------------------------------------------------

package com.tba.enums;

public enum ControlKey {
    PAUSE("p"),
    RESUME("r"),
    STOP("s");

    private final String key;

    ControlKey(final String key) {
        this.key = key;
    }

    @Override
    public String toString()
    {
        return key;
    }
}
