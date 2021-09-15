package com.wx.miaosha.controller;

import com.wx.miaosha.controller.viewobject.ItemVO;
import com.wx.miaosha.error.BusinessException;
import com.wx.miaosha.error.EmBusinessError;
import com.wx.miaosha.response.CommonReturnType;
import com.wx.miaosha.service.ItemService;
import com.wx.miaosha.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/item")
public class ItemController extends BaseController{
    @Resource
    ItemService itemService;

    //创建商品
    @ResponseBody
    @RequestMapping(value = "/create",method = RequestMethod.POST,consumes = BaseController.CONTENT_TYPE_FORMED)
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        //创建成功返回数据
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }
    //查询商品详细信息,不用post
    @ResponseBody
    @RequestMapping("/get")
    public CommonReturnType getItemById(@RequestParam(name = "id") Integer id) throws BusinessException {
        ItemModel item = itemService.getItemById(id);
        if (item == null) {
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }
        ItemVO itemVO = convertVOFromModel(item);
        return CommonReturnType.create(itemVO);
    }
    //列表显示商品
    @ResponseBody
    @RequestMapping("/list")
    public CommonReturnType listItem() {
        List<ItemModel> modelList = itemService.listItem();
        List<ItemVO> collect = modelList.stream().map(this::convertVOFromModel).collect(Collectors.toList());
        return CommonReturnType.create(collect);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        return itemVO;
    }
}
