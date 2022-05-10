package cn.org.xinke.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author wsh
 * @date 2022/5/9 12:42 下午
 */
public class GetAllFile {
    static ArrayList<String> fileNameList = new ArrayList<String>();
    public static void main(String[] args) {
        String fileDir = "/Users/wsh/Projects/file-system2/target/classes/static/upload/";
        ArrayList<String> allFileName = getAllFileName(fileDir);
        System.out.println("size:" +allFileName.size());
        for (String s : allFileName) {
            System.out.println(s);
        }
    }

    public static ArrayList<String> getAllFileName(String path) {
        boolean flag = false;
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
//              System.out.println("文     件：" + tempList[i]);
                fileNameList.add(tempList[i].toString());
//                fileNameList.add(tempList[i].getName());
            }
            if (tempList[i].isDirectory()) {
//              System.out.println("文件夹：" + tempList[i]);

                getAllFileName(tempList[i].getAbsolutePath());
            }
        }
        return fileNameList;
    }

}
