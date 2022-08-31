package com.github.foxnic.commons.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author fangjieli
 * 对JSON进行格式化
 */
class JSONFormater {

    private static final int SPACE_UNIT=4;

    public static String format(String jsonStr){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(jsonStr.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            char ch;
            int read;
            int space=0;
            while((read = in.read()) > 0){
                ch = (char)read;
                switch (ch){
                    case '{': {
                        space = outputAndRightMove(space, ch, out);
                        break;
                    }
                    case '[': {
                        //out.write(ch);
                        // space += SPACE_UNIT;
                        space = outputAndRightMove(space, ch, out);
                        break;
                    }
                    case '}': {
                        space = outputAndLeftMove(space, ch, out);
                        break;
                    }
                    case ']': {
                        space = outputAndLeftMove(space, ch, out);
                        break;
                    }
                    case ',': {
                        out.write(ch);
                        outputNewline(out);
                        out.write(getBlankingStringBytes(space));
                        break;
                    }
                    default: {
                        out.write(ch);
                        break;
                    }
                }
            }
            return out.toString().trim();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static int outputAndRightMove(int space, char ch, ByteArrayOutputStream out) throws IOException {
        //换行
        outputNewline(out);
        //向右缩进
        out.write(getBlankingStringBytes(space));
        out.write(ch);
        outputNewline(out);
        space += SPACE_UNIT;
        //再向右缩进多两个字符
        out.write(getBlankingStringBytes(space));
        return space;
    }
    public static int outputAndLeftMove(int space, char ch, ByteArrayOutputStream out) throws IOException{
        outputNewline(out);
        space -= SPACE_UNIT;
        out.write(getBlankingStringBytes(space));
        out.write(ch);
        return space;
    }
    public static byte[] getBlankingStringBytes(int space){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < space; i++) {
            sb.append(" ");
        }
        return sb.toString().getBytes();
    }

    public static void outputNewline(ByteArrayOutputStream out){
        out.write('\n');
    }
}
