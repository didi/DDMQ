package com.xiaojukeji.chronos.db;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.enums.CFHandlerNames;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;

import java.util.ArrayList;
import java.util.List;


public class CFManager {
    public static ColumnFamilyHandle CFH_DEFAULT;

    static final List<ColumnFamilyDescriptor> CF_DESCRIPTORS = new ArrayList<>();
    static final List<ColumnFamilyHandle> CF_HANDLES = new ArrayList<>();

    static {
        CF_DESCRIPTORS.add(new ColumnFamilyDescriptor(CFHandlerNames.DEFAULT.getName().getBytes(Charsets.UTF_8), OptionsConfig.COLUMN_FAMILY_OPTIONS_DEFAULT));
    }

    static void initCFManger(final List<ColumnFamilyHandle> CF_HANDLES) {
        CFH_DEFAULT = CF_HANDLES.get(CFHandlerNames.DEFAULT.ordinal());
    }
}
