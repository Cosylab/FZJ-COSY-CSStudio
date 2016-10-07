package com.cosylab.fzj.cosy.oc.ui.model;

/**
 * <code>OperationStatus</code> represents the current orbit correction operation status.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public enum OperationStatus {
    IDLE,
    MEASURING_ORBIT,
    CORRECTING_ORBIT,
    CORRECTING_ORM
}