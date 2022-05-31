package cn.org.xinke.service;

import cn.org.xinke.entity.FileItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author wsh
 * @date 2022/5/31 3:42 下午
 */
public interface FileItemService extends IService<FileItem> {

    /**
     * save upload file data
     * @param fileItem
     * @return
     */
    public int saveUploadData(FileItem fileItem);
}
