package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponse {

    private boolean success;
    private String message;
    private Integer item_id;
    private Integer count;
    private List<ItemDto> items;
    private ItemDto item;

    public ItemResponse() {
    }

    public static ItemResponse createSuccess(String message, Integer itemId) {
        ItemResponse r = new ItemResponse();
        r.success = true;
        r.message = message;
        r.item_id = itemId;
        return r;
    }

    public static ItemResponse listSuccess(List<ItemDto> items) {
        ItemResponse r = new ItemResponse();
        r.success = true;
        r.items = items;
        r.count = items != null ? items.size() : 0;
        return r;
    }

    public static ItemResponse singleSuccess(ItemDto item) {
        ItemResponse r = new ItemResponse();
        r.success = true;
        r.item = item;
        return r;
    }

    public static ItemResponse successMessage(String message) {
        ItemResponse r = new ItemResponse();
        r.success = true;
        r.message = message;
        return r;
    }

    public static ItemResponse error(String message) {
        ItemResponse r = new ItemResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }
}
