package cn.org.xinke.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wsh
 * @date 2022/5/31 3:10 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_fs")
public class FileItem {
    private String id;
    private String authorName;
    private String fileName;
    private String majorName;
    private String fileType;
    private String fileSize;
    private String time;
}
