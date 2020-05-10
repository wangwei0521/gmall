package com.xxxzz.gmall.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {

    @Value("${fileServer.url}")
    private String fileUrl;

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String imgUrl=fileUrl;
        if(file!=null){
            System.out.println("multipartFile = " + file.getName()+"|"+file.getSize());
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            //获取连接
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            //获取文件的名称
            String fileName= file.getOriginalFilename();
            //获取文件的后缀名
            String extName = StringUtils.substringAfterLast(fileName, ".");
            //上传图片
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                /**
                 * s = group1
                 * s = M00/00/00/wKhAZV5TpWOAOmQaAADPpElGcsQ108.jpg
                 */
                imgUrl+="/"+path;
            }
        }
        //return "http://192.168.64.101/group1/M00/00/00/wKhAZV5TpQGABQFzAABXevsTCEo834.jpg";
        return imgUrl;
    }
}
