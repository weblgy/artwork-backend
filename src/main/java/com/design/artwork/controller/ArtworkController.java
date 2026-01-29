package com.design.artwork.controller; // ✅ 已修正包名

import com.design.artwork.entity.Artwork;
import com.design.artwork.mapper.ArtworkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
// ... 记得导入 Files 和 Path 包
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.design.artwork.utils.OssUtil;

@RestController
@RequestMapping("/api/artwork")
@CrossOrigin // 允许跨域

public class ArtworkController {

    @Autowired
    private ArtworkMapper artworkMapper;

    @Autowired
    private OssUtil ossUtil; // 注入 OSS 工具

    /**
     * 上传接口 (OSS 版)
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "title", defaultValue = "未命名") String title) {
        if (file.isEmpty()) return "文件为空";

        try {
            // 1. 直接调用工具类上传，拿到云端 URL
            String ossUrl = ossUtil.uploadFile(file);

            // 2. 存入数据库
            Artwork artwork = new Artwork();
            artwork.setTitle(title);
            artwork.setImageUrl(ossUrl);
            // filePath 现在可以存 URL，或者存 OSS 里的文件名，方便删除
            artwork.setFilePath(ossUrl);
            artwork.setCreateTime(LocalDateTime.now());

            // 注意：因为没有存本地，没法直接读取 width/height
            // 如果非要存宽高，需要先用 ImageIO 读流，比较麻烦，这里暂时设为 0 或由前端处理
            artwork.setWidth(0);
            artwork.setHeight(0);

            artworkMapper.insert(artwork);

            return "上传成功！图片链接: " + ossUrl;

        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败: " + e.getMessage();
        }
    }
    /**
     * 获取画稿列表 (按创建时间倒序排列)
     */
    @GetMapping("/list")
    public List<Artwork> getList() {
        // QueryWrapper 是 MyBatis-Plus 的查询构建器
        QueryWrapper<Artwork> query = new QueryWrapper<>();
        // 按 id 倒序 (或者 create_time 倒序)，这样最新上传的排前面
        query.orderByDesc("id");

        return artworkMapper.selectList(query);
    }
    /**
     * 删除接口 (OSS 版)
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Artwork artwork = artworkMapper.selectById(id);
        if (artwork != null) {
            // 1. 从阿里云删除图片
            ossUtil.deleteFile(artwork.getImageUrl());
            // 2. 从数据库删除记录
            artworkMapper.deleteById(id);
        }
        return "删除成功";
    }
    /**
     * 修改画稿标题
     */
    @PostMapping("/update") // 也可以用 @PutMapping
    public String update(@RequestBody Artwork artwork) {
        // 这里会自动根据 artwork.id 去更新其他字段
        // 因为我们只传了 id 和 title，所以只会更新 title
        // updateById 是 MyBatis-Plus 自带的神技
        artworkMapper.updateById(artwork);
        return "修改成功";
    }
}