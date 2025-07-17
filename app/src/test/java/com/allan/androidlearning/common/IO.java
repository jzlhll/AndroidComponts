package com.allan.androidlearning.common;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class IO {
    public final static boolean IS_WIN = System.getProperty("os.name").toLowerCase().contains("win");
    public final static boolean IS_OSX = System.getProperty("os.name").toLowerCase().contains("mac");
    public final static boolean IS_LINUX = !IS_WIN && !IS_OSX;

    final static String[] SystemEnvp = new String[] {"PATH=/opt/homebrew/bin:/opt/homebrew/sbin:/Users/allan/Library/Android/sdk/platform-tools:/Users/Allan/Documents/Tools/ffmpeg-4.3.1-macos64-static/bin:/Users/allan/bin:/Users/allan/Documents/jdk1.8.0.322aarch64_zulu/zulu-8.jdk/Contents/Home/bin:/opt/homebrew/opt/grep/libexec/gnubin:/opt/homebrew/opt/gnu-tar/libexec/gnubin:/opt/homebrew/opt/coreutils/libexec/gnubin:/opt/homebrew/opt/gnu-sed/libexec/gnubin:/opt/homebrew/opt/findutils/libexec/gnubin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Library/Apple/usr/bin"};

    static void currentDirPrint() {
        String s = IS_WIN ? run("chdir") : run("pwd");
        System.out.println("当前的目录为: " + s);
    }

    static String getDirParentDirWithIncline(File file) {
        String fullPath = file.getAbsolutePath();
        if (fullPath.endsWith("\\")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
            int lastIndex = fullPath.lastIndexOf("\\");
            return fullPath.substring(0, lastIndex);
        } else if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
            int lastIndex = fullPath.lastIndexOf("/");
            return fullPath.substring(0, lastIndex);
        } else {
            int lastIndex1 = fullPath.lastIndexOf("/");
            int lastIndex2 = fullPath.lastIndexOf("\\");
            int lastIndex = Math.max(lastIndex1, lastIndex2);
            return fullPath.substring(0, lastIndex);
        }
    }

    static String getFileParentDirWithIncline(File file) {
        String fullPath = file.getAbsolutePath();
        int lastIndex1 = fullPath.lastIndexOf("/");
        int lastIndex2 = fullPath.lastIndexOf("\\");
        int lastIndex = Math.max(lastIndex1, lastIndex2);
        String thisDir = fullPath.substring(0, lastIndex);
        return getDirParentDirWithIncline(new File(thisDir));
    }

    /**
     * 将参数用分隔符/或者\分割拼接。末尾没有/或者\
     */
    static String combinePath(String... paths) {
        String r = combinePathWithInclineEnd(paths);
        return r.substring(0, r.length() - 1);
    }

    /**
     * 将参数用分隔符/或者\分割拼接。末尾保留/或者\
     */
    static String combinePathWithInclineEnd(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String dir: paths) {
            sb.append(dir).append(File.separatorChar);
        }
        return sb.toString();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDirJava(File dir) {
        boolean isHasFailed = false;
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                //递归删除目录中的子目录下
                for (File child : children) {
                    boolean subIsHasFailed = deleteDirJava(child);
                    isHasFailed = isHasFailed | subIsHasFailed;
                }
            }
        }
        // 目录此时为空，可以删除
        return !dir.delete() | isHasFailed; //** 千万不能换成||,会短路。为了避免，把delete放前面
    }

    /**
     * @param dir 文件夹路径
     */
    public static void deleteDir(String dir) {
        //用了很多的java方法都删除不干净。算了。采取命令吧。
        String print;
        if (IS_WIN) {
            print = run("rd /s /q " + dir);
        } else {
            print = run("rm -rf " + dir);
        }

        if (print == null) {
            throw new RuntimeException("delete dir error!");
        }
    }

    static void createDir(String dirs) {
        try {
            Files.createDirectories(Paths.get(dirs));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void copyFile(String srcFile, String targetPath) {
        String print;
        if (IS_WIN) {
            print = run("copy " + srcFile + " " + targetPath);
        } else {
            print = run("cp " + srcFile + " " + targetPath);
        }

        if (print == null) {
            throw new RuntimeException("cp error");
        }
    }

    /**
     * 注意是拷贝目录
     */
    static void copyDir(String srcDir, String targetParentPath) {
        String print;
        if (IS_WIN) {
            int lastIndex = srcDir.lastIndexOf('\\');
            String name = srcDir.substring(lastIndex + 1);
            print = copyFolder(srcDir, targetParentPath + "\\" + name);
        } else {
            print = run("cp -rf " + srcDir + " " + targetParentPath);
        }

        if (print == null) {
            throw new RuntimeException("cp -rf error");
        }
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
            File temp=null;
            for (int i = 0; i < file.length; i++) {
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
                    byte[] b = new byte[1024 * 5];
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

    public static List<String> runBig(String command) {
        return runBig(command, false);
    }

    public static List<String> runBig(String command, boolean printLog) {
        System.out.println("[cmd]: " + command);
        String[] cmdArr = new String[3];
        cmdArr[0] = IS_WIN ? "cmd" : "/bin/sh";
        cmdArr[1] = IS_WIN ? "/c" : "-c";
        cmdArr[2] = command;

        final List<String> results = new ArrayList<>();
        final Process process;
        final BufferedReader bufrIn;
        final BufferedReader bufrError;
        // 执行命令, 返回一个子进程对象（命令在子进程中执行
        try {
            process = Runtime.getRuntime().exec(cmdArr, SystemEnvp);
            // 方法阻塞, 等待命令执行完成（成功会返回0）
            var cs = IO.IS_WIN ? Charset.forName("GBK") : StandardCharsets.UTF_8;
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), cs));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), cs));
            new Thread(()-> {
                // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
                // 读取输出
                String line;
                try {
                    while ((line = bufrIn.readLine()) != null) {
                        if(printLog) System.out.println("[cmd]: " + line);
                        results.add(line);
                    }
                    while ((line = bufrError.readLine()) != null) {
                        if(printLog) System.out.println("[cmd]: " + line);
                        results.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("似乎读取有点小问题了。");
                } finally {
                    closeStream(bufrError);
                    closeStream(bufrIn);
                    // 销毁子进程
                    process.destroy();
                }
            }).start();
            int status = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            //result.setLength(0);
        }
        return results;
    }

    //    var list2= requiresCount(
//            MainShConfig.jdeps
//                    + " --module-path BaseParty/target/classes/:abuild/ target/classes | grep requires");
//        assert list2 != null;
    public static String run(String command) {
        System.out.println("[cmd]: " + command);
        String[] cmdArr = new String[3];
        cmdArr[0] = IS_WIN ? "cmd" : "/bin/sh";
        cmdArr[1] = IS_WIN ? "/c" : "-c";
        cmdArr[2] = command;

        StringBuilder result = new StringBuilder();
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        // 执行命令, 返回一个子进程对象（命令在子进程中执行
        try {
            process = Runtime.getRuntime().exec(cmdArr, null);
            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();
            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            // 读取输出
            String line;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);
            // 销毁子进程
            if(process != null) process.destroy();
        }
        return result == null ? null : result.toString();
    }

    /**
     * 关闭流
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void getAllFilesInDir(Set<File> files, File dir) {
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

    public static void getAllFilesInDirWithFilter(Set<File> files, File dir, IFileFilter fileFilter, IDirFilter dirFilter) {
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

    public static String getParentPath(String path, boolean hasSeparator) {
        if (path.length() == 1) {
            return path;
        }
        char split;
        //由于jar可能存在window路径变成linux路径。故而采取复杂一点的逻辑
        if (path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
            split = '\\';
        } else if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            split = '/';
        } else {
            int index = path.lastIndexOf("/");
            int index2 = path.lastIndexOf("\\");
            if (index2 >= index) {
                split = '\\';
            } else {
                split = '/';
            }

            if (index == -1 && index2 == -1) {
                split = File.separatorChar;
            }
        }

        int index = path.lastIndexOf(split);
        if (hasSeparator) {
            return path.substring(0, index + 1);
        } else {
            return path.substring(0, Math.max(index, 1));
        }
    }

    public static void main(String[] args) {
        System.out.println(getParentPath("\\d:\\ddaf", true));
    }

    public interface IDirFilter {
        boolean filter(File f);
    }

    public interface IFileFilter {
        boolean filter(File f);
    }
}
