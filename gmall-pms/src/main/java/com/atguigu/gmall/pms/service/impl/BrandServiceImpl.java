package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.mapper.BrandMapper;
import com.atguigu.gmall.pms.service.BrandService;
import com.atguigu.gmall.pms.vo.PmsBrandParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Service
@Component
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Override
    public Map<String, Object> pageBrand(String keyword, Integer pageNum, Integer pageSize) {
        BrandMapper brandMapper = getBaseMapper();
        //keyword 按照品牌名或者首字母模糊匹配
        QueryWrapper<Brand> eq =null;
        if(!StringUtils.isEmpty(keyword)){
            eq = new QueryWrapper<Brand>().like("name",keyword).eq("first_letter",keyword);
        }
        IPage<Brand> selectPage = brandMapper.selectPage(new Page<Brand>(pageNum, pageSize), eq);

        //封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize",pageSize);
        map.put("totalPage",selectPage.getPages());
        map.put("total",selectPage.getTotal());
        map.put("pageNum",selectPage.getCurrent());
        map.put("list",selectPage.getRecords());

        return map;
    }

    @Override
    public List<Brand> listAll() {
        BrandMapper brandMapper = getBaseMapper();

        List<Brand> brandList = brandMapper.selectList(null);

        return brandList;
    }

    @Override
    public boolean saveBrand(@Valid PmsBrandParam pmsBrand) {
        BrandMapper brandMapper = getBaseMapper();
        Brand brand = new Brand();
        BeanUtils.copyProperties(pmsBrand,brand);
        int i = brandMapper.insert(brand);
        return i>0;
    }

    @Override
    public void updateByBrandId(Long id, PmsBrandParam pmsBrandParam) {
        BrandMapper brandMapper = getBaseMapper();
        Brand brand = new Brand();
        BeanUtils.copyProperties(pmsBrandParam,brand);
        brand.setId(id);
        int i = brandMapper.updateById(brand);
    }

    @Override
    public boolean updateShowStatusByIds(List<Long> ids, Integer showStatus) {
        BrandMapper brandMapper = getBaseMapper();
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Brand brand = new Brand();
        brand.setShowStatus(showStatus);
        int i = brandMapper.update(brand, queryWrapper);
        return i>0;
    }

    @Override
    public boolean updateFactoryStatusByIds(List<Long> ids, Integer factoryStatus) {
        BrandMapper baseMapper = getBaseMapper();
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Brand brand = new Brand();
        brand.setFactoryStatus(factoryStatus);
        int i = baseMapper.update(brand, queryWrapper);
        return i>0;
    }



}
