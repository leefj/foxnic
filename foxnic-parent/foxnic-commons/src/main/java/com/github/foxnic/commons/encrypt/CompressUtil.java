package com.github.foxnic.commons.encrypt;

import com.github.foxnic.commons.log.Logger;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.*;

public class CompressUtil {

    /**
     * 压缩字符串,默认梳utf-8
     *
     * @param text
     * @return
     */
    public static String compress(String text) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(out)) {
                deflaterOutputStream.write(text.getBytes("UTF-8"));
            }
            return new String(Base64Util.encodeToString(out.toByteArray()));
        } catch (IOException e) {
            Logger.exception("compress error",e);
        }
        return null;
    }


    /**
     * 解压字符串,默认utf-8
     *
     * @param text
     * @return
     */
    public static String decompress(String text) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (OutputStream outputStream = new InflaterOutputStream(os)) {
                outputStream.write(Base64Util.decodeToBtyes(text));
            }
            return new String(os.toByteArray(), "UTF-8");
        } catch (IOException e) {
            Logger.exception("decompress error",e);
        }
        return null;
    }

    /***
     * 压缩GZip
     *
     * @param data
     * @return
     */
    public static String gZip(String data) {
        byte[] result=gZip(data.getBytes());
        return Base64Util.encodeToString(result);
    }

    /***
     * 压缩GZip
     *
     * @param data
     * @return
     */
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /***
     * 解压GZip
     *
     * @param base64
     * @return
     */
    public static String unGZip(String base64) {
        byte[] result=unGZip(Base64Util.decodeToBtyes(base64));
        return new String(result);
    }

    /***
     * 解压GZip
     *
     * @param data
     * @return
     */
    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }


    public static String zip(String data) {
        byte[] result=zip(data.getBytes());
        return Base64Util.encodeToString(result);
    }

    /***
     * 压缩Zip
     *
     * @param data
     * @return
     */
    public static byte[] zip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(data.length);
            zip.putNextEntry(entry);
            zip.write(data);
            zip.closeEntry();
            zip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    public static String unZip(String base64) {
        byte[] result=unZip(Base64Util.decodeToBtyes(base64));
        return new String(result);
    }

    /***
     * 解压Zip
     *
     * @param data
     * @return
     */
    public static byte[] unZip(byte[] data) {

        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    public static String compress7z(String data) {
        byte[] result=compress7z(data.getBytes());
        return Base64Util.encodeToString(result);
    }

    public static byte[] compress7z(byte[] data) {
        ByteArrayInputStream in=new ByteArrayInputStream(data);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try {
            XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = xzIn.read(buffer))) {
                out.write(buffer, 0, n);
            }
            byte[] result=out.toByteArray();
            out.close();
            xzIn.close();
            return result;
        } catch (Exception e) {}
        return null;
    }

//    public static byte[] decompress7z(byte[] data) {
//        ByteArrayInputStream in=new ByteArrayInputStream(data);
//        ByteArrayOutputStream out=new ByteArrayOutputStream();
//        try {
//            XZCompressorOutputStream xzIn = new XZCompressorInputStream();
//            xzIn.write(data);
//
//
//            final byte[] buffer = new byte[1024];
//            int n = 0;
//            while (-1 != (n = xzIn.read(buffer))) {
//                out.write(buffer, 0, n);
//            }
//            byte[] result=out.toByteArray();
//            out.close();
//            xzIn.close();
//            return result;
//        } catch (Exception e) {}
//        return null;
//    }


//    /***
//     * 压缩BZip2
//     *
//     * @param data
//     * @return
//     */
//    public static byte[] bZip2(byte[] data) {
//        byte[] b = null;
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
//            bzip2.write(data);
//            bzip2.flush();
//            bzip2.close();
//            b = bos.toByteArray();
//            bos.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return b;
//    }

//    /***
//     * 解压BZip2
//     *
//     * @param data
//     * @return
//     */
//    public static byte[] unBZip2(byte[] data) {
//        byte[] b = null;
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(data);
//            CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
//            byte[] buf = new byte[1024];
//            int num = -1;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
//                baos.write(buf, 0, num);
//            }
//            b = baos.toByteArray();
//            baos.flush();
//            baos.close();
//            bzip2.close();
//            bis.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return b;
//    }

//    /**
//     * 把字节数组转换成16进制字符串
//     *
//     * @param bArray
//     * @return
//     */
//    public static String bytesToHexString(byte[] bArray) {
//        StringBuffer sb = new StringBuffer(bArray.length);
//        String sTemp;
//        for (int i = 0; i < bArray.length; i++) {
//            sTemp = Integer.toHexString(0xFF & bArray[i]);
//            if (sTemp.length() < 2)
//                sb.append(0);
//            sb.append(sTemp.toUpperCase());
//        }
//        return sb.toString();
//    }


//    /**
//     *jzlib 压缩数据
//     *
//     * @param object
//     * @return
//     * @throws IOException
//     */
//    public static byte[] jzlib(byte[] object) {
//        byte[] data = null;
//        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ZOutputStream zOut = new ZOutputStream(out,
//                    JZlib.Z_DEFAULT_COMPRESSION);
//            DataOutputStream objOut = new DataOutputStream(zOut);
//            objOut.write(object);
//            objOut.flush();
//            zOut.close();
//            data = out.toByteArray();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return data;
//    }
//    /**
//     *jzLib压缩的数据
//     *
//     * @param object
//     * @return
//     * @throws IOException
//     */
//    public static byte[] unjzlib(byte[] object) {
//        byte[] data = null;
//        try {
//            ByteArrayInputStream in = new ByteArrayInputStream(object);
//            ZInputStream zIn = new ZInputStream(in);
//            byte[] buf = new byte[1024];
//            int num = -1;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            while ((num = zIn.read(buf, 0, buf.length)) != -1) {
//                baos.write(buf, 0, num);
//            }
//            data = baos.toByteArray();
//            baos.flush();
//            baos.close();
//            zIn.close();
//            in.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return data;
//    }
//    public static void main(String[] args) {
//        String s = "this is a test";
//
//        byte[] b1 = zip(s.getBytes());
//        System.out.println("zip:" + bytesToHexString(b1));
//        byte[] b2 = unZip(b1);
//        System.out.println("unZip:" + new String(b2));
//        byte[] b3 = bZip2(s.getBytes());
//        System.out.println("bZip2:" + bytesToHexString(b3));
//        byte[] b4 = unBZip2(b3);
//        System.out.println("unBZip2:" + new String(b4));
//        byte[] b5 = gZip(s.getBytes());
//        System.out.println("bZip2:" + bytesToHexString(b5));
//        byte[] b6 = unGZip(b5);
//        System.out.println("unBZip2:" + new String(b6));
//        byte[] b7 = jzlib(s.getBytes());
//        System.out.println("jzlib:" + bytesToHexString(b7));
//        byte[] b8 = unjzlib(b7);
//        System.out.println("unjzlib:" + new String(b8));
//    }
//}

}
