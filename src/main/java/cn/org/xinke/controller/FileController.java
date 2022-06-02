package cn.org.xinke.controller;

import cn.org.xinke.annotation.Login;
import cn.org.xinke.constant.FileTypeEnum;
import cn.org.xinke.entity.FileItem;
import cn.org.xinke.entity.User;
import cn.org.xinke.service.FileItemService;
import cn.org.xinke.util.CacheUtil;
import cn.org.xinke.util.FileTypeUtil;
import cn.org.xinke.util.GetAllFile;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author cinco
 * @description 文件服务器
 * @date 2019-1-21
 */
@Slf4j
@CrossOrigin
@Controller
public class FileController {

    private static final String SLASH = "/";

    @Value("${fs.dir}")
    private String fileDir;
//    String fileDir = this.getClass().getResource("/static").getPath() + "/upload";

    @Value("${fs.uuidName}")
    private Boolean uuidName;

    @Value("${fs.useSm}")
    private Boolean useSm;

    @Value("${fs.useNginx}")
    private Boolean useNginx;

    @Value("${fs.nginxUrl}")
    private String nginxUrl;

    @Value("${admin.uname}")
    private String uname;

    @Value("${admin.pwd}")
    private String pwd;

    @Value("${domain}")
    private String domain;

    @Autowired
    private FileItemService fileItemService;

    /**
     * 登录页
     *
     * @return
     */
    @RequestMapping("/login")
    public String loginPage() {
        return "login.html";
    }

    /**
     * 登录提交认证
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/auth")
    public String auth(User user, HttpSession session) {
        if (user.getUname().equals(uname) && user.getPwd().equals(pwd)) {
            session.setAttribute("LOGIN_USER", user);
            return "redirect:/";
        }
        return "redirect:/login";
    }

    /**
     * 首页
     *
     * @return
     */
    @Login
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    /**
     * 上传文件
     *
     * @param file   文件
     * @param curPos 上传文件时所处的目录位置
     * @return Map
     */
    @Login
    @ResponseBody
    @PostMapping("/file/upload")
    public Map upload(@RequestParam MultipartFile file, @RequestParam String curPos, @RequestParam String name, @RequestParam String major) {
        log.debug("fileDir0==" + fileDir);
        log.debug("name:" + name);
        log.debug("major:" + major);
//        if (fileDir != null &&fileDir.contains("file:/")) {
//            String[] split = fileDir.split(":/");
//            fileDir = split[1];
//            String[] split1 = fileDir.split("file-system/");
//            fileDir = split1[0] + "file-system/upload/";
//        }
        String os = System.getProperty("os.name");
        log.debug("os.name：" + os);
        boolean mac_os = os.contains("Mac OS");
        if (os.contains("Mac OS")) {
            fileDir = "/Users/Shared/fileSystem/";
        }
        curPos = curPos.substring(1) + SLASH;
        log.debug("fileDir1==" + fileDir);
        log.debug("SLASH==" + SLASH);
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        log.debug("fileDir2==" + fileDir);

        // 文件原始名称
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String prefix = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        // 保存到磁盘
        File outFile;
        String path;
        if (uuidName != null && uuidName) {
            path = curPos + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
            outFile = new File(fileDir + path);

            log.debug("fileDir3==" + fileDir);
            log.debug("path==" + path);
        } else {
            int index = 1;
            path = curPos + originalFileName;
            outFile = new File(fileDir + path);
//            if (!outFile.exists()){
//                try {
//                    outFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            while (outFile.exists()) {
                path = curPos + prefix + "(" + index + ")." + suffix;
                outFile = new File(fileDir + path);
                index++;
            }

            log.debug("fileDi4r==" + fileDir);
            log.debug("path==" + path);
        }
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            file.transferTo(outFile);
            FileItem fileItem = new FileItem();
            fileItem.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            fileItem.setAuthorName(name);
            fileItem.setFileName(originalFileName);
            fileItem.setMajorName(major);
            fileItem.setFileType(file.getContentType());
            fileItem.setFileSize(String.valueOf(file.getSize()));
            fileItem.setFilePath(outFile.getPath());
            fileItem.setTime(getDate());
            fileItemService.save(fileItem);
            Map rs = getRS(200, "上传成功", path);
            //生成缩略图
            if (useSm != null && useSm) {
                // 获取文件类型
                String contentType = null;
                try {
                    contentType = new Tika().detect(outFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (contentType != null && contentType.startsWith("image/")) {
                    File smImg = new File(fileDir + "sm/" + path);
                    if (!smImg.getParentFile().exists()) {
                        smImg.getParentFile().mkdirs();
                    }
                    Thumbnails.of(outFile).scale(1f).outputQuality(0.25f).toFile(smImg);
                    rs.put("smUrl", "sm/" + path);
                }
            }
            return rs;
        } catch (Exception e) {
            log.info(e.getMessage());
            return getRS(500, e.getMessage());
        }
    }

    /**
     * nginx转发
     *
     * @param filePath
     * @return
     */
    private String useNginx(String filePath) {
        if (nginxUrl == null) {
            nginxUrl = SLASH;
        }
        if (!nginxUrl.endsWith(SLASH)) {
            nginxUrl += SLASH;
        }
        String newName;
        try {
            newName = URLEncoder.encode(filePath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            newName = filePath;
        }
        return "redirect:" + nginxUrl + newName;
    }

    /**
     * 获取源文件或者缩略图文件
     *
     * @param p
     * @param download 是否下载
     * @param response
     * @return
     */
    private String getFile(String p, boolean download, HttpServletResponse response) {
        if (useNginx) {
            return useNginx(p);
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        outputFile(fileDir + p, download, response);
        return null;
    }

    /**
     * 查看/下载源文件
     *
     * @param p        文件全路径
     * @param d        是否下载,1-下载
     * @param response
     * @return
     */
    @Login
    @GetMapping("/file")
    public String file(@RequestParam("p") String p,
                       @RequestParam(value = "d", required = true) int d,
                       HttpServletResponse response) {
        return getFile(p, d == 1 ? true : false, response);
    }


    /**
     * 根据文件名字查找文件
     *
     * @param response
     * @return
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/selectFile")
    public Map selectFile(String fileName, String authorName, String majorName, HttpServletResponse response) {
        ArrayList<String> list = new ArrayList<>();
//        ArrayList<String> allFileName = GetAllFile.getAllFileName(fileDir, list);
        Map<String, Object> hashMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();


        if (!"".equals(majorName) && "".equals(authorName) && "".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByMajorName = new QueryWrapper<FileItem>();
            selectFileByMajorName.like("major_name", majorName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByMajorName);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else if (!"".equals(authorName) && "".equals(majorName) && "".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByAuthorName = new QueryWrapper<FileItem>();
            selectFileByAuthorName.like("author_name", authorName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByAuthorName);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else if ("".equals(authorName) && "".equals(majorName) && !"".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByFileName = new QueryWrapper<FileItem>();
            selectFileByFileName.like("file_name", fileName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByFileName);
            for (FileItem fileItem : fileItems) {
                boolean directory = new File(fileItem.getFilePath()).isDirectory();
                if (directory) {
                    dataList.add(findDirWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
                } else {
                    dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
                }
            }
        } else if ("".equals(authorName) && !"".equals(majorName) && !"".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByMajorNameAndFileName = new QueryWrapper<>();
            selectFileByMajorNameAndFileName.like("major_name", majorName)
                    .like("file_name", fileName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByMajorNameAndFileName);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else if (!"".equals(authorName) && "".equals(majorName) && !"".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByAuthorNameAndFileName = new QueryWrapper<>();
            selectFileByAuthorNameAndFileName.like("author_name", authorName)
                    .like("file_name", fileName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByAuthorNameAndFileName);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else if (!"".equals(authorName) && !"".equals(majorName) && "".equals(fileName)) {
            QueryWrapper<FileItem> selectFileByAuthorNameAndmajorName = new QueryWrapper<>();
            selectFileByAuthorNameAndmajorName.like("author_name", authorName)
                    .like("major_name", majorName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFileByAuthorNameAndmajorName);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else if (!"".equals(authorName) && !"".equals(majorName) && !"".equals(fileName)) {
            QueryWrapper<FileItem> selectFile = new QueryWrapper<>();
            selectFile.like("author_name", authorName)
                    .like("major_name", majorName)
                    .like("file_name", fileName);
            List<FileItem> fileItems = fileItemService.getBaseMapper().selectList(selectFile);
            for (FileItem fileItem : fileItems) {
                dataList.add(findFileWithFileName(fileItem.getFileName(), fileItem.getFilePath()));
            }
        } else {
            log.info("没有查询条件");
        }
        hashMap.put("code", 200);
        hashMap.put("msg", "查询成功");
        hashMap.put("data", dataList);
        return hashMap;
    }

    /**
     * 返回分享源文件或其缩略图页面或文件
     *
     * @param sid
     * @param download 是否下载
     * @param modelMap
     * @param response
     * @return
     */
    private String returnShareFileOrSm(String sid, boolean download, ModelMap modelMap, HttpServletResponse response) {
        String url = null;
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    url = (String) CacheUtil.get(sid);
                    // 文件是否存在
                    File existFile = new File(fileDir + url);
                    if (!existFile.exists()) {
                        modelMap.put("msg", "该文件已不存在~");
                        return "error.html";
                    }
                } else {
                    modelMap.put("msg", "分享文件已过期");
                    return "error.html";
                }
            } else {
                modelMap.put("msg", "无效的sid");
                return "error.html";
            }
        }
        return getFile(url, download, response);
    }

    /**
     * 查看/下载分享的源文件
     *
     * @param sid      分享sid
     * @param response
     * @return
     */
    @GetMapping("/share/file")
    public String shareFile(@RequestParam(value = "sid", required = true) String sid,
                            @RequestParam(value = "d", required = true) int d,
                            HttpServletResponse response,
                            ModelMap modelMap) {
        return returnShareFileOrSm(sid, d == 1 ? true : false, modelMap, response);
    }

    /**
     * 分享源文件的缩略图
     *
     * @param sid      分享sid
     * @param response
     * @return
     */
    @GetMapping("/share/file/sm")
    public String shareFileSm(@RequestParam(value = "sid", required = true) String sid,
                              HttpServletResponse response,
                              ModelMap modelMap) {
        return returnShareFileOrSm(sid, false, modelMap, response);
    }

    /**
     * 查看缩略图
     *
     * @param p        文件全名
     * @param response
     * @return
     */
    @Login
    @GetMapping("/file/sm")
    public String fileSm(@RequestParam("p") String p, HttpServletResponse response) {
        return getFile(p, false, response);
    }

    /**
     * 输出文件流
     *
     * @param file
     * @param download 是否下载
     * @param response
     */
    private void outputFile(String file, boolean download, HttpServletResponse response) {
        // 判断文件是否存在
        File inFile = new File(file);
        // 文件不存在
        if (!inFile.exists()) {
            PrintWriter writer = null;
            try {
                response.setContentType("text/html;charset=UTF-8");
                writer = response.getWriter();
                writer.write("<!doctype html><title>404 Not Found</title><link rel=\"shorcut icon\" href=\"assets/images/logo.png\"><h1 style=\"text-align: center\">404 Not Found</h1><hr/><p style=\"text-align: center\">FMS Server</p>");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        // 获取文件类型
        String contentType = null;
        try {
            contentType = new Tika().detect(inFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 图片、文本文件,则在线查看
        log.info("文件类型：" + contentType);
        if (FileTypeUtil.canOnlinePreview(contentType) && !download) {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
        } else {
            // 其他文件,强制下载
            response.setContentType("application/force-download");
            String newName;
            try {
                newName = URLEncoder.encode(inFile.getName(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                newName = inFile.getName();
            }
            response.setHeader("Content-Disposition", "attachment;fileName=" + newName);
        }
        // 输出文件流
        OutputStream os = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(inFile);
            os = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件类型
     *
     * @param suffix
     * @param contentType
     * @return
     */
    private String getFileType(String suffix, String contentType) {
        String type;
        if (FileTypeEnum.PPT.getName().equalsIgnoreCase(suffix) || FileTypeEnum.PPTX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PPT.getName();
        } else if (FileTypeEnum.DOC.getName().equalsIgnoreCase(suffix) || FileTypeEnum.DOCX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.DOC.getName();
        } else if (FileTypeEnum.XLS.getName().equalsIgnoreCase(suffix) || FileTypeEnum.XLSX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.XLS.getName();
        } else if (FileTypeEnum.PDF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PDF.getName();
        } else if (FileTypeEnum.HTML.getName().equalsIgnoreCase(suffix) || FileTypeEnum.HTM.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.HTM.getName();
        } else if (FileTypeEnum.TXT.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.TXT.getName();
        } else if (FileTypeEnum.SWF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.FLASH.getName();
        } else if (FileTypeEnum.ZIP.getName().equalsIgnoreCase(suffix) || FileTypeEnum.RAR.getName().equalsIgnoreCase(suffix) || FileTypeEnum.SEVENZ.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.ZIP.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.AUDIO.getName() + SLASH)) {
            type = FileTypeEnum.MP3.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.VIDEO.getName() + SLASH)) {
            type = FileTypeEnum.MP4.getName();
        } else {
            type = FileTypeEnum.FILE.getName();
        }
        return type;
    }

    /**
     * 获取全部文件
     *
     * @param dir
     * @param accept
     * @param exts
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/list")
    public Map list(String dir, String accept, String exts) {
        log.info("dir:" + dir);
        log.info("accept:" + accept);
        log.info("exts:" + accept);

        String os = System.getProperty("os.name");
        log.debug("os.name：" + os);
        boolean mac_os = os.contains("Mac OS");
        if (os.contains("Mac OS")) {
            fileDir = "/Users/Shared/fileSystem/";
        }

        String[] mExts = null;
        if (exts != null && !exts.trim().isEmpty()) {
            mExts = exts.split(",");
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        Map<String, Object> rs = new HashMap<>();
        if (dir == null || SLASH.equals(dir)) {
            dir = "";
        } else if (dir.startsWith(SLASH)) {
            dir = dir.substring(1);
        }
        File file = new File(fileDir + dir);
        File[] listFiles = file.listFiles();
        List<Map> dataList = new ArrayList<>();
        if (listFiles != null) {
            for (File f : listFiles) {
                if ("sm".equals(f.getName())) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>(0);
                // 文件名称
                m.put("name", f.getName());
                // 修改时间
                m.put("updateFileTime", getDate());
                m.put("updateTime", f.lastModified());
                // 是否是目录
                m.put("isDir", f.isDirectory());
                //作者名称
                QueryWrapper<FileItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("file_name", f.getName());
                FileItem fileItem = fileItemService.getBaseMapper().selectOne(queryWrapper);
                if (fileItem == null) {
                    m.put("authorName", "");
                    //专业名称
                    m.put("majorName", "");
                } else {
                    m.put("authorName", fileItem.getAuthorName());
                    //专业名称
                    m.put("majorName", fileItem.getMajorName());
                }

                if (f.isDirectory()) {
                    // 文件类型
                    m.put("type", "dir");
                } else {
                    // 是否支持在线查看
                    boolean flag = false;
                    try {
                        if (FileTypeUtil.canOnlinePreview(new Tika().detect(f))) {
                            flag = true;
                        }
                        m.put("preview", flag);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String type;
                    // 文件地址
                    m.put("url", (dir.isEmpty() ? dir : (dir + SLASH)) + f.getName());
                    // 获取文件类型
                    String contentType = null;
                    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    try {
                        contentType = new Tika().detect(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 筛选文件类型
                    if (accept != null && !accept.trim().isEmpty() && !accept.equals("file")) {
                        if (contentType == null || !contentType.startsWith(accept + SLASH)) {
                            continue;
                        }
                        if (mExts != null) {
                            for (String ext : mExts) {
                                if (!f.getName().endsWith("." + ext)) {
                                    continue;
                                }
                            }
                        }
                    }
                    // 获取文件图标
                    m.put("type", getFileType(suffix, contentType));
                    // 是否有缩略图
                    String smUrl = "sm/" + (dir.isEmpty() ? dir : (dir + SLASH)) + f.getName();
                    if (new File(fileDir + smUrl).exists()) {
                        m.put("hasSm", true);
                        // 缩略图地址
                        m.put("smUrl", smUrl);
                    }
                }
                dataList.add(m);
            }
        }
        // 根据上传时间排序
        Collections.sort(dataList, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                Long l1 = (long) o1.get("updateTime");
                Long l2 = (long) o2.get("updateTime");
                return l1.compareTo(l2);
            }
        });
        // 把文件夹排在前面
        Collections.sort(dataList, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                Boolean l1 = (boolean) o1.get("isDir");
                Boolean l2 = (boolean) o2.get("isDir");
                return l2.compareTo(l1);
            }
        });
        rs.put("code", 200);
        rs.put("msg", "查询成功");
        rs.put("data", dataList);
        return rs;
    }

    /**
     * 递归删除目录下的文件以及目录
     *
     * @param file
     * @return
     */
    public boolean forDelFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                QueryWrapper<FileItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("file_name", f.getName());
                fileItemService.getBaseMapper().delete(queryWrapper);
                forDelFile(f);
            }
        }
        return file.delete();
    }

    /**
     * 删除
     *
     * @param file
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/del")
    public Map del(String file) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (file != null && !file.isEmpty()) {
            File f = new File(fileDir + file);
            File smF = new File(fileDir + "sm/" + file);
            if (f.exists()) {
                // 文件
                if (f.isFile()) {
                    if (f.delete()) {
                        if (smF.exists() && smF.isFile()) {
                            smF.delete();
                        }
                        QueryWrapper<FileItem> wrapper = new QueryWrapper<>();
                        wrapper.eq("file_name", file);
                        fileItemService.getBaseMapper().delete(wrapper);
                        return getRS(200, "文件删除成功");
                    }
                } else {
                    // 目录
                    QueryWrapper<FileItem> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("file_name", file);
                    fileItemService.getBaseMapper().delete(queryWrapper);
                    forDelFile(f);
                    if (smF.exists() && smF.isDirectory()) {
                        forDelFile(smF);
                    }
                    return getRS(200, "目录删除成功");
                }
            } else {
                return getRS(500, "文件或目录不存在");
            }
        }
        return getRS(500, "文件或目录删除失败");
    }

    /**
     * 重命名
     *
     * @param oldFile
     * @param newFile
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/rename")
    public Map rename(String oldFile, String newFile) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (!StringUtils.isEmpty(oldFile) && !StringUtils.isEmpty(newFile)) {
            File f = new File(fileDir + oldFile);
            File smF = new File(fileDir + "sm/" + oldFile);
            File nFile = new File(fileDir + newFile);
            File nsmFile = new File(fileDir + "sm/" + newFile);
            if (f.renameTo(nFile)) {
                if (smF.exists()) {
                    smF.renameTo(nsmFile);
                }
                QueryWrapper<FileItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("file_name", oldFile);
                FileItem fileItem = fileItemService.getBaseMapper().selectOne(queryWrapper);
                fileItem.setFileName(newFile);
                QueryWrapper<FileItem> upDataFileName = new QueryWrapper<>();
                upDataFileName.eq("file_name", oldFile);

                fileItemService.getBaseMapper().update(fileItem, upDataFileName);
                return getRS(200, "重命名成功", SLASH + newFile);
            }
        }
        return getRS(500, "重命名失败");
    }

    /**
     * 获取当前日期
     */
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @param url
     * @return Map
     */
    private Map getRS(int code, String msg, String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        if (url != null) {
            map.put("url", url);
        }
        return map;
    }

    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @return Map
     */
    private Map getRS(int code, String msg) {
        return getRS(code, msg, null);
    }

    /**
     * 新建文件夹
     *
     * @param curPos
     * @param dirName
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/mkdir")
    public Map mkdir(String curPos, String dirName) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (!StringUtils.isEmpty(curPos) && !StringUtils.isEmpty(dirName)) {
            curPos = curPos.substring(1);
            String dirPath = fileDir + curPos + SLASH + dirName;
            File f = new File(dirPath);
            if (f.exists()) {
                return getRS(500, "目录已存在");
            }
            if (!f.exists() && f.mkdir()) {
                FileItem fileItem = new FileItem();
                fileItem.setFileName(dirName);
                fileItem.setFilePath(dirPath);
                fileItem.setTime(getDate());
                fileItem.setFileType("dir");
                fileItemService.save(fileItem);
                return getRS(200, "创建成功");
            }
        }
        return getRS(500, "创建失败");
    }

    /**
     * 分享文件
     *
     * @param file 文件
     * @param time 有效时间(分钟)
     * @return Map
     */
    @Login
    @ResponseBody
    @PostMapping("/api/share")
    public Map share(String file, int time) {
        // 若文件已经分享
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsValue(file)) {
                Set<String> set = CacheUtil.dataExpireMap.keySet();
                // 找出分享的key
                String key = null;
                for (String t : set) {
                    if (CacheUtil.get(t) != null && CacheUtil.get(t).equals(file)) {
                        key = t;
                        break;
                    }
                }
                // 是否在有效期内
                if (key != null) {
                    Date expireDate = CacheUtil.dataExpireMap.get(key);
                    if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                        return getRS(200, "该文件已分享", domain + SLASH + "share?sid=" + key);
                    }
                }
            }
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        String sid = UUID.randomUUID().toString();
        CacheUtil.put(sid, file, time);
        return getRS(200, "分享成功", domain + SLASH + "share?sid=" + sid);
    }

    /**
     * 分享文件展示页面
     *
     * @param sid      分享文件sid
     * @param modelMap
     * @return
     */
    @GetMapping("/share")
    public String sharePage(@RequestParam(value = "sid", required = true) String sid, ModelMap modelMap) {
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    String url = (String) CacheUtil.get(sid);
                    // 文件是否存在
                    File existFile = new File(fileDir + url);
                    if (!existFile.exists()) {
                        modelMap.put("exists", false);
                        modelMap.put("msg", "该文件已不存在~");
                        return "share";
                    }
                    // 检测文件类型
                    String contentType = null;
                    String suffix = existFile.getName().substring(existFile.getName().lastIndexOf(".") + 1);
                    try {
                        contentType = new Tika().detect(existFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 获取文件图标、文件名、图片文件缩略图片地址、过期时间
                    modelMap.put("sid", sid);
                    modelMap.put("type", getFileType(suffix, contentType));
                    modelMap.put("exists", true);
                    modelMap.put("fileName", url.substring(url.lastIndexOf('/') + 1));
                    // 是否有缩略图
                    String smUrl = "sm/" + url;
                    if (new File(fileDir + smUrl).exists()) {
                        modelMap.put("hasSm", true);
                        // 缩略图地址
                        modelMap.put("smUrl", "share/file/sm?sid=" + sid);
                    }
                    modelMap.put("expireTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(CacheUtil.dataExpireMap.get(sid)));
                    // 是否支持浏览器在线查看
                    boolean flag = false;
                    if (FileTypeUtil.canOnlinePreview(contentType)) {
                        flag = true;
                    }
                    modelMap.put("preview", flag);
                    return "share";
                }
            }
        }
        modelMap.put("exists", false);
        modelMap.put("msg", "分享不存在或已经失效~");
        return "share";
    }


    public Map findFileWithFileName(String fileName, String filePath) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> m = new HashMap<>(0);
        log.info("fileName = " + fileName);
        log.info("filePath = " + filePath);

        File f = new File(filePath);
        //Map<String, Object> m = new HashMap<>(0);
        // 文件名称
        m.put("name", f.getName());
        // 修改时间
        m.put("updateFileTime", getDate());
        m.put("updateTime", f.lastModified());
        // 是否是目录
        m.put("isDir", f.isDirectory());
        if (f.isDirectory()) {
            // 文件类型
            m.put("type", "dir");
        } else {
            // 是否支持在线查看
            boolean flag = false;
            try {
                String detect = new Tika().detect(f);
                if (FileTypeUtil.canOnlinePreview(detect)) {
                    flag = true;
                }
                m.put("preview", flag);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String type;
            // 文件地址
            m.put("url", filePath.split(fileDir)[1]);
            // 获取文件类型
            String contentType = null;
            String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
            try {
                contentType = new Tika().detect(f);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 获取文件图标
            m.put("type", getFileType(suffix, contentType));
            // 是否有缩略图
            String smUrl = "sm/" + ("/".isEmpty() ? "/" : ("/" + SLASH)) + f.getName();
            if (new File(fileDir + smUrl).exists()) {
                m.put("hasSm", true);
                // 缩略图地址
                m.put("smUrl", smUrl);
            }
        }
        return m;
    }

    public Map findDirWithFileName(String fileName, String filePath) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> allFileName = GetAllFile.getAllFileName(fileDir, list);
        Map<String, Object> m = new HashMap<>(0);
//        File file = new File(fileName);
//        String name = file.getName();
        log.info("filename==" + fileName);
        for (String sFileName : allFileName) {
            if (sFileName.contains(fileName) && new File(sFileName).isDirectory()) {
                log.info("isDirectory = " + fileName);
                File f = new File(sFileName);
                //Map<String, Object> m = new HashMap<>(0);
                // 文件名称
                m.put("name", f.getName());
                // 修改时间
                m.put("updateFileTime", getDate());
                m.put("updateTime", f.lastModified());
                // 是否是目录
                m.put("isDir", f.isDirectory());
                if (f.isDirectory()) {
                    // 文件类型
                    m.put("type", "dir");
                    m.put("url", filePath.split(fileDir)[1]);
                } else {
                    // 是否支持在线查看
                    boolean flag = false;
                    try {
                        if (FileTypeUtil.canOnlinePreview(new Tika().detect(f))) {
                            flag = true;
                        }
                        m.put("preview", flag);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String type;
                    // 文件地址
                    m.put("url", filePath.split(fileDir)[1]);
                    // 获取文件类型
                    String contentType = null;
                    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    try {
                        contentType = new Tika().detect(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 获取文件图标
                    m.put("type", getFileType(suffix, contentType));
                    // 是否有缩略图
                    String smUrl = "sm/" + ("/".isEmpty() ? "/" : ("/" + SLASH)) + f.getName();
                    if (new File(fileDir + smUrl).exists()) {
                        m.put("hasSm", true);
                        // 缩略图地址
                        m.put("smUrl", smUrl);
                    }
                }
            }
        }
        return m;
    }

}
