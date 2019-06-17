
package com.bingduoduo.editor.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static int length;

    /**
     * 递归删除文件夹
     */
    public static boolean deleteDir(@NonNull File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if (dir == null) {
            return false;
        }
        return dir.delete();
    }

    /**
     * 获取文件目录
     *
     * @param context the mcontext
     * @return file
     */
    public static String getFile(@NonNull Context context) {
        File savedir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            savedir = context.getFilesDir();
            android.util.Log.d("filedir1", "getFile: "+savedir.getAbsolutePath());
        }

        if (savedir == null) {
            savedir = context.getFilesDir();
            android.util.Log.d("filedir2", "getFile: "+savedir.getAbsolutePath());
        }

        if (!savedir.exists()) {
            savedir.mkdirs();
        }
        android.util.Log.d("filedir", "getFile: "+savedir.getAbsolutePath());
        return savedir.getAbsolutePath() + "/home";
    }


    /**
     * 写字节
     * Write byte.
     *
     * @param file    the file
     * @param content the content
     * @throws IOException the io exception
     */
    public static boolean writeByte(@NonNull File file, @NonNull String content) {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] b = content.getBytes();
            out.write(b);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            CloseableClose(out);
        }
    }
    /**
     * 读取文件，一次性读取
     * Read file string.
     *
     * @param file the file
     * @return the string
     */
    public static String readFile(@NonNull File file) {
        if (!file.isFile()) {
            return "";
        }
        Long filelength = file.length();     //获取文件长度
        if (filelength > Integer.MAX_VALUE) {
            return readFileByLines(file);
        }
        byte[] filecontent = new byte[filelength.intValue()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        } finally {
            CloseableClose(in);
        }
        return new String(filecontent);
    }

    /**
     * 按行读取
     * Read file by lines string.
     *
     * @param file the file
     * @return the string
     */
    public static String readFileByLines(@NonNull File file) {
        if (!file.isFile()) {
            return "";
        }
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                builder.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        } finally {
            CloseableClose(reader);
        }

        return builder.toString();
    }

    /**
     * 复制文件
     * Copy file boolean.
     *
     * @param sourceFile the source file
     * @param targetFile the target file
     * @return the boolean
     */
    private static boolean copyFile(@NonNull File sourceFile, @NonNull File targetFile) {
        if (!sourceFile.exists() || targetFile.exists()) {
            //原始文件不存在，目标文件已经存在
            return false;
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(targetFile);
            int temp;
            while ((temp = input.read()) != (-1)) {
                output.write(temp);
            }
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
        } finally {
            CloseableClose(input);
            CloseableClose(output);
        }
        return true;
    }

    public static boolean copyFolder(@NonNull String oldPath, @NonNull String newPath) {
        return copyFolder(new File(oldPath), new File(newPath));
    }

    /**
     * 复制整个文件夹
     * Copy folder.
     *
     * @param oldFile the old path
     * @param newPath the new path
     */
    public static boolean copyFolder(@NonNull File oldFile, @NonNull File newPath) {
        if (oldFile.isFile())//如果是文件，直接复制
            return copyFile(oldFile, new File(newPath, oldFile.getName()));
        try {//文件夹
            newPath.mkdirs(); //如果文件夹不存在 则建立新文件夹
            File[] temps = oldFile.listFiles();
            File temp;
            boolean flag = true;
            length = temps.length;
            for (int i = 0; i < length; i++) {
                temp = temps[i];
                //文件夹里面
                if (temp.isFile()) {
                    File path = new File(newPath, oldFile.getName());
                    path.mkdirs();
                    File file = new File(path, temp.getName());
                    flag = copyFile(temp, file);
                } else if (temp.isDirectory()) {//如果是子文件夹
                    flag = copyFolder(temp, new File(newPath + File.separator + oldFile.getName()));
                }

                if (!flag) {
                    break;
                }
            }
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 移动文件到指定目录
     *
     * @param oldPath String
     * @param newPath String
     */
    public static boolean moveFolder(@NonNull String oldPath, @NonNull String newPath) {
        return moveFolder(new File(oldPath), new File(newPath));
    }

    /**
     * 移动文件夹
     * Move folder.
     *
     * @param oldFile the old path
     * @param newPath the new path
     */
    public static boolean moveFolder(@NonNull File oldFile, File newPath) {
        return copyFolder(oldFile, newPath) && deleteFile(oldFile);
    }

    /**
     * 删除文件
     * Delete file boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean deleteFile(File file) {
        return deleteDir(file);
    }

    public static void CloseableClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();} catch (IOException e) { }
        }
    }


    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String uri2FilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
