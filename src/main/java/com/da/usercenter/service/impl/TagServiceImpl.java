package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.Tag;
import com.da.usercenter.service.TagService;
import com.da.usercenter.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-09-17 10:58:13
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




