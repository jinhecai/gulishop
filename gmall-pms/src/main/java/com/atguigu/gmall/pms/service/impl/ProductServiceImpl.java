package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.RedisCacheConstant;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.vo.PmsProductParam;
import com.atguigu.gmall.search.GmallSearchService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.syntax.Reduction;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;


import javax.lang.model.element.VariableElement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Slf4j
@Component
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Autowired
    ProductMapper productMapper;
    @Autowired
    ProductLadderMapper productLadderMapper;
    @Autowired
    ProductFullReductionMapper productFullReductionMapper;
    @Autowired
    MemberPriceMapper memberPriceMapper;
    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    ProductCategoryMapper productCategoryMapper;
    @Autowired
    SkuStockMapper skuStockMapper;
    @Reference
    GmallSearchService searchService;
    @Autowired
    JedisPool jedisPool;


    ThreadLocal<Product> productThreadLocal =  new ThreadLocal<Product>();

    @Override
    public Map<String, Object> pageProduct(Integer pageSize, Integer pageNum) {

        ProductMapper baseMapper = getBaseMapper();
        Page<Product> page = new Page<>(pageNum, pageSize);

        IPage<Product> selectPage = baseMapper.selectPage(page, null);
        //封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize",pageSize);
        map.put("totalPage",selectPage.getPages());
        map.put("total",selectPage.getTotal());
        map.put("pageNum",selectPage.getCurrent());
        map.put("list",selectPage.getRecords());

        return map;
    }

    /**
     * 事务的传播行为：
     * Propagation{
     *     【REQUIRED(0)】,此方法需要事务，如果没有就开启新事务，如果之前已存在就用旧事务
     *     SUPPORTS(1),支持，有事务用事务，没有不用
     *     MANDATORY（2），强制要求，必须在事务中运行，没有就报错
     *     【REQUIRES_NEW（3）】，需要新的，这个方法必须用一个新的事务来做，不用混用
     *     NOT_SUPPORTED（4），不支持，此方法不能再事务中运行，如果有事务，暂停之前的事务，
     *     NEVER（5），从不用事务，否则抛异常
     *     Nested(6),内嵌事务，还原点
     *
     *     REQUIRED【和大方法用一个事务】
     *     REQUIRES_NEW【用一个新事务】
     *      异常机制还是异常机制
     *
     *
     * }
     * @param productParam
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void create(PmsProductParam productParam) {
        //1.保存商品的基本信息 pms_product(将刚才保存的这个商品的自增id获取出来)
        ProductServiceImpl psProxy =(ProductServiceImpl)AopContext.currentProxy();//拿到自己的代理对象
        //保存SPU和SKU
        psProxy.saveBaseProductInfo(productParam);
//        //productParam.setId(productId);
//        //共享商品信息的基础数据
//        Product product = new Product();
//        BeanUtils.copyProperties(productParam,product);
//        //同线程共享数据
//        productThreadLocal.set(product);

        //4.保存商品的会员价格 pms_member_price
        //5.保存商品的sku库存 pms_sku_stock(sku编码要自动生成)
        //6.保存参数及自定义规格 pms_product_attribute_value()
        //7.更新商品分类数目


        //2.保存商品的阶梯价格 pms_product_ladder
        psProxy.saveProductLadder(productParam.getProductLadderList());
        //3.保存商品满减价格 pms_product_full_redduction
        psProxy.saveProductFullReduction(productParam.getProductFullReductionList());
        psProxy.saveMemberPrice(productParam.getMemberPriceList());
        psProxy.saveProductAttributeValue(productParam.getProductAttributeValueList());
        psProxy.updateProductCategoryCount();


    }

    @Override
    public void publishStatus(List<Long> ids, Integer publishStatus) {
        //1.上架/下架
        if(publishStatus ==1){
            publishProduct(ids);
        }else {
            removeProduct(ids);
        }
    }

    //查询销售属性的列表
    @Override
    public List<EsProductAttributeValue> getProductSaleAttr(Long productId) {

        return productMapper.getProductSaleAttr(productId);
    }
    //查询基本属性值的列表
    @Override
    public List<EsProductAttributeValue> getProductBaseAttr(Long productId) {

        return productMapper.getProductBaseAttr(productId);
    }

    /**
     * 1、缓存的所有key必须给过期时间。
     * 2、过期时间：
     *      1）、数据库能查到的值，给设置长一点；
     *      2）、数据库查不到的值，给设置短一点
     *      3）、为了防止雪崩（同一时刻，大量缓存同时失效，请求全部去DB）-给过期时间加上随机数
     * 3、应用级锁（单机锁：Synchronize，Lock，）
     * 4、分布式锁
     *      1）、占个坑
     *      问题：
     *          1）、执行业务中途出现问题（代码问题，机器问题），导致没有运行到删锁。所有人都获取不到锁怎么办？
     *          2）、执行业务时间太长。让别人等的太久，优化？
     *      2）、分布式锁的伪代码：
     *        function a(){
     *          if(jedis.setnxex("key",token,timeout-3)){
     *              try{
     *                  //执行业务逻辑
     *              }finally{
     *                  jedis.eval(解锁脚本,key,value);
     *              }
     *          }else{
     *              // 等待继续；递归锁，自旋锁（轻量级锁）
     *              a();
     s     *              //while(true){
     *                  //获取锁
     *              //}
     *          };
     *        }
     *  锁粒度：jedis.set("lock:200")
     *       写数据：
     *          if(jedis.setnxex("write:200")){
     *              //1、writeToSor
     *              //2、update/del cache：product:200
     *          }
     *   4、数据一致性？
     *      缓存中的数据和数据库的是一样的；
     *      使用模式？
     * @param productId
     * @return
     */

    @Override
    public Product getProductByIdFromCache(Long productId) {
        Jedis jedis = jedisPool.getResource();

        //1、先去缓存中检索  GULI:PRODUCT:INFO:productId
        Product product = null;
        String s = jedis.get(RedisCacheConstant.PRODUCT_INFO_CACHE_KEY + productId);
        if(StringUtils.isEmpty(s)){
            //2、缓存中没有去找数据库
            //tryLock()
            //3、 去redis中占坑
            //Long lock = jedis.setnx("lock", "123");
            //占坑的时候，我们要给一个唯一标识UUID
           // Long lock = jedis.setnx("lock", "123");
            String token = UUID.randomUUID().toString();
            /**
             * 1）、分布式锁：要被锁住的所有线程，可以去分布式缓存中同时占一个坑;
             * 2）、原子操作；加锁、解锁都要原子操作；
             */
            String lock = jedis.set("lock", token, SetParams.setParams().ex(5).nx());
            if(!StringUtils.isEmpty(lock)&&"ok".equalsIgnoreCase(lock)){
                //获取到锁，查数据，放在缓存中
                //我们设置了超时，锁会自动删
                //1）、刚获取到锁（redis中占位完成），停机了，超时代码没走
                //2）、获取到锁，调用jedis.expire("lock",5);中途网络抖动；
                try{
                    product = productMapper.selectById(productId);
                    //finally  releaseLock();
                    //if(product!=null)
                    //3、放入缓存
                    String json = JSON.toJSONString(product);
                    if(product == null){
                        int anInt = new Random().nextInt(2000);
                        jedis.setex(RedisCacheConstant.PRODUCT_INFO_CACHE_KEY + productId,60+anInt,json);
                    }else{
                        //过期时间
                        int anInt = new Random().nextInt(2000);
                        jedis.setex(RedisCacheConstant.PRODUCT_INFO_CACHE_KEY + productId,60*60*24*3+anInt,json);
                    }
                }finally {
                    //删锁的问题，如果由于业务超出的锁的自动删除时间；我们直接按照key删除锁。会导致删掉别人的锁
                        // if(token.equals(jedis.get("lock"))){
//                        //这样有没有问题？判断是相等，但是正好锁过期？数值正在网络传输中锁过期了怎么办？
//                        //比较与删除也应该原子操作
//                        //脚本删除锁  lua
//                        jedis.del("lock");
//                    }
                    String script =
                            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));
                }

               // jedis.del("lock");
            }else{
                try{
                    Thread.sleep(1000);
                    //如果没有获取到锁去查数据库，我们等待一会，再去缓存看
                    getProductByIdFromCache(productId);
                }catch (InterruptedException e){

                }

            }
        }else {
            //4、缓存中有
            product =JSON.parseObject(s,Product.class);
        }
        jedis.close();
        return product;
    }

    @Override
    public List<Product> getProductNameOrSn(String keyword) {
        ProductMapper baseMapper = getBaseMapper();

        List<Product> productList = baseMapper.selectList(new QueryWrapper<Product>().like("keyword",keyword).or().eq("product_sn",keyword));
                //.selectList(new QueryWrapper<Product>().like("keyword").or().eq("product_sn"));
            return productList;
    }

    @Override
    public boolean updateVerifyStatus(List<Long> ids, Integer verifyStatus) {
        ProductMapper baseMapper = getBaseMapper();
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Product product = new Product();
        Product verifyStatus1 = product.setVerifyStatus(verifyStatus);
        int i = baseMapper.update(verifyStatus1, queryWrapper);
        return i>0;
    }

    @Override
    public boolean recommendStatus(List<Long> ids, Integer recommendStatus) {
        ProductMapper baseMapper = getBaseMapper();
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Product product = new Product();
        Product recommendStatus1 = product.setRecommandStatus(recommendStatus);
        int i = baseMapper.update(recommendStatus1, queryWrapper);
        return i>0;
    }

    @Override
    public boolean updateNewStatus(List<Long> ids, Integer newStatus) {
        ProductMapper baseMapper = getBaseMapper();
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Product product = new Product();
        Product newStatus1 = product.setNewStatus(newStatus);
        int i = baseMapper.update(newStatus1, queryWrapper);
        return i>0;

    }

    @Override
    public boolean updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        ProductMapper baseMapper = getBaseMapper();
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        Product product = new Product();
        Product deleteStatus1 = product.setDeleteStatus(deleteStatus);
        int i = baseMapper.update(deleteStatus1, queryWrapper);
        return i>0;
    }

    @Override
    public SkuStock getSkuInfo(Long skuId) {
        //加上缓存的查询
        SkuStock skuStock = skuStockMapper.selectById(skuId);
        return skuStock;
    }

    private void publishProduct(List<Long> ids){
        //查当前需要上架的商品的sku信息和spu信息
        ids.forEach((id)->{
            //1.spu
            Product product = productMapper.selectById(id);
            //2.需要上架的sku
            List<SkuStock> skuStocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", product.getId()));
           //这个商品所有的参数值
            List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductAttrValues(product.getId());
            //3.改写信息，将其发布到es

            //4.改写信息，将其发布到es;统计上架状态是否全部完成
            AtomicReference<Integer> count = new AtomicReference<Integer>(0);

            skuStocks.forEach((sku)->{
                EsProduct esProduct = new EsProduct();
                BeanUtils.copyProperties(product,esProduct);
                //5.改写商品的标题，加上sku的销售属性
                esProduct.setName(product.getName()+" "+sku.getSp1()+" "+sku.getSp2()+""+sku.getSp3());
                esProduct.setPrice(sku.getPrice());
                esProduct.setStock(sku.getStock());
                esProduct.setSale(sku.getSale());
                esProduct.setAttrValueList(attributeValues);
                //6.改写id,变为sku的id
                esProduct.setId(sku.getId());//直接改为sku的id
                //7.保存到es中; //5个成了3个败了。不成
                boolean es = searchService.saveProductInfoToEs(esProduct);
                count.set(count.get()+1);
                log.info("hahaha");
                if(es){
                    //保存当前的id，list.add(id)
                }
            });

            //8 判断是否完全上架成功，成功改数据库状态
            if(count.get() ==skuStocks.size()){
                //9.修改数据库状态，都是包装类型允许null
                Product update = new Product();
                update.setId(product.getId());
                update.setPublishStatus(1);
                productMapper.updateById(update);
            }else{
                //10。成功的撤销操作，来保证业务数据的一致性；
                //es有失败 list.forEach(remove());
                log.error("上架失败");
            }

        });

    }

    private void removeProduct(List<Long> ids){


    }

    //1.保存商品的基本信息
    @Transactional(propagation = Propagation.REQUIRED)
    public Long saveProduct(PmsProductParam productParam ) {
        Product product = new Product();
        BeanUtils.copyProperties(productParam,product);
        int insert = productMapper.insert(product);
        log.debug("插入商品：{}",product.getId());

        //商品信息共享到ThreadLocal
        productThreadLocal.set(product);
        return product.getId();
    }
    //2.保存商品的阶梯价格 pms_product_ladder
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductLadder(List<ProductLadder> list){
        Product product = productThreadLocal.get();
        //2.保存商品的阶梯价格 pms_product_ladder
        //拿到阶梯价格
        for (ProductLadder ladder : list) {
            ladder.setProductId(product.getId());
            productLadderMapper.insert(ladder);
            log.debug("插入ladder{}",ladder.getId());
        }
    }

    //3.保存商品满减价格 pms_product_full_redduction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductFullReduction(List<ProductFullReduction> list){
        Product product = productThreadLocal.get();
        for (ProductFullReduction reduciotn : list) {
            reduciotn.setProductId(product.getId());
            productFullReductionMapper.insert(reduciotn);
        }
    }

    //4.保存商品的会员价格 pms_member_price
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMemberPrice(List<MemberPrice> memberPrices){
        Product product = productThreadLocal.get();
        for (MemberPrice memberPrice : memberPrices) {
            memberPrice.setProductId(product.getId());
            memberPriceMapper.insert(memberPrice);
        }
    }
    //5.保存商品的sku库存 pms_sku_stock(sku编码要自动生成)
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveSkuInfo(List<SkuStock> skuStocks){
        Product product = productThreadLocal.get();
        //线程安全的，遍历修改不安全
        AtomicReference<Integer> i = new AtomicReference<>(0);
        NumberFormat numberFormat = DecimalFormat.getNumberInstance();
        numberFormat.setMinimumIntegerDigits(2);
        numberFormat.setMaximumIntegerDigits(2);

        skuStocks.forEach(skuStock -> {
            //保存商品id
            skuStock.setProductId(product.getId());
            //sku编码
          //  skuStock.setSkuCode();
            String format = numberFormat.format(i.get());
            String code = "K_"+product.getId()+"_"+format;
            skuStock.setSkuCode(code);
            i.set(i.get()+1);
            skuStockMapper.insert(skuStock);
        });
    }

    //6.保存参数及自定义规格 pms_product_attribute_value()
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(List<ProductAttributeValue> productAttributeValues){
        Product product = productThreadLocal.get();
        productAttributeValues.forEach((pav)->{
            pav.setProductId(product.getId());
            productAttributeValueMapper.insert(pav);
        });
    }
    //7.更新商品分类数目
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProductCategoryCount(){
        Product product = productThreadLocal.get();
        Long id = product.getProductCategoryId();

//        ProductCategory productCategory = new ProductCategory();
//        productCategory.setId(id);
//        productCategory.setProductCount()
        productCategoryMapper.updateCountById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseProductInfo(PmsProductParam productParam){
        ProductServiceImpl psProxy =(ProductServiceImpl)AopContext.currentProxy();//拿到自己的代理对象
        psProxy.saveProduct(productParam);
        psProxy.saveSkuInfo(productParam.getSkuStockList());
    }

}
