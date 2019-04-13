package com.atguigu.gmall.admin.pms.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.service.ProductAttributeService;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.vo.PmsProductParam;
import com.atguigu.gmall.pms.vo.PmsProductQueryParam;
import com.atguigu.gmall.search.GmallSearchService;
import com.atguigu.gmall.to.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品管理Controller
 */
@CrossOrigin
@RestController
@Api(tags = "PmsProductController", description = "商品管理")
@RequestMapping("/product")
public class PmsProductController {
    @Reference
    private ProductService productService;
    @Reference
    private ProductAttributeService productAttributeService;

    @ApiOperation("创建商品")
    @PostMapping(value = "/create")
    public Object create(@RequestBody PmsProductParam productParam,
                         BindingResult bindingResult) {
        // 查询所有一级分类及子分类
        //创建商品
        productService.create(productParam);
        return new CommonResult().success(null);
    }

    @ApiOperation("根据商品id获取商品编辑信息")
    @GetMapping(value = "/updateInfo/{id}")
    public Object getUpdateInfo(@PathVariable Long id) {
        // 根据商品id获取商品编辑信息
        Product product = productService.getById(id);
        return new CommonResult().success(product);

    }

    @ApiOperation("更新商品")
    @PostMapping(value = "/update/{id}")
    public Object update(@PathVariable Long id, @RequestBody PmsProductParam productParam, BindingResult bindingResult) {
        //TODO 更新商品
        return new CommonResult().success(null);
    }

    @ApiOperation("查询商品")
    @GetMapping(value = "/list")
    public Object getList(PmsProductQueryParam productQueryParam,
                          @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 分页查询商品
        Map<String,Object> pageInfo = productService.pageProduct(pageSize,pageNum);

        return new CommonResult().success(pageInfo);
    }

    @ApiOperation("根据商品名称或货号模糊查询")
    @GetMapping(value = "/simpleList")
    public Object getList(String  keyword) {
        // 根据商品名称或货号模糊查询
        List<Product> Info = productService.getProductNameOrSn(keyword);

        return new CommonResult().success(Info);
    }

    @ApiOperation("批量修改审核状态")
    @PostMapping(value = "/update/verifyStatus")
    public Object updateVerifyStatus(@RequestParam("ids") List<Long> ids,
                                     @RequestParam("verifyStatus") Integer verifyStatus,
                                     @RequestParam("detail") String detail) {
        // 批量修改审核状态
        boolean b =productService.updateVerifyStatus(ids,verifyStatus);

        return new CommonResult().success(null);
    }

    @ApiOperation("批量上下架")
    @PostMapping(value = "/update/publishStatus")
    public Object updatePublishStatus(@RequestParam List<Long> ids,
                                     @RequestParam Integer publishStatus) {
        // 批量上下架
        //searchService.publishStatus(ids,publishStatus);
        productService.publishStatus(ids,publishStatus);
        return new CommonResult().success(null);
    }

    @ApiOperation("批量推荐商品")
    @PostMapping(value = "/update/recommendStatus")
    public Object updateRecommendStatus(@RequestParam("ids") List<Long> ids,
                                      @RequestParam("recommendStatus") Integer recommendStatus) {
        //TODO 批量推荐商品
      boolean b =  productService.recommendStatus(ids,recommendStatus);

      return new CommonResult().success(null);
    }

    @ApiOperation("批量设为新品")
    @PostMapping(value = "/update/newStatus")
    public Object updateNewStatus(@RequestParam("ids") List<Long> ids,
                                        @RequestParam("newStatus") Integer newStatus) {
        //TODO 批量设为新品
        boolean b =  productService.updateNewStatus(ids,newStatus);

        return new CommonResult().success(null);
    }

    @ApiOperation("批量修改删除状态")
    @PostMapping(value = "/update/deleteStatus")
    public Object updateDeleteStatus(@RequestParam("ids") List<Long> ids,
                                  @RequestParam("deleteStatus") Integer deleteStatus) {
        //TODO 根据商品id获取商品编辑信息
        boolean b =  productService.updateDeleteStatus(ids,deleteStatus);

        return new CommonResult().success(null);
    }




}
