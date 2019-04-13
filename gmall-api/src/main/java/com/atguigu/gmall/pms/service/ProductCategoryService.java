package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.vo.PmsProductCategoryParam;
import com.atguigu.gmall.pms.vo.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品分类 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    List<PmsProductCategoryWithChildrenItem> listWithChildren();

    boolean saveProductCategory(PmsProductCategoryParam productCategoryParam);

    boolean updateProductCategory(Long id, PmsProductCategoryParam productCategoryParam);

    boolean updateShowStatusById(List<Long> ids, Integer showStatus);

    boolean updateNavStatusById(List<Long> ids, Integer navStatus);

    Map<String,Object> pageProductCategory(Long parentId, Integer pageSize, Integer pageNum);
}
