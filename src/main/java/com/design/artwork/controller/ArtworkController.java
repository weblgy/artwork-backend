package com.design.artwork.controller; // ✅ 已修正包名

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.design.artwork.entity.Artwork;
import com.design.artwork.mapper.ArtworkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import com.design.artwork.utils.OssUtil;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artwork")
@CrossOrigin // 允许跨域

public class ArtworkController {

    @Autowired
    private ArtworkMapper artworkMapper;

    @Autowired
    private OssUtil ossUtil; // 注入 OSS 工具

    /**
     * 1. 上传接口 (接收 category 参数)
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "title", defaultValue = "未命名") String title,
                         @RequestParam(value = "category", defaultValue = "默认") String category) { // 👈 新增参数
        if (file.isEmpty()) return "文件为空";

        try {
            // 上传到 OSS
            String ossUrl = ossUtil.uploadFile(file);

            // 存入数据库
            Artwork artwork = new Artwork();
            artwork.setTitle(title);
            artwork.setCategory(category); // 👈 保存分类
            artwork.setImageUrl(ossUrl);
            artwork.setFilePath(ossUrl);
            artwork.setCreateTime(LocalDateTime.now());

            // 宽高暂时设为0，如果需要可以去读图片流
            artwork.setWidth(0);
            artwork.setHeight(0);

            artworkMapper.insert(artwork);

            return "上传成功";

        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败: " + e.getMessage();
        }
    }
    /**
     * 2. 获取列表接口 (支持按 category 筛选)
     */
    @GetMapping("/list")
    public List<Artwork> getList(@RequestParam(required = false) String title,
                                 @RequestParam(required = false) String category) { // 👈 新增参数
        LambdaQueryWrapper<Artwork> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询标题
        if (title != null && !title.isEmpty()) {
            wrapper.like(Artwork::getTitle, title);
        }

        // 精确查询分类 (如果传了 category 且不是 "全部")
        if (category != null && !category.isEmpty() && !"全部".equals(category)) {
            wrapper.eq(Artwork::getCategory, category);
        }

        wrapper.orderByDesc(Artwork::getCreateTime);
        return artworkMapper.selectList(wrapper);
    }
    /**
     * 3. 获取所有分类接口 (新增方法)
     * 用于前端下拉框和 Tab 栏的显示
     */
    @GetMapping("/categories")
    public List<String> getCategories() {
        // 查询所有数据
        List<Artwork> list = artworkMapper.selectList(null);

        // 使用 Stream 流提取 category 字段并去重
        return list.stream()
                .map(Artwork::getCategory) // 取出分类
                .filter(c -> c != null && !c.isEmpty()) // 排除空值
                .distinct() // 去重
                .collect(Collectors.toList());
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