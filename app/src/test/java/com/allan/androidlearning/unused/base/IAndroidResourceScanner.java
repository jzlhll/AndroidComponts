package com.allan.androidlearning.unused.base;

import com.allan.androidlearning.unused.beans.ids.DrawableId;
import com.allan.androidlearning.unused.beans.ids.LayoutId;
import com.allan.androidlearning.unused.beans.ids.ResId;

import java.util.List;

public abstract class IAndroidResourceScanner {
    public abstract void initResPath(String res);

    public abstract List<DrawableId> scanAllDrawablesMipmaps();
    public abstract List<LayoutId> scanAllLayouts();
    //protected abstract List<File> scanAllMenu();

    public abstract List<ResId> scanAllValues();

    //protected abstract List<String> scanAllIds();
}
