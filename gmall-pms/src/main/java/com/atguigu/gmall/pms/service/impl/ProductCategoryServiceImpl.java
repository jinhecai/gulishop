package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.RedisCacheConstant;
import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.mapper.ProductCategoryMapper;
import com.atguigu.gmall.pms.service.ProductCategoryService;
import com.atguigu.gmall.pms.vo.PmsProductCategoryParam;
import com.atguigu.gmall.pms.vo.PmsProductCategoryWithChildrenItem;
import com.atguigu.gmall.utils.PageUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.util.Timeout;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        //这个数据加缓存
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
       //从redis中拿出这个缓存的值
        String cache = ops.get(RedisCacheConstant.PRODUCT_CATEGORY_CACHE_KEY);
        if(!StringUtils.isEmpty(cache)){
            log.debug("缓存命中");
            //转化过来返回出去
            List<PmsProductCategoryWithChildrenItem> items = JSON.parseArray(cache, PmsProductCategoryWithChildrenItem.class);
            return items;
        }

        log.debug("缓存未命中，去查询数据库");
        ProductCategoryMapper baseMapper = getBaseMapper();
        List<PmsProductCategoryWithChildrenItem> items = baseMapper.listWithChildren(0);
        //查某个菜单的所有子菜单
        //存数据都给一个过期时间比较好
        String jsonString = JSON.toJSONString(items);
        ops.set(RedisCacheConstant.PRODUCT_CATEGORY_CACHE_KEY,jsonString,3, TimeUnit.DAYS);

        return items;
    }

    @Override
    public boolean saveProductCategory(PmsProductCategoryParam productCategoryParam) {
        ProductCategoryMapper baseMapper = getBaseMapper();
        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryParam,productCategory);
        int i = baseMapper.insert(productCategory);

        return i>0;
    }

    @Override
    public boolean updateProductCategory(Long id, PmsProductCategoryParam productCategoryParam) {
        ProductCategoryMapper baseMapper = getBaseMapper();
        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryParam,productCategory);
        productCategory.setId(id);
        int i = baseMapper.updateById(productCategory);
        return i>0;
    }

    @Override
    public boolean updateShowStatusById(List<Long> ids, Integer showStatus) {
        ProductCategoryMapper baseMapper = getBaseMapper();
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        ProductCategory productCategory = new ProductCategory();
        productCategory.setShowStatus(showStatus);
        int i = baseMapper.update(productCategory, queryWrapper);
        return i > 0;
    }

    @Override
    public boolean updateNavStatusById(List<Long> ids, Integer navStatus) {
        ProductCategoryMapper baseMapper = getBaseMapper();
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        ProductCategory productCategory = new ProductCategory();
        productCategory.setNavStatus(navStatus);
        int i = baseMapper.update(productCategory, queryWrapper);
        return i>0;
    }

    //分页查询商品分类
    @Override
    public Map<String, Object> pageProductCategory(Long parentId, Integer pageSize, Integer pageNum) {
        ProductCategoryMapper baseMapper = getBaseMapper();
        Page<ProductCategory> page = new Page<>(pageSize,pageNum);
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        QueryWrapper<ProductCategory> id = queryWrapper.eq("parent_id", parentId);
        IPage<ProductCategory> selectPage = baseMapper.selectPage(page, id);

        return PageUtils.getPageMap(selectPage);
    }
}
