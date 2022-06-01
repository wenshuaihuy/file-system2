package cn.org.xinke;

import cn.org.xinke.entity.FileItem;
import cn.org.xinke.service.FileItemService;
import org.apache.tika.Tika;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2019-01-07 下午 2:22.
 */
//@Ignore
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {
    @Autowired
    private FileItemService fileItemService;

    @org.junit.Test
    public void test() throws IOException {
        Tika tika = new Tika();
        String detect = tika.detect(new File("C:\\EasyFS\\2018\\12\\26\\a0e0bffa077b4218a0739433439521e2.jpg"));
        System.out.println(detect);
    }

    @org.junit.Test
    public void findAll() throws IOException {
        List<FileItem> list = fileItemService.list();
        for (FileItem fileItem : list) {
            System.out.println(fileItem);
        }
    }
    @org.junit.Test
    public void add() throws IOException {
       // FileItem fileItem1 = new FileItem("1", "zhangsan", "test123.txt", "财务", "text", "2020");
        //boolean save = fileItemService.save(fileItem1);

    }

    @org.junit.Test
    public void getTime() throws IOException {
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
        System.out.println(dateFormat.format(date));
    }

    @org.junit.Test
    public void tets3() throws IOException {
        String s = "123456";
        System.out.println(s.substring(1));

    }

}
