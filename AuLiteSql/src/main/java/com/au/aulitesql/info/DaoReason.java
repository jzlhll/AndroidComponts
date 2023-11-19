package com.au.aulitesql.info;

import androidx.annotation.NonNull;

public class DaoReason {
    public DaoReason(String reason) {
        this.reason = reason;
    }

    public final String reason;

    public Exception e;

    public static final DaoReason Success = new DaoReason("success");

    public static final DaoReason Failed = new DaoReason("fail");

    public static DaoReason createFail(String reason) {
        return new DaoReason(reason);
    }

    public static DaoReason createFail(String reason, @NonNull Exception e) {
        var r = new DaoReason(reason);
        r.e = e;
        return r;
    }
}
