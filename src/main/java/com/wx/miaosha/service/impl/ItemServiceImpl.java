package com.wx.miaosha.service.impl;

import com.wx.miaosha.dao.ItemDOMapper;
import com.wx.miaosha.dao.ItemStockDOMapper;
import com.wx.miaosha.dataobject.ItemDO;
import com.wx.miaosha.dataobject.ItemStockDO;
import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.service.ItemService;
import com.wx.miaosha.service.model.ItemModel;
import com.wx.miaosha.validator.ValidationResult;
import com.wx.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class ItemServiceImpl implements ItemService {
    @Resource
    ItemDOMapper itemDOMapper;
    @Resource
    ItemStockDOMapper itemStockDOMapper;
    @Resource
    ValidatorImpl validator;

    private ItemDO convertItemDOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    private ItemStockDO convertItemStockDOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }
    private ItemModel convertModelFromItem(ItemDO itemDO,ItemStockDO itemStockDO) {
        if (itemDO == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        //因为model和DO里price的类型不同，所以copyProperties无法自动匹配，需要自己转换类型
        itemModel.setPrice(BigDecimal.valueOf(itemDO.getPrice()));
        if (itemStockDO != null) {
            itemModel.setStock(itemStockDO.getStock());
        }
        return itemModel;
    }
    @Override
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        ValidationResult validate = validator.validate(itemModel);
        //校验信息
        if (validate.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validate.getErrMsg());
        }
        //将model分别转化为各自的dataobject,再进行插入操作
        ItemDO itemDO = convertItemDOFromModel(itemModel);
        itemDOMapper.insertSelective(itemDO);

        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = convertItemStockDOFromModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOS = itemDOMapper.listItem();
        List<ItemModel> modelList = itemDOS.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            return convertModelFromItem(itemDO, itemStockDO);
        }).collect(Collectors.toList());
        return modelList;
    }
    //商品详情
    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        return convertModelFromItem(itemDO,itemStockDO);
    }
    //库存扣减
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId,amount);
        //更新库存成功 返回true
        //更新库存失败 返回false
        return affectedRow > 0;
    }
    //销量增加
    @Override
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }
}
