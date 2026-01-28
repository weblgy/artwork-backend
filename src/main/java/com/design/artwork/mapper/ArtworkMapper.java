package com.design.artwork.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.design.artwork.entity.Artwork;
import org.apache.ibatis.annotations.Mapper; // 👈 要有这个

@Mapper // 👈 加上这个注解更稳妥
public interface ArtworkMapper extends BaseMapper<Artwork> {
}