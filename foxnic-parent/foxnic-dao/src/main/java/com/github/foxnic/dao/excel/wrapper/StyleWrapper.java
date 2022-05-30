package com.github.foxnic.dao.excel.wrapper;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class StyleWrapper {

    private CellStyle style;
    private WorkBookWrapper book;
    public  StyleWrapper(WorkBookWrapper book, CellStyle style) {
        this.book=book;
        this.style=style;
    }

    private FontWraper font = null;

    public FontWraper font() {
        Font font=this.book.createFont();
        if(this.font==null) {
            this.font = new FontWraper(book,font);
            this.style.setFont(font);
        }
        return this.font;
    }

    public StyleWrapper alignmentHori(HorizontalAlignment alignment) {
        style.setAlignment(alignment);
        return this;
    }

    public StyleWrapper alignmentVert(VerticalAlignment alignment) {
        style.setVerticalAlignment(alignment);
        return this;
    }

    /**
     * 换行
     * */
    public StyleWrapper wrap() {
        style.setWrapText(true);
        return this;
    }

//    public CellWrapper cell() {
//        return c
//    }
//
}
