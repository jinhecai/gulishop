package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.vo.PmsBrandParam;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
public interface BrandService extends IService<Brand> {

    Map<String,Object> pageBrand(String keyword, Integer pageNum, Integer pageSize);

    List<Brand> listAll();


    boolean saveBrand(@Valid PmsBrandParam pmsBrand);

    void updateByBrandId(Long id, PmsBrandParam pmsBrandParam);

    boolean updateShowStatusByIds(List<Long> ids, Integer showStatus);

    boolean updateFactoryStatusByIds(List<Long> ids, Integer factoryStatus);
}
