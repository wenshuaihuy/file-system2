package cn.org.xinke.service.impl;

import cn.org.xinke.entity.FileItem;
import cn.org.xinke.mapper.FileItemMapper;
import cn.org.xinke.service.FileItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author wsh
 * @date 2022/5/31 3:47 下午
 */
@Service
public class FileItemServiceImpl extends ServiceImpl<FileItemMapper, FileItem> implements FileItemService {

    @Override
    public int saveUploadData(FileItem fileItem) {
        FileItem fileItem1 = new FileItem();
        return 0;
    }


}
