package com.github.foxnic.dao.excel.wrapper;

import com.github.foxnic.commons.lang.ColorUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

public class FontWraper {

    private WorkBookWrapper book;
    private Font font;
    public FontWraper(WorkBookWrapper book, Font font) {
        this.book=book;
        this.font=font;
    }

    public FontWraper color(String color) {
        this.book.applyFontColor(font, ColorUtil.toColor(color));
        return this;
    }

    public FontWraper size(int size) {
        font.setFontHeightInPoints((short)size);
        return this;
    }

    public FontWraper bold(boolean bold) {
        font.setBold(bold);
        return this;
    }



}
