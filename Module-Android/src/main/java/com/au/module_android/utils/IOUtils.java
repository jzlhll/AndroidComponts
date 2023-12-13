package com.au.module_android.utils;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public final class IOUtils {
    private IOUtils() {}

    /**
     * 获取dir的父路径。带尾巴。
     */
    @NonNull
    public static String getDirParentDir(@NonNull File dir) {
        if (!dir.isDirectory()) throw new IllegalArgumentException("Error call getDirParentDirWithSplit");

        String fullPath = dir.getAbsolutePath();
        if (fullPath.endsWith(File.separator)) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
            int lastIndex = fullPath.lastIndexOf(File.separator);
            return fullPath.substring(0, lastIndex);
        } else {
            int lastIndex1 = fullPath.lastIndexOf(File.separator);
            return fullPath.substring(0, lastIndex1);
        }
    }

    /**
     * 获取文件所在的路径。
     */
    @NonNull
    public static String getFileDir(@NonNull File file) {
        String fullPath = file.getAbsolutePath();
        int lastIndex = fullPath.lastIndexOf(File.separator);
        return fullPath.substring(0, lastIndex);
    }

    /**
     * 获取文件所在的路径的ParentDir。
     */
    @NonNull
    public static String getFileParentDir(@NonNull File file) {
         return getDirParentDir(new File(getFileDir(file)));
    }

    /**
     * 将参数用分隔符/或者\分割拼接。末尾保留/或者\
     */
    @NonNull
    public static String combinePathWithSeparator(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String dir: paths) {
            sb.append(dir).append(File.separatorChar);
        }
        return sb.toString();
    }

    /**
     * 将参数用分隔符/或者\分割拼接。末尾没有/或者\
     */
    @NonNull
    public static String combinePath(String... paths) {
        String r = combinePathWithSeparator(paths);
        return r.substring(0, r.length() - 1);
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean iterateDeleteDir(@NonNull File dir) {
        boolean isHasFailed = false;
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                //递归删除目录中的子目录下
                for (File child : children) {
                    boolean subIsHasFailed = iterateDeleteDir(child);
                    isHasFailed = isHasFailed | subIsHasFailed;
                }
            }
        }
        // 目录此时为空，可以删除
        return !dir.delete() | isHasFailed; //** 千万不能换成||,会短路。为了避免，把delete放前面
    }

    public static boolean createDir(@NonNull String dirs) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.createDirectories(Paths.get(dirs));
                return true;
            } else {
                File dir = new File(dirs);
                if (!dir.exists()) {
                    return dir.mkdirs();
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static String copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp;
            byte[] b = new byte[1024 * 4];
            for (int i = 0; file != null && i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }
                else{
                    temp=new File(oldPath+File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()));
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "ok";
    }

    /**
     * 遍历得到dir下所有的文件。
     */
    public static void getAllFilesInDir(@NonNull Set<File> files, @NonNull File dir) {
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory())
                    getAllFilesInDir(files, f);
                if (f.isFile()) {
                    files.add(f);
                }
            }
        }
    }

    /**
     * 遍历得到dir下所有的文件。并且支持使用2个filter来过滤。
     */
    public static void getAllFilesInDirWithFilter(@NonNull Set<File> files, @NonNull File dir,
                                                  @Nullable IFilter fileFilter,
                                                  @Nullable IFilter dirFilter) {
        File[] fs = dir.listFiles();
        if (fs != null)
            for (File f : fs) {
                if (f.isDirectory()) {
                    if (dirFilter == null || dirFilter.filter(f)) {
                        getAllFilesInDirWithFilter(files, f, fileFilter, dirFilter);
                    }
                } else if (f.isFile()) {
                    if (fileFilter == null || fileFilter.filter(f))
                        files.add(f);
                }
            }
    }

    interface IFilter {
        boolean filter(File f);
    }
}
