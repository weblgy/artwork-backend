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

@RestController
@RequestMapping("/api/artwork")
@CrossOrigin // 允许跨域

public class ArtworkController {

    @Autowired
    private ArtworkMapper artworkMapper;

    // ⚠️⚠️ 请务必修改为你电脑上的真实路径！
    // 结尾必须带斜杠 "/"
    private static final String UPLOAD_FOLDER = "D:/MyProject/images/";
    private static final String BASE_URL = "http://localhost:8080/images/";

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "title", defaultValue = "未命名") String title) {
        if (file.isEmpty()) return "文件为空";

        try {
            File dir = new File(UPLOAD_FOLDER);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            String newName = UUID.randomUUID().toString() + suffix;

            File dest = new File(dir, newName);
            file.transferTo(dest);

            BufferedImage img = ImageIO.read(dest);
            int width = img.getWidth();
            int height = img.getHeight();

            Artwork artwork = new Artwork();
            artwork.setTitle(title);
            artwork.setImageUrl(BASE_URL + newName);
            artwork.setFilePath(dest.getAbsolutePath());
            artwork.setWidth(width);
            artwork.setHeight(height);
            artwork.setCreateTime(LocalDateTime.now());

            artworkMapper.insert(artwork);

            return "上传成功！图片链接: " + artwork.getImageUrl();

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
     * 删除画稿
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        // 1. 先查出来，为了获取文件路径
        Artwork artwork = artworkMapper.selectById(id);
        if (artwork == null) {
            return "画稿不存在";
        }

        // 2. 删硬盘上的文件 (这一步可选，但推荐加上)
        try {
            if (artwork.getFilePath() != null) {
                Path path = Paths.get(artwork.getFilePath());
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            e.printStackTrace(); // 文件删失败不影响删数据库，记录日志即可
        }

        // 3. 删数据库
        artworkMapper.deleteById(id);

        return "删除成功";
    }
}